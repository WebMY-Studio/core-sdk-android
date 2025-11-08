package com.webmy.core_sdk.model

import android.app.Application
import com.webmy.core_sdk.WebMY

class Config private constructor(
    val application: Application,
    val koinMode: KoinMode,
    val appodealKey: String?,
    val amplitudeKey: String?,
    val useRemoteConfig: Boolean,
) {
    class Builder(private val application: Application) {

        private var koinMode: KoinMode = KoinMode.START
        private var appodealKey: String? = null
        private var amplitudeKey: String? = null
        private var useRemoteConfig: Boolean = false

        /**
         * @param mode See [KoinMode] to use proper value
         */
        fun setKoinMode(mode: KoinMode) = apply {
            this.koinMode = mode
        }

        /**
         * To use Appodeal SDK apart of Appodeal App Key you have to
         * add following code to your build.gradle file
         * manifestPlaceholders["ADMOB_APPLICATION_ID"] = localProperties.readSecret("ADMOB_APPLICATION_ID")
         * and then you have to add ADMOB_APPLICATION_ID into your local.properties file
         */
        fun addAppodealKey(key: String) = apply {
            this.appodealKey = key
        }

        /**
         * @param amplitudeKey Amplitude API key. By default SDK uses ServerZone.EU
         */
        fun addAmplitudeKey(key: String) = apply {
            this.amplitudeKey = key
        }

        /**
         * Read [WebMY] doc before enable this flag
         */
        fun useFirebaseRemoteConfig() = apply {
            this.useRemoteConfig = true
        }

        fun build(): Config {
            return Config(
                application = application,
                koinMode = koinMode,
                appodealKey = appodealKey,
                amplitudeKey = amplitudeKey,
                useRemoteConfig = useRemoteConfig
            )
        }
    }
}