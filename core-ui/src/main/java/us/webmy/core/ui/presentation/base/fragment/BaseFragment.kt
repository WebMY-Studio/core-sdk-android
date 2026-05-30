package us.webmy.core.ui.presentation.base.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import us.webmy.core.ui.presentation.base.navigator.Navigation
import us.webmy.core.ui.presentation.base.navigator.Navigator
import us.webmy.core.ui.presentation.base.viewmodel.BaseViewModel
import us.webmy.core.util.observe

/**
 * Base XML+ViewBinding screen Fragment for the single-activity SDK.
 *
 * Provides:
 * - lifecycle-safe ViewBinding via [inflate] lambda
 * - navigation collection from [viewModel] flow → [Navigator]
 * - Koin scope inheritance from the host Activity
 *
 * Subclasses provide [viewModel] (Koin-injected) and implement [initView] / [observe].
 *
 * Compose-based screens should use BaseComposeFragment instead.
 */
abstract class BaseFragment<VM : BaseViewModel, VB : ViewBinding>(
    private val inflate: (LayoutInflater, ViewGroup?, Boolean) -> VB,
) : androidx.fragment.app.Fragment() {

    private val navigator: Navigator by inject()

    protected abstract val viewModel: VM

    private var _binding: VB? = null
    protected val binding: VB get() = _binding ?: error("ViewBinding accessed outside view lifecycle")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val b = inflate(inflater, container, false)
        _binding = b
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.navigation.observe(viewLifecycleOwner, ::handleNavigation)
        initView()
        observe(viewModel)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    protected abstract fun initView()

    protected abstract fun observe(viewModel: VM)

    private fun handleNavigation(nav: Navigation) {
        navigator.go(nav)
    }
}
