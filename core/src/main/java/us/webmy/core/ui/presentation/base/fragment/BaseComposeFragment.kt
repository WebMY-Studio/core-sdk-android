package us.webmy.core.ui.presentation.base.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import us.webmy.core.ui.compose.theme.AppTheme

/**
 * Base Compose screen Fragment for the single-activity SDK.
 *
 * Subclasses implement [ScreenContent] composable. Navigation is dispatched
 * synchronously from ViewModels via injected [Router][us.webmy.core.ui.presentation.base.navigator.Router].
 */
abstract class BaseComposeFragment : androidx.fragment.app.Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AppTheme { ScreenContent() }
            }
        }
    }

    @Composable
    protected abstract fun ScreenContent()
}
