package com.velora.vault.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.velora.vault.data.repository.AccountProfile
import com.velora.vault.data.repository.AppSettings
import com.velora.vault.data.repository.AppTheme
import com.velora.vault.data.repository.AuthRepository
import com.velora.vault.data.repository.Density
import com.velora.vault.data.repository.SettingsRepository
import com.velora.vault.data.repository.SyncRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val authRepository: AuthRepository,
    private val syncRepository: SyncRepository,
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    val devices = syncRepository.observeDevices()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun account(): AccountProfile? = authRepository.currentAccount()
    fun isBiometricEnabled(): Boolean = authRepository.isBiometricEnabled()
    fun isPinEnabled(): Boolean = authRepository.isPinEnabled()

    fun setTheme(theme: AppTheme) = viewModelScope.launch { settingsRepository.setTheme(theme) }
    fun setDensity(density: Density) = viewModelScope.launch { settingsRepository.setDensity(density) }
    fun setAutoLockSeconds(seconds: Int) = viewModelScope.launch { settingsRepository.setAutoLockSeconds(seconds) }
    fun setLockOnBackground(enabled: Boolean) = viewModelScope.launch { settingsRepository.setLockOnBackground(enabled) }
    fun setClipboardTimeoutSeconds(seconds: Int) = viewModelScope.launch { settingsRepository.setClipboardTimeoutSeconds(seconds) }
    fun setScreenshotProtection(enabled: Boolean) = viewModelScope.launch { settingsRepository.setScreenshotProtection(enabled) }
    fun setSecurityAlertsEnabled(enabled: Boolean) = viewModelScope.launch { settingsRepository.setSecurityAlertsEnabled(enabled) }
    fun setBreachNotificationsEnabled(enabled: Boolean) = viewModelScope.launch { settingsRepository.setBreachNotificationsEnabled(enabled) }
    fun setDocumentExpiryNotificationsEnabled(enabled: Boolean) = viewModelScope.launch { settingsRepository.setDocumentExpiryNotificationsEnabled(enabled) }
    fun setPasswordHealthNotificationsEnabled(enabled: Boolean) = viewModelScope.launch { settingsRepository.setPasswordHealthNotificationsEnabled(enabled) }
    fun setAnalyticsEnabled(enabled: Boolean) = viewModelScope.launch { settingsRepository.setAnalyticsEnabled(enabled) }
    fun setSearchBiometricGateEnabled(enabled: Boolean) = viewModelScope.launch { settingsRepository.setSearchBiometricGateEnabled(enabled) }

    fun enableBiometric() = authRepository.enableBiometric()
    fun disableBiometric() = authRepository.disableBiometric()
    fun setupPin(pin: String) = authRepository.setupPin(pin)
    fun disablePin() = authRepository.disablePin()

    fun manualSync(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            syncRepository.ensureCurrentDeviceRegistered()
            val result = syncRepository.sync(emptyList())
            onResult(result is com.velora.vault.data.repository.SyncOutcome.Success)
        }
    }

    fun logoutOtherDevices() = viewModelScope.launch { syncRepository.logoutOtherDevices() }

    fun logoutAllDevices(onDone: () -> Unit) {
        authRepository.signOutAndWipeLocalVault()
        onDone()
    }

    fun deleteAccount(onDone: () -> Unit) {
        authRepository.signOutAndWipeLocalVault()
        onDone()
    }
}
