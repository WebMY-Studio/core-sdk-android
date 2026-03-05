package us.webmy.core_sdk_extended


import android.app.Application
import com.webmy.core_sdk.Config
import com.webmy.core_sdk.KoinMode

open class ConfigExtended(
    application: Application,
    koinMode: KoinMode,
    amplitudeKey: String?,
    remoteConfigEnabled: Boolean,
    remoteConfigUpdateInterval: Long,
    val adaptyKey: String?,
    val oneTimeProductIds: List<String>,
    val subscriptionProductIds: List<String>,
) : Config(
    application, koinMode, amplitudeKey, remoteConfigEnabled, remoteConfigUpdateInterval
) {
    open class Builder(private val application: Application) : Config.Builder(application) {

        protected var adaptyKey: String? = null
        protected var oneTimeProducts: List<String> = emptyList()
        protected var subscriptionProductIds: List<String> = emptyList()

        fun enableBilling(oneTime: List<String>, subscription: List<String>): Builder = apply {
            this.oneTimeProducts = oneTime
            this.subscriptionProductIds = subscription
        }

        fun enableAdapty(key: String): Builder = apply {
            this.adaptyKey = key
        }

        override fun build(): ConfigExtended {
            return ConfigExtended(
                application = application,
                koinMode = koinMode,
                amplitudeKey = amplitudeKey,
                remoteConfigEnabled = remoteConfigEnabled,
                remoteConfigUpdateInterval = remoteConfigUpdateInterval,
                adaptyKey = adaptyKey,
                subscriptionProductIds = subscriptionProductIds,
                oneTimeProductIds = oneTimeProducts
            )
        }
    }
}