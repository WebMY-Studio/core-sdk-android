package us.webmy.core_sdk.presentation.base.navigator

import android.widget.FrameLayout
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

sealed interface Navigation {

    data class Email(
        val email: String,
        val subject: String?,
        val text: String?
    ) : Navigation

    data class Browser(
        val url: String,
    ) : Navigation

    data class Screen(
        val target: NavigationTarget
    ) : Navigation

    object Finish : Navigation

    data class GooglePlay(
        val applicationId: String
    ) : Navigation

    object RateApp : Navigation

    data class BottomSheet(
        val dialog: BottomSheetDialogFragment
    ) : Navigation

    data class Purchase(
        val productId: String
    ) : Navigation

    sealed interface Ad : Navigation {
        data class Interstitial(
            val source: String? = null
        ) : Ad

        data class Reward(
            val placement: String? = null,
            val grantWhenPremium: Boolean = true,
            val source: String? = null,
            val rewardCallback: (Boolean) -> Unit,
        ) : Ad

        data class Banner(
            val container: FrameLayout
        ) : Ad
    }

    sealed interface Auth : Navigation {
        object OneTime : Auth
        object Session : Auth
    }
}