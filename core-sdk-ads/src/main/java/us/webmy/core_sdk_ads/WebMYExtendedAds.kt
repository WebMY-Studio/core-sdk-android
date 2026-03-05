package us.webmy.core_sdk_ads

import androidx.annotation.CallSuper
import org.koin.core.context.loadKoinModules
import us.webmy.core_sdk_ads.di.sdkModuleExtendedAds
import us.webmy.core_sdk_extended.WebMYExtended

open class WebMYExtendedAds : WebMYExtended<ConfigExtendedAds>() {

    @CallSuper
    override fun init(config: ConfigExtendedAds) {
        super.init(config)

        loadKoinModules(sdkModuleExtendedAds(config))
    }
}