package us.webmy.core_sdk.data.prefs

import us.webmy.core_sdk.tools.preferences.Preferences
import kotlinx.coroutines.flow.Flow

class OnboardingShownPreferences(override val preferences: Preferences) :
    SingleValuePrefs<Boolean> {

    companion object {
        private const val KEY = "Onboarding"
    }


    override fun flow(): Flow<Boolean> = preferences.booleanFlow(KEY, false)

    override fun value(): Boolean = preferences.getBoolean(KEY, false)

    override fun setValue(value: Boolean) {
        preferences.putBoolean(KEY, value)
    }
}