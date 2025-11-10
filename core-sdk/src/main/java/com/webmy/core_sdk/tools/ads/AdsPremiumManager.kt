package com.webmy.core_sdk.tools.ads

import android.app.Activity
import android.widget.FrameLayout
import com.webmy.core_sdk.tools.billing.BillingManager
import com.webmy.core_sdk.tools.billing.containsPurchased
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

interface AdsPremiumManager {
    val isPremiumFlow: Flow<Boolean>

    suspend fun requestBanner(
        activity: Activity,
        container: FrameLayout,
    )

    suspend fun requestReward(
        activity: Activity,
        placement: String? = null,
        rewardCallback: (Boolean) -> Unit,
    )

    suspend fun requestInterstitial(activity: Activity)
}

class RealAdsPremiumManager(
    premiumProductId: String,
    billingManager: BillingManager,
    private val adsManager: AdsManager,
) : AdsPremiumManager {

    override val isPremiumFlow = billingManager.productsFlow
        .map { it.containsPurchased(premiumProductId) }


    override suspend fun requestBanner(activity: Activity, container: FrameLayout) {
        if (isPremiumFlow.first()) {
            adsManager.hideBanner(activity, container)
        } else {
            adsManager.showBanner(activity, container)
        }
    }

    override suspend fun requestInterstitial(activity: Activity) {
        if (!isPremiumFlow.first()) {
            adsManager.showInter(activity)
        }
    }

    override suspend fun requestReward(
        activity: Activity,
        placement: String?,
        rewardCallback: (Boolean) -> Unit,
    ) {
        return adsManager.showReward(activity, placement, rewardCallback)
    }
}