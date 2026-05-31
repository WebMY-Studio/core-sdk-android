package us.webmy.coresdkdemo

import org.koin.androidx.viewmodel.ext.android.viewModel
import us.webmy.coresdkdemo.databinding.FragmentSettingsBinding
import us.webmy.core.ui.presentation.base.fragment.BaseFragment
import us.webmy.core.ui.presentation.base.fragment.requireArgs
import us.webmy.core.ui.presentation.base.navigator.Navigation
import us.webmy.core.ui.presentation.base.viewmodel.BaseViewModel

class SettingsViewModel : BaseViewModel() {
    fun onBackClick() = navigateTo(Navigation.Back)
}

class SettingsFragment : BaseFragment<SettingsViewModel, FragmentSettingsBinding>(
    inflate = { inflater, container, attach ->
        FragmentSettingsBinding.inflate(inflater, container, attach)
    },
) {
    override val viewModel: SettingsViewModel by viewModel()

    override fun initView() {
        val args: SettingsArgs = requireArgs()
        binding.tvTitle.text = "${args.title} (userId=${args.userId})"
        binding.btnBack.setOnClickListener { viewModel.onBackClick() }
    }

    override fun observe(viewModel: SettingsViewModel) = Unit
}
