package com.velora.vault.core.security

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Orchestrates Velora's envelope-encryption scheme:
 *
 *  - A single random 256-bit "vault key" is generated once, at vault
 *    creation. It is the actual SQLCipher database passphrase and is never
 *    written to disk in the clear.
 *  - The vault key is independently wrapped (AES-GCM) under up to three
 *    key-encryption-keys: one derived from the master password (PBKDF2),
 *    one derived from the device PIN (PBKDF2), and one held inside the
 *    Android Keystore behind a biometric/device-credential gate. Deleting
 *    any one wrapping revokes that unlock method without touching the
 *    others or re-encrypting the database.
 *  - The master password and PIN themselves are never stored anywhere,
 *    not even hashed for comparison — successful decryption of the
 *    wrapped vault key *is* the proof of a correct password.
 */
@Singleton
class VaultKeyManager @Inject constructor(
    private val prefs: SecureVaultPrefs,
    private val keystoreCrypto: KeystoreCryptoManager,
) {

    fun isVaultProvisioned(): Boolean = prefs.getBoolean(SecureVaultPrefs.Keys.VAULT_PROVISIONED, false)

    /** Call once, when the user finishes choosing a master password during onboarding. */
    fun provisionVault(masterPassword: CharArray): ByteArray {
        val vaultKey = KeyDerivation.randomKeyBytes()
        val salt = KeyDerivation.randomSalt()
        val kek = KeyDerivation.deriveKeyEncryptionKey(masterPassword, salt)
        val wrapped = KeyDerivation.wrap(kek, vaultKey)

        prefs.putString(SecureVaultPrefs.Keys.MASTER_SALT, salt.toBase64())
        prefs.putInt(SecureVaultPrefs.Keys.MASTER_ITERATIONS, KeyDerivation.DEFAULT_ITERATIONS)
        prefs.putString(SecureVaultPrefs.Keys.WRAPPED_VAULT_KEY_PASSWORD, wrapped.toStoredString())
        prefs.putBoolean(SecureVaultPrefs.Keys.VAULT_PROVISIONED, true)
        return vaultKey
    }

    fun unlockWithMasterPassword(masterPassword: CharArray): ByteArray? {
        val saltB64 = prefs.getString(SecureVaultPrefs.Keys.MASTER_SALT) ?: return null
        val wrappedStr = prefs.getString(SecureVaultPrefs.Keys.WRAPPED_VAULT_KEY_PASSWORD) ?: return null
        val iterations = prefs.getInt(SecureVaultPrefs.Keys.MASTER_ITERATIONS, KeyDerivation.DEFAULT_ITERATIONS)
        val kek = KeyDerivation.deriveKeyEncryptionKey(masterPassword, saltB64.fromBase64(), iterations)
        return runCatching { KeyDerivation.unwrap(kek, EncryptedPayload.fromStoredString(wrappedStr)) }.getOrNull()
    }

    /** Re-wraps the vault key under a new master-password KEK, e.g. during a password change. */
    fun rewrapForNewMasterPassword(vaultKey: ByteArray, newMasterPassword: CharArray) {
        val salt = KeyDerivation.randomSalt()
        val kek = KeyDerivation.deriveKeyEncryptionKey(newMasterPassword, salt)
        val wrapped = KeyDerivation.wrap(kek, vaultKey)
        prefs.putString(SecureVaultPrefs.Keys.MASTER_SALT, salt.toBase64())
        prefs.putInt(SecureVaultPrefs.Keys.MASTER_ITERATIONS, KeyDerivation.DEFAULT_ITERATIONS)
        prefs.putString(SecureVaultPrefs.Keys.WRAPPED_VAULT_KEY_PASSWORD, wrapped.toStoredString())
    }

    // --- PIN unlock -----------------------------------------------------

    fun setupPin(pin: String, vaultKey: ByteArray) {
        val salt = KeyDerivation.randomSalt()
        val kek = KeyDerivation.deriveKeyEncryptionKey(pin.toCharArray(), salt)
        val wrapped = KeyDerivation.wrap(kek, vaultKey)
        prefs.putString(SecureVaultPrefs.Keys.PIN_SALT, salt.toBase64())
        prefs.putString(SecureVaultPrefs.Keys.WRAPPED_VAULT_KEY_PIN, wrapped.toStoredString())
    }

    fun isPinEnabled(): Boolean = prefs.getString(SecureVaultPrefs.Keys.WRAPPED_VAULT_KEY_PIN) != null

    fun unlockWithPin(pin: String): ByteArray? {
        val saltB64 = prefs.getString(SecureVaultPrefs.Keys.PIN_SALT) ?: return null
        val wrappedStr = prefs.getString(SecureVaultPrefs.Keys.WRAPPED_VAULT_KEY_PIN) ?: return null
        val kek = KeyDerivation.deriveKeyEncryptionKey(pin.toCharArray(), saltB64.fromBase64())
        return runCatching { KeyDerivation.unwrap(kek, EncryptedPayload.fromStoredString(wrappedStr)) }.getOrNull()
    }

    fun disablePin() {
        prefs.remove(SecureVaultPrefs.Keys.PIN_SALT)
        prefs.remove(SecureVaultPrefs.Keys.WRAPPED_VAULT_KEY_PIN)
    }

    // --- Biometric unlock -------------------------------------------------
    // The Keystore key itself is authentication-gated, so encryption here
    // does not require a fresh prompt, but decryption (unlockingpath) does —
    // BiometricAuthManager supplies the pre-authenticated Cipher.

    fun isBiometricEnabled(): Boolean = prefs.getBoolean(SecureVaultPrefs.Keys.BIOMETRIC_ENABLED, false)

    fun wrapVaultKeyForBiometric(vaultKey: ByteArray) {
        val cipher = keystoreCrypto.createAuthenticatedEncryptCipher(KeystoreCryptoManager.ALIAS_VAULT_KEY_WRAP)
        val ciphertext = cipher.doFinal(vaultKey)
        prefs.putString(SecureVaultPrefs.Keys.WRAPPED_VAULT_KEY_BIOMETRIC, ciphertext.toBase64())
        prefs.putString(SecureVaultPrefs.Keys.WRAPPED_VAULT_KEY_BIOMETRIC_IV, cipher.iv.toBase64())
        prefs.putBoolean(SecureVaultPrefs.Keys.BIOMETRIC_ENABLED, true)
    }

    fun biometricUnlockCipher(): Cipher? {
        val ivB64 = prefs.getString(SecureVaultPrefs.Keys.WRAPPED_VAULT_KEY_BIOMETRIC_IV) ?: return null
        return runCatching {
            keystoreCrypto.createAuthenticatedDecryptCipher(KeystoreCryptoManager.ALIAS_VAULT_KEY_WRAP, ivB64.fromBase64())
        }.getOrNull()
    }

    /** Call after [androidx.biometric.BiometricPrompt] succeeds with the cipher from [biometricUnlockCipher]. */
    fun unlockWithAuthenticatedCipher(cipher: Cipher): ByteArray? {
        val ciphertextB64 = prefs.getString(SecureVaultPrefs.Keys.WRAPPED_VAULT_KEY_BIOMETRIC) ?: return null
        return runCatching { cipher.doFinal(ciphertextB64.fromBase64()) }.getOrNull()
    }

    fun disableBiometric() {
        prefs.putBoolean(SecureVaultPrefs.Keys.BIOMETRIC_ENABLED, false)
        prefs.remove(SecureVaultPrefs.Keys.WRAPPED_VAULT_KEY_BIOMETRIC)
        prefs.remove(SecureVaultPrefs.Keys.WRAPPED_VAULT_KEY_BIOMETRIC_IV)
        keystoreCrypto.deleteKey(KeystoreCryptoManager.ALIAS_VAULT_KEY_WRAP)
    }

    // --- Recovery code ----------------------------------------------------

    /** Generates a one-time recovery code, wraps the vault key with it, and returns it for one-time display. */
    fun generateRecoveryCode(vaultKey: ByteArray): String {
        val code = randomRecoveryCode()
        val salt = KeyDerivation.randomSalt()
        val kek = KeyDerivation.deriveKeyEncryptionKey(code.toCharArray(), salt)
        val wrapped = KeyDerivation.wrap(kek, vaultKey)
        prefs.putString(SecureVaultPrefs.Keys.RECOVERY_SALT, salt.toBase64())
        prefs.putString(SecureVaultPrefs.Keys.WRAPPED_VAULT_KEY_RECOVERY, wrapped.toStoredString())
        return code
    }

    fun unlockWithRecoveryCode(code: String): ByteArray? {
        val saltB64 = prefs.getString(SecureVaultPrefs.Keys.RECOVERY_SALT) ?: return null
        val wrappedStr = prefs.getString(SecureVaultPrefs.Keys.WRAPPED_VAULT_KEY_RECOVERY) ?: return null
        val kek = KeyDerivation.deriveKeyEncryptionKey(code.trim().uppercase().toCharArray(), saltB64.fromBase64())
        return runCatching { KeyDerivation.unwrap(kek, EncryptedPayload.fromStoredString(wrappedStr)) }.getOrNull()
    }

    /** Wipes all wrapped-key material. Used for "log out of all devices" and account deletion. */
    fun wipeAll() {
        prefs.clearAll()
        keystoreCrypto.deleteKey(KeystoreCryptoManager.ALIAS_VAULT_KEY_WRAP)
        keystoreCrypto.deleteKey(KeystoreCryptoManager.ALIAS_DOCUMENT_KEY)
    }

    private fun randomRecoveryCode(): String {
        val alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789" // no ambiguous chars
        val random = SecureRandom()
        return (0 until 4).joinToString("-") {
            (0 until 5).map { alphabet[random.nextInt(alphabet.length)] }.joinToString("")
        }
    }

    private fun ByteArray.toBase64(): String = Base64.encodeToString(this, Base64.NO_WRAP)
    private fun String.fromBase64(): ByteArray = Base64.decode(this, Base64.NO_WRAP)
}
