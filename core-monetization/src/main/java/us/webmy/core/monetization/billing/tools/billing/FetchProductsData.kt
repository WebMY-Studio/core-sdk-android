package us.webmy.core.monetization.billing.tools.billing

import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase

internal data class FetchProductsData(
    val oneTimePurchases: List<Purchase>,
    val subscriptionPurchases: List<Purchase>,
    val oneTimeDetails: List<ProductDetails>,
    val subscriptionDetails: List<ProductDetails>
)