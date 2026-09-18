package com.velora.vault.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.velora.vault.data.model.VaultCategory
import com.velora.vault.data.model.VaultItemSummary
import com.velora.vault.data.repository.VaultRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val activeCategory: VaultCategory? = null,
    val results: List<VaultItemSummary> = emptyList(),
    val recentSearches: List<String> = emptyList(),
    val isSearching: Boolean = false,
)

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val vaultRepository: VaultRepository,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val activeCategory = MutableStateFlow<VaultCategory?>(null)
    private val recentSearches = MutableStateFlow<List<String>>(emptyList())
    private val allItems = vaultRepository.observeAllSummaries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val uiState: StateFlow<SearchUiState> = combine(
        query,
        query.debounce(180).distinctUntilChanged(),
        activeCategory,
        allItems,
        recentSearches,
    ) { rawQuery, debounced, category, items, recents ->
        val filtered = items.filter { item ->
            (category == null || item.category == category) &&
                (debounced.isBlank() || item.title.contains(debounced, ignoreCase = true) || item.subtitle.contains(debounced, ignoreCase = true))
        }
        SearchUiState(
            query = rawQuery,
            activeCategory = category,
            results = if (debounced.isBlank() && category == null) emptyList() else filtered,
            recentSearches = recents,
            isSearching = rawQuery != debounced,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SearchUiState())

    fun onQueryChange(value: String) {
        query.value = value
    }

    fun onCategorySelected(category: VaultCategory?) {
        activeCategory.value = if (activeCategory.value == category) null else category
    }

    fun commitSearch() {
        val trimmed = query.value.trim()
        if (trimmed.isBlank()) return
        viewModelScope.launch {
            recentSearches.value = (listOf(trimmed) + recentSearches.value.filterNot { it.equals(trimmed, true) }).take(6)
        }
    }

    fun clearRecent() {
        recentSearches.value = emptyList()
    }
}
