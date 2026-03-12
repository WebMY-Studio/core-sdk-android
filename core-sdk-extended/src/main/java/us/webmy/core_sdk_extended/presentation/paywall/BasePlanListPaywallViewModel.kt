package us.webmy.core_sdk_extended.presentation.paywall

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import us.webmy.core_sdk.presentation.base.navigator.NavigationProvider
import us.webmy.core_sdk.tools.analytics.AnalyticsManager
import us.webmy.core_sdk_extended.domain.interactor.PremiumInteractor
import us.webmy.core_sdk_extended.presentation.paywall.adapter.SubscriptionsUiModel
import us.webmy.core_sdk_extended.presentation.paywall.base.BasePaywallViewModel
import us.webmy.core_sdk_extended.presentation.paywall.model.PaywallUiState
import us.webmy.core_sdk_extended.presentation.paywall.model.PlanListPaywallConfig

abstract class BasePlanListPaywallViewModel(
    private val config: PlanListPaywallConfig,
    navigationProvider: NavigationProvider,
    premiumInteractor: PremiumInteractor,
    analyticsManager: AnalyticsManager
) : BasePaywallViewModel(
    navigationProvider, premiumInteractor, analyticsManager
) {
    private val selectedPlanFlow = MutableStateFlow(config.defaultSelectedPlanId)

    private val subscriptionFlow = subscriptionsFlow
        .map { subs -> subs.filter { config.planList.contains(it.id) } }

    val paywallUiState =
        combine(subscriptionFlow, selectedPlanFlow) { subscriptions, selectedPlanId ->
            val plans = subscriptions.map {
                SubscriptionsUiModel(
                    productId = it.id,
                    title = it.title,
                    isSelected = it.id == selectedPlanId,
                    subscription = it,
                )
            }

            PaywallUiState(
                plans = plans,
                selectedPlanId = selectedPlanId
            )
        }

    fun onContinueClick() {
        viewModelScope.launch {
            val plan = paywallUiState.first().plans.find { it.isSelected } ?: return@launch
            purchase(plan.productId)
        }
    }

    fun onPlanSelected(productId: String) {
        selectedPlanFlow.value = productId
    }
}