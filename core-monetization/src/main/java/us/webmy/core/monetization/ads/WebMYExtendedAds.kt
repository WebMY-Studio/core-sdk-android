package us.webmy.core.monetization.ads

import us.webmy.core.WebMY
import org.koin.core.context.loadKoinModules
import us.webmy.core.monetization.ads.di.adsModule

fun WebMY.initAds(
    appodealKey: String,
    premiumProductIds: List<String> = emptyList()
) {
    loadKoinModules(adsModule(application, appodealKey, premiumProductIds))
}
