package us.webmy.core.monetization.billing.presentation.paywall

import kotlinx.coroutines.flow.mapNotNull
import us.webmy.core.monetization.billing.presentation.paywall.base.BasePaywallViewModel
import us.webmy.core.monetization.billing.presentation.paywall.model.OfferPaywallConfig
import us.webmy.core.monetization.billing.presentation.paywall.model.OfferUiState
import us.webmy.core.monetization.billing.tools.billing.BillingManager
import us.webmy.core.monetization.billing.tools.billing.Product
import us.webmy.core.tools.analytics.AnalyticsManager

abstract class BaseOfferPaywallViewModel(
    private val config: OfferPaywallConfig,
    billingManager: BillingManager,
    analyticsManager: AnalyticsManager,
) : BasePaywallViewModel(billingManager, analyticsManager) {

    val offerUiStateFlow = subscriptionsFlow
        .mapNotNull {
            val annual = it.phaseFor(config.basePlanId) ?: return@mapNotNull null
            val discount = it.phaseFor(config.offerPlanId) ?: return@mapNotNull null

            OfferUiState(annual, discount)
        }

    private fun List<Product.Subscription>.phaseFor(planId: String) =
        find { it.id == planId }?.phases?.firstOrNull()

    fun onContinueClick() {
        purchase(config.offerPlanId)
    }
}
