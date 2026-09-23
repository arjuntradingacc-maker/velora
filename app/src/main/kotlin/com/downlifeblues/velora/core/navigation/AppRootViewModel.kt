package com.downlifeblues.velora.core.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.downlifeblues.velora.core.security.VaultSession
import com.downlifeblues.velora.core.security.VaultSessionManager
import com.downlifeblues.velora.data.repository.AppTheme
import com.downlifeblues.velora.data.repository.AuthRepository
import com.downlifeblues.velora.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

sealed interface AppRootState {
    data object Loading : AppRootState
    data object NeedsOnboarding : AppRootState
    data object NeedsAccountSetup : AppRootState
    data object NeedsUnlock : AppRootState
    data object Ready : AppRootState
}

@HiltViewModel
class AppRootViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    sessionManager: VaultSessionManager,
    settingsRepository: SettingsRepository,
) : ViewModel() {

    val state: StateFlow<AppRootState> = sessionManager.session.map { session ->
        when {
            !authRepository.isOnboardingCompleted() -> AppRootState.NeedsOnboarding
            !authRepository.isVaultProvisioned() -> AppRootState.NeedsAccountSetup
            session !is VaultSession.Unlocked -> AppRootState.NeedsUnlock
            else -> AppRootState.Ready
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, AppRootState.Loading)

    /** Drives [com.downlifeblues.velora.core.design.VeloraTheme] — the one place the persisted Appearance choice actually takes effect. */
    val themePreference: StateFlow<AppTheme> = settingsRepository.settings
        .map { it.theme }
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppTheme.SYSTEM)
}
