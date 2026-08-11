package us.webmy.core.monetization.billing.paywall

import android.os.Bundle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import us.webmy.core.monetization.billing.BillingManager
import us.webmy.core.monetization.billing.Product
import us.webmy.core.monetization.billing.PurchaseOutcome
import us.webmy.core.analytics.AnalyticsManager
import us.webmy.core.navigation.Navigation
import us.webmy.core.presentation.BaseViewModel

abstract class BasePaywallViewModel(
    private val billingManager: BillingManager,
    private val analyticsManager: AnalyticsManager,
) : BaseViewModel() {

    abstract val originProperty: String

    init {
        logEvent(eventName = "paywall_shown")
    }

    protected val subscriptionsFlow = billingManager.subscribeProducts()
        .map { it.filterIsInstance<Product.Subscription>() }

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
