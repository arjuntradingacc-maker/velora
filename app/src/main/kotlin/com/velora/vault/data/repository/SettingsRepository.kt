package com.velora.vault.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.velora.vault.core.security.ClipboardTimeoutManager
import com.velora.vault.core.security.VaultSessionManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

enum class AppTheme { LIGHT, DARK, SYSTEM }
enum class Density { COMFORTABLE, COMPACT }

private val Context.dataStore by preferencesDataStore(name = "velora_settings")

data class AppSettings(
    val theme: AppTheme = AppTheme.SYSTEM,
    val density: Density = Density.COMFORTABLE,
    val autoLockSeconds: Int = 30,
    val lockOnBackground: Boolean = true,
    val clipboardTimeoutSeconds: Int = 30,
    val screenshotProtection: Boolean = true,
    val biometricUnlockEnabled: Boolean = false,
    val securityAlertsEnabled: Boolean = true,
    val breachNotificationsEnabled: Boolean = true,
    val documentExpiryNotificationsEnabled: Boolean = true,
    val passwordHealthNotificationsEnabled: Boolean = true,
    val analyticsEnabled: Boolean = false,
    val searchBiometricGateEnabled: Boolean = true,
)

/** All non-secret user preferences, backed by Jetpack DataStore. */
@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sessionManager: VaultSessionManager,
    private val clipboardTimeoutManager: ClipboardTimeoutManager,
) {
    private object Keys {
        val THEME = stringPreferencesKey("theme")
        val DENSITY = stringPreferencesKey("density")
        val AUTO_LOCK_SECONDS = intPreferencesKey("auto_lock_seconds")
        val LOCK_ON_BACKGROUND = booleanPreferencesKey("lock_on_background")
        val CLIPBOARD_TIMEOUT_SECONDS = intPreferencesKey("clipboard_timeout_seconds")
        val SCREENSHOT_PROTECTION = booleanPreferencesKey("screenshot_protection")
        val BIOMETRIC_UNLOCK_ENABLED = booleanPreferencesKey("biometric_unlock_enabled")
        val SECURITY_ALERTS = booleanPreferencesKey("security_alerts")
        val BREACH_NOTIFICATIONS = booleanPreferencesKey("breach_notifications")
        val DOCUMENT_EXPIRY_NOTIFICATIONS = booleanPreferencesKey("document_expiry_notifications")
        val PASSWORD_HEALTH_NOTIFICATIONS = booleanPreferencesKey("password_health_notifications")
        val ANALYTICS_ENABLED = booleanPreferencesKey("analytics_enabled")
        val SEARCH_BIOMETRIC_GATE = booleanPreferencesKey("search_biometric_gate")
    }

    val settings: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        val parsed = AppSettings(
            theme = prefs[Keys.THEME]?.let { runCatching { AppTheme.valueOf(it) }.getOrNull() } ?: AppTheme.SYSTEM,
            density = prefs[Keys.DENSITY]?.let { runCatching { Density.valueOf(it) }.getOrNull() } ?: Density.COMFORTABLE,
            autoLockSeconds = prefs[Keys.AUTO_LOCK_SECONDS] ?: 30,
            lockOnBackground = prefs[Keys.LOCK_ON_BACKGROUND] ?: true,
            clipboardTimeoutSeconds = prefs[Keys.CLIPBOARD_TIMEOUT_SECONDS] ?: 30,
            screenshotProtection = prefs[Keys.SCREENSHOT_PROTECTION] ?: true,
            biometricUnlockEnabled = prefs[Keys.BIOMETRIC_UNLOCK_ENABLED] ?: false,
            securityAlertsEnabled = prefs[Keys.SECURITY_ALERTS] ?: true,
            breachNotificationsEnabled = prefs[Keys.BREACH_NOTIFICATIONS] ?: true,
            documentExpiryNotificationsEnabled = prefs[Keys.DOCUMENT_EXPIRY_NOTIFICATIONS] ?: true,
            passwordHealthNotificationsEnabled = prefs[Keys.PASSWORD_HEALTH_NOTIFICATIONS] ?: true,
            analyticsEnabled = prefs[Keys.ANALYTICS_ENABLED] ?: false,
            searchBiometricGateEnabled = prefs[Keys.SEARCH_BIOMETRIC_GATE] ?: true,
        )
        applyToRuntime(parsed)
        parsed
    }

    /** Keeps the live session/clipboard managers in sync with persisted settings on every read. */
    private fun applyToRuntime(settings: AppSettings) {
        sessionManager.autoLockSeconds = settings.autoLockSeconds
        sessionManager.lockImmediatelyOnBackground = settings.lockOnBackground
        clipboardTimeoutManager.timeoutSeconds = settings.clipboardTimeoutSeconds
    }

    suspend fun setTheme(theme: AppTheme) = context.dataStore.edit { it[Keys.THEME] = theme.name }
    suspend fun setDensity(density: Density) = context.dataStore.edit { it[Keys.DENSITY] = density.name }
    suspend fun setAutoLockSeconds(seconds: Int) = context.dataStore.edit { it[Keys.AUTO_LOCK_SECONDS] = seconds }
    suspend fun setLockOnBackground(enabled: Boolean) = context.dataStore.edit { it[Keys.LOCK_ON_BACKGROUND] = enabled }
    suspend fun setClipboardTimeoutSeconds(seconds: Int) =
        context.dataStore.edit { it[Keys.CLIPBOARD_TIMEOUT_SECONDS] = seconds }
    suspend fun setScreenshotProtection(enabled: Boolean) =
        context.dataStore.edit { it[Keys.SCREENSHOT_PROTECTION] = enabled }
    suspend fun setBiometricUnlockEnabled(enabled: Boolean) =
        context.dataStore.edit { it[Keys.BIOMETRIC_UNLOCK_ENABLED] = enabled }
    suspend fun setSecurityAlertsEnabled(enabled: Boolean) = context.dataStore.edit { it[Keys.SECURITY_ALERTS] = enabled }
    suspend fun setBreachNotificationsEnabled(enabled: Boolean) =
        context.dataStore.edit { it[Keys.BREACH_NOTIFICATIONS] = enabled }
    suspend fun setDocumentExpiryNotificationsEnabled(enabled: Boolean) =
        context.dataStore.edit { it[Keys.DOCUMENT_EXPIRY_NOTIFICATIONS] = enabled }
    suspend fun setPasswordHealthNotificationsEnabled(enabled: Boolean) =
        context.dataStore.edit { it[Keys.PASSWORD_HEALTH_NOTIFICATIONS] = enabled }
    suspend fun setAnalyticsEnabled(enabled: Boolean) = context.dataStore.edit { it[Keys.ANALYTICS_ENABLED] = enabled }
    suspend fun setSearchBiometricGateEnabled(enabled: Boolean) =
        context.dataStore.edit { it[Keys.SEARCH_BIOMETRIC_GATE] = enabled }
}
