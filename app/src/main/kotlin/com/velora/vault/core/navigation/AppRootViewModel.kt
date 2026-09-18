package com.velora.vault.core.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.velora.vault.core.security.VaultSession
import com.velora.vault.core.security.VaultSessionManager
import com.velora.vault.data.repository.AuthRepository
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
) : ViewModel() {

    val state: StateFlow<AppRootState> = sessionManager.session.map { session ->
        when {
            !authRepository.isOnboardingCompleted() -> AppRootState.NeedsOnboarding
            !authRepository.isVaultProvisioned() -> AppRootState.NeedsAccountSetup
            session !is VaultSession.Unlocked -> AppRootState.NeedsUnlock
            else -> AppRootState.Ready
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, AppRootState.Loading)
}
