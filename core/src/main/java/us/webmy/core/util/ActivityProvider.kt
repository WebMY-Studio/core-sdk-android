package us.webmy.core.util

import android.app.Activity
import us.webmy.core.presentation.WebmyActivity

/**
 * Tracks the currently foreground Activity. Used by managers that need an Activity
 * (BillingClient, Appodeal, BiometricPrompt, Share intents) without taking Activity
 * as a parameter on every call.
 */
interface ActivityProvider {
    val current: Activity?
    fun requireCurrent(): Activity
    fun requireHost(): WebmyActivity
    fun bindHost(activity: WebmyActivity)
}
