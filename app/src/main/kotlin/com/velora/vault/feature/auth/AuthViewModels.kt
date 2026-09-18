package com.velora.vault.feature.auth

import androidx.lifecycle.ViewModel
import com.velora.vault.core.util.PasswordStrength
import com.velora.vault.core.util.PasswordStrengthResult
import com.velora.vault.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.crypto.Cipher
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(private val authRepository: AuthRepository) : ViewModel() {
    fun createAccount(email: String, displayName: String): Boolean {
        if (email.isBlank() || !email.contains("@")) return false
        authRepository.createAccount(email.trim(), displayName.ifBlank { email.substringBefore("@") })
        return true
    }
}

@HiltViewModel
class MasterPasswordViewModel @Inject constructor(private val authRepository: AuthRepository) : ViewModel() {
    fun evaluate(password: String): PasswordStrengthResult = PasswordStrength.evaluate(password)

    fun createMasterPassword(password: String): Boolean {
        if (password.length < 10) return false
        authRepository.createMasterPassword(password.toCharArray())
        return true
    }
}

@HiltViewModel
class BiometricSetupViewModel @Inject constructor(private val authRepository: AuthRepository) : ViewModel() {
    fun enrollmentCipher(): Cipher? = authRepository.biometricEnrollmentCipher()
    fun completeEnrollment(cipher: Cipher): Boolean = authRepository.completeBiometricEnrollment(cipher)
}

@HiltViewModel
class RecoveryRevealViewModel @Inject constructor(private val authRepository: AuthRepository) : ViewModel() {
    fun generateCode(): String? = authRepository.generateRecoveryCode()
}

@HiltViewModel
class UnlockViewModel @Inject constructor(private val authRepository: AuthRepository) : ViewModel() {
    fun isBiometricEnabled(): Boolean = authRepository.isBiometricEnabled()
    fun biometricCipher(): Cipher? = authRepository.biometricUnlockCipher()
    fun unlockWithBiometric(cipher: Cipher): Boolean = authRepository.unlockWithBiometricCipher(cipher)
    fun unlockWithMasterPassword(password: String): Boolean =
        authRepository.unlockWithMasterPassword(password.toCharArray())
    fun isPinEnabled(): Boolean = authRepository.isPinEnabled()
    fun unlockWithPin(pin: String): Boolean = authRepository.unlockWithPin(pin)
    fun accountEmail(): String? = authRepository.currentAccount()?.email
}

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(private val authRepository: AuthRepository) : ViewModel() {
    fun unlockWithRecoveryCode(code: String): Boolean = authRepository.unlockWithRecoveryCode(code)
}
