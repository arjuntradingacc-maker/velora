package com.velora.vault.feature.vault.custom

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.velora.vault.data.local.entity.CustomItemEntity
import com.velora.vault.data.model.CustomField
import com.velora.vault.data.repository.VaultRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class CustomItemFormState(
    val id: String? = null,
    val title: String = "",
    val categoryLabel: String = "Custom",
    val details: String = "",
)

@HiltViewModel
class AddEditCustomItemViewModel @Inject constructor(
    private val vaultRepository: VaultRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(CustomItemFormState())
    val state: StateFlow<CustomItemFormState> = _state
    private var createdAt = System.currentTimeMillis()

    fun load(itemId: String?) {
        if (itemId == null) return
        viewModelScope.launch {
            val existing = vaultRepository.observeCustomItems().map { it.firstOrNull { c -> c.id == itemId } }.first()
            existing?.let {
                createdAt = it.createdAt
                _state.value = CustomItemFormState(it.id, it.title, it.categoryLabel, it.fields.firstOrNull()?.value.orEmpty())
            }
        }
    }

    fun update(block: (CustomItemFormState) -> CustomItemFormState) { _state.value = block(_state.value) }

    fun save(onDone: () -> Unit) {
        val s = _state.value
        if (s.title.isBlank()) return
        viewModelScope.launch {
            vaultRepository.saveCustomItem(
                CustomItemEntity(
                    id = s.id ?: UUID.randomUUID().toString(),
                    title = s.title.trim(),
                    categoryLabel = s.categoryLabel.ifBlank { "Custom" },
                    fields = if (s.details.isBlank()) emptyList() else listOf(CustomField("Details", s.details)),
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
            val existing = vaultRepository.observeCustomItems().map { it.firstOrNull { c -> c.id == id } }.first()
            existing?.let { vaultRepository.deleteCustomItem(it) }
            onDeleted()
        }
    }
}
