package com.webmy.core_sdk.model

import android.app.Application


/**
 * @param appodealKey Appodeal API key
 * @param amplitudeKey Amplitude API key. By default SDK uses ServerZone.EU
 * @param koinMode See [KoinMode] to use proper value
 */
class Config private constructor(
    val application: Application,
    val appodealKey: String?,
    val amplitudeKey: String?,
    val koinMode: KoinMode,
) {
    class Builder(private val application: Application) {
        private var appodealKey: String? = null
        private var amplitudeKey: String? = null
        private var koinMode: KoinMode = KoinMode.START


        fun addAppodealKey(key: String) = apply {
            this.appodealKey = key
        }

        fun addAmplitudeKey(key: String) = apply {
            this.amplitudeKey = key
        }

        fun setKoinMode(mode: KoinMode) = apply {
            this.koinMode = mode
        }

        fun build(): Config {
            return Config(
                application = application,
                appodealKey = appodealKey,
                amplitudeKey = amplitudeKey,
                koinMode = koinMode
            )
        }
    }
}