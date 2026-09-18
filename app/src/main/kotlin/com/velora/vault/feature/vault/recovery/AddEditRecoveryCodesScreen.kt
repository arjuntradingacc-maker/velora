package com.velora.vault.feature.vault.recovery

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.velora.vault.core.design.Velora
import com.velora.vault.core.design.VeloraSpacing
import com.velora.vault.core.design.VeloraType
import com.velora.vault.core.design.components.VeloraDetailTopBar
import com.velora.vault.core.design.components.VeloraIconButton
import com.velora.vault.core.design.components.VeloraPrimaryButton
import com.velora.vault.core.design.components.VeloraTextField

@Composable
fun AddEditRecoveryCodesScreen(
    entryId: String?,
    onDone: () -> Unit,
    onBack: () -> Unit,
    viewModel: AddEditRecoveryCodesViewModel = hiltViewModel(),
) {
    val colors = Velora.colors
    val state by viewModel.state.collectAsState()
    LaunchedEffect(entryId) { viewModel.load(entryId) }

    Column(modifier = Modifier.fillMaxSize().background(colors.background).systemBarsPadding().verticalScroll(rememberScrollState())) {
        VeloraDetailTopBar(
            title = if (entryId == null) "Add recovery codes" else "Recovery codes",
            onBack = onBack,
            actions = {
                if (entryId != null) {
                    VeloraIconButton(icon = Icons.Outlined.Delete, contentDescription = "Delete", onClick = { viewModel.delete(onBack) })
                }
            },
        )
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = VeloraSpacing.xl)) {
            VeloraTextField(
                value = state.serviceName,
                onValueChange = { v -> viewModel.update { it.copy(serviceName = v) } },
                label = "Service",
                modifier = Modifier.padding(bottom = VeloraSpacing.md),
            )
            Text("One code per line", style = VeloraType.label, color = colors.textSecondary, modifier = Modifier.padding(bottom = VeloraSpacing.xs))
            VeloraTextField(
                value = state.codesText,
                onValueChange = { v -> viewModel.update { it.copy(codesText = v) } },
                label = "Codes",
                singleLine = false,
                minLines = 8,
                modifier = Modifier.padding(bottom = VeloraSpacing.xl),
            )
            VeloraPrimaryButton(text = "Save", onClick = { viewModel.save(onDone) }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(VeloraSpacing.xxl))
        }
    }
}
