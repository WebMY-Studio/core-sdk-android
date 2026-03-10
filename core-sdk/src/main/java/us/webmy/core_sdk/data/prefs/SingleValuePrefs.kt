package us.webmy.core_sdk.data.prefs

import us.webmy.core_sdk.tools.preferences.Preferences
import kotlinx.coroutines.flow.Flow

interface SingleValuePrefs<T> {

    val preferences: Preferences

    fun flow(): Flow<T>

    fun value(): T

    fun setValue(value: T)
}