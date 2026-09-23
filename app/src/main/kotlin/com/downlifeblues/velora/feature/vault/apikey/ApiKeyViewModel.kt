package com.downlifeblues.velora.feature.vault.apikey

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.downlifeblues.velora.data.local.entity.ApiKeyEntity
import com.downlifeblues.velora.data.repository.VaultRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class ApiKeyFormState(
    val id: String? = null,
    val serviceName: String = "",
    val keyLabel: String = "",
    val keyValue: String = "",
    val notes: String = "",
)

@HiltViewModel
class AddEditApiKeyViewModel @Inject constructor(
    private val vaultRepository: VaultRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(ApiKeyFormState())
    val state: StateFlow<ApiKeyFormState> = _state
    private var createdAt = System.currentTimeMillis()

    fun load(apiKeyId: String?) {
        if (apiKeyId == null) return
        viewModelScope.launch {
            val existing = vaultRepository.observeApiKeys().map { it.firstOrNull { k -> k.id == apiKeyId } }.first()
            existing?.let {
                createdAt = it.createdAt
                _state.value = ApiKeyFormState(it.id, it.serviceName, it.keyLabel, it.keyValue, it.notes.orEmpty())
            }
        }
    }

    fun update(block: (ApiKeyFormState) -> ApiKeyFormState) { _state.value = block(_state.value) }

    fun save(onDone: () -> Unit) {
        val s = _state.value
        if (s.serviceName.isBlank() || s.keyValue.isBlank()) return
        viewModelScope.launch {
            vaultRepository.saveApiKey(
                ApiKeyEntity(
                    id = s.id ?: UUID.randomUUID().toString(),
                    serviceName = s.serviceName.trim(),
                    keyLabel = s.keyLabel.trim(),
                    keyValue = s.keyValue.trim(),
                    notes = s.notes.ifBlank { null },
                    createdAt = if (s.id == null) System.currentTimeMillis() else createdAt,
                    updatedAt = System.currentTimeMillis(),
                ),
            )
            onDone()
        }
    }

    fun delete(onDeleted: () -> Unit) {
        val id = _state.value.id ?: return
        viewModelScope.launch {
            val existing = vaultRepository.observeApiKeys().map { it.firstOrNull { k -> k.id == id } }.first()
            existing?.let { vaultRepository.deleteApiKey(it) }
            onDeleted()
        }
    }
}
