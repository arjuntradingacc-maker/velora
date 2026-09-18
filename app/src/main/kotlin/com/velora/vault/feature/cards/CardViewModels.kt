package com.velora.vault.feature.cards

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.velora.vault.data.local.entity.CardNetwork
import com.velora.vault.data.local.entity.PaymentCardEntity
import com.velora.vault.data.repository.VaultRepository
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
class CardDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val vaultRepository: VaultRepository,
) : ViewModel() {
    private val cardId: String = savedStateHandle["id"] ?: ""

    val card: StateFlow<PaymentCardEntity?> = vaultRepository.observeCards()
        .map { list -> list.firstOrNull { it.id == cardId } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun delete(onDeleted: () -> Unit) {
        val c = card.value ?: return
        viewModelScope.launch { vaultRepository.deleteCard(c); onDeleted() }
    }
}

data class AddEditCardState(
    val id: String? = null,
    val nickname: String = "",
    val cardholderName: String = "",
    val network: CardNetwork = CardNetwork.OTHER,
    val fullNumber: String = "",
    val expiryMonth: String = "",
    val expiryYear: String = "",
    val cvv: String = "",
    val notes: String = "",
)

@HiltViewModel
class AddEditCardViewModel @Inject constructor(
    private val vaultRepository: VaultRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(AddEditCardState())
    val state: StateFlow<AddEditCardState> = _state
    private var createdAt = System.currentTimeMillis()

    fun load(cardId: String?) {
        if (cardId == null) return
        viewModelScope.launch {
            val existing = vaultRepository.observeCards().map { it.firstOrNull { c -> c.id == cardId } }.first()
            existing?.let {
                createdAt = it.createdAt
                _state.value = AddEditCardState(
                    it.id, it.nickname, it.cardholderName, it.network, it.fullNumber,
                    it.expiryMonth.toString(), it.expiryYear.toString(), it.cvv, it.notes.orEmpty(),
                )
            }
        }
    }

    fun update(block: (AddEditCardState) -> AddEditCardState) { _state.value = block(_state.value) }

    fun save(onDone: () -> Unit) {
        val s = _state.value
        if (s.nickname.isBlank() || s.fullNumber.isBlank()) return
        viewModelScope.launch {
            vaultRepository.saveCard(
                PaymentCardEntity(
                    id = s.id ?: UUID.randomUUID().toString(),
                    nickname = s.nickname.trim(),
                    cardholderName = s.cardholderName.trim(),
                    network = s.network,
                    fullNumber = s.fullNumber.filter { it.isDigit() },
                    expiryMonth = s.expiryMonth.toIntOrNull() ?: 1,
                    expiryYear = s.expiryYear.toIntOrNull() ?: (2020 + 5),
                    cvv = s.cvv,
                    notes = s.notes.ifBlank { null },
                    createdAt = if (s.id == null) System.currentTimeMillis() else createdAt,
                    updatedAt = System.currentTimeMillis(),
                ),
            )
            onDone()
        }
    }
}
