package us.webmy.core_sdk.presentation.base.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import us.webmy.core_sdk.presentation.base.navigator.Navigation

open class BaseViewModel() : ViewModel() {

    private val _navigation = MutableSharedFlow<Navigation>()
    val navigation = _navigation.asSharedFlow()

    fun navigateTo(navigation: Navigation) {
        viewModelScope.launch {
            _navigation.emit(navigation)
        }
    }
}