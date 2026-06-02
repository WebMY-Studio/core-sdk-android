package us.webmy.core.monetization.billing.domain.interactor

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import us.webmy.core.monetization.billing.tools.billing.BillingManager

interface PremiumUseCase {

    val isPremiumFlow: Flow<Boolean>
}

suspend fun PremiumUseCase.isPremium() = isPremiumFlow.first()

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
