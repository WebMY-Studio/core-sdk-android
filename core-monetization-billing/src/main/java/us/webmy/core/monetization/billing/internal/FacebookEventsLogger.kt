package us.webmy.core.monetization.billing.internal

import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.facebook.appevents.AppEventsConstants
import com.facebook.appevents.AppEventsLogger
import java.math.BigDecimal
import java.util.Currency
import androidx.core.content.edit

private const val TAG = "FbEventsLogger"
private const val PREFS_NAME = "webmy_fb_events"
private const val KEY_FIRST_OPEN = "first_open_logged"
private const val TRIAL_KEY_PREFIX = "trial_"
private const val EVENT_FIRST_OPEN = "first_open"

/**
 * Logs the three Meta events the product team tracks:
 *  - "First_Open" — once per install, on first SDK init
 *  - "StartTrial" — a subscription with a free intro phase was actually purchased
 *  - "Purchase"   — a direct purchase, or a trial that converted to paid
 *
 * Trial conversion has no client callback, so it is detected on later launches:
 * a trial start is persisted with its computed end time, and if the subscription
 * is still returned by queryPurchases past that time, Play has billed the user.
 *
 * Automatic IAP logging must be disabled in the Meta App Dashboard, because it
 * re-logs restored purchases returned by queryPurchasesAsync and inflates counts.
 */
internal class FacebookEventsLogger(private val application: Application) {

    private val prefs by lazy {
        application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val logger: AppEventsLogger? by lazy {
        runCatching { AppEventsLogger.newLogger(application) }
            .onFailure { Log.w(TAG, "Facebook SDK unavailable, events won't be logged", it) }
            .getOrNull()
    }

    private val loggedTokens = mutableSetOf<String>()

    fun logFirstOpenIfNeeded() {
        if (prefs.getBoolean(KEY_FIRST_OPEN, false)) return
        prefs.edit { putBoolean(KEY_FIRST_OPEN, true) }
        runCatching { logger?.logEvent(EVENT_FIRST_OPEN) }
            .onFailure { Log.w(TAG, "$EVENT_FIRST_OPEN failed", it) }
    }

    /** Call only for fresh (unacknowledged) purchases from onPurchasesUpdated. */
    fun logNewPurchase(purchase: Purchase, details: ProductDetails?) {
        if (details == null) return
        synchronized(loggedTokens) {
            if (!loggedTokens.add(purchase.purchaseToken)) return
        }
        val trialPhase = trialPhase(details)
        if (trialPhase != null) {
            logStartTrial(purchase, details, trialPhase)
        } else {
            val price = paidPrice(details) ?: return
            logPurchase(purchase, details.productId, price)
        }
    }

    /** Call with the current SUBS purchases on every refresh (app start included). */
    fun checkTrialConversions(activeSubscriptions: List<Purchase>) {
        val trialKeys = prefs.all.keys.filter { it.startsWith(TRIAL_KEY_PREFIX) }
        if (trialKeys.isEmpty()) return
        val byToken = activeSubscriptions.associateBy { it.purchaseToken }
        val now = System.currentTimeMillis()
        trialKeys.forEach { key ->
            val record = TrialRecord.parse(prefs.getString(key, null))
            if (record == null) {
                prefs.edit().remove(key).apply()
                return@forEach
            }
            if (now < record.trialEndMillis) return@forEach
            val purchase = byToken[key.removePrefix(TRIAL_KEY_PREFIX)]
            // Still owned past the trial end — Play billed the user; absent means
            // it was cancelled during the trial and expired without converting
            if (purchase != null) {
                logPurchase(
                    purchase = purchase,
                    productId = record.productId,
                    price = Price(record.priceMicros, record.currencyCode),
                )
            }
            prefs.edit().remove(key).apply()
        }
    }

    private fun logStartTrial(
        purchase: Purchase,
        details: ProductDetails,
        trialPhase: ProductDetails.PricingPhase,
    ) {
        val price = paidPrice(details)
        runCatching {
            val params = Bundle().apply {
                putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, details.productId)
                purchase.orderId?.let { putString("fb_order_id", it) }
                price?.let { putString(AppEventsConstants.EVENT_PARAM_CURRENCY, it.currencyCode) }
            }
            val valueToSum = price?.let { it.amountMicros / 1_000_000.0 } ?: 0.0
            logger?.logEvent(AppEventsConstants.EVENT_NAME_START_TRIAL, valueToSum, params)
        }.onFailure { Log.w(TAG, "StartTrial failed", it) }

        val trialEnd = periodToMillis(trialPhase.billingPeriod)?.let { purchase.purchaseTime + it }
        if (trialEnd == null || price == null) {
            Log.w(TAG, "can't schedule trial conversion for ${details.productId}")
            return
        }
        val record = TrialRecord(details.productId, trialEnd, price.amountMicros, price.currencyCode)
        prefs.edit()
            .putString(TRIAL_KEY_PREFIX + purchase.purchaseToken, record.serialize())
            .apply()
    }

