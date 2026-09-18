package com.velora.vault.feature.notes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.velora.vault.data.local.entity.NoteFormat
import com.velora.vault.data.local.entity.SecureNoteEntity
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
class NoteDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val vaultRepository: VaultRepository,
) : ViewModel() {
    private val noteId: String = savedStateHandle["id"] ?: ""

    val note: StateFlow<SecureNoteEntity?> = vaultRepository.observeNotes()
        .map { list -> list.firstOrNull { it.id == noteId } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun toggleFavorite() {
        val n = note.value ?: return
        viewModelScope.launch { vaultRepository.saveNote(n.copy(isFavorite = !n.isFavorite, updatedAt = System.currentTimeMillis())) }
    }

    fun delete(onDeleted: () -> Unit) {
        val n = note.value ?: return
        viewModelScope.launch { vaultRepository.deleteNote(n); onDeleted() }
    }
}

data class AddEditNoteState(
    val id: String? = null,
    val title: String = "",
    val body: String = "",
    val format: NoteFormat = NoteFormat.PLAIN,
    val isLoaded: Boolean = false,
)

@HiltViewModel
class AddEditNoteViewModel @Inject constructor(
    private val vaultRepository: VaultRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(AddEditNoteState())
    val state: StateFlow<AddEditNoteState> = _state

    private var createdAt = System.currentTimeMillis()

    fun load(noteId: String?) {
        if (noteId == null) {
            _state.value = AddEditNoteState(isLoaded = true)
            return
        }
        viewModelScope.launch {
            val note = vaultRepository.observeNotes().map { list -> list.firstOrNull { n -> n.id == noteId } }.first()
            if (note != null) {
                createdAt = note.createdAt
                _state.value = AddEditNoteState(note.id, note.title, note.body, note.format, isLoaded = true)
            } else {
                _state.value = AddEditNoteState(isLoaded = true)
            }
        }
    }

    fun updateTitle(value: String) { _state.value = _state.value.copy(title = value) }
    fun updateBody(value: String) { _state.value = _state.value.copy(body = value) }

    fun save(onDone: () -> Unit) {
        val s = _state.value
        if (s.title.isBlank()) return
        viewModelScope.launch {
            vaultRepository.saveNote(
                SecureNoteEntity(
                    id = s.id ?: UUID.randomUUID().toString(),
                    title = s.title.trim(),
                    body = s.body,
                    format = s.format,
                    createdAt = if (s.id == null) System.currentTimeMillis() else createdAt,
                    updatedAt = System.currentTimeMillis(),
                ),
            )
            onDone()
        }
    }
}
