package com.downlifeblues.velora.feature.identity

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.downlifeblues.velora.data.local.entity.IdentityEntity
import com.downlifeblues.velora.data.repository.VaultRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class IdentityDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val vaultRepository: VaultRepository,
) : ViewModel() {
    private val identityId: String = savedStateHandle["id"] ?: ""

    val identity: StateFlow<IdentityEntity?> = vaultRepository.observeIdentities()
        .map { list -> list.firstOrNull { it.id == identityId } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun delete(onDeleted: () -> Unit) {
        val i = identity.value ?: return
        viewModelScope.launch { vaultRepository.deleteIdentity(i); onDeleted() }
    }
}

data class AddEditIdentityState(
    val id: String? = null,
    val label: String = "",
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val addressLine: String = "",
    val city: String = "",
    val postalCode: String = "",
    val country: String = "",
    val dateOfBirth: String = "",
    val passportNumber: String = "",
    val driverLicenseNumber: String = "",
)

@HiltViewModel
class AddEditIdentityViewModel @Inject constructor(
    private val vaultRepository: VaultRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(AddEditIdentityState())
    val state: StateFlow<AddEditIdentityState> = _state
    private var createdAt = System.currentTimeMillis()

    fun load(identityId: String?) {
        if (identityId == null) return
        viewModelScope.launch {
            val existing = vaultRepository.observeIdentities().map { it.firstOrNull { i -> i.id == identityId } }.first()
            existing?.let {
                createdAt = it.createdAt
                _state.value = AddEditIdentityState(
                    it.id, it.label, it.fullName, it.email.orEmpty(), it.phone.orEmpty(), it.addressLine.orEmpty(),
                    it.city.orEmpty(), it.postalCode.orEmpty(), it.country.orEmpty(), it.dateOfBirth.orEmpty(),
                    it.passportNumber.orEmpty(), it.driverLicenseNumber.orEmpty(),
                )
            }
        }
    }

    fun update(block: (AddEditIdentityState) -> AddEditIdentityState) { _state.value = block(_state.value) }

    fun save(onDone: () -> Unit) {
        val s = _state.value
        if (s.label.isBlank() || s.fullName.isBlank()) return
        viewModelScope.launch {
            vaultRepository.saveIdentity(
                IdentityEntity(
                    id = s.id ?: UUID.randomUUID().toString(),
                    label = s.label.trim(),
                    fullName = s.fullName.trim(),
                    email = s.email.ifBlank { null },
                    phone = s.phone.ifBlank { null },
                    addressLine = s.addressLine.ifBlank { null },
                    city = s.city.ifBlank { null },
                    postalCode = s.postalCode.ifBlank { null },
                    country = s.country.ifBlank { null },
                    dateOfBirth = s.dateOfBirth.ifBlank { null },
                    passportNumber = s.passportNumber.ifBlank { null },
                    driverLicenseNumber = s.driverLicenseNumber.ifBlank { null },
                    createdAt = if (s.id == null) System.currentTimeMillis() else createdAt,
                    updatedAt = System.currentTimeMillis(),
                ),
            )
            onDone()
        }
    }
}
