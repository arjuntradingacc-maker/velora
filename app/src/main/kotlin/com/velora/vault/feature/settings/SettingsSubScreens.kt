package com.velora.vault.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.velora.vault.core.design.Velora
import com.velora.vault.core.design.VeloraSpacing
import com.velora.vault.core.design.VeloraType
import com.velora.vault.core.design.components.SectionHeader
import com.velora.vault.core.design.components.SettingsSwitchRow
import com.velora.vault.core.design.components.VeloraDetailTopBar
import com.velora.vault.core.design.components.VeloraPrimaryButton
import com.velora.vault.core.design.components.VeloraSecondaryButton
import com.velora.vault.core.design.components.VeloraSurfaceCard
import com.velora.vault.core.design.components.VeloraTextField
import com.velora.vault.core.security.BiometricAuthManager
import com.velora.vault.core.security.BiometricResult
import com.velora.vault.data.repository.AppTheme
import com.velora.vault.data.repository.Density

@Composable
fun SettingsSecurityScreen(onBack: () -> Unit, viewModel: SettingsViewModel = hiltViewModel()) {
    val colors = Velora.colors
    val settings by viewModel.settings.collectAsState()
    val context = LocalContext.current
    val biometricManager = remember { BiometricAuthManager() }
    var showPinSheet by remember { mutableStateOf(false) }
    var biometricEnabled by remember { mutableStateOf(viewModel.isBiometricEnabled()) }
    var pinEnabled by remember { mutableStateOf(viewModel.isPinEnabled()) }

    Column(modifier = Modifier.fillMaxSize().background(colors.background).systemBarsPadding().verticalScroll(rememberScrollState())) {
        VeloraDetailTopBar(title = "Security", onBack = onBack)
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = VeloraSpacing.xl)) {
            VeloraSurfaceCard(modifier = Modifier.fillMaxWidth()) {
                SettingsSwitchRow(
                    title = "Biometric unlock",
                    checked = biometricEnabled,
                    onCheckedChange = { enabled ->
                        if (enabled) {
                            (context as? FragmentActivity)?.let { activity ->
                                biometricManager.authenticate(activity, title = "Confirm it's you") { result ->
                                    if (result is BiometricResult.Success) {
                                        viewModel.enableBiometric()
                                        biometricEnabled = true
                                    }
                                }
                            }
                        } else {
                            viewModel.disableBiometric()
                            biometricEnabled = false
                        }
                    },
                )
                SettingsSwitchRow(
                    title = "PIN unlock",
                    checked = pinEnabled,
                    onCheckedChange = { enabled ->
                        if (enabled) {
                            showPinSheet = true
                        } else {
                            viewModel.disablePin()
                            pinEnabled = false
                        }
                    },
                )
                SettingsSwitchRow(
                    title = "Lock immediately in background",
                    checked = settings.lockOnBackground,
                    onCheckedChange = viewModel::setLockOnBackground,
                )
                SettingsSwitchRow(
                    title = "Screenshot protection",
                    subtitle = "Blocks screenshots on sensitive screens",
                    checked = settings.screenshotProtection,
                    onCheckedChange = viewModel::setScreenshotProtection,
                )
                SettingsSwitchRow(
                    title = "Biometric gate for search results",
                    checked = settings.searchBiometricGateEnabled,
                    onCheckedChange = viewModel::setSearchBiometricGateEnabled,
                )
            }
            androidx.compose.foundation.layout.Spacer(Modifier.height(VeloraSpacing.lg))
            Text("Auto-lock after ${settings.autoLockSeconds}s of inactivity", style = VeloraType.body, color = colors.textPrimary)
            Slider(
                value = settings.autoLockSeconds.toFloat(),
                onValueChange = { viewModel.setAutoLockSeconds(it.toInt()) },
                valueRange = 0f..300f,
            )
            androidx.compose.foundation.layout.Spacer(Modifier.height(VeloraSpacing.md))
            Text("Clear clipboard after ${settings.clipboardTimeoutSeconds}s", style = VeloraType.body, color = colors.textPrimary)
            Slider(
                value = settings.clipboardTimeoutSeconds.toFloat(),
                onValueChange = { viewModel.setClipboardTimeoutSeconds(it.toInt()) },
                valueRange = 0f..120f,
            )
            androidx.compose.foundation.layout.Spacer(Modifier.height(VeloraSpacing.xxxl))
        }
    }

    if (showPinSheet) {
        PinSetupSheet(
            onDismiss = { showPinSheet = false },
            onSet = { pin -> viewModel.setupPin(pin); pinEnabled = true; showPinSheet = false },
        )
    }
}

