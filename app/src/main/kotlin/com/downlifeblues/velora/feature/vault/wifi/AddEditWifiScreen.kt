package com.downlifeblues.velora.feature.vault.wifi

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
import com.downlifeblues.velora.core.design.components.VeloraPasswordField
import com.downlifeblues.velora.core.design.components.VeloraPrimaryButton
import com.downlifeblues.velora.core.design.components.VeloraTextField

@Composable
fun AddEditWifiScreen(
    wifiId: String?,
    onDone: () -> Unit,
    onBack: () -> Unit,
    viewModel: AddEditWifiViewModel = hiltViewModel(),
) {
    val colors = Velora.colors
    val state by viewModel.state.collectAsState()
    LaunchedEffect(wifiId) { viewModel.load(wifiId) }

    Column(modifier = Modifier.fillMaxSize().background(colors.background).systemBarsPadding().verticalScroll(rememberScrollState())) {
        VeloraDetailTopBar(
            title = if (wifiId == null) "Add Wi-Fi" else "Wi-Fi network",
            onBack = onBack,
            actions = {
                if (wifiId != null) {
                    VeloraIconButton(icon = Icons.Outlined.Delete, contentDescription = "Delete", onClick = { viewModel.delete(onBack) })
                }
            },
        )
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = VeloraSpacing.xl)) {
            VeloraTextField(value = state.ssid, onValueChange = { v -> viewModel.update { it.copy(ssid = v) } }, label = "Network name (SSID)", modifier = Modifier.padding(bottom = VeloraSpacing.md))
            VeloraPasswordField(value = state.password, onValueChange = { v -> viewModel.update { it.copy(password = v) } }, label = "Password")
            Spacer(Modifier.height(VeloraSpacing.md))
            VeloraTextField(value = state.security, onValueChange = { v -> viewModel.update { it.copy(security = v) } }, label = "Security type", modifier = Modifier.padding(bottom = VeloraSpacing.md))
            VeloraTextField(
                value = state.notes,
                onValueChange = { v -> viewModel.update { it.copy(notes = v) } },
                label = "Notes",
                singleLine = false,
                minLines = 3,
                modifier = Modifier.padding(bottom = VeloraSpacing.xl),
            )
            VeloraPrimaryButton(text = "Save", onClick = { viewModel.save(onDone) }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(VeloraSpacing.xxl))
        }
    }
}
