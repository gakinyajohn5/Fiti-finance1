package com.fitifinance.comrade.security

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import androidx.core.content.ContextCompat

object BiometricAuthenticator {

    /** True if the device has usable fingerprint/face hardware with something enrolled. */
    fun isAvailable(activity: FragmentActivity): Boolean {
        val manager = BiometricManager.from(activity)
        return manager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) ==
            BiometricManager.BIOMETRIC_SUCCESS
    }

    fun prompt(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit = {}
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                onError(errString.toString())
            }
        }

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock Fiti Finance")
            .setSubtitle("Use your fingerprint or face to continue")
            .setNegativeButtonText("Use PIN instead")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK)
            .build()

        BiometricPrompt(activity, executor, callback).authenticate(promptInfo)
    }
}
