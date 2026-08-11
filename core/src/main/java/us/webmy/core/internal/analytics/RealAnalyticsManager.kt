package us.webmy.core.internal.analytics

import android.os.Bundle
import com.amplitude.android.Amplitude
import com.google.firebase.analytics.FirebaseAnalytics
import us.webmy.core.analytics.AnalyticsManager

internal class RealAnalyticsManager(
    private val amplitude: Amplitude?,
    private val firebase: FirebaseAnalytics,
) : AnalyticsManager {

    override fun logEvent(eventName: String, props: Map<String, Any?>?) {
        amplitude?.track(eventName, props)
    }

    override fun logFirebase(eventName: String, bundle: Bundle) {
        firebase.logEvent(eventName, bundle)
    }
}
