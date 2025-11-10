package com.webmy.core_sdk.tools.ads

import android.app.Activity
import android.app.Application
import android.widget.FrameLayout
import androidx.core.view.isVisible
import com.appodeal.ads.Appodeal
import com.appodeal.ads.BannerCallbacks
import com.appodeal.ads.InterstitialCallbacks
import com.appodeal.ads.RewardedVideoCallbacks
import com.appodeal.ads.initializing.ApdInitializationCallback
import com.appodeal.ads.initializing.ApdInitializationError
import com.webmy.core_sdk.BuildConfig
import com.webmy.core_sdk.tools.analytics.AnalyticsManager
import com.webmy.core_sdk.util.dpToPx

/**
 * Use this entity if you want to handle ads by yourself
 *
 * Otherwise, use [AdsPremiumManager]
 */
interface AdsManager {

    fun showBanner(
        activity: Activity,
        container: FrameLayout,
    ): Boolean

    fun hideBanner(
        activity: Activity,
        container: FrameLayout,
    )

    fun showReward(
        activity: Activity,
        placement: String? = null,
        rewardCallback: (Boolean) -> Unit,
    )

    fun showInter(activity: Activity): Boolean

    fun destroy()
}

internal class RealAdsManager(
    private val analyticsManager: AnalyticsManager,
    application: Application,
    key: String,
) : AdsManager {

    companion object {
        private const val ANALYTICS_ERROR_EVENT = "appodeal_initialization_error"
        private const val DEFAULT_PLACEMENT = "default"
    }

    private var rewardCallback: ((Boolean) -> Unit)? = null

    private val adTypes = Appodeal.INTERSTITIAL or Appodeal.REWARDED_VIDEO or Appodeal.BANNER_VIEW

    init {
        Appodeal.setTesting(testMode = BuildConfig.DEBUG)
        Appodeal.initialize(
            context = application,
            appKey = key,
            adTypes = adTypes,
            callback = object : ApdInitializationCallback {
                override fun onInitializationFinished(
                    errors: List<ApdInitializationError>?,
                ) {
                    if (!errors.isNullOrEmpty()) {
                        val map = mutableMapOf<String, Any?>()
                        errors.mapIndexed { index, error ->
                            map["Error$index"] = error.message
                        }
                        analyticsManager.logEvent(ANALYTICS_ERROR_EVENT, map)
                    }
                }
            }
        )

        setBannerCallbacks()
        setInterstitialCallbacks()
        setRewardedVideoCallbacks()
    }

    override fun showBanner(
        activity: Activity,
        container: FrameLayout,
    ): Boolean {
        val adView = Appodeal.getBannerView(container.context)
        container.addView(adView)
        container.setPadding(0, 16.dpToPx(), 0, 0)

        val isShown = Appodeal.show(activity, Appodeal.BANNER_VIEW)
        container.isVisible = isShown
        return isShown
    }

    override fun hideBanner(activity: Activity, container: FrameLayout) {
        container.isVisible = false
        Appodeal.hide(activity, Appodeal.BANNER_VIEW)
    }

    override fun showReward(
        activity: Activity,
        placement: String?,
        rewardCallback: (Boolean) -> Unit,
    ) {
        val placementName = placement ?: DEFAULT_PLACEMENT
        if (!Appodeal.canShow(Appodeal.REWARDED_VIDEO, placementName)) return
        val isShown = Appodeal.show(activity, Appodeal.REWARDED_VIDEO, placementName)
        if (isShown) {
            this.rewardCallback = rewardCallback
        }
    }

    override fun showInter(activity: Activity): Boolean {
        if (!Appodeal.isLoaded(Appodeal.INTERSTITIAL)) return false
        return Appodeal.show(activity, Appodeal.INTERSTITIAL)
    }

    override fun destroy() {
        Appodeal.destroy(adTypes)
    }

    private fun setInterstitialCallbacks() {
        Appodeal.setInterstitialCallbacks(object : InterstitialCallbacks {
            override fun onInterstitialLoaded(isPrecache: Boolean) {
            }

            override fun onInterstitialFailedToLoad() {
            }

            override fun onInterstitialShown() {
            }

            override fun onInterstitialShowFailed() {
            }

            override fun onInterstitialClicked() {
            }

            override fun onInterstitialClosed() {
            }

            override fun onInterstitialExpired() {
            }

        })
    }

    private fun setBannerCallbacks() {
        Appodeal.setBannerCallbacks(object : BannerCallbacks {
            override fun onBannerLoaded(height: Int, isPrecache: Boolean) {
            }

            override fun onBannerFailedToLoad() {
            }

            override fun onBannerShown() {
            }

            override fun onBannerShowFailed() {
            }

            override fun onBannerClicked() {
            }

            override fun onBannerExpired() {
            }
        })
    }

    private fun setRewardedVideoCallbacks() {
        Appodeal.setRewardedVideoCallbacks(object : RewardedVideoCallbacks {
            override fun onRewardedVideoLoaded(isPrecache: Boolean) {
            }

            override fun onRewardedVideoFailedToLoad() {
            }

            override fun onRewardedVideoShown() {
            }

            override fun onRewardedVideoShowFailed() {
            }

            override fun onRewardedVideoFinished(amount: Double, currency: String) {
            }

            override fun onRewardedVideoClosed(finished: Boolean) {
                rewardCallback?.invoke(finished)
                rewardCallback = null
            }

            override fun onRewardedVideoExpired() {
            }

            override fun onRewardedVideoClicked() {
            }
        })
    }
}
