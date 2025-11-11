package com.webmy.core_sdk

import android.app.Application
import kotlin.time.Duration

class Config private constructor(
    val application: Application,
    val koinMode: KoinMode,
    val appodealKey: String?,
    val premiumProductId: String?,
    val amplitudeKey: String?,
    val remoteConfigEnabled: Boolean,
    val remoteConfigUpdateInterval: Long,
    val oneTimeProducts: List<String>,
    val firstSkipAdsAmountRemoteConfigKey: String?,
    val skipAdsAmountRemoteConfigKey: String?,
    val showDebugAds: Boolean,
) {
    class Builder(private val application: Application) {

        private var koinMode: KoinMode = KoinMode.START
        private var appodealKey: String? = null
        private var premiumProductId: String? = null
        private var amplitudeKey: String? = null
        private var remoteConfigEnabled: Boolean = false
        private var remoteConfigUpdateInterval: Long = -1
        private var oneTimeProducts: List<String> = emptyList()
        private var firstSkipAdsAmountRemoteConfigKey: String? = null
        private var skipAdsAmountRemoteConfigKey: String? = null
        private var showDebugAds: Boolean = false

        /**
         * @param mode See [KoinMode] to use proper value
         */
        fun setKoinMode(mode: KoinMode) = apply {
            this.koinMode = mode
        }

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
        fun enableAds(appodealKey: String, premiumProductId: String? = null) = apply {
            this.appodealKey = appodealKey
            this.premiumProductId = premiumProductId
        }

        /**
         * @param amplitudeKey Amplitude API key. By default SDK uses ServerZone.EU
         */
        fun enableAnalytics(amplitudeKey: String) = apply {
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
        fun enableRemoteConfig(
            updateInterval: Duration = Duration.ZERO,
            firstSkipAdsAmountKey: String? = null,
            skipAdsAmountKey: String? = null,
        ) = apply {
            this.remoteConfigEnabled = true
            this.remoteConfigUpdateInterval = updateInterval.inWholeMilliseconds
            this.firstSkipAdsAmountRemoteConfigKey = firstSkipAdsAmountKey
            this.skipAdsAmountRemoteConfigKey = skipAdsAmountKey
        }

        fun enableBilling(oneTimeProducts: List<String>) = apply {
            this.oneTimeProducts = oneTimeProducts
        }

        fun enableDebugAds() = apply {
            this.showDebugAds = true
        }

        fun build(): Config {
            return Config(
                application = application,
                koinMode = koinMode,
                appodealKey = appodealKey,
                premiumProductId = premiumProductId,
                amplitudeKey = amplitudeKey,
                remoteConfigEnabled = remoteConfigEnabled,
                remoteConfigUpdateInterval = remoteConfigUpdateInterval,
                oneTimeProducts = oneTimeProducts,
                firstSkipAdsAmountRemoteConfigKey = firstSkipAdsAmountRemoteConfigKey,
                skipAdsAmountRemoteConfigKey = skipAdsAmountRemoteConfigKey,
                showDebugAds = showDebugAds
            )
        }
    }
}