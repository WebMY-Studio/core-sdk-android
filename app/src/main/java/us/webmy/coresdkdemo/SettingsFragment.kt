package us.webmy.coresdkdemo

import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import us.webmy.coresdkdemo.databinding.FragmentSettingsBinding
import us.webmy.core.ui.presentation.base.fragment.BaseFragment
import us.webmy.core.ui.presentation.base.fragment.requireArgs
import us.webmy.core.ui.presentation.base.navigator.Navigation
import us.webmy.core.ui.presentation.base.viewmodel.BaseViewModel

class SettingsViewModel(val args: SettingsArgs) : BaseViewModel() {
    fun onBackClick() = navigateTo(Navigation.Back)
}

class SettingsFragment : BaseFragment<SettingsViewModel, FragmentSettingsBinding>(
    inflate = { inflater, container, attach ->
        FragmentSettingsBinding.inflate(inflater, container, attach)
    },
) {
    override val viewModel: SettingsViewModel by viewModel {
        parametersOf(requireArgs<SettingsArgs>())
    }

    override fun initView() {
        val args = viewModel.args
        binding.tvTitle.text = "${args.title} (userId=${args.userId})"
        binding.btnBack.setOnClickListener { viewModel.onBackClick() }
    }

    override fun observe(viewModel: SettingsViewModel) = Unit
}
