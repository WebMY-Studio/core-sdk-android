package us.webmy.core.ui.presentation.base.navigator

import android.os.Bundle
import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment

typealias ComposeSheetContent = @Composable () -> Unit

sealed interface Navigation {

    /**
     * Open a fragment in the host activity's container.
     * Use the `screen<F>(...)` helper for a one-liner.
     */
    data class Screen(
        val fragmentClass: Class<out Fragment>,
        val args: Bundle? = null,
        val addToBackStack: Boolean = true,
    ) : Navigation {
        companion object {
            const val PAYLOAD_KEY: String = "webmy_payload"
        }
    }

    object Back : Navigation
    data class PopUpTo(val tag: String, val inclusive: Boolean = false) : Navigation

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
 * One-liner: build [Navigation.Screen] targeting [F] with an optional Parcelable
 * payload. Read in the destination fragment via `requireArgs<T>()`.
 *
 * ```
 * navigator.go(screen<SettingsFragment>(SettingsArgs("42", "Hi")))
 * ```
 */
inline fun <reified F : Fragment> screen(
    payload: Parcelable? = null,
    addToBackStack: Boolean = true,
): Navigation.Screen {
    val args = payload?.let { bundleOf(Navigation.Screen.PAYLOAD_KEY to it) }
    return Navigation.Screen(F::class.java, args, addToBackStack)
}
