package us.webmy.core.analytics

import android.os.Bundle

interface AnalyticsManager {
    fun logEvent(eventName: String, props: Map<String, Any?>? = null)

    fun logFirebase(eventName: String, bundle: Bundle = Bundle())
}
