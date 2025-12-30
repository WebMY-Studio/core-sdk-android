package com.webmy.core_sdk.domain.interactor

import androidx.appcompat.app.AppCompatActivity
import com.webmy.core_sdk.tools.billing.BillingManager
import com.webmy.core_sdk.tools.billing.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlin.collections.any
import kotlin.collections.filterIsInstance

interface PremiumInteractor {

    val subscriptionsFlow: Flow<List<Product.Subscription>>

    val isPremiumFlow: Flow<Boolean>

    suspend fun purchase(productId: String, activity: AppCompatActivity)

}

suspend fun PremiumInteractor.isPremium() = isPremiumFlow.first()

class RealPremiumInteractor(
    val billingManager: BillingManager
) : PremiumInteractor {

    override val subscriptionsFlow = billingManager.productsFlow
        .map {
            it.filterIsInstance<Product.Subscription>()
        }

    override val isPremiumFlow = subscriptionsFlow
        .map { products ->
            products.any { it.isPurchased }
        }

    override suspend fun purchase(productId: String, activity: AppCompatActivity) {
        billingManager.purchase(activity, productId)
    }
}