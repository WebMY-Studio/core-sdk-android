package us.webmy.core.ui.presentation.base.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import us.webmy.core.ui.presentation.base.viewmodel.BaseViewModel

/**
 * Base XML+ViewBinding screen Fragment for the single-activity SDK.
 *
 * Provides lifecycle-safe ViewBinding via [inflate] lambda. Navigation is dispatched
 * synchronously from the ViewModel via injected [Router][us.webmy.core.ui.presentation.base.navigator.Router].
 *
 * Subclasses provide [viewModel] (Koin-injected) and implement [initView] / [observe].
 */
abstract class BaseFragment<VM : BaseViewModel, VB : ViewBinding>(
    private val inflate: (LayoutInflater, ViewGroup?, Boolean) -> VB,
) : androidx.fragment.app.Fragment() {

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
        initView()
        observe(viewModel)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    protected abstract fun initView()

    protected abstract fun observe(viewModel: VM)
}
