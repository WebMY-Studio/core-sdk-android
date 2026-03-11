package us.webmy.core_sdk.presentation.base.viewmodel

import android.content.Intent
import android.os.Build
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

    protected inline fun <reified T> Intent.getPayload(): T {
        val clazz = T::class.java
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getParcelableExtra(clazz.name, clazz)!!
        } else {
            getParcelableExtra(clazz.name)!!
        }
    }
}