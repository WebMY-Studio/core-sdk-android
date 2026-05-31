package us.webmy.core.ui.data.prefs

import kotlinx.coroutines.flow.Flow
import us.webmy.core.data.prefs.SingleValuePrefs
import us.webmy.core.tools.preferences.Preferences

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
