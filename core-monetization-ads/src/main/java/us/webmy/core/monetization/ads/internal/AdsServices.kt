package us.webmy.core.monetization.ads.internal

import android.app.Application
import us.webmy.core.internal.di.ServiceRegistry
import us.webmy.core.monetization.ads.DisplayAdUseCase
import us.webmy.core.monetization.ads.InterstitialThrottleConfig
import us.webmy.core.monetization.ads.internal.RealDisplayAdUseCase
import us.webmy.core.monetization.ads.AdsManager
import us.webmy.core.monetization.ads.internal.RealAdsManager

internal fun registerAdsServices(
    application: Application,
    appodealKey: String,
    throttleConfigProvider: suspend () -> InterstitialThrottleConfig,
) = with(ServiceRegistry) {
    register<AdsManager> {
        RealAdsManager(
            key = appodealKey,
            application = application,
            activityProvider = resolve(),
            analyticsManager = resolve(),
            firebaseAnalytics = resolve(),
        )
    }

    register<DisplayAdUseCase> {
        RealDisplayAdUseCase(
            adsManager = resolve(),
            premiumUseCase = resolve(),
            throttleConfigProvider = throttleConfigProvider,
        )
    }
}
