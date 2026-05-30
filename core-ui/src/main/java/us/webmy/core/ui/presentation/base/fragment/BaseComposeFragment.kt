package us.webmy.core.ui.presentation.base.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import us.webmy.core.ui.compose.theme.AppTheme
import us.webmy.core.ui.presentation.base.navigator.Navigation
import us.webmy.core.ui.presentation.base.navigator.Navigator
import us.webmy.core.ui.presentation.base.viewmodel.BaseViewModel
import us.webmy.core.util.observe

/**
 * Base Compose screen Fragment for the single-activity SDK.
 *
 * Subclasses provide [viewModel] (or null) and implement [ScreenContent] composable.
 * Navigation collection from [viewModel] runs automatically when [viewModel] is non-null.
 */
abstract class BaseComposeFragment : androidx.fragment.app.Fragment() {

    private val navigator: Navigator by inject()

    /** Optional ViewModel. Override to hook navigation events. */
    protected open val viewModel: BaseViewModel? = null

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel?.navigation?.observe(viewLifecycleOwner, ::handleNavigation)
    }

    @Composable
    protected abstract fun ScreenContent()

    private fun handleNavigation(nav: Navigation) {
        navigator.go(nav)
    }
}
