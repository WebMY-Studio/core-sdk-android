package com.webmy.core_sdk.tools.ads

import android.app.Application
import com.appodeal.ads.Appodeal
import com.appodeal.ads.initializing.ApdInitializationCallback
import com.appodeal.ads.initializing.ApdInitializationError
import com.webmy.core_sdk.BuildConfig
import com.webmy.core_sdk.tools.analytics.AnalyticsManager

interface AdsManager {

}

internal class RealAdsManager(
    private val analyticsManager: AnalyticsManager,
    application: Application,
    key: String,
) : AdsManager {

    companion object {
        private const val ANALYTICS_ERROR_EVENT = "appodeal_initialization_error"
    }

    private var currentSessionActionCount = 0L
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
    }
}
