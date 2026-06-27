package com.example.taskerine_v2.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Top-level DataStore instance, tied to Context
val Context.dataStore by preferencesDataStore(name = "taskerine_prefs")

class PreferencesManager(private val context: Context) {

    companion object {
        private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode_enabled")
        private val REMEMBERED_USER_ID_KEY = stringPreferencesKey("remembered_user_id")
    }

    // --- Dark mode ---
    val isDarkModeEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[DARK_MODE_KEY] ?: false   // default: light mode
    }

    suspend fun setDarkModeEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[DARK_MODE_KEY] = enabled
        }
    }

    // --- Remembered session ---
    // If a user ID is stored here, the app auto-logs them in on launch.
    // Null/absent means no remembered session -> show Welcome screen.
    val rememberedUserId: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[REMEMBERED_USER_ID_KEY]
    }

    suspend fun setRememberedUserId(userId: String?) {
        context.dataStore.edit { prefs ->
            if (userId == null) {
                prefs.remove(REMEMBERED_USER_ID_KEY)
            } else {
                prefs[REMEMBERED_USER_ID_KEY] = userId
            }
        }
    }
}