package com.webmy.core_sdk.tools.ads

import android.app.Activity
import android.widget.FrameLayout
import com.webmy.core_sdk.tools.billing.BillingManager
import com.webmy.core_sdk.tools.billing.containsPurchased
import com.webmy.core_sdk.tools.remoteconfig.RemoteConfigManager
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
        autoGrant: Boolean = true,
        rewardCallback: (Boolean) -> Unit,
    )

    fun requestInterstitial(activity: Activity)
}

class RealAdsPremiumManager(
    premiumProductId: String,
    private val firstSkipAdsAmountRemoteConfigKey: String?,
    private val skipAdsAmountRemoteConfigKey: String?,
    billingManager: BillingManager,
    private val adsManager: AdsManager,
    private val remoteConfigManager: RemoteConfigManager,
) : AdsPremiumManager, CoroutineScope {

    companion object {
        private const val DEFAULT_FIRST_SKIP_AMOUNT = 2L
        private const val DEFAULT_SKIP_AMOUNT = 3L
    }

    private var currentTriggerInterCount = 0L

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

            if (!isPremium) {
                val firstSkipAdsAmount = firstSkipAdsAmountRemoteConfigKey?.let {
                    remoteConfigManager.getSyncedLong(it)
                        .getOrDefault(DEFAULT_FIRST_SKIP_AMOUNT)
                } ?: DEFAULT_FIRST_SKIP_AMOUNT

                val skipAdsAmount = skipAdsAmountRemoteConfigKey?.let {
                    remoteConfigManager.getSyncedLong(it)
                        .getOrDefault(DEFAULT_SKIP_AMOUNT)
                } ?: DEFAULT_SKIP_AMOUNT

                currentTriggerInterCount++
                if (currentTriggerInterCount < firstSkipAdsAmount) return@launch

                val countSinceInitial = currentTriggerInterCount - firstSkipAdsAmount

                if (countSinceInitial % skipAdsAmount == 0L) {
                    withContext(Dispatchers.Main) {
                        adsManager.showInter(activity)
                    }
                }
            }
        }
    }

    override fun requestReward(
        activity: Activity,
        placement: String?,
        autoGrant: Boolean,
        rewardCallback: (Boolean) -> Unit,
    ) {
        launch {
            if (isPremiumFlow.first()) {
                if (autoGrant) rewardCallback(true)
            } else {
                withContext(Dispatchers.Main) {
                    adsManager.showReward(activity, placement, rewardCallback)
                }
            }
        }
    }
}