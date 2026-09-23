package com.downlifeblues.velora.feature.documents

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.downlifeblues.velora.data.local.entity.DocumentEntity
import com.downlifeblues.velora.data.local.entity.DocumentType
import com.downlifeblues.velora.data.repository.DocumentStorageRepository
import com.downlifeblues.velora.data.repository.VaultRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class DocumentsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val vaultRepository: VaultRepository,
    private val documentStorageRepository: DocumentStorageRepository,
) : ViewModel() {

    val documents: StateFlow<List<DocumentEntity>> = vaultRepository.observeDocuments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun importDocument(uri: android.net.Uri, displayName: String) {
        viewModelScope.launch {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return@launch
            val encryptedPath = documentStorageRepository.storeDocument(bytes)
            val type = when {
                displayName.endsWith(".pdf", true) -> DocumentType.PDF
                displayName.endsWith(".png", true) || displayName.endsWith(".jpg", true) || displayName.endsWith(".jpeg", true) -> DocumentType.IMAGE
                displayName.endsWith(".txt", true) -> DocumentType.TEXT
                else -> DocumentType.OTHER
            }
            vaultRepository.saveDocument(
                DocumentEntity(
                    id = UUID.randomUUID().toString(),
                    name = displayName,
                    type = type,
                    encryptedFilePath = encryptedPath,
                    sizeBytes = bytes.size.toLong(),
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                ),
            )
        }
    }
}

@HiltViewModel
class DocumentDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val vaultRepository: VaultRepository,
    private val documentStorageRepository: DocumentStorageRepository,
) : ViewModel() {
    private val documentId: String = savedStateHandle["id"] ?: ""

    val document: StateFlow<DocumentEntity?> = vaultRepository.observeDocuments()
        .map { list -> list.firstOrNull { it.id == documentId } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun delete(onDeleted: () -> Unit) {
        val d = document.value ?: return
        viewModelScope.launch {
            documentStorageRepository.deleteDocument(d.encryptedFilePath)
            vaultRepository.deleteDocument(d)
            onDeleted()
        }
    }
}