@Composable
private fun PinSetupSheet(onDismiss: () -> Unit, onSet: (String) -> Unit) {
    var pin by remember { mutableStateOf("") }
    val colors = Velora.colors
    androidx.compose.material3.ModalBottomSheet(onDismissRequest = onDismiss, containerColor = colors.surface) {
        Column(modifier = Modifier.fillMaxWidth().padding(VeloraSpacing.xl)) {
            Text("Set a PIN", style = VeloraType.title, color = colors.textPrimary)
            androidx.compose.foundation.layout.Spacer(Modifier.height(VeloraSpacing.md))
            VeloraTextField(
                value = pin,
                onValueChange = { pin = it.filter(Char::isDigit).take(8) },
                label = "PIN",
                keyboardType = androidx.compose.ui.text.input.KeyboardType.NumberPassword,
                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
            )
            androidx.compose.foundation.layout.Spacer(Modifier.height(VeloraSpacing.lg))
            VeloraPrimaryButton(text = "Set PIN", onClick = { if (pin.length >= 4) onSet(pin) }, modifier = Modifier.fillMaxWidth())
            androidx.compose.foundation.layout.Spacer(Modifier.height(VeloraSpacing.lg))
        }
    }
}

@Composable
fun SettingsVaultScreen(onBack: () -> Unit) {
    val colors = Velora.colors
    Column(modifier = Modifier.fillMaxSize().background(colors.background).systemBarsPadding()) {
        VeloraDetailTopBar(title = "Vault", onBack = onBack)
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = VeloraSpacing.xl)) {
            Text(
                "Categories, tags, and favorites are managed directly from each item and category screen throughout the vault.",
                style = VeloraType.body,
                color = colors.textSecondary,
            )
        }
    }
}

@Composable
fun SettingsSyncScreen(onBack: () -> Unit, viewModel: SettingsViewModel = hiltViewModel()) {
    val colors = Velora.colors
    val devices by viewModel.devices.collectAsState()
    var syncing by remember { mutableStateOf(false) }
    var lastSyncMessage by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize().background(colors.background).systemBarsPadding().verticalScroll(rememberScrollState())) {
        VeloraDetailTopBar(title = "Sync", onBack = onBack)
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = VeloraSpacing.xl)) {
            VeloraPrimaryButton(
                text = if (syncing) "Syncing…" else "Sync now",
                onClick = {
                    syncing = true
                    viewModel.manualSync { success ->
                        syncing = false
                        lastSyncMessage = if (success) "Synced just now." else "Couldn't reach the sync service. Your vault is still safe locally."
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )
            lastSyncMessage?.let {
                Text(it, style = VeloraType.bodySmall, color = colors.textSecondary, modifier = Modifier.padding(top = VeloraSpacing.sm))
            }
            androidx.compose.foundation.layout.Spacer(Modifier.height(VeloraSpacing.lg))
            SectionHeader(title = "Devices")
            androidx.compose.foundation.layout.Spacer(Modifier.height(VeloraSpacing.sm))
            devices.forEach { device ->
                VeloraSurfaceCard(modifier = Modifier.fillMaxWidth().padding(bottom = VeloraSpacing.sm)) {
                    Text(device.deviceName, style = VeloraType.titleSmall, color = colors.textPrimary)
                    Text(device.platform, style = VeloraType.bodySmall, color = colors.textSecondary)
                }
            }
            androidx.compose.foundation.layout.Spacer(Modifier.height(VeloraSpacing.md))
            VeloraSecondaryButton(text = "Log out other devices", onClick = viewModel::logoutOtherDevices, modifier = Modifier.fillMaxWidth())
            androidx.compose.foundation.layout.Spacer(Modifier.height(VeloraSpacing.xxxl))
        }
    }
}

