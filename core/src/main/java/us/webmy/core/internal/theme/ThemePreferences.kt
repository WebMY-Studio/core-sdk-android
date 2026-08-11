package us.webmy.core.internal.theme

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import us.webmy.core.theme.BuildInThemeIds
import us.webmy.core.theme.ThemeId
import us.webmy.core.prefs.Preferences
import us.webmy.core.prefs.SingleValuePrefs

internal class ThemePreferences(override val preferences: Preferences) :
    SingleValuePrefs<ThemeId> {

    companion object {
        private const val KEY = "ThemeId"
    }

    override fun flow(): Flow<ThemeId> =
        preferences.stringFlow(KEY).map { it.toThemeId() }

    override fun value(): ThemeId = preferences.getString(KEY).toThemeId()

    override fun setValue(value: ThemeId) {
        preferences.putString(KEY, value)
    }

    private fun String?.toThemeId(): ThemeId =
        this?.takeIf { it.isNotEmpty() } ?: BuildInThemeIds.DEFAULT
}
