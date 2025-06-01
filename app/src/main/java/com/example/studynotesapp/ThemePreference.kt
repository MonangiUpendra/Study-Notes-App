package com.example.studynotesapp

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

// Create a DataStore instance attached to the context
private val Context.dataStore by preferencesDataStore(name = "user_settings")

object ThemePreference {
    private val NIGHT_MODE_KEY = booleanPreferencesKey("night_mode")

    // Load night mode setting
    fun isNightMode(context: Context): Boolean = runBlocking {
        context.dataStore.data.map { prefs -> prefs[NIGHT_MODE_KEY] ?: false }.first()
    }

    // Save night mode setting
    suspend fun setNightMode(context: Context, enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[NIGHT_MODE_KEY] = enabled
        }
    }
}
