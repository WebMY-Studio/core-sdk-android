package com.webmy.core_sdk.tools.ads

import android.app.Activity
import android.app.Application
import androidx.appcompat.app.AppCompatActivity
import com.appodeal.ads.Appodeal
import com.appodeal.ads.BannerCallbacks
import com.appodeal.ads.InterstitialCallbacks
import com.appodeal.ads.RewardedVideoCallbacks
import com.appodeal.ads.initializing.ApdInitializationCallback
import com.appodeal.ads.initializing.ApdInitializationError
import com.webmy.core_sdk.BuildConfig
import com.webmy.core_sdk.tools.analytics.AnalyticsManager

interface AdsManager {

    companion object {
        private const val DEFAULT_PLACEMENT = "default"
    }

    fun showReward(
        activity: Activity,
        placement: String = DEFAULT_PLACEMENT,
        rewardCallback: (Boolean) -> Unit,
    )

    fun showInter(activity: Activity): Boolean
}

internal class RealAdsManager(
    private val analyticsManager: AnalyticsManager,
    application: Application,
    key: String,
) : AdsManager {

    companion object {
        private const val ANALYTICS_ERROR_EVENT = "appodeal_initialization_error"
    }

    private var rewardCallback: ((Boolean) -> Unit)? = null

    init {
        Appodeal.setTesting(testMode = BuildConfig.DEBUG)
        Appodeal.initialize(
            context = application,
            appKey = key,
            adTypes = Appodeal.INTERSTITIAL or Appodeal.REWARDED_VIDEO or Appodeal.BANNER_VIEW,
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

    override fun showReward(
        activity: Activity,
        placement: String,
        rewardCallback: (Boolean) -> Unit,
    ) {
        if (!Appodeal.canShow(Appodeal.REWARDED_VIDEO, placement)) return

        val isShown = Appodeal.show(activity, Appodeal.REWARDED_VIDEO, placement)

        if (!isShown) return
        this.rewardCallback = rewardCallback
    }

    override fun showInter(activity: Activity): Boolean {
        if (!Appodeal.isLoaded(Appodeal.INTERSTITIAL)) return false
        return Appodeal.show(activity, Appodeal.INTERSTITIAL)
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
