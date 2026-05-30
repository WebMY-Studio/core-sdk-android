package us.webmy.core.monetization.billing.domain.interactor

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import us.webmy.core.monetization.billing.tools.billing.BillingManager
import us.webmy.core.monetization.billing.tools.billing.Product

interface PremiumInteractor {

    val subscriptionsFlow: Flow<List<Product.Subscription>>

    val isPremiumFlow: Flow<Boolean>

}

suspend fun PremiumInteractor.isPremium() = isPremiumFlow.first()

internal class RealPremiumInteractor(
    billingManager: BillingManager
) : PremiumInteractor {

    override val subscriptionsFlow = billingManager.subscribeProducts()
        .map {
            it.filterIsInstance<Product.Subscription>()
        }

    override val isPremiumFlow = subscriptionsFlow
        .map { products ->
            products.any { it.isPurchased }
        }
}