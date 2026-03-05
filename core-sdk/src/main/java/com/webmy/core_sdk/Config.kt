package com.webmy.core_sdk

import android.app.Application
import kotlin.time.Duration

open class Config(
    val application: Application,
    val koinMode: KoinMode,
    val amplitudeKey: String?,
    val remoteConfigEnabled: Boolean,
    val remoteConfigUpdateInterval: Long,
) {
    open class Builder(private val application: Application) {
        protected var koinMode: KoinMode = KoinMode.START

        protected var amplitudeKey: String? = null
        protected var remoteConfigEnabled: Boolean = false
        protected var remoteConfigUpdateInterval: Long = -1

        /**
         * @param mode See [KoinMode] to use proper value
         */
        fun setKoinMode(mode: KoinMode): Builder = apply {
            this.koinMode = mode
        }


        /**
         * @param amplitudeKey Amplitude API key. By default SDK uses ServerZone.EU
         */
        fun enableAnalytics(amplitudeKey: String): Builder = apply {
            this.amplitudeKey = amplitudeKey
        }

        /**
         * Firebase integration guide:
         *
         * To enable Firebase services in your app, follow these steps:
         *
         * 1. Add the `google-services.json` file to your **app module**.
         *
         * 2. In your app module’s `build.gradle.kts`, apply the plugins:
         *    ```
         *    alias(libs.plugins.google.services)
         *    alias(libs.plugins.firebase.crashlytics)
         *    ```
         *
         * 3. In your **root** `build.gradle.kts`, declare the same plugins with `apply false`:
         *    ```
         *    alias(libs.plugins.google.services) apply false
         *    alias(libs.plugins.firebase.crashlytics) apply false
         *    ```
         */
        fun enableRemoteConfig(updateInterval: Duration = Duration.ZERO): Builder = apply {
            this.remoteConfigEnabled = true
            this.remoteConfigUpdateInterval = updateInterval.inWholeMilliseconds
        }

        open fun build(): Config {
            return Config(
                application = application,
                koinMode = koinMode,
                amplitudeKey = amplitudeKey,
                remoteConfigEnabled = remoteConfigEnabled,
                remoteConfigUpdateInterval = remoteConfigUpdateInterval,
            )
        }
    }
}