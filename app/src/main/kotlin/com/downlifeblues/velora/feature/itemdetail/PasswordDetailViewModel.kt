package com.downlifeblues.velora.feature.itemdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.downlifeblues.velora.core.security.ClipboardTimeoutManager
import com.downlifeblues.velora.data.local.entity.LoginEntity
import com.downlifeblues.velora.data.repository.VaultRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PasswordDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val vaultRepository: VaultRepository,
    private val clipboardTimeoutManager: ClipboardTimeoutManager,
) : ViewModel() {

    private val loginId: String = savedStateHandle["id"] ?: ""

    val login: StateFlow<LoginEntity?> = vaultRepository.observeLogin(loginId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allLogins: StateFlow<List<LoginEntity>> = vaultRepository.observeLogins()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch { vaultRepository.markLoginUsed(loginId) }
    }

    fun copyUsername() {
        login.value?.username?.let { clipboardTimeoutManager.copy("Username", it) }
    }

    fun copyPassword() {
        login.value?.password?.let { clipboardTimeoutManager.copy("Password", it) }
    }

    fun toggleFavorite() {
        val current = login.value ?: return
        viewModelScope.launch { vaultRepository.setLoginFavorite(current.id, !current.isFavorite) }
    }

    fun delete(onDeleted: () -> Unit) {
        val current = login.value ?: return
        viewModelScope.launch {
            vaultRepository.deleteLogin(current)
            onDeleted()
        }
    }
}
