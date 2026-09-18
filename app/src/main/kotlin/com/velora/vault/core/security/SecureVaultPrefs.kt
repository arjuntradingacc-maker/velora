package com.velora.vault.core.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Small metadata store (salts, wrapped key envelopes, iteration counts) —
 * never raw secrets. Backed by [EncryptedSharedPreferences] so even this
 * metadata is AES-256 encrypted at rest, satisfying "no plaintext secrets
 * in SharedPreferences" for defense in depth even though none of these
 * values are usable without the user's master password/biometric/PIN.
 */
@Singleton
class SecureVaultPrefs @Inject constructor(@ApplicationContext context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "velora_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    fun putString(key: String, value: String?) = prefs.edit().putString(key, value).apply()
    fun getString(key: String): String? = prefs.getString(key, null)
    fun putInt(key: String, value: Int) = prefs.edit().putInt(key, value).apply()
    fun getInt(key: String, default: Int) = prefs.getInt(key, default)
    fun putBoolean(key: String, value: Boolean) = prefs.edit().putBoolean(key, value).apply()
    fun getBoolean(key: String, default: Boolean) = prefs.getBoolean(key, default)
    fun remove(key: String) = prefs.edit().remove(key).apply()
    fun clearAll() = prefs.edit().clear().apply()

    object Keys {
        const val MASTER_SALT = "master_salt"
        const val MASTER_ITERATIONS = "master_iterations"
        const val WRAPPED_VAULT_KEY_PASSWORD = "wrapped_vault_key_password"
        const val WRAPPED_VAULT_KEY_BIOMETRIC = "wrapped_vault_key_biometric"
        const val WRAPPED_VAULT_KEY_BIOMETRIC_IV = "wrapped_vault_key_biometric_iv"
        const val WRAPPED_VAULT_KEY_PIN = "wrapped_vault_key_pin"
        const val PIN_SALT = "pin_salt"
        const val WRAPPED_VAULT_KEY_RECOVERY = "wrapped_vault_key_recovery"
        const val RECOVERY_SALT = "recovery_salt"
        const val BIOMETRIC_ENABLED = "biometric_enabled"
        const val VAULT_PROVISIONED = "vault_provisioned"
        const val ONBOARDING_COMPLETED = "onboarding_completed"
        const val AUTO_LOCK_SECONDS = "auto_lock_seconds"
        const val LOCK_ON_BACKGROUND = "lock_on_background"
    }
}
