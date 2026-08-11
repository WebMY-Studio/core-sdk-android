package us.webmy.core.presentation

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import us.webmy.core.prefs.OnboardingShownPreferences
import us.webmy.core.presentation.BaseViewModel
import us.webmy.core.analytics.AnalyticsManager
import us.webmy.core.util.singleReplaySharedFlow

abstract class BaseOnboardingViewModel<T : OnboardingModel>(
    private val onboardingShownPreferences: OnboardingShownPreferences,
    private val analyticsManager: AnalyticsManager,
) : BaseViewModel() {

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

    fun onNextClick() {
        viewModelScope.launch {
            val page = currentItem.first().index
            if (page == onboardingModels.size - 1) {
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