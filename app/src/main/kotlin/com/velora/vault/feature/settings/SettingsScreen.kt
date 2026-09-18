package com.velora.vault.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudQueue
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.velora.vault.core.design.Velora
import com.velora.vault.core.design.VeloraSpacing
import com.velora.vault.core.design.VeloraType
import com.velora.vault.core.design.components.SectionHeader
import com.velora.vault.core.design.components.SettingsNavRow
import com.velora.vault.core.design.components.VeloraSurfaceCard
import com.velora.vault.core.design.components.VeloraTextButton

@Composable
fun SettingsScreen(
    onOpenSecurity: () -> Unit,
    onOpenVault: () -> Unit,
    onOpenSync: () -> Unit,
    onOpenAppearance: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onLoggedOut: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val colors = Velora.colors
    val account = viewModel.account()
    var showLogoutConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().background(colors.background).systemBarsPadding().verticalScroll(rememberScrollState()),
    ) {
        Text("Settings", style = VeloraType.headline, color = colors.textPrimary, modifier = Modifier.padding(horizontal = VeloraSpacing.xl, vertical = VeloraSpacing.md))

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = VeloraSpacing.xl, vertical = VeloraSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(52.dp).clip(CircleShape).background(colors.accent),
                contentAlignment = Alignment.Center,
            ) {
                Text((account?.displayName?.firstOrNull() ?: 'V').uppercaseChar().toString(), style = VeloraType.title, color = colors.onAccent)
            }
            Column(modifier = Modifier.padding(start = VeloraSpacing.md)) {
                Text(account?.displayName ?: "Velora user", style = VeloraType.titleSmall, color = colors.textPrimary)
                Text(account?.email ?: "", style = VeloraType.bodySmall, color = colors.textSecondary)
            }
        }

        Column(modifier = Modifier.padding(horizontal = VeloraSpacing.xl, vertical = VeloraSpacing.md)) {
            SectionHeader(title = "General")
            VeloraSurfaceCard(modifier = Modifier.fillMaxWidth().padding(top = VeloraSpacing.sm)) {
                SettingsNavRow(title = "Security", subtitle = "Biometrics, auto-lock, PIN", onClick = onOpenSecurity, leadingIcon = Icons.Outlined.Lock)
                SettingsNavRow(title = "Vault", subtitle = "Categories, favorites, defaults", onClick = onOpenVault, leadingIcon = Icons.Outlined.Tune)
                SettingsNavRow(title = "Sync", subtitle = "Devices, backup, manual sync", onClick = onOpenSync, leadingIcon = Icons.Outlined.CloudQueue)
                SettingsNavRow(title = "Appearance", subtitle = "Theme, accent, density", onClick = onOpenAppearance, leadingIcon = Icons.Outlined.ColorLens)
                SettingsNavRow(title = "Notifications", subtitle = "Security and health alerts", onClick = onOpenNotifications, leadingIcon = Icons.Outlined.Notifications)
                SettingsNavRow(title = "Privacy", subtitle = "Analytics, export, delete account", onClick = onOpenPrivacy, leadingIcon = Icons.Outlined.PrivacyTip)
            }
            androidx.compose.foundation.layout.Spacer(Modifier.height(VeloraSpacing.xl))
            VeloraTextButton(text = "Log out of all devices", onClick = { showLogoutConfirm = true }, color = colors.critical)
            androidx.compose.foundation.layout.Spacer(Modifier.height(VeloraSpacing.xxxl))
        }
    }

    if (showLogoutConfirm) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = { Text("Log out of all devices?") },
            text = { Text("This clears your local vault key material on this device. You'll need your master password or recovery code to get back in.") },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { viewModel.logoutAllDevices(onLoggedOut) }) { Text("Log out", color = colors.critical) }
            },
            dismissButton = { androidx.compose.material3.TextButton(onClick = { showLogoutConfirm = false }) { Text("Cancel") } },
        )
    }
}
