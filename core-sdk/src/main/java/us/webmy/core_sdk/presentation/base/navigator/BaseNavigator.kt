package us.webmy.core_sdk.presentation.base.navigator

import android.content.Intent
import android.os.Build
import android.os.Parcelable
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.play.core.review.ReviewManagerFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import us.webmy.core_sdk.tools.biometrics.domain.BiometricsServiceFactory
import us.webmy.core_sdk.util.coerceToUnit
import us.webmy.core_sdk.util.executeSuspend

interface Navigator {
    fun navigate(activity: AppCompatActivity, nav: Navigation)
}

abstract class BaseNavigator(
    private val biometricsServiceFactory: BiometricsServiceFactory
) : Navigator {

    private fun finish(activity: AppCompatActivity) {
        activity.finish()
    }

    protected abstract fun open(activity: AppCompatActivity, target: NavigationTarget)

    private fun openBrowser(activity: AppCompatActivity, url: String) {
        activity.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
    }

    private fun openEmailApp(
        activity: AppCompatActivity,
        email: String,
        subject: String?,
        text: String?
    ) {
        try {
            Intent(Intent.ACTION_SENDTO).apply {
                data = "mailto:$email".toUri()
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, text)
                activity.startActivity(this)
            }
        } catch (_: Exception) {
        }
    }

    private fun openGooglePlay(activity: AppCompatActivity, applicationId: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = "market://details?id=$applicationId".toUri()
                setPackage("com.android.vending")
            }
            activity.startActivity(intent)
        } catch (_: Exception) {
            // Fallback to browser if Play Store app is not available
            val url = "https://play.google.com/store/apps/details?id=$applicationId"
            openBrowser(activity, url)
        }
    }

    private fun showBottomSheet(activity: AppCompatActivity, dialog: BottomSheetDialogFragment) {
        dialog.show(activity.supportFragmentManager, dialog.javaClass.simpleName)
    }

    private fun authenticate(activity: AppCompatActivity, auth: Navigation.Auth) {
        CoroutineScope(Dispatchers.Main).launch {
            val biometricService = biometricsServiceFactory.create(activity)
            when (auth) {
                Navigation.Auth.OneTime -> biometricService.performOneTimeAuthentication()
                Navigation.Auth.Session -> biometricService.performSessionAuthentication()
            }
        }
    }

    private fun showRateApp(activity: AppCompatActivity) {
        CoroutineScope(Dispatchers.Main).launch {
            ReviewManagerFactory.create(activity)
                .requestReviewFlow()
                .executeSuspend()
                .coerceToUnit()
        }
    }

    override fun navigate(activity: AppCompatActivity, nav: Navigation) {
        when (nav) {
            is Navigation.Finish -> finish(activity)
            is Navigation.GooglePlay -> openGooglePlay(activity, nav.applicationId)
            is Navigation.Browser -> openBrowser(activity, nav.url)
            is Navigation.Email -> openEmailApp(activity, nav.email, nav.subject, nav.text)
            is Navigation.BottomSheet -> showBottomSheet(activity, nav.dialog)
            is Navigation.Screen -> open(activity, nav.target)
            is Navigation.Auth -> authenticate(activity, nav)
            is Navigation.RateApp -> showRateApp(activity)
            is Navigation.Purchase -> Unit
            is Navigation.Ad -> Unit
        }
    }
}

fun <T : Parcelable> Intent.withPayload(payload: T): Intent {
    return apply {
        putExtra(payload::class.java.name, payload)
    }
}

inline fun <reified T : Parcelable> Intent.getPayload(): T {
    val clazz = T::class.java
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelableExtra(clazz.name, clazz)!!
    } else {
        @Suppress("DEPRECATION")
        (getParcelableExtra(clazz.name) as T?)!!
    }
}

inline fun <reified VM : ViewModel, reified T : Parcelable> ComponentActivity.viewModelWithPayload() =
    viewModel<VM> { parametersOf(intent.getPayload<T>()) }