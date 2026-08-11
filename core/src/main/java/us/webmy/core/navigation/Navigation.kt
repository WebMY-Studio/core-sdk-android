package us.webmy.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey

typealias ComposeSheetContent = @Composable () -> Unit

sealed interface Navigation {

    /**
     * Push [key] onto the Compose back stack. The key is matched to a composable by the
     * entry provider declared in `WebmyActivity.screens()`.
     *
     * With [addToBackStack] `false` the current top is replaced instead of stacked on.
     */
    data class Screen(
        val key: NavKey,
        val addToBackStack: Boolean = true,
    ) : Navigation

    /** Clear the whole back stack and make [key] the only (root) entry. */
    data class Root(val key: NavKey) : Navigation

    /** Pop the top entry. No-op at the root — system back finishes the Activity there. */
    object Back : Navigation

    /** Pop back to [key]; with [inclusive] `true` [key] itself is popped too. */
    data class PopUpTo(val key: NavKey, val inclusive: Boolean = false) : Navigation

    data class Email(
        val email: String,
        val subject: String? = null,
        val text: String? = null,
    ) : Navigation

    data class Browser(val url: String) : Navigation

    data class GooglePlay(val applicationId: String) : Navigation

    object RateApp : Navigation

    object Finish : Navigation

    data class Sheet(val content: ComposeSheetContent) : Navigation
    object DismissSheet : Navigation

    sealed interface Auth : Navigation {
        val onResult: ((Result<Unit>) -> Unit)?

        data class OneTime(
            override val onResult: ((Result<Unit>) -> Unit)? = null,
        ) : Auth

        data class Session(
            override val onResult: ((Result<Unit>) -> Unit)? = null,
        ) : Auth
    }
}

/**
 * One-liner for [Navigation.Screen]. Screen arguments live in the key itself:
 *
 * ```
 * data class DetailsKey(val id: String) : NavKey
 *
 * // push
 * router.go(screen(DetailsKey("42")))
 *
 * // read — the key is handed to the composable by the entry provider
 * entry<DetailsKey> { key -> DetailsScreen(key.id) }
 * ```
 */
fun screen(key: NavKey, addToBackStack: Boolean = true): Navigation.Screen =
    Navigation.Screen(key, addToBackStack)
