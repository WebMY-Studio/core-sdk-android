package us.webmy.core.ui.presentation.base.navigator

import androidx.fragment.app.FragmentActivity

/**
 * Routes [Navigation] events to side-effects (FragmentManager transactions, system
 * intents, BiometricPrompt, ...).
 *
 * Called synchronously from ViewModels — order of [go] calls is preserved.
 */
interface Router {
    fun go(nav: Navigation): Result<Unit>

    /** Bind a FragmentActivity host + container id for fragment-based screen routing. */
    fun bind(activity: FragmentActivity, containerId: Int)
}
