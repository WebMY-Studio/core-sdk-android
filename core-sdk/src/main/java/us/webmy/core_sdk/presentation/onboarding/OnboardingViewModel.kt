package us.webmy.core_sdk.presentation.onboarding

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import us.webmy.core_sdk.data.prefs.OnboardingShownPreferences
import us.webmy.core_sdk.presentation.base.navigator.NavigationProvider
import us.webmy.core_sdk.presentation.base.viewmodel.BaseViewModel
import us.webmy.core_sdk.tools.analytics.AnalyticsManager
import us.webmy.core_sdk.util.singleReplaySharedFlow

abstract class BaseOnboardingViewModel<T : OnboardingModel>(
    private val onboardingShownPreferences: OnboardingShownPreferences,
    private val analyticsManager: AnalyticsManager,
    navigationProvider: NavigationProvider
) : BaseViewModel(navigationProvider) {

    init {
        analyticsManager.logEvent("onboarding_shown")
    }

    abstract val onboardingModels: List<T>

    abstract fun navigateNext()

    val currentItem = singleReplaySharedFlow<T>()

    fun onCloseClick() {
        analyticsManager.logEvent("onboarding_closed")
        onboardingShownPreferences.setValue(true)
        navigateNext()
    }

    fun onNextClick(pageCount: Int) {
        viewModelScope.launch {
            val page = currentItem.first().index
            if (page == pageCount - 1) {
                onCloseClick()
            } else {
                currentItem.emit(onboardingModels[page + 1])
            }
        }
    }

    open fun onItemSelected(page: Int) {
        viewModelScope.launch { currentItem.emit(onboardingModels[page]) }
    }
}