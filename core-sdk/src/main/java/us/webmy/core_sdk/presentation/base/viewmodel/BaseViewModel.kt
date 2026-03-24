package us.webmy.core_sdk.presentation.base.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import us.webmy.core_sdk.presentation.base.navigator.Navigation
import us.webmy.core_sdk.presentation.base.navigator.NavigationProvider

open class BaseViewModel(
    private val navigationProvider: NavigationProvider
) : ViewModel() {

    val navigation: SharedFlow<Navigation>
        get() = navigationProvider.subscribeNavigation()

    protected fun navigateTo(navigation: Navigation) {
        viewModelScope.launch {
            navigationProvider.navigateTo(navigation)
        }
    }
}