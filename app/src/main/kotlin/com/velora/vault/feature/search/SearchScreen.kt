package com.velora.vault.feature.search

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.hilt.navigation.compose.hiltViewModel
import com.velora.vault.core.design.Velora
import com.velora.vault.core.design.VeloraRadius
import com.velora.vault.core.design.VeloraSpacing
import com.velora.vault.core.design.VeloraType
import com.velora.vault.core.design.components.PillTone
import com.velora.vault.core.design.components.StatusPill
import com.velora.vault.core.design.components.VaultItemRow
import com.velora.vault.core.design.components.VeloraIconButton
import com.velora.vault.data.model.VaultCategory

@Composable
fun SearchScreen(
    onBack: () -> Unit,
    onOpenItem: (String, String) -> Unit,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val colors = Velora.colors
    val state by viewModel.uiState.collectAsState()
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    androidx.compose.runtime.LaunchedEffect(Unit) { focusRequester.requestFocus() }

    androidx.compose.foundation.layout.Column(
        modifier = Modifier.fillMaxSize().background(colors.background).systemBarsPadding(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = VeloraSpacing.md, vertical = VeloraSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            VeloraIconButton(icon = Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back", onClick = onBack)
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::onQueryChange,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = VeloraSpacing.sm)
                    .focusRequester(focusRequester),
                placeholder = { Text("Search your vault") },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = colors.textSecondary) },
                trailingIcon = {
                    if (state.query.isNotEmpty()) {
                        androidx.compose.material3.IconButton(onClick = { viewModel.onQueryChange("") }) {
                            Icon(Icons.Outlined.Close, contentDescription = "Clear", tint = colors.textSecondary)
                        }
                    }
                },
                singleLine = true,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(VeloraRadius.control),
                textStyle = VeloraType.body,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    viewModel.commitSearch()
                    focusManager.clearFocus()
                }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = colors.surfaceElevated,
                    unfocusedContainerColor = colors.surfaceElevated,
                    focusedBorderColor = colors.accent,
                    unfocusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                    cursorColor = colors.accent,
                ),
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = VeloraSpacing.lg, vertical = VeloraSpacing.xs),
            horizontalArrangement = Arrangement.spacedBy(VeloraSpacing.xs),
        ) {
            VaultCategory.entries.forEach { category ->
                val selected = state.activeCategory == category
                StatusPill(
                    text = category.displayName,
                    tone = if (selected) PillTone.Accent else PillTone.Neutral,
                    modifier = Modifier.then(
                        Modifier.clickableChip { viewModel.onCategorySelected(category) },
                    ),
                )
            }
        }

        when {
            state.query.isBlank() && state.activeCategory == null -> {
                RecentSearchesList(
                    recents = state.recentSearches,
                    onRecentClick = { viewModel.onQueryChange(it); viewModel.commitSearch() },
                    onClear = viewModel::clearRecent,
                )
            }
            state.results.isEmpty() -> {
                com.velora.vault.core.design.components.EmptyState(
                    icon = Icons.Outlined.Search,
                    title = "No matches.",
                    message = "Try a different name, username, or website.",
                )
            }
            else -> {
                LazyColumn(contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = VeloraSpacing.lg, vertical = VeloraSpacing.sm)) {
                    items(state.results, key = { it.id }) { item ->
                        VaultItemRow(
                            icon = item.category.icon,
                            title = item.title,
                            subtitle = item.subtitle,
                            categoryLabel = item.category.displayName,
                            isFavorite = item.isFavorite,
                            onClick = { onOpenItem(item.category.name, item.id) },
                            modifier = Modifier.animateItem(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentSearchesList(recents: List<String>, onRecentClick: (String) -> Unit, onClear: () -> Unit) {
    val colors = Velora.colors
    if (recents.isEmpty()) return
    androidx.compose.foundation.layout.Column(modifier = Modifier.padding(horizontal = VeloraSpacing.lg, vertical = VeloraSpacing.md)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Recent searches", style = VeloraType.label, color = colors.textSecondary)
            Text(
                "Clear",
                style = VeloraType.label,
                color = colors.accent,
                modifier = Modifier.clickableChip(onClear),
            )
        }
        recents.forEach { recent ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickableChip { onRecentClick(recent) }
                    .padding(vertical = VeloraSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Outlined.History, contentDescription = null, tint = colors.textSecondary, modifier = Modifier)
                Text(recent, style = VeloraType.body, color = colors.textPrimary, modifier = Modifier.padding(start = VeloraSpacing.sm))
            }
        }
    }
}

@Composable
private fun Modifier.clickableChip(onClick: () -> Unit): Modifier {
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    return this.then(
        androidx.compose.foundation.clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
    )
}
