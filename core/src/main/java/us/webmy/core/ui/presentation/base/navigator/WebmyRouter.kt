package us.webmy.core.ui.presentation.base.navigator

import android.content.Intent
import androidx.core.net.toUri
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.google.android.play.core.review.ReviewManagerFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import us.webmy.core.error.SdkError
import us.webmy.core.tools.biometrics.domain.BiometricsService
import us.webmy.core.ui.single.SheetController
import us.webmy.core.util.ActivityProvider
import us.webmy.core.util.coerceToUnit

/**
 * Fragment-based [Router]. Holds a [FragmentActivity] reference and a container id;
 * `Navigation.Screen` is dispatched via `supportFragmentManager.beginTransaction`.
 *
 * Compose bottom sheets are routed through [sheetController] (rendered by a ComposeView
 * overlay in `WebmyActivity`).
 */
class WebmyRouter(
    private val activityProvider: ActivityProvider,
    private val biometricsService: BiometricsService,
    val sheetController: SheetController,
) : Router {

    private var activityRef: FragmentActivity? = null
    private var containerId: Int = 0

    private val asyncScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun bind(activity: FragmentActivity, containerId: Int) {
        this.activityRef = activity
        this.containerId = containerId
    }

    private fun requireActivity(): FragmentActivity =
        activityRef ?: throw SdkError.BindingMissing("Router not bound to FragmentActivity")

    override fun go(nav: Navigation): Result<Unit> = when (nav) {
        is Navigation.Screen -> openScreen(nav)
        is Navigation.Back -> back()
        is Navigation.PopUpTo -> popUpTo(nav.tag, nav.inclusive)
        is Navigation.Browser -> openBrowser(nav.url)
        is Navigation.Email -> openEmail(nav)
        is Navigation.GooglePlay -> openGooglePlay(nav.applicationId)
        is Navigation.RateApp -> requestReview()
        is Navigation.Finish -> finish()
        is Navigation.Sheet -> sheet(nav.content)
        is Navigation.DismissSheet -> dismissSheet()
        is Navigation.Auth -> authenticate(nav)
    }

    private fun dismissSheet() = runCatching { sheetController.dismiss() }

    private fun back() = runCatching {
        requireActivity().supportFragmentManager.popBackStack()
    }

    private fun popUpTo(tag: String, inclusive: Boolean) = runCatching {
        val index = if (inclusive) {
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        } else {
            0
        }
        requireActivity().supportFragmentManager.popBackStack(tag, index)
    }

    private fun sheet(content: ComposeSheetContent) = runCatching { sheetController.show(content) }
    private fun finish() = runCatching {
        activityProvider.requireCurrent().finish()
    }

    private fun openScreen(nav: Navigation.Screen): Result<Unit> = runCatching {
        val activity = requireActivity()
        val fm = activity.supportFragmentManager
        val fragment = fm.fragmentFactory.instantiate(activity.classLoader, nav.fragmentClass.name)
        if (nav.args != null) fragment.arguments = nav.args
        val tag = nav.fragmentClass.name
        fm.beginTransaction()
            .replace(containerId, fragment, tag)
            .apply { if (nav.addToBackStack) addToBackStack(tag) }
            .commit()
    }

    private fun openBrowser(url: String): Result<Unit> = runCatching {
        activityProvider.requireCurrent().startActivity(
            Intent(Intent.ACTION_VIEW, url.toUri())
        )
    }

    private fun openEmail(nav: Navigation.Email): Result<Unit> = runCatching {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = "mailto:${nav.email}".toUri()
            putExtra(Intent.EXTRA_SUBJECT, nav.subject)
            putExtra(Intent.EXTRA_TEXT, nav.text)
        }
        activityProvider.requireCurrent().startActivity(intent)
    }

    private fun openGooglePlay(applicationId: String): Result<Unit> {
        val activity = activityProvider.requireCurrent()
        return runCatching {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = "market://details?id=$applicationId".toUri()
                setPackage("com.android.vending")
            }
            activity.startActivity(intent)
        }.recoverCatching {
            activity.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    "https://play.google.com/store/apps/details?id=$applicationId".toUri()
                )
            )
        }
    }

    private fun requestReview(): Result<Unit> = runCatching {
        val activity = requireActivity()
        val manager = ReviewManagerFactory.create(activity)
        manager.requestReviewFlow().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                manager.launchReviewFlow(activity, task.result)
            }
        }
    }

    private fun authenticate(auth: Navigation.Auth): Result<Unit> {
        asyncScope.launch {
            val result = when (auth) {
                is Navigation.Auth.OneTime -> biometricsService.performOneTimeAuthentication()
                is Navigation.Auth.Session -> biometricsService.performSessionAuthentication()
            }.coerceToUnit()
            auth.onResult?.invoke(result)
        }
        return Result.success(Unit)
    }
}
