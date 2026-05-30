package us.webmy.core.monetization.billing.tools.billing

import android.app.Application
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
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
import kotlinx.coroutines.suspendCancellableCoroutine
import us.webmy.core.error.SdkError
import us.webmy.core.util.ActivityProvider
import us.webmy.core.util.flatMap

interface BillingManager {

    suspend fun awaitInitialized(): Result<Unit>

    fun subscribeProducts(): Flow<List<Product>>

    fun purchase(productId: String): Result<Unit>

    fun canBePurchased(productId: String): Result<Boolean>
}

private const val TAG = "BillingManager"
private const val ACK_MAX_ATTEMPTS = 4
private const val ACK_INITIAL_DELAY_MS = 1_000L

class RealBillingManager(
    application: Application,
    private val activityProvider: ActivityProvider,
    private val oneTimeProducts: Set<String>,
    private val subscriptionProducts: Set<String>,
) : BillingManager, PurchasesUpdatedListener {

    private sealed interface InitState {
        object Idle : InitState
        object Loading : InitState
        object Ready : InitState
        data class Failed(val error: Throwable) : InitState
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val initState = MutableStateFlow<InitState>(InitState.Idle)

    private val productsState = MutableStateFlow<List<Product>>(emptyList())

    // Cached billing data — accessed sync in purchase() / canBePurchased().
    @Volatile
    private var oneTimeDetails: Map<String, ProductDetails> = emptyMap()

    @Volatile
    private var subscriptionDetails: Map<String, ProductDetails> = emptyMap()

    @Volatile
    private var purchasedIds: Set<String> = emptySet()

    private val pendingPurchaseParams = PendingPurchasesParams.newBuilder()
        .enableOneTimeProducts()
        .enablePrepaidPlans()
        .build()

    private val billingClient = BillingClient.newBuilder(application)
        .enablePendingPurchases(pendingPurchaseParams)
        .setListener(this)
        .build()

    private val queryOneTimePurchasesParams = QueryPurchasesParams.newBuilder()
        .setProductType(BillingClient.ProductType.INAPP)
        .build()

    private val querySubscriptionPurchasesParams = QueryPurchasesParams.newBuilder()
        .setProductType(BillingClient.ProductType.SUBS)
        .build()

    private val queryOneTimeDetailsParams by lazy {
        QueryProductDetailsParams.newBuilder()
            .setProductList(
                oneTimeProducts.map {
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(it)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
                }
            ).build()
    }

    private val querySubscriptionDetailsParams by lazy {
        QueryProductDetailsParams.newBuilder()
            .setProductList(
                subscriptionProducts.map {
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(it)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                }
            ).build()
    }

    override fun subscribeProducts(): Flow<List<Product>> = productsState.asStateFlow()

    init {
        initState.value = InitState.Loading
        scope.launch {
            initState.value = connect()
                .flatMap {
                    fetchProducts()
                }
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

    override fun purchase(productId: String): Result<Unit> = runCatching {
        check(initState.value is InitState.Ready) {
            "BillingManager not initialized — call init() and awaitInitialized() first"
        }
        val activity = activityProvider.requireCurrent()
        val details = oneTimeDetails[productId] ?: subscriptionDetails[productId]
        ?: throw SdkError.NotSupported("Product $productId not found")

        val productParamsBuilder = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)

        val offerToken = details.subscriptionOfferDetails?.firstOrNull()?.offerToken
        if (offerToken != null) productParamsBuilder.setOfferToken(offerToken)

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productParamsBuilder.build()))
            .build()

        billingClient.launchBillingFlow(activity, flowParams)
        Unit
    }

    override fun canBePurchased(productId: String): Result<Boolean> = runCatching {
        val product = productsState.value.find { it.id == productId }
            ?: throw SdkError.NotSupported("Product $productId not found")
        !product.isPurchased
    }

    private suspend fun connect(): Result<Unit> = suspendCancellableCoroutine { cont ->
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                val result =
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        Result.success(Unit)
                    } else {
                        Result.failure(SdkError.Billing.FlowFailed("Setup failed: ${billingResult.responseCode} ${billingResult.debugMessage}"))
                    }
                if (!cont.isCompleted) cont.resumeWith(Result.success(result))
            }

            override fun onBillingServiceDisconnected() {
                Log.w(TAG, "billing service disconnected")
                // Don't fail init — `purchase()` will surface failure later.
            }
        })
    }

    private suspend fun fetchProducts(): Result<Unit> = runCatching {
        val oneTimePurchases =
            queryPurchases(queryOneTimePurchasesParams, oneTimeProducts.isNotEmpty())
        val subscriptionPurchases =
            queryPurchases(querySubscriptionPurchasesParams, subscriptionProducts.isNotEmpty())
        val oneTimeList = queryDetails(queryOneTimeDetailsParams, oneTimeProducts.isNotEmpty())
        val subscriptionList =
            queryDetails(querySubscriptionDetailsParams, subscriptionProducts.isNotEmpty())

        purchasedIds = buildSet {
            (oneTimePurchases + subscriptionPurchases).forEach { p ->
                if (p.isAcknowledged) addAll(p.products)
            }
        }
        oneTimeDetails = oneTimeList.associateBy { it.productId }
        subscriptionDetails = subscriptionList.associateBy { it.productId }

        emitProducts()
    }

    private suspend fun queryPurchases(
        params: QueryPurchasesParams,
        enabled: Boolean
    ): List<Purchase> {
        if (!enabled) return emptyList()
        val result = billingClient.queryPurchasesAsync(params)
        if (result.billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
            throw SdkError.Billing.FlowFailed("queryPurchases failed: ${result.billingResult.responseCode}")
        }
        return result.purchasesList
    }

    private suspend fun queryDetails(
        params: QueryProductDetailsParams,
        enabled: Boolean
    ): List<ProductDetails> {
        if (!enabled) return emptyList()
        val result = billingClient.queryProductDetails(params)
        if (result.billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
            throw SdkError.Billing.FlowFailed("queryProductDetails failed: ${result.billingResult.responseCode}")
        }
        return result.productDetailsList.orEmpty()
    }

    private fun emitProducts() {
        productsState.update { buildProducts() }
    }

    private fun buildProducts(): List<Product> = buildList {
        oneTimeDetails.values.forEach { detail ->
            val offer = detail.oneTimePurchaseOfferDetails ?: return@forEach
            add(
                Product.OneTime(
                    id = detail.productId,
                    title = detail.name,
                    formattedPrice = offer.formattedPrice,
                    isPurchased = purchasedIds.contains(detail.productId),
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
        if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) return
        purchases?.forEach(::handlePurchase)
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) return
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
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        var attempt = 0
        var delayMs = ACK_INITIAL_DELAY_MS
        while (attempt < ACK_MAX_ATTEMPTS) {
            attempt++
            val ok = runCatching { acknowledgeOnce(params) }.getOrDefault(false)
            if (ok) {
                markPurchased(purchase.products)
                return
            }
            delay(delayMs)
            delayMs *= 2
        }
    }

    private suspend fun acknowledgeOnce(params: AcknowledgePurchaseParams): Boolean =
        suspendCancellableCoroutine { cont ->
            billingClient.acknowledgePurchase(params) { result ->
                if (!cont.isCompleted) {
                    cont.resumeWith(
                        Result.success(result.responseCode == BillingClient.BillingResponseCode.OK)
                    )
                }
            }
        }
}
