package us.webmy.core.monetization.billing.paywall

import android.content.Context
import us.webmy.core.R
import us.webmy.core.internal.util.DateTimeFormatter
import us.webmy.core.monetization.billing.Product
import java.time.Period

data class SubscriptionsUiModel(
    val productId: String,
    val title: String,
    val isSelected: Boolean,
    val subscription: Product.Subscription
) {

    fun getFormattedFree(context: Context): String? {
        return subscription.phases.find { it.priceMicros == 0L }?.let {
            val periodFormatted =
                DateTimeFormatter.formatPeriod(context, it.billingPeriod, 1)
            context.getString(R.string.paywall_free, periodFormatted)
        }
    }

    fun getFormattedPrice(context: Context): String? {
        val phase = subscription.phases.find { it.priceMicros != 0L } ?: return null

        val price = (phase.priceMicros / 10_000L) / 100f

        val period = Period.parse(phase.billingPeriod)
        val periodFormatted = DateTimeFormatter.formatPeriod(context, period)

        return context.getString(
            R.string.paywall_price_placeholder,
            price.toString(),
            phase.currency,
            periodFormatted
        )

    }

    fun getFormattedPriceWeek(context: Context): String? {
        val phase = subscription.phases.find { it.priceMicros != 0L } ?: return null
        val period = Period.parse(phase.billingPeriod)

        val delitel = when {
            period.years != 0 -> 52.14f * period.years
            period.months != 0 -> 4.34f * period.months
            period.days != 0 -> period.days / 7f
            else -> 1f
        }
        val pricePerWeek = (phase.priceMicros / 10_000L) / (delitel * 100f)
        val pricePerWeekRound = (pricePerWeek * 100).toInt() / 100f

        return context.getString(
            R.string.paywall_price_placeholder_2,
            pricePerWeekRound.toString(),
            phase.currency,
            context.getString(R.string.week)
        )
    }
}