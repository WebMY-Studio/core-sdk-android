package us.webmy.core.monetization.billing.domain.interactor

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import us.webmy.core.monetization.billing.tools.billing.BillingManager
import us.webmy.core.monetization.billing.tools.billing.Product

interface PremiumUseCase {

    val subscriptionsFlow: Flow<List<Product.Subscription>>

    val isPremiumFlow: Flow<Boolean>
}

suspend fun PremiumUseCase.isPremium() = isPremiumFlow.first()

internal class RealPremiumUseCase(
    billingManager: BillingManager,
    private val premiumProductIds: Set<String>,
) : PremiumUseCase {

    override val subscriptionsFlow = billingManager.subscribeProducts()
        .map { it.filterIsInstance<Product.Subscription>() }

    override val isPremiumFlow = billingManager.subscribeProducts()
        .map { products ->
            if (premiumProductIds.isEmpty()) {
                products.any { it.isPurchased }
            } else {
                products.any { p -> p.isPurchased && p.id in premiumProductIds }
            }
        }
}
