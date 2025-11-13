package com.webmy.core_sdk.tools.analytics

import com.amplitude.android.Amplitude
import com.appodeal.ads.ext.toBundle
import com.google.firebase.analytics.FirebaseAnalytics

interface AnalyticsManager {
    fun logEvent(eventName: String, props: Map<String, Any?>? = null)
}

internal class RealAnalyticsManager(
    private val amplitude: Amplitude?,
    private val firebase: FirebaseAnalytics?
) : AnalyticsManager {

    override fun logEvent(eventName: String, props: Map<String, Any?>?) {
        amplitude?.track(eventName, props)
        firebase?.logEvent(eventName, props?.toBundle())
    }
}