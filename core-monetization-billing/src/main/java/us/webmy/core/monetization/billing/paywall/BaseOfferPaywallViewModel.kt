package us.webmy.core.monetization.billing.paywall

import kotlinx.coroutines.flow.mapNotNull
import us.webmy.core.monetization.billing.paywall.BasePaywallViewModel
import us.webmy.core.monetization.billing.paywall.OfferPaywallConfig
import us.webmy.core.monetization.billing.paywall.OfferUiState
import us.webmy.core.monetization.billing.BillingManager
import us.webmy.core.monetization.billing.Product
import us.webmy.core.analytics.AnalyticsManager

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