@Composable
fun SettingsAppearanceScreen(onBack: () -> Unit, viewModel: SettingsViewModel = hiltViewModel()) {
    val colors = Velora.colors
    val settings by viewModel.settings.collectAsState()
    Column(modifier = Modifier.fillMaxSize().background(colors.background).systemBarsPadding()) {
        VeloraDetailTopBar(title = "Appearance", onBack = onBack)
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = VeloraSpacing.xl)) {
            SectionHeader(title = "Theme")
            androidx.compose.foundation.layout.Spacer(Modifier.height(VeloraSpacing.sm))
            listOf(AppTheme.SYSTEM to "System", AppTheme.LIGHT to "Light", AppTheme.DARK to "Dark").forEach { (theme, label) ->
                SettingsSwitchRow(title = label, checked = settings.theme == theme, onCheckedChange = { if (it) viewModel.setTheme(theme) })
            }
            androidx.compose.foundation.layout.Spacer(Modifier.height(VeloraSpacing.lg))
            SectionHeader(title = "Density")
            androidx.compose.foundation.layout.Spacer(Modifier.height(VeloraSpacing.sm))
            listOf(Density.COMFORTABLE to "Comfortable", Density.COMPACT to "Compact").forEach { (density, label) ->
                SettingsSwitchRow(title = label, checked = settings.density == density, onCheckedChange = { if (it) viewModel.setDensity(density) })
            }
        }
    }
}

@Composable
fun SettingsNotificationsScreen(onBack: () -> Unit, viewModel: SettingsViewModel = hiltViewModel()) {
    val colors = Velora.colors
    val settings by viewModel.settings.collectAsState()
    Column(modifier = Modifier.fillMaxSize().background(colors.background).systemBarsPadding()) {
        VeloraDetailTopBar(title = "Notifications", onBack = onBack)
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = VeloraSpacing.xl)) {
            SettingsSwitchRow(title = "Security alerts", checked = settings.securityAlertsEnabled, onCheckedChange = viewModel::setSecurityAlertsEnabled)
            SettingsSwitchRow(title = "Breach notifications", checked = settings.breachNotificationsEnabled, onCheckedChange = viewModel::setBreachNotificationsEnabled)
            SettingsSwitchRow(title = "Document expiration", checked = settings.documentExpiryNotificationsEnabled, onCheckedChange = viewModel::setDocumentExpiryNotificationsEnabled)
            SettingsSwitchRow(title = "Password health", checked = settings.passwordHealthNotificationsEnabled, onCheckedChange = viewModel::setPasswordHealthNotificationsEnabled)
        }
    }
}

@Composable
fun SettingsPrivacyScreen(
    onBack: () -> Unit,
    onAccountDeleted: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val colors = Velora.colors
    val settings by viewModel.settings.collectAsState()
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().background(colors.background).systemBarsPadding()) {
        VeloraDetailTopBar(title = "Privacy", onBack = onBack)
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = VeloraSpacing.xl)) {
            SettingsSwitchRow(
                title = "Share anonymous usage analytics",
                subtitle = "Never includes vault contents, passwords, or item names",
                checked = settings.analyticsEnabled,
                onCheckedChange = viewModel::setAnalyticsEnabled,
            )
            androidx.compose.foundation.layout.Spacer(Modifier.height(VeloraSpacing.xl))
            Text(
                "Deleting your account permanently erases your local vault and all key material on this device. This cannot be undone.",
                style = VeloraType.bodySmall,
                color = colors.textSecondary,
            )
            androidx.compose.foundation.layout.Spacer(Modifier.height(VeloraSpacing.sm))
            VeloraSecondaryButton(text = "Delete account", onClick = { showDeleteConfirm = true }, modifier = Modifier.fillMaxWidth())
        }
    }

    if (showDeleteConfirm) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete your account?") },
            text = { Text("This erases your vault on this device permanently.") },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { viewModel.deleteAccount(onAccountDeleted) }) { Text("Delete", color = colors.critical) }
            },
            dismissButton = { androidx.compose.material3.TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") } },
        )
    }
}
