package com.velora.vault.data.repository

import com.velora.vault.core.security.BiometricAuthManager
import com.velora.vault.core.security.SecureVaultPrefs
import com.velora.vault.core.security.VaultKeyManager
import com.velora.vault.core.security.VaultSessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.crypto.Cipher
import javax.inject.Inject
import javax.inject.Singleton

data class AccountProfile(val email: String, val displayName: String)

/**
 * Orchestrates account + vault unlock state. Account identity (email) is
 * kept separately from the master password on purpose: the backend (see
 * [com.velora.vault.data.remote.AuthApi]) only ever authenticates *who you
 * are* for sync purposes — it never sees, and cannot derive, the master
 * password or vault key, which stay entirely on-device.
 */
@Singleton
class AuthRepository @Inject constructor(
    private val prefs: SecureVaultPrefs,
    private val vaultKeyManager: VaultKeyManager,
    private val sessionManager: VaultSessionManager,
    private val biometricAuthManager: BiometricAuthManager,
) {
    private object Keys {
        const val ACCOUNT_EMAIL = "account_email"
        const val ACCOUNT_DISPLAY_NAME = "account_display_name"
    }

    fun isAccountCreated(): Boolean = prefs.getString(Keys.ACCOUNT_EMAIL) != null

    fun isOnboardingCompleted(): Boolean = prefs.getBoolean(SecureVaultPrefs.Keys.ONBOARDING_COMPLETED, false)
    fun setOnboardingCompleted() = prefs.putBoolean(SecureVaultPrefs.Keys.ONBOARDING_COMPLETED, true)

    fun isVaultProvisioned(): Boolean = vaultKeyManager.isVaultProvisioned()

    val sessionState: Flow<Boolean> = sessionManager.session.map { it is com.velora.vault.core.security.VaultSession.Unlocked }

    fun currentAccount(): AccountProfile? {
        val email = prefs.getString(Keys.ACCOUNT_EMAIL) ?: return null
        val name = prefs.getString(Keys.ACCOUNT_DISPLAY_NAME) ?: email.substringBefore("@")
        return AccountProfile(email, name)
    }

    /** Records the account locally. Sync (task-level, see SyncRepository) is what would call the real backend. */
    fun createAccount(email: String, displayName: String) {
        prefs.putString(Keys.ACCOUNT_EMAIL, email)
        prefs.putString(Keys.ACCOUNT_DISPLAY_NAME, displayName)
    }

    /** Step 2 of onboarding: choose the master password that will protect the vault. */
    fun createMasterPassword(masterPassword: CharArray) {
        val vaultKey = vaultKeyManager.provisionVault(masterPassword)
        sessionManager.unlock(vaultKey)
    }

    fun unlockWithMasterPassword(masterPassword: CharArray): Boolean {
        val vaultKey = vaultKeyManager.unlockWithMasterPassword(masterPassword) ?: return false
        sessionManager.unlock(vaultKey)
        return true
    }

    fun unlockWithPin(pin: String): Boolean {
        val vaultKey = vaultKeyManager.unlockWithPin(pin) ?: return false
        sessionManager.unlock(vaultKey)
        return true
    }

    fun isPinEnabled(): Boolean = vaultKeyManager.isPinEnabled()
    fun setupPin(pin: String) {
        val vaultKey = sessionManager.currentVaultKeyOrNull() ?: return
        vaultKeyManager.setupPin(pin, vaultKey)
    }
    fun disablePin() = vaultKeyManager.disablePin()

    fun isBiometricEnabled(): Boolean = vaultKeyManager.isBiometricEnabled()

    /** Step 1: get a cipher to show the user a biometric prompt with. Null means the device can't set this key up right now. */
    fun biometricEnrollmentCipher(): Cipher? = vaultKeyManager.biometricEnrollmentCipher()

    /** Step 2: call with the cipher the biometric prompt handed back on success, never a cipher you built yourself. */
    fun completeBiometricEnrollment(authenticatedCipher: Cipher): Boolean {
        val vaultKey = sessionManager.currentVaultKeyOrNull() ?: return false
        return vaultKeyManager.wrapVaultKeyForBiometric(vaultKey, authenticatedCipher)
    }
    fun disableBiometric() = vaultKeyManager.disableBiometric()
    fun biometricUnlockCipher(): Cipher? = vaultKeyManager.biometricUnlockCipher()
    fun unlockWithBiometricCipher(cipher: Cipher): Boolean {
        val vaultKey = vaultKeyManager.unlockWithAuthenticatedCipher(cipher) ?: return false
        sessionManager.unlock(vaultKey)
        return true
    }

    fun generateRecoveryCode(): String? {
        val vaultKey = sessionManager.currentVaultKeyOrNull() ?: return null
        return vaultKeyManager.generateRecoveryCode(vaultKey)
    }

    fun unlockWithRecoveryCode(code: String): Boolean {
        val vaultKey = vaultKeyManager.unlockWithRecoveryCode(code) ?: return false
        sessionManager.unlock(vaultKey)
        return true
    }

    fun lock() = sessionManager.lock()

    /** "Log out of all devices": wipes local key material and the account marker, forcing full re-onboarding. */
    fun signOutAndWipeLocalVault() {
        sessionManager.lock()
        vaultKeyManager.wipeAll()
        prefs.remove(Keys.ACCOUNT_EMAIL)
        prefs.remove(Keys.ACCOUNT_DISPLAY_NAME)
    }
}
