package us.webmy.core.monetization.billing.presentation.paywall.base

import android.os.Bundle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import us.webmy.core.monetization.billing.domain.interactor.PremiumUseCase
import us.webmy.core.monetization.billing.tools.billing.BillingManager
import us.webmy.core.monetization.billing.tools.billing.PurchaseOutcome
import us.webmy.core.tools.analytics.AnalyticsManager
import us.webmy.core.ui.presentation.base.navigator.Navigation
import us.webmy.core.ui.presentation.base.viewmodel.BaseViewModel

abstract class BasePaywallViewModel(
    private val billingManager: BillingManager,
    premiumUseCase: PremiumUseCase,
    private val analyticsManager: AnalyticsManager,
) : BaseViewModel() {

    abstract val originProperty: String

    init {
        logEvent(eventName = "paywall_shown")
    }

    protected val subscriptionsFlow = premiumUseCase.subscriptionsFlow

    protected fun purchase(productId: String) {
        viewModelScope.launch {
            val outcome = billingManager.purchase(productId)
            when (outcome) {
                is PurchaseOutcome.Success -> {
                    logEvent(eventName = "purchase_success")
                    navigateTo(Navigation.Back)
                }
                is PurchaseOutcome.Pending -> logEvent(eventName = "purchase_pending")
                is PurchaseOutcome.Cancelled -> logEvent(eventName = "purchase_cancelled")
                is PurchaseOutcome.Failed -> logEvent(eventName = "purchase_failed")
            }
        }
    }

    fun onCloseClick() {
        navigateTo(Navigation.Back)
    }

    private fun logEvent(eventName: String) {
        val prop = "paywall_place" to originProperty
        analyticsManager.logEvent(
            eventName = eventName,
            props = mapOf(prop)
        )
        val bundle = Bundle(1).apply { putString(prop.first, prop.second) }
        analyticsManager.logFirebase(
            eventName = eventName,
            bundle = bundle
        )
    }
}
