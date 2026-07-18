package us.webmy.core.monetization.ads.tools.ads

import android.app.Activity
import android.app.Application
import android.util.Log
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
import us.webmy.core.tools.analytics.AnalyticsManager
import us.webmy.core.util.ActivityProvider
import us.webmy.core.util.dpToPx
import us.webmy.core.util.isHostInDebugMode

/**
 * Wraps Appodeal SDK with analytics + logging on all callbacks.
 *
 * Activity is pulled from [ActivityProvider]; callers (composables, ViewModels,
 * [DisplayAdUseCase][us.webmy.core.monetization.ads.domain.DisplayAdUseCase]) do not pass it.
 */
interface AdsManager {

    fun init()

    fun showBanner(container: FrameLayout): Boolean

    fun hideBanner(container: FrameLayout)

    fun showReward(
        source: String? = null,
        placement: String? = null,
        rewardCallback: (Boolean) -> Unit,
    )

    fun showInter(source: String? = null)

    fun destroy()
}

internal class RealAdsManager(
    private val key: String,
    private val application: Application,
    private val activityProvider: ActivityProvider,
    private val analyticsManager: AnalyticsManager,
    private val firebaseAnalytics: FirebaseAnalytics,
) : AdsManager, AdRevenueCallbacks, ApdInitializationCallback {

    companion object {
        private const val DEFAULT_PLACEMENT = "default"
        private const val TAG = "AdsManager"
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
                Log.w(TAG, "init error #$index: ${error.message}")
            }
            analyticsManager.logEvent("ad_initialization_error", map)
        }
    }

    override fun showBanner(container: FrameLayout): Boolean {
        val activity = currentActivityOrNull() ?: return false
        val adView = Appodeal.getBannerView(container.context)
        container.addView(adView)
        container.setPadding(0, 0, 0, 16.dpToPx())

        val isShown = Appodeal.show(activity, Appodeal.BANNER_VIEW)
        container.isVisible = isShown
        return isShown
    }

    override fun hideBanner(container: FrameLayout) {
        container.isVisible = false
        val activity = currentActivityOrNull() ?: return
        Appodeal.hide(activity, Appodeal.BANNER_VIEW)
    }

    override fun showReward(
        source: String?,
        placement: String?,
        rewardCallback: (Boolean) -> Unit,
    ) {
        val activity = currentActivityOrNull()
        if (activity == null) {
            rewardCallback(false)
            return
        }
        val placementName = placement ?: DEFAULT_PLACEMENT

        val canShow = Appodeal.canShow(Appodeal.REWARDED_VIDEO, placementName)
        val isShown = if (canShow) {
            Appodeal.show(activity, Appodeal.REWARDED_VIDEO, placementName)
        } else {
            false
        }
        if (isShown) {
            this.rewardCallback = rewardCallback
        } else {
            rewardCallback(false)
        }

        sendAnalytics(
            canShow = canShow,
            isShown = isShown,
            adType = Appodeal.REWARDED_VIDEO,
            source = source
        )
    }

    override fun showInter(source: String?) {
        val activity = currentActivityOrNull() ?: return
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

    private fun currentActivityOrNull(): Activity? {
        val a = activityProvider.current
        if (a == null) Log.w(TAG, "no foreground Activity — ad call skipped")
        return a
    }

    private fun setInterstitialCallbacks() {
        Appodeal.setInterstitialCallbacks(object : InterstitialCallbacks {
            override fun onInterstitialLoaded(isPrecache: Boolean) {
                Log.d(TAG, "interstitial loaded (precache=$isPrecache)")
            }

            override fun onInterstitialFailedToLoad() {
                Log.w(TAG, "interstitial failed to load")
                analyticsManager.logEvent("ad_load_failed", mapOf("type" to "inter"))
            }

            override fun onInterstitialShown() {
                Log.d(TAG, "interstitial shown")
            }

            override fun onInterstitialShowFailed() {
                Log.w(TAG, "interstitial show failed")
                analyticsManager.logEvent("ad_show_failed", mapOf("type" to "inter"))
            }

            override fun onInterstitialClicked() {
                analyticsManager.logEvent("ad_clicked", mapOf("type" to "inter"))
            }

            override fun onInterstitialClosed() {
                Log.d(TAG, "interstitial closed")
            }

            override fun onInterstitialExpired() {
                Log.w(TAG, "interstitial expired")
            }
        })
    }

    private fun setBannerCallbacks() {
        Appodeal.setBannerCallbacks(object : BannerCallbacks {
            override fun onBannerLoaded(height: Int, isPrecache: Boolean) {
                Log.d(TAG, "banner loaded height=$height precache=$isPrecache")
            }

            override fun onBannerFailedToLoad() {
                Log.w(TAG, "banner failed to load")
                analyticsManager.logEvent("ad_load_failed", mapOf("type" to "banner"))
            }

            override fun onBannerShown() {
                Log.d(TAG, "banner shown")
            }

            override fun onBannerShowFailed() {
                Log.w(TAG, "banner show failed")
                analyticsManager.logEvent("ad_show_failed", mapOf("type" to "banner"))
            }

            override fun onBannerClicked() {
                analyticsManager.logEvent("ad_clicked", mapOf("type" to "banner"))
            }

            override fun onBannerExpired() {
                Log.w(TAG, "banner expired")
            }
        })
    }

    private fun setRewardedVideoCallbacks() {
        Appodeal.setRewardedVideoCallbacks(object : RewardedVideoCallbacks {
            override fun onRewardedVideoLoaded(isPrecache: Boolean) {
                Log.d(TAG, "reward loaded precache=$isPrecache")
            }

            override fun onRewardedVideoFailedToLoad() {
                Log.w(TAG, "reward failed to load")
                analyticsManager.logEvent("ad_load_failed", mapOf("type" to "reward"))
            }

            override fun onRewardedVideoShown() {
                Log.d(TAG, "reward shown")
            }

            override fun onRewardedVideoShowFailed() {
                Log.w(TAG, "reward show failed")
                analyticsManager.logEvent("ad_show_failed", mapOf("type" to "reward"))
            }

            override fun onRewardedVideoFinished(amount: Double, currency: String) {
                Log.d(TAG, "reward finished amount=$amount $currency")
                analyticsManager.logEvent(
                    "ad_reward_finished",
                    mapOf("amount" to amount, "currency" to currency)
                )
            }

            override fun onRewardedVideoClosed(finished: Boolean) {
                rewardCallback?.invoke(finished)
                rewardCallback = null
            }

            override fun onRewardedVideoExpired() {
                Log.w(TAG, "reward expired")
            }

            override fun onRewardedVideoClicked() {
                analyticsManager.logEvent("ad_clicked", mapOf("type" to "reward"))
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
