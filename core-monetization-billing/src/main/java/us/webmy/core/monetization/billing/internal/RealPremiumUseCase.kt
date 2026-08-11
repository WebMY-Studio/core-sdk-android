package us.webmy.core.monetization.billing.internal

import kotlinx.coroutines.flow.map
import us.webmy.core.monetization.billing.BillingManager
import us.webmy.core.monetization.billing.PremiumUseCase

internal class RealPremiumUseCase(
    billingManager: BillingManager,
    private val premiumProductIds: Set<String>,
) : PremiumUseCase {

    override val isPremiumFlow = billingManager.subscribeProducts()
        .map {
            it.any { product ->
                product.isPurchased && product.id in premiumProductIds
            }
        }
}
