package us.webmy.core_sdk_ads

import com.webmy.core_sdk.WebMY
import org.koin.core.context.loadKoinModules
import us.webmy.core_sdk_ads.di.adsModule

fun WebMY.initAds(
    appodealKey: String,
    premiumProductIds: List<String> = emptyList()
) {
    loadKoinModules(adsModule(application, appodealKey, premiumProductIds))
}
