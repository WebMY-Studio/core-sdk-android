package us.webmy.core.ui.presentation.base.navigator

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

/**
 * Routes [Navigation] events to side-effects (Compose back stack mutations, system
 * intents, BiometricPrompt, ...).
 *
 * Called synchronously from ViewModels — order of [go] calls is preserved.
 */
interface Router {
    fun go(nav: Navigation): Result<Unit>

    /**
     * The Compose back stack rendered by `WebmyActivity`'s `NavDisplay`. Owned by the
     * Router (a Koin singleton), so it survives configuration changes.
     */
    val backStack: NavBackStack<NavKey>
}
