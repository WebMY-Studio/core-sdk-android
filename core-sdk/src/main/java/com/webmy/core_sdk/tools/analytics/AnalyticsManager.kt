package com.webmy.core_sdk.tools.analytics

import android.os.Bundle
import com.amplitude.android.Amplitude
import com.google.firebase.analytics.FirebaseAnalytics

interface AnalyticsManager {
    fun logEvent(eventName: String, props: Map<String, Any?>? = null)

    fun logFirebase(eventName: String, bundle: Bundle = Bundle())
}

internal class RealAnalyticsManager(
    private val amplitude: Amplitude?,
    private val firebase: FirebaseAnalytics?
) : AnalyticsManager {

    override fun logEvent(eventName: String, props: Map<String, Any?>?) {
        amplitude?.track(eventName, props)
    }

    override fun logFirebase(eventName: String, bundle: Bundle) {
        firebase?.logEvent(eventName, bundle)
    }
}