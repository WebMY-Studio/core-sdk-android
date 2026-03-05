package us.webmy.core_sdk_extended

import androidx.annotation.CallSuper
import com.adapty.Adapty
import com.adapty.models.AdaptyConfig
import com.facebook.appevents.AppEventsLogger
import com.webmy.core_sdk.WebMY
import org.koin.core.context.loadKoinModules
import us.webmy.core_sdk_extended.di.sdkModuleExtended


open class WebMYExtended<T : ConfigExtended> : WebMY<T>() {


    @CallSuper
    override fun init(config: T) {
        super.init(config)

        loadKoinModules(sdkModuleExtended(config))

        val adaptyKey = config.adaptyKey
        if (!adaptyKey.isNullOrEmpty()) {
            Adapty.activate(
                config.application,
                AdaptyConfig.Builder(adaptyKey).build()
            )
        }

        try {
            AppEventsLogger.activateApp(config.application)
        } catch (e: Exception) {

        }
    }
}