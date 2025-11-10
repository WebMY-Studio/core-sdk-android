package com.webmy.core_sdk.tools.ads

import android.app.Activity
import android.widget.FrameLayout
import com.webmy.core_sdk.tools.billing.BillingManager
import com.webmy.core_sdk.tools.billing.containsPurchased
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

interface AdsPremiumManager {
    val isPremiumFlow: Flow<Boolean>

    fun requestBanner(
        activity: Activity,
        container: FrameLayout,
    )

    fun requestReward(
        activity: Activity,
        placement: String? = null,
        rewardCallback: (Boolean) -> Unit,
    )

    fun requestInterstitial(activity: Activity)
}

class RealAdsPremiumManager(
    premiumProductId: String,
    billingManager: BillingManager,
    private val adsManager: AdsManager,
) : AdsPremiumManager, CoroutineScope {

    override val isPremiumFlow = billingManager.productsFlow
        .map { it.containsPurchased(premiumProductId) }

    override val coroutineContext: CoroutineContext = Dispatchers.IO

    override fun requestBanner(activity: Activity, container: FrameLayout) {
        launch {
            if (isPremiumFlow.first()) {
                adsManager.hideBanner(activity, container)
            } else {
                adsManager.showBanner(activity, container)
            }
        }
    }

    override fun requestInterstitial(activity: Activity) {
        launch {
            if (!isPremiumFlow.first()) {
                adsManager.showInter(activity)
            }
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