package com.fitifinance.comrade.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitifinance.comrade.data.entity.Recipient
import com.fitifinance.comrade.data.entity.UserProfile
import com.fitifinance.comrade.engine.ThemeMode
import com.fitifinance.comrade.repository.FinanceRepository
import com.fitifinance.comrade.settings.AppPreferences
import com.fitifinance.comrade.settings.AppSettingsState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsScreenState(
    val profile: UserProfile = UserProfile(),
    val appSettings: AppSettingsState = AppSettingsState(),
    val recipients: List<Recipient> = emptyList()
)

class SettingsViewModel(
    private val repository: FinanceRepository,
    private val appPreferences: AppPreferences
) : ViewModel() {

    val uiState: StateFlow<SettingsScreenState> = combine(
        repository.observeProfileFlowSafe(),
        appPreferences.state,
        repository.observeRecipients()
    ) { profile, appSettings, recipients ->
        SettingsScreenState(profile, appSettings, recipients)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsScreenState())

    // ---- Budgets ----
    fun saveBudgets(
        dailyMealBudgetKes: Double,
        monthlyBudgetKes: Double,
        nightOutBudgetKes: Double,
        fareShieldKes: Double
    ) {
        viewModelScope.launch {
            val current = repository.getProfile()
            repository.saveProfile(
                current.copy(
                    dailyMealBudgetKes = dailyMealBudgetKes,
                    monthlyBudgetKes = monthlyBudgetKes,
                    nightOutBudgetKes = nightOutBudgetKes,
                    fareShieldKes = fareShieldKes
                )
            )
        }
    }

    // ---- Profile / persona ----
    fun saveProfileBasics(profile: UserProfile) {
        viewModelScope.launch { repository.saveProfile(profile) }
    }

    // ---- Notifications ----
    fun setNotifyDamageControl(enabled: Boolean) = viewModelScope.launch { appPreferences.setNotifyDamageControl(enabled) }
    fun setNotifyPendingCategorization(enabled: Boolean) = viewModelScope.launch { appPreferences.setNotifyPendingCategorization(enabled) }
    fun setNotifyBudgetThreshold(enabled: Boolean) = viewModelScope.launch { appPreferences.setNotifyBudgetThreshold(enabled) }

    // ---- Privacy ----
    fun setHideAmounts(hide: Boolean) = viewModelScope.launch { appPreferences.setHideAmounts(hide) }
    fun setAutoLockDelaySeconds(seconds: Int) = viewModelScope.launch { appPreferences.setAutoLockDelaySeconds(seconds) }

    // ---- Appearance ----
    fun setThemeAutoMode(auto: Boolean) = viewModelScope.launch { appPreferences.setThemeAutoMode(auto) }
    fun setManualThemeMode(mode: ThemeMode) = viewModelScope.launch { appPreferences.setManualThemeMode(mode) }

    // ---- Data & recipients ----
    fun deleteRecipient(fullName: String) = viewModelScope.launch { repository.deleteRecipient(fullName) }

    fun clearAllData(onDone: () -> Unit) {
        viewModelScope.launch {
            repository.clearAllData()
            onDone()
        }
    }

    suspend fun buildTransactionsCsv(): String = repository.buildTransactionsCsv()
}
