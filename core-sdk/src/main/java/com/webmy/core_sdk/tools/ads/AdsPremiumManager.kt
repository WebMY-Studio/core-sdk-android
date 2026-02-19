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

    interface Factory {

        fun create(
            configCreator: suspend () -> AdsPremiumConfig
        ): AdsPremiumManager
    }

    val isPremiumFlow: Flow<Boolean>

    fun requestBanner(
        activity: Activity,
        container: FrameLayout
    )

    fun requestReward(
        activity: Activity,
        placement: String? = null,
        grantWhenPremium: Boolean = true,
        source: String? = null,
        rewardCallback: (Boolean) -> Unit,
    )

    fun requestInterstitial(
        activity: Activity,
        source: String? = null
    )
}

internal class RealAdsPremiumManager(
    premiumProductIds: List<String>,
    billingManager: BillingManager,
    private val adsManager: AdsManager,
    val configCreator: suspend () -> AdsPremiumConfig
) : AdsPremiumManager, CoroutineScope {

    private var currentTriggerInterCount = 0L

    override val coroutineContext: CoroutineContext = Dispatchers.IO

    private var config: AdsPremiumConfig? = null

    override val isPremiumFlow = billingManager.productsFlow.map { products ->
        premiumProductIds.any { premiumProductIds ->
            products.containsPurchased(premiumProductIds)
        }
    }

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

    override fun requestInterstitial(
        activity: Activity,
        source: String?
    ) {
        launch {
            val isPremium = isPremiumFlow.first()

            if (!isPremium) {
                val config = getConfig()

                val firstSkipAdsAmount = config.firstSkipAdsAmount
                val skipAdsAmount = config.skipAdsAmount

                currentTriggerInterCount++
                if (currentTriggerInterCount < firstSkipAdsAmount) return@launch

                val countSinceInitial = currentTriggerInterCount - firstSkipAdsAmount

                if (countSinceInitial % skipAdsAmount == 0L) {
                    withContext(Dispatchers.Main) {
                        adsManager.showInter(
                            activity = activity,
                            source = source
                        )
                    }
                }
            }
        }
    }

    override fun requestReward(
        activity: Activity,
        placement: String?,
        grantWhenPremium: Boolean,
        source: String?,
        rewardCallback: (Boolean) -> Unit,
    ) {
        launch {
            if (isPremiumFlow.first()) {
                if (grantWhenPremium) rewardCallback(true)
            } else {
                withContext(Dispatchers.Main) {
                    adsManager.showReward(
                        activity = activity,
                        source = source,
                        placement = placement,
                        rewardCallback = rewardCallback
                    )
                }
            }
        }
    }

    private suspend fun getConfig() = config ?: configCreator()
}

internal class AdsPremiumManagerFactory(
    private val premiumProductIds: List<String>,
    private val billingManager: BillingManager,
    private val adsManager: AdsManager,
) : AdsPremiumManager.Factory {

    override fun create(configCreator: suspend () -> AdsPremiumConfig): AdsPremiumManager {
        return RealAdsPremiumManager(
            premiumProductIds,
            billingManager,
            adsManager,
            configCreator
        )
    }
}