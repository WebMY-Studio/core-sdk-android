package us.webmy.coresdkdemo.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import us.webmy.coresdkdemo.SettingsViewModel

val appModule = module {
    viewModel { SettingsViewModel() }
}
