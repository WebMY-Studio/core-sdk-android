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
import kotlinx.coroutines.withContext
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

    override val coroutineContext: CoroutineContext = Dispatchers.IO

    override val isPremiumFlow = billingManager.productsFlow
        .map { it.containsPurchased(premiumProductId) }

    override fun requestBanner(activity: Activity, container: FrameLayout) {
        launch {
            val isPremium = isPremiumFlow.first()
            withContext(Dispatchers.Main) {
                if (isPremium) {
                    adsManager.hideBanner(activity, container)
                } else {
                    adsManager.showBanner(activity, container)
                }
            }
        }
    }

    override fun requestInterstitial(activity: Activity) {
        launch {
            val isPremium = isPremiumFlow.first()
            withContext(Dispatchers.Main) {
                if (!isPremium) {
                    adsManager.showInter(activity)
                }
            }
        }
    }

    override fun requestReward(
        activity: Activity,
        placement: String?,
        rewardCallback: (Boolean) -> Unit,
    ) {
        adsManager.showReward(activity, placement, rewardCallback)
    }

}