package us.webmy.core.monetization.ads

import org.koin.core.context.loadKoinModules
import us.webmy.core.WebMY
import us.webmy.core.monetization.ads.di.adsModule
import us.webmy.core.monetization.ads.domain.InterstitialThrottleConfig

fun WebMY.initAds(
    appodealKey: String,
    throttleConfigProvider: suspend () -> InterstitialThrottleConfig = { InterstitialThrottleConfig.Default },
) {
    loadKoinModules(adsModule(application, appodealKey, throttleConfigProvider))
}
