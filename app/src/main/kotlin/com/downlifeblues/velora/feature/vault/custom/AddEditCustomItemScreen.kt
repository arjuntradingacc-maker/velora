package com.downlifeblues.velora.feature.vault.custom

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.downlifeblues.velora.core.design.Velora
import com.downlifeblues.velora.core.design.VeloraSpacing
import com.downlifeblues.velora.core.design.components.VeloraDetailTopBar
import com.downlifeblues.velora.core.design.components.VeloraIconButton
import com.downlifeblues.velora.core.design.components.VeloraPrimaryButton
import com.downlifeblues.velora.core.design.components.VeloraTextField

@Composable
fun AddEditCustomItemScreen(
    itemId: String?,
    onDone: () -> Unit,
    onBack: () -> Unit,
    viewModel: AddEditCustomItemViewModel = hiltViewModel(),
) {
    val colors = Velora.colors
    val state by viewModel.state.collectAsState()
    LaunchedEffect(itemId) { viewModel.load(itemId) }

    Column(modifier = Modifier.fillMaxSize().background(colors.background).systemBarsPadding().verticalScroll(rememberScrollState())) {
        VeloraDetailTopBar(
            title = if (itemId == null) "Add custom item" else "Custom item",
            onBack = onBack,
            actions = {
                if (itemId != null) {
                    VeloraIconButton(icon = Icons.Outlined.Delete, contentDescription = "Delete", onClick = { viewModel.delete(onBack) })
                }
            },
        )
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = VeloraSpacing.xl)) {
            VeloraTextField(state.title, { v -> viewModel.update { it.copy(title = v) } }, "Title", modifier = Modifier.padding(bottom = VeloraSpacing.md))
            VeloraTextField(state.categoryLabel, { v -> viewModel.update { it.copy(categoryLabel = v) } }, "Category label", modifier = Modifier.padding(bottom = VeloraSpacing.md))
            VeloraTextField(
                state.details, { v -> viewModel.update { it.copy(details = v) } }, "Details",
                singleLine = false, minLines = 6,
                modifier = Modifier.padding(bottom = VeloraSpacing.xl),
            )
            VeloraPrimaryButton(text = "Save", onClick = { viewModel.save(onDone) }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(VeloraSpacing.xxl))
        }
    }
}
