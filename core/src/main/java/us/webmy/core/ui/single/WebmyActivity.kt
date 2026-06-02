package us.webmy.core.ui.single

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.view.ActionProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import org.koin.android.ext.android.inject
import us.webmy.core.R
import us.webmy.core.ui.compose.theme.AppTheme
import us.webmy.core.ui.presentation.base.navigator.Router
import us.webmy.core.util.ActivityProvider

/**
 * Single-activity host for SDK-based apps. Hosts a single fragment container plus a
 * Compose overlay for bottom sheets driven by [SheetController].
 *
 * Subclass and override [createStartFragment] — that fragment is shown on first launch.
 * All subsequent navigation goes through [Router] (XML via `BaseFragment`, Compose via
 * `BaseComposeFragment`).
 *
 * Example:
 * ```
 * class MainActivity : WebmyActivity() {
 *     override fun createStartFragment() = HomeFragment()
 * }
 * ```
 */
abstract class WebmyActivity : FragmentActivity(R.layout.webmy_activity) {

    private val router: Router by inject()
    private val sheetController: SheetController by inject()

    private val activityProvider: ActivityProvider by inject()

    /** Built once on first launch (skipped on configuration change / process restore). */
    protected abstract fun createStartFragment(): Fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        activityProvider.bindHost(this)
        router.bind(this, R.id.webmyContainer)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.webmyContainer, createStartFragment())
                .commit()
        }

        val overlay = findViewById<ComposeView>(R.id.webmyComposeOverlay)
        overlay.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        overlay.setContent {
            AppTheme {
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
