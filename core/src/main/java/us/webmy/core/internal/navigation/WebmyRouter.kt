package us.webmy.core.internal.navigation

import us.webmy.core.navigation.ComposeSheetContent
import us.webmy.core.navigation.Navigation
import us.webmy.core.navigation.Router
import android.content.Intent
import androidx.core.net.toUri
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.google.android.play.core.review.ReviewManagerFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import us.webmy.core.biometrics.BiometricsService
import us.webmy.core.internal.navigation.SheetController
import us.webmy.core.util.ActivityProvider
import us.webmy.core.util.coerceToUnit

/**
 * Compose-only [Router]. `Navigation.Screen` mutates [backStack], which `WebmyActivity`
 * renders through a Navigation 3 `NavDisplay` — no fragments, no XML.
 *
 * Compose bottom sheets are routed through [sheetController] (rendered by the same
 * `setContent` tree in `WebmyActivity`).
 */
internal class WebmyRouter(
    private val activityProvider: ActivityProvider,
    private val biometricsService: BiometricsService,
    val sheetController: SheetController,
) : Router {

    override val backStack: NavBackStack<NavKey> = NavBackStack()

    private val asyncScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun go(nav: Navigation): Result<Unit> = when (nav) {
        is Navigation.Screen -> openScreen(nav)
        is Navigation.Root -> root(nav.key)
        is Navigation.Back -> back()
        is Navigation.PopUpTo -> popUpTo(nav.key, nav.inclusive)
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

    /**
     * No-op at the root: `NavDisplay` requires a non-empty stack, and back is left to the
     * system there (which finishes the Activity).
     */
    private fun back() = runCatching {
        if (backStack.size > 1) backStack.removeLastOrNull()
        Unit
    }

    private fun popUpTo(key: NavKey, inclusive: Boolean) = runCatching {
        val index = backStack.lastIndexOf(key)
        if (index >= 0) {
            val keepCount = (if (inclusive) index else index + 1).coerceAtLeast(1)
            while (backStack.size > keepCount) {
                backStack.removeLastOrNull()
            }
        }
    }

    private fun sheet(content: ComposeSheetContent) = runCatching { sheetController.show(content) }

    private fun finish() = runCatching {
        activityProvider.requireCurrent().finish()
    }

    private fun openScreen(nav: Navigation.Screen): Result<Unit> = runCatching {
        if (!nav.addToBackStack) backStack.removeLastOrNull()
        backStack.add(nav.key)
    }

    private fun root(key: NavKey): Result<Unit> = runCatching {
        backStack.clear()
        backStack.add(key)
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
        val activity = activityProvider.requireHost()
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
