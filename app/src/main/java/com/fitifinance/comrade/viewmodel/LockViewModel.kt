package com.fitifinance.comrade.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitifinance.comrade.security.SecurityPreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SecurityState(
    val lockEnabled: Boolean = false,
    val biometricEnabled: Boolean = false,
    val hasPinSet: Boolean = false
)

class LockViewModel(private val securityPrefs: SecurityPreferences) : ViewModel() {

    val securityState: StateFlow<SecurityState> = combine(
        securityPrefs.isLockEnabled,
        securityPrefs.isBiometricEnabled,
        securityPrefs.hasPinSet
    ) { lockEnabled, biometricEnabled, hasPinSet ->
        SecurityState(lockEnabled, biometricEnabled, hasPinSet)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SecurityState())

    fun setPin(pin: String, onDone: () -> Unit) {
        viewModelScope.launch {
            securityPrefs.setPin(pin)
            onDone()
        }
    }

    suspend fun verifyPin(pin: String): Boolean = securityPrefs.verifyPin(pin)

    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch { securityPrefs.setBiometricEnabled(enabled) }
    }

    fun disableLock() {
        viewModelScope.launch { securityPrefs.disableLock() }
    }
}
