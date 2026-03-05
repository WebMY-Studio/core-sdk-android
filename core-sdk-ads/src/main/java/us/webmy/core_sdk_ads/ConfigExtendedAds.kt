package us.webmy.core_sdk_ads


import android.app.Application
import com.webmy.core_sdk.KoinMode
import us.webmy.core_sdk_extended.ConfigExtended

open class ConfigExtendedAds(
    application: Application,
    koinMode: KoinMode,
    amplitudeKey: String?,
    remoteConfigEnabled: Boolean,
    remoteConfigUpdateInterval: Long,
    adaptyKey: String?,
    oneTimeProductIds: List<String>,
    subscriptionProductIds: List<String>,
    val appodealKey: String?,
    val premiumProductIds: List<String>,
) : ConfigExtended(
    application,
    koinMode,
    amplitudeKey,
    remoteConfigEnabled,
    remoteConfigUpdateInterval,
    adaptyKey,
    oneTimeProductIds,
    subscriptionProductIds
) {
    open class Builder(private val application: Application) : ConfigExtended.Builder(application) {

        private var appodealKey: String? = null
        private var premiumProductIds: List<String> = emptyList()

        /**
         * Appodeal SDK integration guide:
         *
         * To use the Appodeal SDK, in addition to providing your Appodeal App Key,
         * you must add the following configuration to your **build.gradle** or **build.gradle.kts** file:
         *
         * Option 1 — Kotlin DSL:
         * ```
         * manifestPlaceholders["ADMOB_APPLICATION_ID"] = localProperties.readSecret("ADMOB_APPLICATION_ID")
         * ```
         *
         * Option 2 — Groovy DSL:
         * ```
         * manifestPlaceholders = [ADMOB_APPLICATION_ID: readRawSecret("ADMOB_APPLICATION_ID")]
         * ```
         *
         * Then, add your `ADMOB_APPLICATION_ID` value to the **local.properties** file:
         * ```
         * ADMOB_APPLICATION_ID=ca-app-pub-XXXXXXXX~YYYYYYYY
         * ```
         */
        fun enableAds(
            appodealKey: String,
            premiumProductIds: List<String> = emptyList()
        ): Builder = apply {
            this.appodealKey = appodealKey
            this.premiumProductIds = premiumProductIds
        }

        override fun build(): ConfigExtendedAds {
            return ConfigExtendedAds(
                application = application,
                koinMode = koinMode,
                amplitudeKey = amplitudeKey,
                remoteConfigEnabled = remoteConfigEnabled,
                remoteConfigUpdateInterval = remoteConfigUpdateInterval,
                adaptyKey = adaptyKey,
                subscriptionProductIds = subscriptionProductIds,
                oneTimeProductIds = oneTimeProducts,
                appodealKey = appodealKey,
                premiumProductIds = premiumProductIds
            )
        }
    }
}