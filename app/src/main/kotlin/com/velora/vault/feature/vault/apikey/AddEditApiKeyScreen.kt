package com.velora.vault.feature.vault.apikey

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
import com.velora.vault.core.design.Velora
import com.velora.vault.core.design.VeloraSpacing
import com.velora.vault.core.design.components.VeloraDetailTopBar
import com.velora.vault.core.design.components.VeloraIconButton
import com.velora.vault.core.design.components.VeloraPasswordField
import com.velora.vault.core.design.components.VeloraPrimaryButton
import com.velora.vault.core.design.components.VeloraTextField

@Composable
fun AddEditApiKeyScreen(
    apiKeyId: String?,
    onDone: () -> Unit,
    onBack: () -> Unit,
    viewModel: AddEditApiKeyViewModel = hiltViewModel(),
) {
    val colors = Velora.colors
    val state by viewModel.state.collectAsState()
    LaunchedEffect(apiKeyId) { viewModel.load(apiKeyId) }

    Column(modifier = Modifier.fillMaxSize().background(colors.background).systemBarsPadding().verticalScroll(rememberScrollState())) {
        VeloraDetailTopBar(
            title = if (apiKeyId == null) "Add API key" else "API key",
            onBack = onBack,
            actions = {
                if (apiKeyId != null) {
                    VeloraIconButton(icon = Icons.Outlined.Delete, contentDescription = "Delete", onClick = { viewModel.delete(onBack) })
                }
            },
        )
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = VeloraSpacing.xl)) {
            VeloraTextField(state.serviceName, { v -> viewModel.update { it.copy(serviceName = v) } }, "Service name", modifier = Modifier.padding(bottom = VeloraSpacing.md))
            VeloraTextField(state.keyLabel, { v -> viewModel.update { it.copy(keyLabel = v) } }, "Key label", modifier = Modifier.padding(bottom = VeloraSpacing.md))
            VeloraPasswordField(state.keyValue, { v -> viewModel.update { it.copy(keyValue = v) } }, "Key value")
            Spacer(Modifier.height(VeloraSpacing.md))
            VeloraTextField(
                state.notes, { v -> viewModel.update { it.copy(notes = v) } }, "Notes",
                singleLine = false, minLines = 3,
                modifier = Modifier.padding(bottom = VeloraSpacing.xl),
            )
            VeloraPrimaryButton(text = "Save", onClick = { viewModel.save(onDone) }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(VeloraSpacing.xxl))
        }
    }
}
