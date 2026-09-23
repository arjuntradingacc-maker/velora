package com.downlifeblues.velora.core.security

import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Password/PIN-based key derivation and software-side AES-GCM envelope
 * wrapping. This intentionally uses only standard JCE primitives
 * (PBKDF2WithHmacSHA256, AES/GCM/NoPadding) — no hand-rolled cryptography.
 */
object KeyDerivation {
    const val DEFAULT_ITERATIONS = 210_000
    private const val KEY_LENGTH_BITS = 256
    private const val GCM_TAG_BITS = 128

    fun randomSalt(lengthBytes: Int = 32): ByteArray {
        val salt = ByteArray(lengthBytes)
        SecureRandom().nextBytes(salt)
        return salt
    }

    fun randomKeyBytes(lengthBytes: Int = 32): ByteArray {
        val bytes = ByteArray(lengthBytes)
        SecureRandom().nextBytes(bytes)
        return bytes
    }

    /** Derives a 256-bit key-encryption-key from a password/PIN and salt via PBKDF2. */
    fun deriveKeyEncryptionKey(
        secret: CharArray,
        salt: ByteArray,
        iterations: Int = DEFAULT_ITERATIONS,
    ): SecretKeySpec {
        val spec = PBEKeySpec(secret, salt, iterations, KEY_LENGTH_BITS)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val keyBytes = factory.generateSecret(spec).encoded
        spec.clearPassword()
        return SecretKeySpec(keyBytes, "AES")
    }

    /** Wraps [payload] (e.g. the raw vault key) with [kek] using AES-GCM. Returns iv || ciphertext, base64-ready via caller. */
    fun wrap(kek: SecretKeySpec, payload: ByteArray): EncryptedPayload {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, kek)
        val ciphertext = cipher.doFinal(payload)
        return EncryptedPayload(cipher.iv, ciphertext)
    }

    fun unwrap(kek: SecretKeySpec, payload: EncryptedPayload): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, kek, GCMParameterSpec(GCM_TAG_BITS, payload.iv))
        return cipher.doFinal(payload.ciphertext)
    }
}
