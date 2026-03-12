package us.webmy.core_sdk_extended.presentation.paywall.base

import androidx.core.os.bundleOf
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import us.webmy.core_sdk.presentation.base.navigator.Navigation
import us.webmy.core_sdk.presentation.base.navigator.NavigationProvider
import us.webmy.core_sdk.presentation.base.viewmodel.BaseViewModel
import us.webmy.core_sdk.tools.analytics.AnalyticsManager
import us.webmy.core_sdk_extended.domain.interactor.PremiumInteractor

abstract class BasePaywallViewModel(
    navigationProvider: NavigationProvider,
    private val premiumInteractor: PremiumInteractor,
    private val analyticsManager: AnalyticsManager
) : BaseViewModel(navigationProvider) {

    abstract val originProperty: String

    init {
        startPurchaseObservation()
        logEvent(eventName = "paywall_shown")
    }

    protected val subscriptionsFlow = premiumInteractor.subscriptionsFlow

    private var purchaseInitiated = false

    protected fun purchase(productId: String) {
        purchaseInitiated = true
        navigateTo(Navigation.Purchase(productId))
    }

    fun onCloseClick() {
        navigateTo(Navigation.Finish)
    }

    private fun startPurchaseObservation() {
        viewModelScope.launch {
            premiumInteractor.isPremiumFlow
                .filter { purchaseInitiated && it }
                .first()
            logEvent(eventName = "purchase_success")
            navigateTo(Navigation.Finish)
        }
    }

    private fun logEvent(eventName: String) {
        val prop = "paywall_place" to originProperty
        analyticsManager.logEvent(
            eventName = eventName,
            props = mapOf(prop)
        )
        analyticsManager.logFirebase(
            eventName = eventName,
            bundle = bundleOf(prop)
        )
    }
}