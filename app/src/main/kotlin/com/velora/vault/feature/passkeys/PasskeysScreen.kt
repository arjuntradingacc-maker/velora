package com.velora.vault.feature.passkeys

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.velora.vault.core.design.Velora
import com.velora.vault.core.design.VeloraSpacing
import com.velora.vault.core.design.VeloraType
import com.velora.vault.core.design.components.EmptyState
import com.velora.vault.core.design.components.VaultItemRow
import com.velora.vault.core.design.components.VeloraDetailTopBar
import com.velora.vault.core.design.components.VeloraIconButton
import com.velora.vault.core.design.components.VeloraTextField
import com.velora.vault.core.design.icons.VeloraIcons
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PasskeysScreen(
    onBack: () -> Unit,
    onOpenPasskey: (String) -> Unit,
    viewModel: PasskeysViewModel = hiltViewModel(),
) {
    val colors = Velora.colors
    val passkeys by viewModel.passkeys.collectAsState()
    var showCreateSheet by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().background(colors.background).systemBarsPadding()) {
        VeloraDetailTopBar(
            title = "Passkeys",
            onBack = onBack,
            actions = { VeloraIconButton(icon = Icons.Outlined.Add, contentDescription = "Create passkey", onClick = { showCreateSheet = true }) },
        )

        if (passkeys.isEmpty()) {
            EmptyState(
                icon = VeloraIcons.Passkeys,
                title = "No passkeys yet.",
                message = "Create a passkey for a supported app or site to sign in without a password.",
                action = { com.velora.vault.core.design.components.VeloraPrimaryButton(text = "Create passkey", onClick = { showCreateSheet = true }) },
            )
        } else {
            LazyColumn(contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = VeloraSpacing.lg, vertical = VeloraSpacing.sm)) {
                items(passkeys, key = { it.id }) { passkey ->
                    VaultItemRow(
                        icon = VeloraIcons.Passkeys,
                        title = passkey.relyingPartyName,
                        subtitle = passkey.username,
                        categoryLabel = "Passkey",
                        isFavorite = passkey.isFavorite,
                        onClick = { onOpenPasskey(passkey.id) },
                    )
                }
            }
        }
    }

    if (showCreateSheet) {
        CreatePasskeySheet(
            onDismiss = { showCreateSheet = false },
            onCreate = { rpName, rpId, username ->
                viewModel.recordCreatedPasskey(rpName, rpId, username, credentialRef = "local-provider-ref")
                showCreateSheet = false
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreatePasskeySheet(onDismiss: () -> Unit, onCreate: (String, String, String) -> Unit) {
    var service by remember { mutableStateOf("") }
    var domain by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    val colors = Velora.colors

    androidx.compose.material3.ModalBottomSheet(onDismissRequest = onDismiss, containerColor = colors.surface) {
        Column(modifier = Modifier.fillMaxWidth().padding(VeloraSpacing.xl)) {
            Text("Create a passkey", style = VeloraType.title, color = colors.textPrimary)
            Text(
                "Uses Android's built-in passkey provider — the private key never leaves your device's secure hardware.",
                style = VeloraType.bodySmall,
                color = colors.textSecondary,
                modifier = Modifier.padding(top = VeloraSpacing.xs, bottom = VeloraSpacing.lg),
            )
            VeloraTextField(value = service, onValueChange = { service = it }, label = "Service name", modifier = Modifier.padding(bottom = VeloraSpacing.md))
            VeloraTextField(value = domain, onValueChange = { domain = it }, label = "Domain", modifier = Modifier.padding(bottom = VeloraSpacing.md))
            VeloraTextField(value = username, onValueChange = { username = it }, label = "Username", modifier = Modifier.padding(bottom = VeloraSpacing.lg))
            com.velora.vault.core.design.components.VeloraPrimaryButton(
                text = "Create",
                onClick = { if (service.isNotBlank() && username.isNotBlank()) onCreate(service, domain.ifBlank { service.lowercase() }, username) },
                modifier = Modifier.fillMaxWidth(),
            )
            androidx.compose.foundation.layout.Spacer(Modifier.height(VeloraSpacing.lg))
        }
    }
}

@Composable
fun PasskeyDetailScreen(
    passkeyId: String,
    onBack: () -> Unit,
    viewModel: PasskeyDetailViewModel = hiltViewModel(),
) {
    val colors = Velora.colors
    val passkey by viewModel.passkey.collectAsState()
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val current = passkey

    Column(modifier = Modifier.fillMaxSize().background(colors.background).systemBarsPadding()) {
        VeloraDetailTopBar(
            title = current?.relyingPartyName ?: "Passkey",
            onBack = onBack,
            actions = {
                VeloraIconButton(
                    icon = Icons.Outlined.Delete,
                    contentDescription = "Delete",
                    onClick = { showDeleteConfirm = true },
                )
            },
        )
        if (current != null) {
            Column(modifier = Modifier.padding(horizontal = VeloraSpacing.xl)) {
                DetailRow("Service", current.relyingPartyName)
                DetailRow("Username", current.username)
                DetailRow("Device", current.deviceLabel)
                DetailRow("Created", SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(current.createdAt)))
                DetailRow("Last used", current.lastUsedAt?.let { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(it)) } ?: "Never")
            }
        }
    }

    if (showDeleteConfirm) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete this passkey?") },
            text = { Text("You'll need to sign in another way on this service afterward.") },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { viewModel.delete(onBack) }) { Text("Delete", color = colors.critical) }
            },
            dismissButton = { androidx.compose.material3.TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    val colors = Velora.colors
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = VeloraSpacing.sm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = VeloraType.bodySmall, color = colors.textSecondary)
        Text(value, style = VeloraType.bodySmall, color = colors.textPrimary)
    }
}
