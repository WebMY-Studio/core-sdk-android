package us.webmy.core_sdk_extended.presentation.paywall

import androidx.lifecycle.viewModelScope
import us.webmy.core_sdk.R
import us.webmy.core_sdk.presentation.adapters.subscriptions.SubscriptionsUiModel
import us.webmy.core_sdk.presentation.base.navigator.BaseNavigator
import us.webmy.core_sdk.tools.analytics.AnalyticsManager
import us.webmy.core_sdk.tools.formatters.DateTimeFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import us.webmy.core_sdk_extended.domain.interactor.PremiumInteractor
import us.webmy.core_sdk_extended.presentation.paywall.base.BasePaywallViewModel
import us.webmy.core_sdk_extended.presentation.paywall.model.PaywallUiState
import us.webmy.core_sdk_extended.presentation.paywall.model.PlanListPaywallConfig
import java.time.Period

abstract class BasePlanListPaywallViewModel(
    private val config: PlanListPaywallConfig,
    private val navigator: BaseNavigator,
    premiumInteractor: PremiumInteractor,
    analyticsManager: AnalyticsManager
) : BasePaywallViewModel(
    navigator, premiumInteractor, analyticsManager
) {
    private val selectedPlanFlow = MutableStateFlow(config.defaultSelectedPlanId)

    private val subscriptionFlow = subscriptionsFlow
        .map { subs -> subs.filter { config.planList.contains(it.id) } }

    val paywallUiState =
        combine(subscriptionFlow, selectedPlanFlow) { subscriptions, selectedPlanId ->
            val context = navigator.activity
            val plans = subscriptions.mapNotNull {
                val freeFormatted = it.phases.find { it.priceMicros == 0L }?.let {
                    val periodFormatted =
                        DateTimeFormatter.formatPeriod(context, it.billingPeriod, 1)
                    context.getString(R.string.paywall_free, periodFormatted)
                }

                val phase = it.phases.find { it.priceMicros != 0L } ?: return@mapNotNull null

                val price = (phase.priceMicros / 10_000L) / 100f

                val period = Period.parse(phase.billingPeriod)
                val periodFormatted = DateTimeFormatter.formatPeriod(context, period)

                val priceFormatted = context.getString(
                    R.string.paywall_price_placeholder,
                    price.toString(),
                    phase.currency,
                    periodFormatted
                )

                val delitel = when {
                    period.years != 0 -> 52.14f * period.years
                    period.months != 0 -> 4.34f * period.months
                    period.days != 0 -> period.days / 7f
                    else -> 1f
                }
                val pricePerWeek = (phase.priceMicros / 10_000L) / (delitel * 100f)
                val pricePerWeekRound = (pricePerWeek * 100).toInt() / 100f

                val priceWeekFormatted = context.getString(
                    R.string.paywall_price_placeholder_2,
                    pricePerWeekRound.toString(),
                    phase.currency,
                    context.getString(R.string.week)
                )

                SubscriptionsUiModel(
                    productId = it.id,
                    title = it.title,
                    isSelected = it.id == selectedPlanId,
                    freeFormatted = freeFormatted,
                    formattedPrice = priceFormatted,
                    formattedPriceWeek = priceWeekFormatted
                )
            }

            val buttonText = subscriptions
                .find { it.id == selectedPlanId }
                ?.phases
                ?.firstOrNull()
                ?.let {
                    val hasFreeTrial = it.priceMicros == 0L
                    if (hasFreeTrial) {
                        context.getString(R.string.paywall_btn_text_free)
                    } else {
                        context.getString(R.string.paywall_btn_text_continue)
                    }
                }
            PaywallUiState(
                plans = plans,
                buttonText = buttonText.orEmpty()
            )
        }


    fun onConfirmClick() {
        viewModelScope.launch {
            val plan = paywallUiState.first().plans.find { it.isSelected } ?: return@launch
            purchase(plan.productId)
        }
    }

    fun onPlanSelected(productId: String) {
        selectedPlanFlow.value = productId
    }
}