package com.webmy.core_sdk.tools.analytics

import com.amplitude.android.Amplitude

interface AnalyticsManager {
    fun logEvent(eventName: String, props: Map<String, Any?>? = null)
}

internal class RealAnalyticsManager(private val amplitude: Amplitude) : AnalyticsManager {

    override fun logEvent(eventName: String, props: Map<String, Any?>?) {
        amplitude.track(eventName, props)
    }
}