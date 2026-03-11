package us.webmy.core_sdk_extended.domain.interactor

import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import us.webmy.core_sdk_extended.tools.billing.BillingManager
import us.webmy.core_sdk_extended.tools.billing.Product

interface PremiumInteractor {

    val subscriptionsFlow: Flow<List<Product.Subscription>>

    val isPremiumFlow: Flow<Boolean>

    fun purchase(productId: String, activity: AppCompatActivity)

}

suspend fun PremiumInteractor.isPremium() = isPremiumFlow.first()

internal class RealPremiumInteractor(
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

    override fun purchase(productId: String, activity: AppCompatActivity) {
        billingManager.purchase(activity, productId)
    }
}