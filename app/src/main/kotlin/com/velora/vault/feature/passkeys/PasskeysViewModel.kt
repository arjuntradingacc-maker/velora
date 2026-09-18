package com.velora.vault.feature.passkeys

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.velora.vault.data.local.entity.PasskeyEntity
import com.velora.vault.data.repository.VaultRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class PasskeysViewModel @Inject constructor(
    private val vaultRepository: VaultRepository,
) : ViewModel() {

    val passkeys: StateFlow<List<PasskeyEntity>> = vaultRepository.observePasskeys()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * A real integration calls `androidx.credentials.CredentialManager.createCredential`
     * with a `CreatePublicKeyCredentialRequest` built from a relying party's WebAuthn
     * challenge, then persists the *public* key attestation this returns — never a
     * private key, which stays inside the platform's own passkey provider. Without a
     * live relying party to challenge against, this records the resulting credential
     * reference the same way a completed flow would.
     */
    fun recordCreatedPasskey(relyingPartyName: String, relyingPartyId: String, username: String, credentialRef: String) {
        viewModelScope.launch {
            vaultRepository.savePasskey(
                PasskeyEntity(
                    id = UUID.randomUUID().toString(),
                    relyingPartyName = relyingPartyName,
                    relyingPartyId = relyingPartyId,
                    username = username,
                    credentialId = UUID.randomUUID().toString(),
                    providerCredentialRef = credentialRef,
                    deviceLabel = android.os.Build.MODEL,
                    createdAt = System.currentTimeMillis(),
                ),
            )
        }
    }
}

@HiltViewModel
class PasskeyDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val vaultRepository: VaultRepository,
) : ViewModel() {

    private val passkeyId: String = savedStateHandle["id"] ?: ""

    val passkey: StateFlow<PasskeyEntity?> = vaultRepository.observePasskeys()
        .map { list -> list.firstOrNull { it.id == passkeyId } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun delete(onDeleted: () -> Unit) {
        val current = passkey.value ?: return
        viewModelScope.launch {
            vaultRepository.deletePasskey(current)
            onDeleted()
        }
    }
}
