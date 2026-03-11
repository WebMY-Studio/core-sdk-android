package us.webmy.core_sdk.presentation.base.navigator

import kotlinx.coroutines.flow.MutableSharedFlow

class NavigationProvider {
    private val navigation = MutableSharedFlow<Navigation>()

    fun subscribeNavigation() = navigation

    suspend fun navigateTo(nav: Navigation) {
        navigation.emit(nav)
    }
}