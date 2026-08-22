package com.fitifinance.comrade.security
import kotlinx.coroutines.flow.first

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.security.MessageDigest

private val Context.securityDataStore by preferencesDataStore(name = "fiti_security")

/**
 * Handles the "App Lock" feature: a user-set PIN (never stored in plaintext,
 * only its SHA-256 hash) plus an opt-in toggle for biometric (fingerprint/
 * face) unlock as a faster alternative to typing the PIN.
 */
class SecurityPreferences(private val context: Context) {

    private object Keys {
        val PIN_HASH = stringPreferencesKey("pin_hash")
        val LOCK_ENABLED = booleanPreferencesKey("lock_enabled")
        val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
    }

    /** True once the comrade has opted into PIN/biometric protection. */
    val isLockEnabled: Flow<Boolean> =
        context.securityDataStore.data.map { it[Keys.LOCK_ENABLED] ?: false }

    val isBiometricEnabled: Flow<Boolean> =
        context.securityDataStore.data.map { it[Keys.BIOMETRIC_ENABLED] ?: false }

    val hasPinSet: Flow<Boolean> =
        context.securityDataStore.data.map { !it[Keys.PIN_HASH].isNullOrBlank() }

    suspend fun setPin(pin: String) {
        context.securityDataStore.edit { prefs ->
            prefs[Keys.PIN_HASH] = hash(pin)
            prefs[Keys.LOCK_ENABLED] = true
        }
    }

    suspend fun verifyPin(pin: String): Boolean {
        val stored = context.securityDataStore.data.map { it[Keys.PIN_HASH] }
        var matched = false
        stored.collectOnce { matched = it == hash(pin) }
        return matched
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.securityDataStore.edit { prefs -> prefs[Keys.BIOMETRIC_ENABLED] = enabled }
    }

    /** Turns off App Lock entirely and forgets the PIN. */
    suspend fun disableLock() {
        context.securityDataStore.edit { prefs ->
            prefs[Keys.LOCK_ENABLED] = false
            prefs[Keys.BIOMETRIC_ENABLED] = false
            prefs.remove(Keys.PIN_HASH)
        }
    }

    private fun hash(value: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(value.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }

    private suspend fun <T> Flow<T>.collectOnce(action: (T) -> Unit) {
        kotlinx.coroutines.flow.first { action(it); true }
    }
}
