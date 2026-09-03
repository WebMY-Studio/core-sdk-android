package us.webmy.core.monetization.billing.paywall

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import us.webmy.core.analytics.AnalyticsManager
import us.webmy.core.monetization.billing.BillingManager
import us.webmy.core.monetization.billing.Product
import us.webmy.core.monetization.billing.PurchaseOutcome
import us.webmy.core.navigation.Navigation
import us.webmy.core.presentation.BaseViewModel

abstract class BasePaywallViewModel(
    private val billingManager: BillingManager,
    private val analyticsManager: AnalyticsManager,
) : BaseViewModel() {

    abstract val originProperty: String

    init {
        logPaywallEvent(eventName = "paywall_shown")
    }

    protected val subscriptionsFlow = billingManager.subscribeProducts()
        .map { it.filterIsInstance<Product.Subscription>() }

    protected fun purchase(productId: String) {
        viewModelScope.launch {
            val outcome = billingManager.purchase(productId)
            when (outcome) {
                is PurchaseOutcome.Success -> {
                    logPaywallEvent(eventName = "purchase_success")
                    navigateTo(Navigation.Back)
                }

                is PurchaseOutcome.Pending -> logPaywallEvent(eventName = "purchase_pending")
                is PurchaseOutcome.Cancelled -> logPaywallEvent(eventName = "purchase_cancelled")
                is PurchaseOutcome.Failed -> logPaywallEvent(eventName = "purchase_failed")
            }
        }
    }

    fun onCloseClick() {
        navigateTo(Navigation.Back)
    }

    private fun logPaywallEvent(eventName: String, productId: String? = null) {
        analyticsManager.logEvent(
            eventName = eventName,
            props = mutableMapOf(
                "paywall_place" to originProperty,
                "product_id" to productId
            )
        )
    }
}
