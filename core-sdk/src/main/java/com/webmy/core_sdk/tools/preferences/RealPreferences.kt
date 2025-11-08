package com.webmy.core_sdk.tools.preferences

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map

class RealPreferences(application: Application) : Preferences {

    private val sharedPreferences =
        application.getSharedPreferences(
            application.javaClass.name + "_prefs",
            Context.MODE_PRIVATE
        )

    private val listeners = mutableSetOf<SharedPreferences.OnSharedPreferenceChangeListener>()

    override fun contains(field: String) = sharedPreferences.contains(field)

    override fun putString(
        field: String,
        value: String?,
    ) {
        sharedPreferences.edit().putString(field, value).apply()
    }

    override fun getString(
        field: String,
        defaultValue: String,
    ): String {
        return sharedPreferences.getString(field, defaultValue) ?: defaultValue
    }

    override fun getString(field: String): String? {
        return sharedPreferences.getString(field, null)
    }

    override fun putBoolean(
        field: String,
        value: Boolean,
    ) {
        sharedPreferences.edit().putBoolean(field, value).apply()
    }

    override fun getBoolean(
        field: String,
        defaultValue: Boolean,
    ): Boolean {
        return sharedPreferences.getBoolean(field, defaultValue)
    }

    override fun putInt(
        field: String,
        value: Int,
    ) {
        sharedPreferences.edit().putInt(field, value).apply()
    }

    override fun getInt(
        field: String,
        defaultValue: Int,
    ): Int {
        return sharedPreferences.getInt(field, defaultValue)
    }

    override fun putLong(
        field: String,
        value: Long,
    ) {
        sharedPreferences.edit().putLong(field, value).apply()
    }

    override fun getLong(
        field: String,
        defaultValue: Long,
    ): Long {
        return sharedPreferences.getLong(field, defaultValue)
    }

    override fun putStringSet(field: String, value: Set<String>) {
        sharedPreferences.edit().putStringSet(field, value).apply()
    }

    override fun getStringSet(field: String): Set<String> {
        return sharedPreferences.getStringSet(field, hashSetOf()) ?: hashSetOf()
    }

    override fun removeField(field: String) {
        sharedPreferences.edit().remove(field).apply()
    }

    override fun stringFlow(
        field: String,
        initialValueProducer: (suspend () -> String)?,
    ): Flow<String?> =
        keyFlow(field)
            .map {
                if (contains(field)) {
                    getString(field)
                } else {
                    val initialValue = initialValueProducer?.invoke()
                    putString(field, initialValue)
                    initialValue
                }
            }

    override fun booleanFlow(
        field: String,
        defaultValue: Boolean,
    ): Flow<Boolean> {
        return keyFlow(field).map {
            getBoolean(field, defaultValue)
        }
    }

    override fun longFlow(field: String, defaultValue: Long): Flow<Long> {
        return keyFlow(field).map {
            getLong(field, defaultValue)
        }
    }

    override fun intFlow(field: String, defaultValue: Int): Flow<Int> {
        return keyFlow(field).map {
            getInt(field, defaultValue)
        }
    }

    override fun stringSetFlow(field: String): Flow<Set<String>> {
        return keyFlow(field).map {
            getStringSet(it)
        }
    }

    override fun keyFlow(key: String): Flow<String> =
        keysFlow(key)
            .map { it.first() }

    override fun keysFlow(vararg keys: String): Flow<List<String>> =
        callbackFlow {
            send(keys.toList())

            val listener =
                SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                    if (key in keys) {
                        trySend(listOf(key!!))
                    }
                }

            listeners.add(listener)
            sharedPreferences.registerOnSharedPreferenceChangeListener(listener)

            awaitClose {
                listeners.remove(listener)
                sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
            }
        }

    override fun edit(): Editor {
        return SharedPreferenceEditor(sharedPreferences)
    }

    private class SharedPreferenceEditor(private val sharedPreferences: SharedPreferences) :
        Editor {
        private val editor = sharedPreferences.edit()

        override fun putString(
            field: String,
            value: String?,
        ) {
            editor.putString(field, value)
        }

        override fun putBoolean(
            field: String,
            value: Boolean,
        ) {
            editor.putBoolean(field, value)
        }

        override fun putInt(
            field: String,
            value: Int,
        ) {
            editor.putInt(field, value)
        }

        override fun putLong(
            field: String,
            value: Long,
        ) {
            editor.putLong(field, value)
        }

        override fun remove(field: String) {
            editor.remove(field)
        }

        override fun apply() {
            editor.apply()
        }
    }
}
