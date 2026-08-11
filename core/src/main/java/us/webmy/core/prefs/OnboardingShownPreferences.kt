package us.webmy.core.prefs

import kotlinx.coroutines.flow.Flow
import us.webmy.core.prefs.Preferences

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
