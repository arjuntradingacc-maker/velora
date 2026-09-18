package com.velora.vault.core.security

import javax.inject.Inject
import javax.inject.Singleton

/** Encrypts TOTP seeds at rest with their own Keystore key, independent of the SQLCipher database passphrase. */
@Singleton
class OtpSecretCrypto @Inject constructor(private val keystoreCrypto: KeystoreCryptoManager) {

    fun encryptSecret(base32Secret: String): String =
        keystoreCrypto.encrypt(KeystoreCryptoManager.ALIAS_OTP_KEY, base32Secret.toByteArray(Charsets.UTF_8))
            .toStoredString()

    fun decryptSecret(stored: String): String =
        String(
            keystoreCrypto.decrypt(KeystoreCryptoManager.ALIAS_OTP_KEY, EncryptedPayload.fromStoredString(stored)),
            Charsets.UTF_8,
        )
}
