package com.velora.vault.feature.itemdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.velora.vault.core.util.GeneratorOptions
import com.velora.vault.core.util.PasswordGenerator
import com.velora.vault.data.local.entity.LoginEntity
import com.velora.vault.data.model.CustomField
import com.velora.vault.data.repository.VaultRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class AddEditPasswordState(
    val id: String? = null,
    val name: String = "",
    val website: String = "",
    val username: String = "",
    val password: String = "",
    val notes: String = "",
    val tags: List<String> = emptyList(),
    val isFavorite: Boolean = false,
    val isLoaded: Boolean = false,
)

@HiltViewModel
class AddEditPasswordViewModel @Inject constructor(
    private val vaultRepository: VaultRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AddEditPasswordState())
    val state: StateFlow<AddEditPasswordState> = _state.asStateFlow()

    private var createdAt: Long = System.currentTimeMillis()
    private var passwordUpdatedAt: Long = System.currentTimeMillis()
    private var existingCustomFields: List<CustomField> = emptyList()

    fun load(loginId: String?) {
        if (loginId == null) {
            _state.value = AddEditPasswordState(isLoaded = true)
            return
        }
        viewModelScope.launch {
            val existing = vaultRepository.getLogin(loginId)
            if (existing != null) {
                createdAt = existing.createdAt
                passwordUpdatedAt = existing.passwordUpdatedAt
                existingCustomFields = existing.customFields
                _state.value = AddEditPasswordState(
                    id = existing.id,
                    name = existing.name,
                    website = existing.websiteUrl.orEmpty(),
                    username = existing.username.orEmpty(),
                    password = existing.password,
                    notes = existing.notes.orEmpty(),
                    tags = existing.tags,
                    isFavorite = existing.isFavorite,
                    isLoaded = true,
                )
            } else {
                _state.value = AddEditPasswordState(isLoaded = true)
            }
        }
    }

    fun updateName(value: String) = update { it.copy(name = value) }
    fun updateWebsite(value: String) = update { it.copy(website = value) }
    fun updateUsername(value: String) = update { it.copy(username = value) }
    fun updatePassword(value: String) = update { it.copy(password = value) }
    fun updateNotes(value: String) = update { it.copy(notes = value) }

    fun generatePassword() {
        val generated = PasswordGenerator.generate(GeneratorOptions())
        passwordUpdatedAt = System.currentTimeMillis()
        update { it.copy(password = generated) }
    }

    private inline fun update(block: (AddEditPasswordState) -> AddEditPasswordState) {
        _state.value = block(_state.value)
    }

    fun save(onDone: () -> Unit) {
        val s = _state.value
        if (s.name.isBlank() || s.password.isBlank()) return
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val entity = LoginEntity(
                id = s.id ?: UUID.randomUUID().toString(),
                name = s.name.trim(),
                websiteUrl = s.website.trim().ifBlank { null },
                username = s.username.trim().ifBlank { null },
                password = s.password,
                notes = s.notes.trim().ifBlank { null },
                tags = s.tags,
                customFields = existingCustomFields,
                isFavorite = s.isFavorite,
                passwordUpdatedAt = passwordUpdatedAt,
                createdAt = if (s.id == null) now else createdAt,
                updatedAt = now,
            )
            vaultRepository.saveLogin(entity)
            onDone()
        }
    }
}
