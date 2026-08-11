package us.webmy.core.monetization.billing.internal

import android.app.Activity
import android.app.Application
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.consumePurchase
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import kotlinx.coroutines.suspendCancellableCoroutine
import us.webmy.core.SdkError

internal class BillingClientWrapper(
    application: Application,
    listener: PurchasesUpdatedListener,
    onDisconnected: () -> Unit,
) {

    private val pendingPurchaseParams = PendingPurchasesParams.newBuilder()
        .enableOneTimeProducts()
        .enablePrepaidPlans()
        .build()

    val client: BillingClient = BillingClient.newBuilder(application)
        .enablePendingPurchases(pendingPurchaseParams)
        .setListener(listener)
        .build()

    private val disconnectCallback = onDisconnected

    suspend fun connect(): Result<Unit> = suspendCancellableCoroutine { cont ->
        client.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                val result =
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        Result.success(Unit)
                    } else {
                        Result.failure(
                            SdkError.Billing.FlowFailed("Setup failed: ${billingResult.responseCode} ${billingResult.debugMessage}")
                        )
                    }
                if (!cont.isCompleted) cont.resumeWith(Result.success(result))
            }

            override fun onBillingServiceDisconnected() {
                disconnectCallback.invoke()
            }
        })
    }

    suspend fun queryPurchases(productType: String): Result<List<Purchase>> = runCatching {
        val params = QueryPurchasesParams.newBuilder().setProductType(productType).build()
        val result = client.queryPurchasesAsync(params)
        if (result.billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
            throw SdkError.Billing.FlowFailed("queryPurchases failed: ${result.billingResult.responseCode}")
        }
        result.purchasesList
    }

    suspend fun queryProductDetails(
        productIds: Set<String>,
        productType: String,
    ): Result<List<ProductDetails>> = runCatching {
        if (productIds.isEmpty()) return@runCatching emptyList()
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                productIds.map {
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(it)
                        .setProductType(productType)
                        .build()
                }
            ).build()
        val result = client.queryProductDetails(params)
        if (result.billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
            throw SdkError.Billing.FlowFailed("queryProductDetails failed: ${result.billingResult.responseCode}")
        }
        result.productDetailsList.orEmpty()
    }

    fun launchBillingFlow(activity: Activity, params: BillingFlowParams): BillingResult =
        client.launchBillingFlow(activity, params)

    suspend fun acknowledge(purchaseToken: String): Boolean = suspendCancellableCoroutine { cont ->
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchaseToken)
            .build()
        client.acknowledgePurchase(params) { result ->
            if (!cont.isCompleted) {
                cont.resumeWith(
                    Result.success(result.responseCode == BillingClient.BillingResponseCode.OK)
                )
            }
        }
    }

    suspend fun consume(purchaseToken: String): Boolean {
        val params = ConsumeParams.newBuilder().setPurchaseToken(purchaseToken).build()
        val result = runCatching { client.consumePurchase(params) }.getOrNull() ?: return false
        return result.billingResult.responseCode == BillingClient.BillingResponseCode.OK
    }
}
