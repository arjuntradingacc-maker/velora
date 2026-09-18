package com.velora.vault.feature.vault

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.velora.vault.data.model.VaultCategory
import com.velora.vault.data.model.VaultItemSummary
import com.velora.vault.data.repository.VaultRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class VaultCategoryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    vaultRepository: VaultRepository,
) : ViewModel() {

    val category: VaultCategory = savedStateHandle.get<String>("category")
        ?.let { name -> runCatching { VaultCategory.valueOf(name) }.getOrNull() }
        ?: VaultCategory.CUSTOM

    val items: StateFlow<List<VaultItemSummary>> = vaultRepository.observeAllSummaries()
        .map { all -> all.filter { it.category == category }.sortedByDescending { it.updatedAt } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
