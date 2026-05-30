package us.webmy.core.ui.presentation.base.fragment

import android.os.Build
import android.os.Parcelable
import androidx.fragment.app.Fragment
import us.webmy.core.ui.presentation.base.navigator.Navigation

/**
 * Reads the Parcelable payload passed via [Navigation.Screen.Screen] secondary constructor
 * (stashed under [Navigation.Screen.PAYLOAD_KEY]).
 *
 * Usage:
 * ```
 * @Parcelize data class DetailsArgs(val id: String) : Parcelable
 *
 * // push
 * navigator.navigate(Navigation.Screen(DetailsRoute, DetailsArgs("42")))
 *
 * // read
 * val args: DetailsArgs = requireArgs()
 * ```
 */
inline fun <reified T : Parcelable> Fragment.requireArgs(): T = args()
    ?: error("Required Parcelable arg of type ${T::class.java.simpleName} is missing in ${this::class.java.simpleName}")

/** Optional Parcelable payload (see [requireArgs]). */
inline fun <reified T : Parcelable> Fragment.args(): T? {
    val bundle = arguments ?: return null
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        bundle.getParcelable(Navigation.Screen.PAYLOAD_KEY, T::class.java)
    } else {
        @Suppress("DEPRECATION")
        bundle.getParcelable(Navigation.Screen.PAYLOAD_KEY) as? T
    }
}
