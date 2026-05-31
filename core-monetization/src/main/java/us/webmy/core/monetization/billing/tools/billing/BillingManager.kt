package us.webmy.core.monetization.billing.tools.billing

import android.app.Application
import android.util.Log
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import us.webmy.core.error.SdkError
import us.webmy.core.monetization.billing.tools.billing.internal.BillingClientWrapper
import us.webmy.core.util.ActivityProvider
import us.webmy.core.util.flatMap

interface BillingManager {

    suspend fun awaitInitialized(): Result<Unit>

    fun subscribeProducts(): Flow<List<Product>>

    suspend fun purchase(productId: String): PurchaseOutcome

    fun canBePurchased(productId: String): Result<Boolean>
}

private const val TAG = "BillingManager"
private const val ACK_MAX_ATTEMPTS = 4
private const val ACK_INITIAL_DELAY_MS = 1_000L
private const val RECONNECT_MAX_DELAY_MS = 5 * 60_000L

class RealBillingManager(
    application: Application,
    private val activityProvider: ActivityProvider,
    private val oneTimeProducts: Set<String>,
    private val subscriptionProducts: Set<String>,
    private val consumableProducts: Set<String> = emptySet(),
) : BillingManager, PurchasesUpdatedListener {

    init {
        require(consumableProducts.all { it in oneTimeProducts }) {
            "consumableProducts must be a subset of oneTimeProducts"
        }
    }

    private sealed interface InitState {
        object Idle : InitState
        object Loading : InitState
        object Ready : InitState
        data class Failed(val error: Throwable) : InitState
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val initState = MutableStateFlow<InitState>(InitState.Idle)
    private val reconnectMutex = Mutex()
    private val purchaseMutex = Mutex()

    private val productsState = MutableStateFlow<List<Product>>(emptyList())

    @Volatile
    private var oneTimeDetails: Map<String, ProductDetails> = emptyMap()

    @Volatile
    private var subscriptionDetails: Map<String, ProductDetails> = emptyMap()

    @Volatile
    private var purchasedIds: Set<String> = emptySet()

    @Volatile
    private var pendingPurchase: CompletableDeferred<PurchaseOutcome>? = null

    private val billing = BillingClientWrapper(
        application = application,
        listener = this,
        onDisconnected = {
            Log.w(TAG, "billing service disconnected — scheduling reconnect")
            scope.launch { reconnectLoop() }
        }
    )

    override fun subscribeProducts(): Flow<List<Product>> = productsState.asStateFlow()

    init {
        initState.value = InitState.Loading
        scope.launch {
            initState.value = billing.connect()
                .flatMap { fetchProducts() }
                .fold(
                    onSuccess = { InitState.Ready },
                    onFailure = { InitState.Failed(it) },
                )
        }
    }

    override suspend fun awaitInitialized(): Result<Unit> {
        val terminal = initState
            .filter { it is InitState.Ready || it is InitState.Failed }
            .first()
        return when (terminal) {
            is InitState.Ready -> Result.success(Unit)
            is InitState.Failed -> Result.failure(terminal.error)
            else -> Result.failure(IllegalStateException("Unreachable"))
        }
    }

    override suspend fun purchase(productId: String): PurchaseOutcome = purchaseMutex.withLock {
        if (initState.value !is InitState.Ready) {
            return@withLock PurchaseOutcome.Failed(
                SdkError.Billing.FlowFailed("BillingManager not initialized")
            )
        }
        val activity = activityProvider.current ?: return@withLock PurchaseOutcome.Failed(
            SdkError.Billing.FlowFailed("No foreground Activity")
        )
        val details = oneTimeDetails[productId] ?: subscriptionDetails[productId]
            ?: return@withLock PurchaseOutcome.Failed(
                SdkError.Billing.FlowFailed("Product $productId not found")
            )

        val productParamsBuilder = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)
        details.subscriptionOfferDetails?.firstOrNull()?.offerToken?.let {
            productParamsBuilder.setOfferToken(it)
        }
        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productParamsBuilder.build()))
            .build()

        val deferred = CompletableDeferred<PurchaseOutcome>()
        pendingPurchase = deferred
        try {
            val launchResult = billing.launchBillingFlow(activity, flowParams)
            if (launchResult.responseCode != BillingClient.BillingResponseCode.OK) {
                return@withLock PurchaseOutcome.Failed(
                    SdkError.Billing.FlowFailed("launchBillingFlow failed: ${launchResult.responseCode}")
                )
            }
            deferred.await()
        } finally {
            pendingPurchase = null
        }
    }

    override fun canBePurchased(productId: String): Result<Boolean> = runCatching {
        val product = productsState.value.find { it.id == productId }
            ?: throw SdkError.NotSupported("Product $productId not found")
        !product.isPurchased
    }

    private suspend fun reconnectLoop() {
        if (!reconnectMutex.tryLock()) return
        try {
            initState.value = InitState.Loading
            var attempt = 0
            while (true) {
                val delayMs = (1000L shl attempt.coerceAtMost(8))
                    .coerceAtMost(RECONNECT_MAX_DELAY_MS)
                delay(delayMs)
                attempt++
                val result = billing.connect().flatMap { fetchProducts() }
                if (result.isSuccess) {
                    initState.value = InitState.Ready
                    Log.i(TAG, "billing reconnected after $attempt attempts")
                    return
                }
                Log.w(TAG, "billing reconnect attempt $attempt failed")
            }
        } finally {
            reconnectMutex.unlock()
        }
    }

    private suspend fun fetchProducts(): Result<Unit> = runCatching {
        val oneTimePurchases = if (oneTimeProducts.isEmpty()) emptyList()
        else billing.queryPurchases(BillingClient.ProductType.INAPP).getOrThrow()
        val subscriptionPurchases = if (subscriptionProducts.isEmpty()) emptyList()
        else billing.queryPurchases(BillingClient.ProductType.SUBS).getOrThrow()
        val oneTimeList = billing.queryProductDetails(oneTimeProducts, BillingClient.ProductType.INAPP).getOrThrow()
        val subscriptionList = billing.queryProductDetails(subscriptionProducts, BillingClient.ProductType.SUBS).getOrThrow()

        purchasedIds = buildSet {
            (oneTimePurchases + subscriptionPurchases).forEach { p ->
                if (p.isAcknowledged) addAll(p.products)
            }
        }
        oneTimeDetails = oneTimeList.associateBy { it.productId }
        subscriptionDetails = subscriptionList.associateBy { it.productId }

        emitProducts()

        // Re-consume any leftover consumables from previous session
        oneTimePurchases.forEach { p ->
            if (p.products.any { it in consumableProducts }) {
                scope.launch { consumeWithRetry(p) }
            }
        }
    }

    private fun emitProducts() {
        productsState.update { buildProducts() }
    }

    private fun buildProducts(): List<Product> = buildList {
        oneTimeDetails.values.forEach { detail ->
            val offer = detail.oneTimePurchaseOfferDetails ?: return@forEach
            val isConsumable = detail.productId in consumableProducts
            val purchased = !isConsumable && purchasedIds.contains(detail.productId)
            add(
                Product.OneTime(
                    id = detail.productId,
                    title = detail.name,
                    formattedPrice = offer.formattedPrice,
                    isPurchased = purchased,
                    consumable = isConsumable,
                )
            )
        }
        subscriptionDetails.values.forEach { detail ->
            val offer = detail.subscriptionOfferDetails?.firstOrNull() ?: return@forEach
            val phases = offer.pricingPhases.pricingPhaseList.map {
                Product.Subscription.Phase(
                    formattedPrice = it.formattedPrice,
                    priceMicros = it.priceAmountMicros,
                    currency = it.priceCurrencyCode,
                    billingPeriod = it.billingPeriod,
                    cycles = it.billingCycleCount,
                )
            }
            add(
                Product.Subscription(
                    id = detail.productId,
                    title = detail.name,
                    isPurchased = purchasedIds.contains(detail.productId),
                    offerToken = offer.offerToken,
                    phases = phases,
                )
            )
        }
    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        val outcome: PurchaseOutcome = when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                when (purchases?.firstOrNull()?.purchaseState) {
                    Purchase.PurchaseState.PURCHASED -> PurchaseOutcome.Success
                    Purchase.PurchaseState.PENDING -> PurchaseOutcome.Pending
                    else -> PurchaseOutcome.Failed(
                        SdkError.Billing.FlowFailed("Unknown purchase state")
                    )
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> PurchaseOutcome.Cancelled
            else -> PurchaseOutcome.Failed(
                SdkError.Billing.FlowFailed("Purchase failed: ${billingResult.responseCode} ${billingResult.debugMessage}")
            )
        }
        pendingPurchase?.complete(outcome)

        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            purchases?.forEach(::handlePurchase)
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) return
        val isConsumable = purchase.products.any { it in consumableProducts }
        if (isConsumable) {
            scope.launch { consumeWithRetry(purchase) }
            return
        }
        if (purchase.isAcknowledged) {
            markPurchased(purchase.products)
            return
        }
        scope.launch { acknowledgeWithRetry(purchase) }
    }

    private fun markPurchased(productIds: List<String>) {
        purchasedIds = purchasedIds + productIds
        emitProducts()
    }

    private suspend fun acknowledgeWithRetry(purchase: Purchase) {
        var attempt = 0
        var delayMs = ACK_INITIAL_DELAY_MS
        while (attempt < ACK_MAX_ATTEMPTS) {
            attempt++
            val ok = runCatching { billing.acknowledge(purchase.purchaseToken) }.getOrDefault(false)
            if (ok) {
                markPurchased(purchase.products)
                return
            }
            delay(delayMs)
            delayMs *= 2
        }
        Log.w(TAG, "acknowledge failed after $attempt attempts for ${purchase.products}")
    }

    private suspend fun consumeWithRetry(purchase: Purchase) {
        var attempt = 0
        var delayMs = ACK_INITIAL_DELAY_MS
        while (attempt < ACK_MAX_ATTEMPTS) {
            attempt++
            val ok = runCatching { billing.consume(purchase.purchaseToken) }.getOrDefault(false)
            if (ok) {
                emitProducts()
                return
            }
            delay(delayMs)
            delayMs *= 2
        }
        Log.w(TAG, "consume failed after $attempt attempts for ${purchase.products}")
    }
}
