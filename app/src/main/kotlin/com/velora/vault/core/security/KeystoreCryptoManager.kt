package com.velora.vault.core.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

private const val ANDROID_KEYSTORE = "AndroidKeyStore"
private const val TRANSFORMATION = "AES/GCM/NoPadding"
private const val GCM_TAG_BITS = 128

/** Ciphertext plus the IV needed to decrypt it. Safe to persist as-is (never contains key material). */
data class EncryptedPayload(val iv: ByteArray, val ciphertext: ByteArray) {
    fun toStoredString(): String =
        Base64.encodeToString(iv, Base64.NO_WRAP) + ":" + Base64.encodeToString(ciphertext, Base64.NO_WRAP)

    companion object {
        fun fromStoredString(value: String): EncryptedPayload {
            val (ivPart, cipherPart) = value.split(":", limit = 2)
            return EncryptedPayload(
                iv = Base64.decode(ivPart, Base64.NO_WRAP),
                ciphertext = Base64.decode(cipherPart, Base64.NO_WRAP),
            )
        }
    }
}

/**
 * Thin, deliberately boring wrapper around the Android Keystore. No custom
 * cryptography lives here — only key generation/retrieval and AES-GCM
 * authenticated encryption via the platform's own provider. Keys are
 * generated inside the secure hardware (TEE/StrongBox when present) and
 * their key material never leaves it; the app only ever holds opaque
 * [SecretKey] handles.
 */
@Singleton
class KeystoreCryptoManager @Inject constructor() {

    private val keyStore: KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }

    /**
     * Returns the named key, generating it on first use. [requireAuthentication]
     * gates the key behind biometric/device-credential auth (used for the
     * vault-key-wrapping key so unlocking always requires a fresh biometric
     * check); [validitySeconds] allows a short grace window to avoid
     * re-prompting for rapid successive operations.
     */
    fun getOrCreateKey(
        alias: String,
        requireAuthentication: Boolean = false,
        validitySeconds: Int = -1,
    ): SecretKey {
        keyStore.getKey(alias, null)?.let { return it as SecretKey }

        val builder = KeyGenParameterSpec.Builder(
            alias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .setRandomizedEncryptionRequired(true)

        if (requireAuthentication) {
            builder.setUserAuthenticationRequired(true)
            if (validitySeconds > 0) {
                builder.setUserAuthenticationParameters(
                    validitySeconds,
                    KeyProperties.AUTH_BIOMETRIC_STRONG or KeyProperties.AUTH_DEVICE_CREDENTIAL,
                )
            } else {
                builder.setUserAuthenticationParameters(
                    0,
                    KeyProperties.AUTH_BIOMETRIC_STRONG or KeyProperties.AUTH_DEVICE_CREDENTIAL,
                )
            }
        }
        // Ask for StrongBox when the device has a dedicated secure element;
        // silently fall back to the TEE-backed keystore otherwise.
        try {
            builder.setIsStrongBoxBacked(true)
            return generate(builder.build())
        } catch (_: Exception) {
            builder.setIsStrongBoxBacked(false)
            return generate(builder.build())
        }
    }

    private fun generate(spec: KeyGenParameterSpec): SecretKey {
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        generator.init(spec)
        return generator.generateKey()
    }

    fun deleteKey(alias: String) {
        if (keyStore.containsAlias(alias)) keyStore.deleteEntry(alias)
    }

    fun encrypt(alias: String, plaintext: ByteArray): EncryptedPayload {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey(alias))
        val ciphertext = cipher.doFinal(plaintext)
        return EncryptedPayload(cipher.iv, ciphertext)
    }

    fun decrypt(alias: String, payload: EncryptedPayload): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(GCM_TAG_BITS, payload.iv)
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(alias), spec)
        return cipher.doFinal(payload.ciphertext)
    }

    /** Builds a decrypt-mode [Cipher] for [alias] without running it — used to bind a [androidx.biometric.BiometricPrompt.CryptoObject]. */
    fun createAuthenticatedDecryptCipher(alias: String, iv: ByteArray): Cipher {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(GCM_TAG_BITS, iv)
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(alias, requireAuthentication = true), spec)
        return cipher
    }

    fun createAuthenticatedEncryptCipher(alias: String): Cipher {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey(alias, requireAuthentication = true))
        return cipher
    }

    companion object {
        const val ALIAS_VAULT_KEY_WRAP = "velora.vault_key_wrap.v1"
        const val ALIAS_DOCUMENT_KEY = "velora.document_key.v1"
        const val ALIAS_OTP_KEY = "velora.otp_key.v1"
    }
}
