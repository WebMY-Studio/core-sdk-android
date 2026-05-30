package us.webmy.core.monetization.billing.presentation.paywall.base

import android.os.Bundle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import us.webmy.core.ui.presentation.base.navigator.Navigation
import us.webmy.core.ui.presentation.base.viewmodel.BaseViewModel
import us.webmy.core.tools.analytics.AnalyticsManager
import us.webmy.core.monetization.billing.domain.interactor.PremiumInteractor

abstract class BasePaywallViewModel(
    private val premiumInteractor: PremiumInteractor,
    private val analyticsManager: AnalyticsManager
) : BaseViewModel() {

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
        val bundle = Bundle(1).apply { putString(prop.first, prop.second) }
        analyticsManager.logFirebase(
            eventName = eventName,
            bundle = bundle
        )
    }
}