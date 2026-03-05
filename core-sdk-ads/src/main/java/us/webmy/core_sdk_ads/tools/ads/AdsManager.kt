package us.webmy.core_sdk_ads.tools.ads

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
import com.appodeal.ads.revenue.AdRevenueCallbacks
import com.appodeal.ads.revenue.RevenueInfo
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import com.webmy.core_sdk.tools.analytics.AnalyticsManager
import com.webmy.core_sdk.util.dpToPx
import com.webmy.core_sdk.util.isHostInDebugMode

/**
 * Use this entity if you want to handle ads by yourself
 *
 * Otherwise, use [AdsPremiumManager]
 */
interface AdsManager {

    fun init()

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
        source: String? = null,
        placement: String? = null,
        rewardCallback: (Boolean) -> Unit,
    )

    fun showInter(
        activity: Activity,
        source: String? = null,
    )

    fun destroy()
}

internal class RealAdsManager(
    private val key: String,
    private val application: Application,
    private val analyticsManager: AnalyticsManager,
    private val firebaseAnalytics: FirebaseAnalytics
) : AdsManager, AdRevenueCallbacks, ApdInitializationCallback {

    companion object {
        private const val DEFAULT_PLACEMENT = "default"
    }

    private var rewardCallback: ((Boolean) -> Unit)? = null

    private val adTypes = Appodeal.INTERSTITIAL or Appodeal.REWARDED_VIDEO or Appodeal.BANNER_VIEW

    override fun init() {
        Appodeal.setTesting(testMode = application.isHostInDebugMode())
        Appodeal.initialize(
            context = application,
            appKey = key,
            adTypes = adTypes,
            callback = this
        )
        Appodeal.setAdRevenueCallbacks(this)

        setBannerCallbacks()
        setInterstitialCallbacks()
        setRewardedVideoCallbacks()
    }

    override fun onAdRevenueReceive(revenueInfo: RevenueInfo) {
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.AD_IMPRESSION) {
            param(FirebaseAnalytics.Param.AD_PLATFORM, revenueInfo.platform)
            param(FirebaseAnalytics.Param.SOURCE, revenueInfo.networkName)
            param(FirebaseAnalytics.Param.AD_FORMAT, revenueInfo.adTypeString)
            param(FirebaseAnalytics.Param.AD_UNIT_NAME, revenueInfo.adUnitName)
            param(FirebaseAnalytics.Param.CURRENCY, revenueInfo.currency)
            param(FirebaseAnalytics.Param.VALUE, revenueInfo.revenue)
        }
    }

    override fun onInitializationFinished(errors: List<ApdInitializationError>?) {
        if (!errors.isNullOrEmpty()) {
            val map = mutableMapOf<String, Any?>()
            errors.mapIndexed { index, error ->
                map["error_$index"] = error.message
            }
            analyticsManager.logEvent("ad_initialization_error", map)
        }
    }

    override fun showBanner(
        activity: Activity,
        container: FrameLayout,
    ): Boolean {
        val adView = Appodeal.getBannerView(container.context)
        container.addView(adView)
        container.setPadding(0, 0, 0, 16.dpToPx())

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
        source: String?,
        placement: String?,
        rewardCallback: (Boolean) -> Unit,
    ) {
        val placementName = placement ?: DEFAULT_PLACEMENT

        val canShow = Appodeal.canShow(Appodeal.REWARDED_VIDEO, placementName)
        val isShown = if (canShow) {
            Appodeal.show(activity, Appodeal.REWARDED_VIDEO, placementName)
        } else {
            false
        }
        if (isShown) {
            this.rewardCallback = rewardCallback
        }

        sendAnalytics(
            canShow = canShow,
            isShown = isShown,
            adType = Appodeal.REWARDED_VIDEO,
            source = source
        )
    }

    override fun showInter(
        activity: Activity,
        source: String?
    ) {
        val canShow = Appodeal.canShow(Appodeal.INTERSTITIAL)
        val isShown = if (canShow) {
            Appodeal.show(activity, Appodeal.INTERSTITIAL)
        } else {
            false
        }

        sendAnalytics(
            canShow = canShow,
            isShown = isShown,
            adType = Appodeal.INTERSTITIAL,
            source = source
        )
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

    private fun sendAnalytics(canShow: Boolean, isShown: Boolean, adType: Int, source: String?) {
        val error = when {
            !canShow -> "cant_show"
            !isShown -> "not_shown"
            else -> null
        }

        val analyticsAdType = when (adType) {
            Appodeal.INTERSTITIAL -> "inter"
            Appodeal.REWARDED_VIDEO -> "reward"
            else -> null
        }

        val props = mutableMapOf(
            "placement" to source,
            "type" to analyticsAdType
        )
        if (error == null) {
            analyticsManager.logEvent("ad_shown", props)
        } else {
            props["error"] = error
            analyticsManager.logEvent("ad_error", props)
        }
    }
}
