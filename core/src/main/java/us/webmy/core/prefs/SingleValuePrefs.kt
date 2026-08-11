package us.webmy.core.prefs

import us.webmy.core.prefs.Preferences
import kotlinx.coroutines.flow.Flow

interface SingleValuePrefs<T> {

    val preferences: Preferences

    fun flow(): Flow<T>

    fun value(): T

    fun setValue(value: T)
}