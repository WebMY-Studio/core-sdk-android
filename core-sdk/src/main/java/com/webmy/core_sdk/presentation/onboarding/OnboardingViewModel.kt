package com.webmy.core_sdk.presentation.onboarding

import androidx.lifecycle.viewModelScope
import com.webmy.core_sdk.data.prefs.OnboardingShownPreferences
import com.webmy.core_sdk.presentation.base.viewmodel.BaseViewModel
import com.webmy.core_sdk.tools.analytics.AnalyticsManager
import com.webmy.core_sdk.util.singleReplaySharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

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