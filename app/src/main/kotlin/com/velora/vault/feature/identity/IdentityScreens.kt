package com.velora.vault.feature.identity

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
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.velora.vault.core.design.Velora
import com.velora.vault.core.design.VeloraSpacing
import com.velora.vault.core.design.components.VeloraDetailTopBar
import com.velora.vault.core.design.components.VeloraIconButton
import com.velora.vault.core.design.components.VeloraPrimaryButton
import com.velora.vault.core.design.components.VeloraSurfaceCard
import com.velora.vault.core.design.components.VeloraTextField
import com.velora.vault.core.security.ScreenshotProtected

@Composable
fun IdentityDetailScreen(
    identityId: String,
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    viewModel: IdentityDetailViewModel = hiltViewModel(),
) {
    ScreenshotProtected()
    val colors = Velora.colors
    val identity by viewModel.identity.collectAsState()
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val current = identity

    Column(modifier = Modifier.fillMaxSize().background(colors.background).systemBarsPadding().verticalScroll(rememberScrollState())) {
        VeloraDetailTopBar(
            title = current?.label ?: "Identity",
            onBack = onBack,
            actions = {
                VeloraIconButton(icon = Icons.Outlined.Edit, contentDescription = "Edit", onClick = { onEdit(identityId) })
                VeloraIconButton(icon = Icons.Outlined.Delete, contentDescription = "Delete", onClick = { showDeleteConfirm = true })
            },
        )
        if (current != null) {
            VeloraSurfaceCard(modifier = Modifier.fillMaxWidth().padding(horizontal = VeloraSpacing.xl)) {
                InfoLine("Full name", current.fullName)
                current.email?.let { InfoLine("Email", it) }
                current.phone?.let { InfoLine("Phone", it) }
                current.addressLine?.let { InfoLine("Address", it) }
                current.city?.let { InfoLine("City", it) }
                current.postalCode?.let { InfoLine("Postal code", it) }
                current.country?.let { InfoLine("Country", it) }
                current.dateOfBirth?.let { InfoLine("Date of birth", it) }
                current.passportNumber?.let { InfoLine("Passport number", it) }
                current.driverLicenseNumber?.let { InfoLine("Driver license number", it) }
            }
        }
    }

    if (showDeleteConfirm) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete this identity?") },
            confirmButton = { androidx.compose.material3.TextButton(onClick = { viewModel.delete(onBack) }) { Text("Delete", color = colors.critical) } },
            dismissButton = { androidx.compose.material3.TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    val colors = Velora.colors
    Column(modifier = Modifier.padding(vertical = VeloraSpacing.xs)) {
        Text(label, style = com.velora.vault.core.design.VeloraType.label, color = colors.textSecondary)
        Text(value, style = com.velora.vault.core.design.VeloraType.body, color = colors.textPrimary)
    }
}

@Composable
fun AddEditIdentityScreen(
    identityId: String?,
    onDone: () -> Unit,
    onBack: () -> Unit,
    viewModel: AddEditIdentityViewModel = hiltViewModel(),
) {
    val colors = Velora.colors
    val state by viewModel.state.collectAsState()
    LaunchedEffect(identityId) { viewModel.load(identityId) }

    Column(modifier = Modifier.fillMaxSize().background(colors.background).systemBarsPadding().verticalScroll(rememberScrollState())) {
        VeloraDetailTopBar(title = if (identityId == null) "Add identity" else "Edit identity", onBack = onBack)
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = VeloraSpacing.xl)) {
            val fields = listOf(
                "Label" to state.label to { v: String -> viewModel.update { it.copy(label = v) } },
                "Full name" to state.fullName to { v: String -> viewModel.update { it.copy(fullName = v) } },
                "Email" to state.email to { v: String -> viewModel.update { it.copy(email = v) } },
                "Phone" to state.phone to { v: String -> viewModel.update { it.copy(phone = v) } },
                "Address" to state.addressLine to { v: String -> viewModel.update { it.copy(addressLine = v) } },
                "City" to state.city to { v: String -> viewModel.update { it.copy(city = v) } },
                "Postal code" to state.postalCode to { v: String -> viewModel.update { it.copy(postalCode = v) } },
                "Country" to state.country to { v: String -> viewModel.update { it.copy(country = v) } },
                "Date of birth" to state.dateOfBirth to { v: String -> viewModel.update { it.copy(dateOfBirth = v) } },
                "Passport number" to state.passportNumber to { v: String -> viewModel.update { it.copy(passportNumber = v) } },
                "Driver license number" to state.driverLicenseNumber to { v: String -> viewModel.update { it.copy(driverLicenseNumber = v) } },
            )
            fields.forEach { (pair, onChange) ->
                val (label, value) = pair
                VeloraTextField(value = value, onValueChange = onChange, label = label, modifier = Modifier.padding(bottom = VeloraSpacing.md))
            }
            VeloraPrimaryButton(text = "Save", onClick = { viewModel.save(onDone) }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(VeloraSpacing.xxl))
        }
    }
}
