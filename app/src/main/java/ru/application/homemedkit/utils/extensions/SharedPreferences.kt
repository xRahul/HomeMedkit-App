package ru.application.homemedkit.utils.extensions

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

fun <E : Enum<E>> SharedPreferences.Editor.putEnum(key: String, value: E) {
    putString(key, value.name)
}

inline fun <reified E : Enum<E>> SharedPreferences.getEnum(key: String, defaultValue: E) =
    runCatching { getString(key, defaultValue.name)?.let { enumValueOf<E>(it) } ?: defaultValue }
        .getOrDefault(defaultValue)

inline fun <reified E : Enum<E>> SharedPreferences.getEnumFlow(key: String, defaultValue: E) =
    flow(key) { getEnum(key, defaultValue) }

@Suppress("UNCHECKED_CAST")
inline fun <reified T> SharedPreferences.safeGetValue(key: String, defaultValue: T): T {
    val result: Any? = when (defaultValue) {
        is Boolean -> getBoolean(key, defaultValue)
        is Int -> getInt(key, defaultValue)
        is Long -> getLong(key, defaultValue)
        is Float -> getFloat(key, defaultValue)
        is String -> getString(key, defaultValue)
        is Set<*> -> getStringSet(key, defaultValue.filterIsInstance<String>().toSet())
        else -> {
            edit { remove(key) }
            null
        }
    }
    return (result as? T) ?: defaultValue
}

inline fun <reified T> SharedPreferences.getFlow(key: String, defaultValue: T) = flow(key) {
    safeGetValue(key, defaultValue)
}

inline fun <T> SharedPreferences.flow(
    key: String,
    crossinline mapper: SharedPreferences.(key: String) -> T
) = callbackFlow {
    val listener = SharedPreferences.OnSharedPreferenceChangeListener { preferences, changedKey ->
        if (key == changedKey) {
            trySend(preferences.mapper(changedKey))
        }
    }

    registerOnSharedPreferenceChangeListener(listener)

    if (contains(key)) {
        send(mapper(key))
    }

    awaitClose { unregisterOnSharedPreferenceChangeListener(listener) }
}