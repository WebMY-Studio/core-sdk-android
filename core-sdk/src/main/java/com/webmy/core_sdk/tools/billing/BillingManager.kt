package com.webmy.core_sdk.tools.billing

import android.app.Activity
import android.app.Application
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
import com.webmy.core_sdk.util.awaitTrue
import com.webmy.core_sdk.util.coerceToUnit
import com.webmy.core_sdk.util.failure
import com.webmy.core_sdk.util.flatMap
import com.webmy.core_sdk.util.singleReplaySharedFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.CoroutineContext

interface BillingManager {

    val productsFlow: Flow<List<Product>>

    suspend fun fetchProducts(): Result<Unit>

    suspend fun purchase(activity: Activity, productId: String): Result<Unit>

    suspend fun canBePurchased(productId: String): Boolean

    suspend fun awaitInitialized()
}


class RealBillingManager(
    application: Application,
    private val oneTimeProducts: Set<String>,
    private val subscriptionProducts: Set<String>,
) : BillingManager, PurchasesUpdatedListener, CoroutineScope {

    override val coroutineContext: CoroutineContext = Dispatchers.IO

    private val pendingPurchaseParams = PendingPurchasesParams.newBuilder()
        .enableOneTimeProducts()
        .enablePrepaidPlans()
        .build()

    private val billingClient = BillingClient.newBuilder(application)
        .enablePendingPurchases(pendingPurchaseParams)
        .setListener(this)
        .build()

    private val isConnected = MutableStateFlow(false)
    private val connectMutex = Mutex()

    init {
        launch {
            connectMutex.withLock {
                isConnected.value = false

                billingClient.startConnection(object : BillingClientStateListener {
                    override fun onBillingServiceDisconnected() {
                    }

                    override fun onBillingSetupFinished(billingResult: BillingResult) {
                        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                            isConnected.value = true
                        }
                    }
                })
            }

            isConnected.awaitTrue()
            fetchProducts()
        }
    }

    private val purchasesFlow = singleReplaySharedFlow<Set<String>>()
    private val oneTimeDetailsFlow = singleReplaySharedFlow<Map<String, ProductDetails>>()
    private val subscriptionDetailsFlow = singleReplaySharedFlow<Map<String, ProductDetails>>()

    override val productsFlow = combine(
        purchasesFlow,
        oneTimeDetailsFlow,
        subscriptionDetailsFlow
    ) { purchases, oneTimeDetails, subscriptionDetails ->
        buildList {
            oneTimeDetails.values.forEach { detail ->
                val productId = detail.productId
                val offerDetails = detail.oneTimePurchaseOfferDetails
                if (offerDetails != null) {
                    add(
                        Product.OneTime(
                            id = productId,
                            title = detail.title,
                            offerToken = offerDetails.offerToken,
                            formattedPrice = offerDetails.formattedPrice,
                            isPurchased = purchases.contains(productId)
                        )
                    )
                }

            }
            subscriptionDetails.values.forEach { detail ->
                val productId = detail.productId
                val offerDetails = detail.subscriptionOfferDetails?.firstOrNull()
                if (offerDetails != null) {
                    val phases = offerDetails.pricingPhases.pricingPhaseList.map {
                        it.billingCycleCount
                        Product.Subscription.Phase(
                            formattedPrice = it.formattedPrice,
                            priceMicros = it.priceAmountMicros,
                            currency = it.priceCurrencyCode,
                            billingPeriod = it.billingPeriod,
                            cycles = it.billingCycleCount
                        )
                    }
                    add(
                        Product.Subscription(
                            id = productId,
                            title = detail.title,
                            isPurchased = purchases.contains(productId),
                            offerToken = offerDetails.offerToken,
                            phases = phases
                        )
                    )
                }
            }
        }
    }


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

    override suspend fun fetchProducts(): Result<Unit> {
        return queryOneTimePurchases()
            .flatMap { oneTimePurchases ->
                querySubscriptionPurchases()
                    .map { subscriptionPurchases -> oneTimePurchases to subscriptionPurchases }
            }
            .flatMap { (oneTimePurchases, subscriptionPurchases) ->
                queryOneTimeProductsDetails()
                    .flatMap { oneTimeDetails ->
                        querySubscriptionProductsDetails()
                            .map { subscriptionDetails ->
                                FetchProductsData(
                                    oneTimePurchases = oneTimePurchases,
                                    subscriptionPurchases = subscriptionPurchases,
                                    oneTimeDetails = oneTimeDetails,
                                    subscriptionDetails = subscriptionDetails
                                )
                            }
                    }
            }
            .map { data ->
                val purchasesSet = buildSet {
                    data.oneTimePurchases.forEach { purchase ->
                        purchase.products.forEach { productId ->
                            if (purchase.isAcknowledged) add(productId)
                        }
                    }
                    data.subscriptionPurchases.forEach { purchase ->
                        purchase.products.forEach { productId ->
                            if (purchase.isAcknowledged) add(productId)
                        }
                    }
                }

                val oneTimeDetailsMap = mutableMapOf<String, ProductDetails>()
                data.oneTimeDetails.forEach { detail ->
                    oneTimeDetailsMap[detail.productId] = detail
                }

                val subscriptionDetailsMap = mutableMapOf<String, ProductDetails>()
                data.subscriptionDetails.forEach { detail ->
                    subscriptionDetailsMap[detail.productId] = detail
                }

                purchasesFlow.emit(purchasesSet)
                oneTimeDetailsFlow.emit(oneTimeDetailsMap)
                subscriptionDetailsFlow.emit(subscriptionDetailsMap)
            }
            .coerceToUnit()
    }

    override suspend fun purchase(activity: Activity, productId: String): Result<Unit> {
        val tokenFlow = productsFlow
            .mapNotNull { it.find { it.id == productId }?.offerToken }

        val offerDetails = combine(
            oneTimeDetailsFlow,
            subscriptionDetailsFlow
        ) { oneTimeDetails, subscriptionDetails ->
            val details = oneTimeDetails[productId] ?: subscriptionDetails[productId]
            details
        }
            .filterNotNull()
            .map { tokenFlow.first() to it }
            .first()

        return runCatching {
            val params = BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(offerDetails.second)
                .setOfferToken(offerDetails.first)
                .build()

            val flowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(listOf(params))
                .build()

            billingClient.launchBillingFlow(activity, flowParams)
        }.coerceToUnit()
    }

    private suspend fun queryOneTimePurchases(): Result<List<Purchase>> {
        if (oneTimeProducts.isEmpty()) {
            return Result.success(emptyList())
        }
        return runCatching {
            billingClient.queryPurchasesAsync(queryOneTimePurchasesParams)
        }
            .flatMap {
                if (it.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Result.success(it.purchasesList)
                } else {
                    failure("Unable to fetch purchased one-time products")
                }
            }
    }

    private suspend fun querySubscriptionPurchases(): Result<List<Purchase>> {
        if (subscriptionProducts.isEmpty()) {
            return Result.success(emptyList())
        }
        return runCatching {
            billingClient.queryPurchasesAsync(querySubscriptionPurchasesParams)
        }
            .flatMap {
                if (it.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Result.success(it.purchasesList)
                } else {
                    failure("Unable to fetch purchased subscription products")
                }
            }
    }

    private suspend fun queryOneTimeProductsDetails(): Result<List<ProductDetails>> {
        if (oneTimeProducts.isEmpty()) {
            return Result.success(emptyList())
        }
        return runCatching {
            billingClient.queryProductDetails(queryOneTimeDetailsParams)
        }
            .flatMap {
                if (it.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Result.success(it.productDetailsList.orEmpty())
                } else {
                    failure("Unable to fetch one-time products")
                }
            }
    }

    private suspend fun querySubscriptionProductsDetails(): Result<List<ProductDetails>> {
        if (subscriptionProducts.isEmpty()) {
            return Result.success(emptyList())
        }
        return runCatching {
            billingClient.queryProductDetails(querySubscriptionDetailsParams)
        }
            .flatMap {
                if (it.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Result.success(it.productDetailsList.orEmpty())
                } else {
                    failure("Unable to fetch subscription products")
                }
            }
    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?,
    ) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            purchases?.forEach(::handlePurchase)
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val params = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()

                billingClient.acknowledgePurchase(params) { result ->
                    if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                        launch {
                            val set = purchasesFlow.first().toMutableSet()
                            set.addAll(purchase.products)
                            purchasesFlow.emit(set)
                        }
                    }
                }
            }
        }
    }

    override suspend fun canBePurchased(productId: String): Boolean {
        val premiumProductAvailableToPurchase = productsFlow
            .map { products ->
                products.filter { !it.isPurchased }
                    .find { it.id == productId }
            }
            .first()

        return premiumProductAvailableToPurchase != null
    }

    override suspend fun awaitInitialized() {
        productsFlow.first()
    }
}