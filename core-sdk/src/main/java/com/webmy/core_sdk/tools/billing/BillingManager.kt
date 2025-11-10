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
import com.webmy.core_sdk.util.flatMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

interface BillingManager {

    val products: Flow<List<OneTimeProduct>>

    suspend fun fetchProducts(): Result<Unit>

    suspend fun purchase(activity: Activity, productId: String): Result<Unit>

    suspend fun canBePurchased(productId: String): Boolean
}

class RealBillingManager(
    application: Application,
    oneTimeProducts: List<String>,
) : BillingManager, PurchasesUpdatedListener {

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
        CoroutineScope(Dispatchers.IO)
            .launch {
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

    private val purchases = MutableStateFlow<Set<String>>(setOf())
    private val details = MutableStateFlow<Map<String, ProductDetails>>(mapOf())

    override val products = combine(purchases, details) { purchases, details ->
        details.values.map {
            val productId = it.productId
            val formattedPrice = it.oneTimePurchaseOfferDetails?.formattedPrice

            OneTimeProduct(
                id = productId,
                formattedPrice = formattedPrice,
                isPurchased = purchases.contains(productId)
            )
        }
    }


    val queryPurchasesParams = QueryPurchasesParams.newBuilder()
        .setProductType(BillingClient.ProductType.INAPP)
        .build()

    val queryDetailsParams = QueryProductDetailsParams.newBuilder()
        .setProductList(
            oneTimeProducts.map {
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(it)
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
            }
        ).build()

    override suspend fun fetchProducts(): Result<Unit> {
        return queryPurchases()
            .flatMap { purchases ->
                queryProductsDetails()
                    .map { details -> details to purchases }
            }
            .map { (detailsList, purchasesList) ->
                val purchasesSet = buildSet {
                    purchasesList.forEach { purchase ->
                        purchase.products.forEach { productId ->
                            if (purchase.isAcknowledged) add(productId)
                        }
                    }
                }


                val detailsMap = mutableMapOf<String, ProductDetails>()
                detailsList.forEach { detail ->
                    detailsMap[detail.productId] = detail
                }

                purchases.value = purchasesSet
                details.value = detailsMap
            }
            .coerceToUnit()
    }

    override suspend fun purchase(activity: Activity, productId: String): Result<Unit> {
        return queryProductsDetails()
            .map { details -> details.find { it.productId == productId } }
            .flatMap {
                if (it == null) {
                    Result.failure(Throwable("Cannot find product with :productId = $productId"))
                } else {
                    Result.success(it)
                }
            }
            .flatMap {
                val offerToken = it.oneTimePurchaseOfferDetails?.offerToken

                if (offerToken == null) {
                    Result.failure(Throwable("Cannot find offer token for :productId = $productId"))
                } else {
                    runCatching {
                        val flowParams = BillingFlowParams.newBuilder()
                            .setProductDetailsParamsList(
                                listOf(
                                    BillingFlowParams.ProductDetailsParams.newBuilder()
                                        .setProductDetails(it)
                                        .build()
                                )
                            ).build()

                        billingClient.launchBillingFlow(activity, flowParams)
                    }
                }
            }
            .coerceToUnit()
    }

    private suspend fun queryPurchases(): Result<List<Purchase>> {
        return runCatching {
            billingClient.queryPurchasesAsync(queryPurchasesParams)
        }
            .flatMap {
                if (it.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Result.success(it.purchasesList)
                } else {
                    Result.failure(Throwable("Unable to fetch purchased products"))
                }
            }
    }

    private suspend fun queryProductsDetails(): Result<List<ProductDetails>> {
        return runCatching {
            billingClient.queryProductDetails(queryDetailsParams)
        }
            .flatMap {
                if (it.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Result.success(it.productDetailsList.orEmpty())
                } else {
                    Result.failure(Throwable("Unable to fetch products"))
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
                        val set = purchases.value.toMutableSet()
                        set.addAll(purchase.products)
                        purchases.value = set
                    }
                }
            }
        }
    }

    override suspend fun canBePurchased(productId: String): Boolean {
        val premiumProductAvailableToPurchase = products
            .map { products ->
                products.filter { !it.isPurchased }
                    .find { it.id == productId }
            }
            .first()

        return premiumProductAvailableToPurchase != null
    }
}