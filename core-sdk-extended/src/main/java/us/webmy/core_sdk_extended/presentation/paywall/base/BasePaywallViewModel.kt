package us.webmy.core_sdk_extended.presentation.paywall.base

import androidx.core.os.bundleOf
import androidx.lifecycle.viewModelScope
import us.webmy.core_sdk.presentation.base.navigator.BaseNavigator
import us.webmy.core_sdk.presentation.base.viewmodel.BaseViewModel
import us.webmy.core_sdk.tools.analytics.AnalyticsManager
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import us.webmy.core_sdk_extended.domain.interactor.PremiumInteractor

abstract class BasePaywallViewModel(
    private val navigator: BaseNavigator,
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

    suspend fun purchase(productId: String) {
        purchaseInitiated = true
        premiumInteractor.purchase(productId, navigator.activity)
    }

    fun onCloseClick() {
        navigator.finish()
    }

    private fun startPurchaseObservation() {
        viewModelScope.launch {
            premiumInteractor.isPremiumFlow
                .filter { purchaseInitiated && it }
                .first()
            logEvent(eventName = "purchase_success")
            navigator.finish()
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