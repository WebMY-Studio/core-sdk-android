package com.webmy.core_sdk.tools.ads

import android.app.Activity
import android.widget.FrameLayout
import com.webmy.core_sdk.tools.billing.BillingManager
import com.webmy.core_sdk.tools.billing.containsPurchased
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlin.coroutines.CoroutineContext

interface AdsPremiumManager {
    val isPremiumFlow: StateFlow<Boolean>

    fun requestBanner(
        activity: Activity,
        container: FrameLayout,
    ): Boolean

    fun requestReward(
        activity: Activity,
        placement: String? = null,
        rewardCallback: (Boolean) -> Unit,
    )

    fun requestInterstitial(activity: Activity): Boolean
}

class RealAdsPremiumManager(
    premiumProductId: String,
    billingManager: BillingManager,
    private val adsManager: AdsManager,
) : AdsPremiumManager, CoroutineScope {

    override val isPremiumFlow = billingManager.productsFlow
        .map { it.containsPurchased(premiumProductId) }
        .stateIn(
            scope = this,
            started = SharingStarted.Eagerly,
            initialValue = false
        )

    override val coroutineContext: CoroutineContext = Dispatchers.IO

    override fun requestBanner(activity: Activity, container: FrameLayout): Boolean {
        return if (isPremiumFlow.value) {
            adsManager.hideBanner(activity, container)
            false
        } else {
            adsManager.showBanner(activity, container)
        }
    }

    override fun requestInterstitial(activity: Activity): Boolean {
        return if (isPremiumFlow.value) {
            false
        } else {
            adsManager.showInter(activity)
        }
    }

    override fun requestReward(
        activity: Activity,
        placement: String?,
        rewardCallback: (Boolean) -> Unit,
    ) {
        return adsManager.showReward(activity, placement, rewardCallback)
    }
}