    private fun logPurchase(purchase: Purchase, productId: String, price: Price) {
        runCatching {
            val params = Bundle().apply {
                putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, productId)
                // Meta deduplicates purchase events server-side by order id
                purchase.orderId?.let { putString("fb_order_id", it) }
            }
            logger?.logPurchase(
                BigDecimal(price.amountMicros).movePointLeft(6),
                Currency.getInstance(price.currencyCode),
                params,
            )
        }.onFailure { Log.w(TAG, "Purchase failed", it) }
    }

    private data class Price(val amountMicros: Long, val currencyCode: String)

    /** First free phase of the default offer, i.e. the trial. */
    private fun trialPhase(details: ProductDetails): ProductDetails.PricingPhase? =
        details.subscriptionOfferDetails
            ?.firstOrNull()
            ?.pricingPhases
            ?.pricingPhaseList
            ?.firstOrNull()
            ?.takeIf { it.priceAmountMicros == 0L }

    /** What the user is actually charged: one-time price, or first paid phase. */
    private fun paidPrice(details: ProductDetails): Price? {
        details.oneTimePurchaseOfferDetails?.let {
            return Price(it.priceAmountMicros, it.priceCurrencyCode)
        }
        val phase = details.subscriptionOfferDetails
            ?.firstOrNull()
            ?.pricingPhases
            ?.pricingPhaseList
            ?.firstOrNull { it.priceAmountMicros > 0 }
            ?: return null
        return Price(phase.priceAmountMicros, phase.priceCurrencyCode)
    }

    /** Billing periods are ISO 8601: P3D, P1W, P1M, P1Y. */
    private fun periodToMillis(period: String): Long? {
        val match = Regex("^P(\\d+)([DWMY])$").find(period) ?: return null
        val count = match.groupValues[1].toLongOrNull() ?: return null
        val dayMillis = 24L * 60 * 60 * 1000
        return when (match.groupValues[2]) {
            "D" -> count * dayMillis
            "W" -> count * 7 * dayMillis
            "M" -> count * 30 * dayMillis
            "Y" -> count * 365 * dayMillis
            else -> null
        }
    }

    private data class TrialRecord(
        val productId: String,
        val trialEndMillis: Long,
        val priceMicros: Long,
        val currencyCode: String,
    ) {
        fun serialize(): String = "$productId|$trialEndMillis|$priceMicros|$currencyCode"

        companion object {
            fun parse(raw: String?): TrialRecord? {
                val parts = raw?.split("|") ?: return null
                if (parts.size != 4) return null
                return TrialRecord(
                    productId = parts[0],
                    trialEndMillis = parts[1].toLongOrNull() ?: return null,
                    priceMicros = parts[2].toLongOrNull() ?: return null,
                    currencyCode = parts[3],
                )
            }
        }
    }
}
