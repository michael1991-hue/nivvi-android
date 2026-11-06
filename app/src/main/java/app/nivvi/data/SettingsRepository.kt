package app.nivvi.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "nivvi_settings")

object SettingsKeys {
    val HIGH_ENABLED = booleanPreferencesKey("high_enabled")
    val LOW_ENABLED = booleanPreferencesKey("low_enabled")
    val HIGH_LIMIT = intPreferencesKey("high_limit")
    val LOW_LIMIT = intPreferencesKey("low_limit")
    val DWELL_SECONDS = intPreferencesKey("dwell_seconds")
}

data class AlertSettings(
    val highEnabled: Boolean = false,
    val lowEnabled: Boolean = false,
    val highLimit: Int = 180,
    val lowLimit: Int = 50,
    val dwellSeconds: Int = 15
)

class SettingsRepository(private val context: Context) {
    val alertSettings: Flow<AlertSettings> = context.dataStore.data.map { prefs ->
        AlertSettings(
            highEnabled = prefs[SettingsKeys.HIGH_ENABLED] ?: false,
            lowEnabled = prefs[SettingsKeys.LOW_ENABLED] ?: false,
            highLimit = prefs[SettingsKeys.HIGH_LIMIT] ?: 180,
            lowLimit = prefs[SettingsKeys.LOW_LIMIT] ?: 50,
            dwellSeconds = prefs[SettingsKeys.DWELL_SECONDS] ?: 15
        )
    }

    suspend fun update(settings: AlertSettings) {
        context.dataStore.edit { prefs ->
            prefs[SettingsKeys.HIGH_ENABLED] = settings.highEnabled
            prefs[SettingsKeys.LOW_ENABLED] = settings.lowEnabled
            prefs[SettingsKeys.HIGH_LIMIT] = settings.highLimit
            prefs[SettingsKeys.LOW_LIMIT] = settings.lowLimit
            prefs[SettingsKeys.DWELL_SECONDS] = settings.dwellSeconds
        }
    }
}
