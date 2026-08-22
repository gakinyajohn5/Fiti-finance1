package com.fitifinance.comrade.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.fitifinance.comrade.engine.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.appSettingsDataStore by preferencesDataStore(name = "fiti_app_settings")

data class AppSettingsState(
    val hideAmounts: Boolean = false,
    val autoLockDelaySeconds: Int = 0, // 0 = lock immediately on background
    val notifyDamageControl: Boolean = true,
    val notifyPendingCategorization: Boolean = true,
    val notifyBudgetThreshold: Boolean = true,
    val themeAutoMode: Boolean = true, // true = location-driven, false = manual pick below
    val manualThemeMode: ThemeMode = ThemeMode.CAMPUS
)

/** Home for every toggle/preference in the Settings area that isn't the PIN itself. */
class AppPreferences(private val context: Context) {

    private object Keys {
        val HIDE_AMOUNTS = booleanPreferencesKey("hide_amounts")
        val AUTO_LOCK_DELAY_SECONDS = intPreferencesKey("auto_lock_delay_seconds")
        val NOTIFY_DAMAGE_CONTROL = booleanPreferencesKey("notify_damage_control")
        val NOTIFY_PENDING_CATEGORIZATION = booleanPreferencesKey("notify_pending_categorization")
        val NOTIFY_BUDGET_THRESHOLD = booleanPreferencesKey("notify_budget_threshold")
        val THEME_AUTO_MODE = booleanPreferencesKey("theme_auto_mode")
        val MANUAL_THEME_MODE = stringPreferencesKey("manual_theme_mode")
    }

    val state: Flow<AppSettingsState> = context.appSettingsDataStore.data.map { prefs ->
        AppSettingsState(
            hideAmounts = prefs[Keys.HIDE_AMOUNTS] ?: false,
            autoLockDelaySeconds = prefs[Keys.AUTO_LOCK_DELAY_SECONDS] ?: 0,
            notifyDamageControl = prefs[Keys.NOTIFY_DAMAGE_CONTROL] ?: true,
            notifyPendingCategorization = prefs[Keys.NOTIFY_PENDING_CATEGORIZATION] ?: true,
            notifyBudgetThreshold = prefs[Keys.NOTIFY_BUDGET_THRESHOLD] ?: true,
            themeAutoMode = prefs[Keys.THEME_AUTO_MODE] ?: true,
            manualThemeMode = (prefs[Keys.MANUAL_THEME_MODE] ?: ThemeMode.CAMPUS.name)
                .let { runCatching { ThemeMode.valueOf(it) }.getOrDefault(ThemeMode.CAMPUS) }
        )
    }

    suspend fun setHideAmounts(hide: Boolean) {
        context.appSettingsDataStore.edit { it[Keys.HIDE_AMOUNTS] = hide }
    }

    suspend fun setAutoLockDelaySeconds(seconds: Int) {
        context.appSettingsDataStore.edit { it[Keys.AUTO_LOCK_DELAY_SECONDS] = seconds }
    }

    suspend fun setNotifyDamageControl(enabled: Boolean) {
        context.appSettingsDataStore.edit { it[Keys.NOTIFY_DAMAGE_CONTROL] = enabled }
    }

    suspend fun setNotifyPendingCategorization(enabled: Boolean) {
        context.appSettingsDataStore.edit { it[Keys.NOTIFY_PENDING_CATEGORIZATION] = enabled }
    }

    suspend fun setNotifyBudgetThreshold(enabled: Boolean) {
        context.appSettingsDataStore.edit { it[Keys.NOTIFY_BUDGET_THRESHOLD] = enabled }
    }

    suspend fun setThemeAutoMode(auto: Boolean) {
        context.appSettingsDataStore.edit { it[Keys.THEME_AUTO_MODE] = auto }
    }

    suspend fun setManualThemeMode(mode: ThemeMode) {
        context.appSettingsDataStore.edit { it[Keys.MANUAL_THEME_MODE] = mode.name }
    }
}
