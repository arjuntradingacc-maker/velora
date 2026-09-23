package com.downlifeblues.velora.core.security

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import javax.crypto.Cipher
import javax.inject.Inject
import javax.inject.Singleton

sealed interface BiometricResult {
    data class Success(val cipher: Cipher?) : BiometricResult
    data class Failed(val message: String) : BiometricResult
    data object Cancelled : BiometricResult
}

enum class BiometricAvailability { AVAILABLE, NO_HARDWARE, NOT_ENROLLED, TEMPORARILY_UNAVAILABLE, UNSUPPORTED }

private const val AUTH_TYPES = BiometricManager.Authenticators.BIOMETRIC_STRONG or
    BiometricManager.Authenticators.DEVICE_CREDENTIAL

/** Thin wrapper over [BiometricPrompt] so screens deal in a small sealed result type instead of callbacks. */
@Singleton
class BiometricAuthManager @Inject constructor() {

    fun availability(activity: FragmentActivity): BiometricAvailability {
        val manager = BiometricManager.from(activity)
        return when (manager.canAuthenticate(AUTH_TYPES)) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricAvailability.AVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricAvailability.NO_HARDWARE
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricAvailability.NOT_ENROLLED
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricAvailability.TEMPORARILY_UNAVAILABLE
            else -> BiometricAvailability.UNSUPPORTED
        }
    }

    fun authenticate(
        activity: FragmentActivity,
        title: String,
        subtitle: String? = null,
        cipher: Cipher? = null,
        onResult: (BiometricResult) -> Unit,
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        val prompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onResult(BiometricResult.Success(result.cryptoObject?.cipher))
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    if (errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
                        errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON ||
                        errorCode == BiometricPrompt.ERROR_CANCELED
                    ) {
                        onResult(BiometricResult.Cancelled)
                    } else {
                        onResult(BiometricResult.Failed(errString.toString()))
                    }
                }

                override fun onAuthenticationFailed() {
                    // A single failed attempt; the prompt stays open for retry so we don't surface this as terminal.
                }
            },
        )

        val infoBuilder = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setAllowedAuthenticators(AUTH_TYPES)
        subtitle?.let { infoBuilder.setSubtitle(it) }

        if (cipher != null) {
            prompt.authenticate(infoBuilder.build(), BiometricPrompt.CryptoObject(cipher))
        } else {
            prompt.authenticate(infoBuilder.build())
        }
    }
}
