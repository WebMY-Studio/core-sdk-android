package us.webmy.core.ui.presentation.base.navigator

import androidx.fragment.app.FragmentActivity

/**
 * Routes [Navigation] events to side-effects (FragmentManager transactions, system
 * intents, BillingClient, Appodeal, BiometricPrompt, ...).
 *
 * Fire-and-forget — no `scope.launch` required on the caller side. Async results
 * (auth, reward) are returned via callback fields on the relevant `Navigation` cases.
 */
interface Navigator {
    /** Fire-and-forget. Runs on the navigator's internal Main scope. */
    fun go(nav: Navigation): Result<Unit>

    /** Bind a FragmentActivity host + container id for fragment-based screen routing. */
    fun bind(activity: FragmentActivity, containerId: Int)

    /** Unbind the previously bound Activity. */
    fun unbind()
}
