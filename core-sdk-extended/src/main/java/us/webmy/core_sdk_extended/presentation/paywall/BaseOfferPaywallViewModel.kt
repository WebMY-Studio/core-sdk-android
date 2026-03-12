package us.webmy.core_sdk_extended.presentation.paywall

import kotlinx.coroutines.flow.mapNotNull
import us.webmy.core_sdk.presentation.base.navigator.NavigationProvider
import us.webmy.core_sdk.tools.analytics.AnalyticsManager
import us.webmy.core_sdk_extended.domain.interactor.PremiumInteractor
import us.webmy.core_sdk_extended.presentation.paywall.base.BasePaywallViewModel
import us.webmy.core_sdk_extended.presentation.paywall.model.OfferPaywallConfig
import us.webmy.core_sdk_extended.presentation.paywall.model.OfferUiState
import us.webmy.core_sdk_extended.tools.billing.Product

abstract class BaseOfferPaywallViewModel(
    private val config: OfferPaywallConfig,
    navigationProvider: NavigationProvider,
    premiumInteractor: PremiumInteractor,
    analyticsManager: AnalyticsManager
) : BasePaywallViewModel(
    navigationProvider, premiumInteractor, analyticsManager
) {

    val offerUiStateFlow = subscriptionsFlow
        .mapNotNull {
            val annual = it.phaseFor(config.basePlanId) ?: return@mapNotNull null
            val discount = it.phaseFor(config.offerPlanId) ?: return@mapNotNull null

            OfferUiState(annual, discount)
        }

    private fun List<Product.Subscription>.phaseFor(planId: String) =
        find { it.id == config.offerPlanId }?.phases?.firstOrNull()

    fun onContinueClick() {
        purchase(config.offerPlanId)
    }
}