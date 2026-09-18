package com.velora.vault.feature.itemdetail

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.velora.vault.core.design.Velora
import com.velora.vault.core.design.VeloraSpacing
import com.velora.vault.core.design.components.StrengthBar
import com.velora.vault.core.design.components.VeloraDetailTopBar
import com.velora.vault.core.design.components.VeloraPasswordField
import com.velora.vault.core.design.components.VeloraPrimaryButton
import com.velora.vault.core.design.components.VeloraSecondaryButton
import com.velora.vault.core.design.components.VeloraTextField
import com.velora.vault.core.util.PasswordStrength

@Composable
fun AddEditPasswordScreen(
    loginId: String?,
    onDone: () -> Unit,
    onBack: () -> Unit,
    viewModel: AddEditPasswordViewModel = hiltViewModel(),
) {
    val colors = Velora.colors
    val state by viewModel.state.collectAsState()

    LaunchedEffect(loginId) { viewModel.load(loginId) }

    val strength = remember(state.password) { PasswordStrength.evaluate(state.password) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .systemBarsPadding()
            .verticalScroll(rememberScrollState()),
    ) {
        VeloraDetailTopBar(title = if (loginId == null) "Add password" else "Edit password", onBack = onBack)

        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = VeloraSpacing.xl)) {
            VeloraTextField(value = state.name, onValueChange = viewModel::updateName, label = "Name", modifier = Modifier.padding(bottom = VeloraSpacing.md))
            VeloraTextField(
                value = state.website,
                onValueChange = viewModel::updateWebsite,
                label = "Website",
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Uri,
                modifier = Modifier.padding(bottom = VeloraSpacing.md),
            )
            VeloraTextField(
                value = state.username,
                onValueChange = viewModel::updateUsername,
                label = "Username or email",
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Email,
                modifier = Modifier.padding(bottom = VeloraSpacing.md),
            )
            VeloraPasswordField(value = state.password, onValueChange = viewModel::updatePassword, label = "Password")
            if (state.password.isNotEmpty()) {
                StrengthBar(score = strength.score, label = strength.label, modifier = Modifier.padding(top = VeloraSpacing.sm))
            }
            Spacer(Modifier.height(VeloraSpacing.sm))
            VeloraSecondaryButton(
                text = "Generate password",
                onClick = viewModel::generatePassword,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(VeloraSpacing.md))
            VeloraTextField(
                value = state.notes,
                onValueChange = viewModel::updateNotes,
                label = "Notes",
                singleLine = false,
                minLines = 3,
                modifier = Modifier.padding(bottom = VeloraSpacing.xxxl),
            )
            VeloraPrimaryButton(text = "Save", onClick = { viewModel.save(onDone) }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(VeloraSpacing.xxl))
        }
    }
}
