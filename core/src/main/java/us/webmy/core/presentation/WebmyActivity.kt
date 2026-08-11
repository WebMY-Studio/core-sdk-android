package us.webmy.core.presentation

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import us.webmy.core.WebMY
import us.webmy.core.internal.di.ServiceRegistry
import us.webmy.core.internal.navigation.SheetController
import us.webmy.core.theme.AppTheme
import us.webmy.core.navigation.Navigation
import us.webmy.core.navigation.Router
import us.webmy.core.util.ActivityProvider

/**
 * Single-activity host for SDK-based apps. Pure Compose: no XML layout, no fragments.
 * Screens are rendered by a Navigation 3 [NavDisplay] over the back stack owned by
 * [Router]; bottom sheets are stacked on top via [SheetController].
 *
 * It extends [FragmentActivity] (not `ComponentActivity`) purely because
 * `androidx.biometric`'s `BiometricPrompt` requires one — no fragment is ever added.
 *
 * Subclass and implement [startScreen] (the root key) and [screens] (key → composable).
 *
 * Example:
 * ```
 * data object HomeKey : NavKey
 * data class DetailsKey(val id: String) : NavKey
 *
 * class MainActivity : WebmyActivity() {
 *     override fun startScreen(): NavKey = HomeKey
 *
 *     override fun EntryProviderScope<NavKey>.screens() {
 *         entry<HomeKey> { HomeScreen() }
 *         entry<DetailsKey> { key -> DetailsScreen(key.id) }
 *     }
 * }
 * ```
 *
 * The back stack lives in the [Router] singleton, so it survives configuration changes.
 * It is not restored after process death — the app restarts at [startScreen].
 */
abstract class WebmyActivity : FragmentActivity() {

    private val router: Router get() = WebMY.router
    private val sheetController: SheetController by lazy { ServiceRegistry.resolve() }

    private val activityProvider: ActivityProvider get() = WebMY.activityProvider

    /** Root back stack entry, used on first launch (and after process death). */
    protected abstract fun startScreen(): NavKey

    /** Declare one `entry<Key> { ... }` per screen key this app can navigate to. */
    protected abstract fun EntryProviderScope<NavKey>.screens()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        activityProvider.bindHost(this)

        if (router.backStack.isEmpty()) {
            router.backStack.add(startScreen())
        }

        setContent {
            AppTheme {
                val entries = remember { entryProvider { screens() } }
                NavDisplay(
                    modifier = Modifier.fillMaxSize(),
                    backStack = router.backStack,
                    onBack = { router.go(Navigation.Back) },
                    entryProvider = entries,
                )
                SheetOverlay(sheetController)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SheetOverlay(controller: SheetController) {
    val current = controller.content ?: return
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        modifier = Modifier.fillMaxSize(),
        onDismissRequest = { controller.dismiss() },
        sheetState = sheetState,
    ) {
        current()
    }
}
