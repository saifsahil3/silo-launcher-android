package com.example.core.flag

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.example.core.config.EnvironmentConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = EnvironmentConfig.current.preferencesName
)

/**
 * Repository responsible for reading, updating, and persisting feature flag states.
 */
class FeatureFlagRepository(private val context: Context) {

    fun isEnabled(flag: FeatureFlag): Flow<Boolean> {
        val prefKey = booleanPreferencesKey(flag.key)
        return context.dataStore.data.map { preferences ->
            preferences[prefKey] ?: flag.defaultValue
        }
    }

    suspend fun isFeatureEnabled(flag: FeatureFlag): Boolean {
        return isEnabled(flag).first()
    }

    suspend fun setEnabled(flag: FeatureFlag, enabled: Boolean) {
        val prefKey = booleanPreferencesKey(flag.key)
        context.dataStore.edit { preferences ->
            preferences[prefKey] = enabled
        }
    }

    suspend fun resetAll() {
        context.dataStore.edit { preferences ->
            FeatureFlag.entries.forEach { flag ->
                val prefKey = booleanPreferencesKey(flag.key)
                preferences.remove(prefKey)
            }
        }
    }
}
