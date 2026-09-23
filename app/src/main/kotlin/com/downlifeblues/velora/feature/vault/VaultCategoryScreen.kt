package com.downlifeblues.velora.feature.vault

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.downlifeblues.velora.core.design.Velora
import com.downlifeblues.velora.core.design.VeloraSpacing
import com.downlifeblues.velora.core.design.components.EmptyState
import com.downlifeblues.velora.core.design.components.VaultItemRow
import com.downlifeblues.velora.core.design.components.VeloraDetailTopBar
import com.downlifeblues.velora.core.design.components.VeloraIconButton

@Composable
fun VaultCategoryScreen(
    categoryName: String,
    onBack: () -> Unit,
    onOpenItem: (String) -> Unit,
    onAddItem: () -> Unit,
    viewModel: VaultCategoryViewModel = hiltViewModel(),
) {
    val colors = Velora.colors
    val items by viewModel.items.collectAsState()

    androidx.compose.foundation.layout.Column(
        modifier = Modifier.fillMaxSize().background(colors.background).systemBarsPadding(),
    ) {
        VeloraDetailTopBar(
            title = viewModel.category.displayName,
            onBack = onBack,
            actions = { VeloraIconButton(icon = Icons.Outlined.Add, contentDescription = "Add", onClick = onAddItem) },
        )

        if (items.isEmpty()) {
            EmptyState(
                icon = viewModel.category.icon,
                title = "No ${viewModel.category.displayName.lowercase()} yet.",
                message = "Add your first one and it'll show up here.",
                action = {
                    com.downlifeblues.velora.core.design.components.VeloraPrimaryButton(text = "Add ${viewModel.category.displayName.removeSuffix("s")}", onClick = onAddItem)
                },
            )
        } else {
            LazyColumn(contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = VeloraSpacing.lg, vertical = VeloraSpacing.sm)) {
                items(items, key = { it.id }) { item ->
                    VaultItemRow(
                        icon = item.category.icon,
                        title = item.title,
                        subtitle = item.subtitle,
                        categoryLabel = item.category.displayName,
                        isFavorite = item.isFavorite,
                        onClick = { onOpenItem(item.id) },
                    )
                }
            }
        }
    }
}
