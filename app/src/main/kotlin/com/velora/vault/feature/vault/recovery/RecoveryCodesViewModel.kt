package com.velora.vault.feature.vault.recovery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.velora.vault.data.local.entity.RecoveryCodeEntity
import com.velora.vault.data.repository.VaultRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class RecoveryCodesFormState(
    val id: String? = null,
    val serviceName: String = "",
    val codesText: String = "",
)

@HiltViewModel
class AddEditRecoveryCodesViewModel @Inject constructor(
    private val vaultRepository: VaultRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(RecoveryCodesFormState())
    val state: StateFlow<RecoveryCodesFormState> = _state
    private var createdAt = System.currentTimeMillis()
    private var usedCodes: List<String> = emptyList()

    fun load(entryId: String?) {
        if (entryId == null) return
        viewModelScope.launch {
            val existing = vaultRepository.observeRecoveryCodes().map { it.firstOrNull { c -> c.id == entryId } }.first()
            existing?.let {
                createdAt = it.createdAt
                usedCodes = it.usedCodes
                _state.value = RecoveryCodesFormState(it.id, it.serviceName, it.codes.joinToString("\n"))
            }
        }
    }

    fun update(block: (RecoveryCodesFormState) -> RecoveryCodesFormState) { _state.value = block(_state.value) }

    fun save(onDone: () -> Unit) {
        val s = _state.value
        val codes = s.codesText.lines().map { it.trim() }.filter { it.isNotBlank() }
        if (s.serviceName.isBlank() || codes.isEmpty()) return
        viewModelScope.launch {
            vaultRepository.saveRecoveryCodes(
                RecoveryCodeEntity(
                    id = s.id ?: UUID.randomUUID().toString(),
                    serviceName = s.serviceName.trim(),
                    codes = codes,
                    usedCodes = usedCodes,
                    createdAt = if (s.id == null) System.currentTimeMillis() else createdAt,
                ),
            )
            onDone()
        }
    }

    fun delete(onDeleted: () -> Unit) {
        val id = _state.value.id ?: return
        viewModelScope.launch {
            val existing = vaultRepository.observeRecoveryCodes().map { it.firstOrNull { c -> c.id == id } }.first()
            existing?.let { vaultRepository.deleteRecoveryCodes(it) }
            onDeleted()
        }
    }
}
