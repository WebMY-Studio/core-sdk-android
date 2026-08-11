package us.webmy.core.monetization.ads

import us.webmy.core.WebMY
import us.webmy.core.internal.di.ServiceRegistry
import us.webmy.core.monetization.ads.DisplayAdUseCase
import us.webmy.core.monetization.ads.InterstitialThrottleConfig
import us.webmy.core.monetization.ads.internal.registerAdsServices
import us.webmy.core.monetization.ads.AdsManager
import us.webmy.core.monetization.billing.PremiumUseCase

private const val ADS_NOT_INITIALIZED =
    "WebMY: Ads are not initialized. Call WebMY.initAds(...) first."

val WebMY.ads: AdsManager
    get() = ServiceRegistry.resolve(missingMessage = ADS_NOT_INITIALIZED)

val WebMY.displayAd: DisplayAdUseCase
    get() = ServiceRegistry.resolve(missingMessage = ADS_NOT_INITIALIZED)

fun WebMY.initAds(
    appodealKey: String,
    throttleConfigProvider: suspend () -> InterstitialThrottleConfig = { InterstitialThrottleConfig.Default },
) {
    check(ServiceRegistry.isRegistered(PremiumUseCase::class)) {
        "WebMY.initAds requires billing. Call WebMY.initBilling(...) before WebMY.initAds(...)."
    }
    registerAdsServices(application, appodealKey, throttleConfigProvider)
}
