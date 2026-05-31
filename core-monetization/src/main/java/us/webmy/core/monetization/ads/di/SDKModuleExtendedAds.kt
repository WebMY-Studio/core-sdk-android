package us.webmy.core.monetization.ads.di

import android.app.Application
import org.koin.dsl.module
import us.webmy.core.monetization.ads.domain.DisplayAdUseCase
import us.webmy.core.monetization.ads.domain.InterstitialThrottleConfig
import us.webmy.core.monetization.ads.domain.RealDisplayAdUseCase
import us.webmy.core.monetization.ads.tools.ads.AdsManager
import us.webmy.core.monetization.ads.tools.ads.RealAdsManager

internal fun adsModule(
    application: Application,
    appodealKey: String,
    throttleConfigProvider: suspend () -> InterstitialThrottleConfig,
) = module {
    single<AdsManager> {
        RealAdsManager(
            key = appodealKey,
            application = application,
            activityProvider = get(),
            analyticsManager = get(),
            firebaseAnalytics = get(),
        )
    }

    single<DisplayAdUseCase> {
        RealDisplayAdUseCase(
            adsManager = get(),
            premiumUseCase = get(),
            throttleConfigProvider = throttleConfigProvider,
        )
    }
}
