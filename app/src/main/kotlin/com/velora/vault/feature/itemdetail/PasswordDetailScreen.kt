package com.velora.vault.feature.itemdetail

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
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.velora.vault.core.design.Velora
import com.velora.vault.core.design.VeloraSpacing
import com.velora.vault.core.design.VeloraType
import com.velora.vault.core.design.components.CopyRow
import com.velora.vault.core.design.components.PillTone
import com.velora.vault.core.design.components.StatusPill
import com.velora.vault.core.design.components.StrengthBar
import com.velora.vault.core.design.components.VeloraDetailTopBar
import com.velora.vault.core.design.components.VeloraIconButton
import com.velora.vault.core.design.components.VeloraSurfaceCard
import com.velora.vault.core.security.ScreenshotProtected
import com.velora.vault.core.util.PasswordStrength
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun PasswordDetailScreen(
    loginId: String,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDeleted: () -> Unit,
    viewModel: PasswordDetailViewModel = hiltViewModel(),
) {
    ScreenshotProtected()
    val colors = Velora.colors
    val login by viewModel.login.collectAsState()
    val allLogins by viewModel.allLogins.collectAsState()
    var revealed by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val current = login
    if (current == null) {
        Box(modifier = Modifier.fillMaxSize().background(colors.background)) {}
        return
    }

    val strength = remember(current.password) { PasswordStrength.evaluate(current.password) }
    val isReused = remember(current.password, allLogins) {
        current.password.isNotBlank() && allLogins.count { it.password == current.password } > 1
    }
    val ageDays = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - current.passwordUpdatedAt)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .systemBarsPadding()
            .verticalScroll(rememberScrollState()),
    ) {
        VeloraDetailTopBar(
            title = "",
            onBack = onBack,
            actions = {
                VeloraIconButton(
                    icon = if (current.isFavorite) Icons.Outlined.Star else Icons.Outlined.StarBorder,
                    contentDescription = "Favorite",
                    tint = if (current.isFavorite) colors.accent else colors.textSecondary,
                    onClick = viewModel::toggleFavorite,
                )
                VeloraIconButton(icon = Icons.Outlined.Edit, contentDescription = "Edit", onClick = onEdit)
                VeloraIconButton(icon = Icons.Outlined.Delete, contentDescription = "Delete", onClick = { showDeleteConfirm = true })
            },
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(top = VeloraSpacing.md)) {
            Box(
                modifier = Modifier.size(72.dp).clip(CircleShape).background(colors.surfaceElevated),
                contentAlignment = Alignment.Center,
            ) {
                Text(current.name.take(1).uppercase(), style = VeloraType.headline, color = colors.accent)
            }
            Text(current.name, style = VeloraType.headline, color = colors.textPrimary, modifier = Modifier.padding(top = VeloraSpacing.md))
            if (!current.username.isNullOrBlank()) {
                Text(current.username, style = VeloraType.body, color = colors.textSecondary)
            }
        }

        androidx.compose.foundation.layout.Spacer(Modifier.height(VeloraSpacing.xl))

        VeloraSurfaceCard(modifier = Modifier.fillMaxWidth().padding(horizontal = VeloraSpacing.xl)) {
            Text("Password", style = VeloraType.label, color = colors.textSecondary)
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = VeloraSpacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    if (revealed) current.password else "•".repeat(current.password.length.coerceAtMost(18)),
                    style = VeloraType.monoMedium,
                    color = colors.textPrimary,
                    modifier = Modifier.weight(1f),
                )
                VeloraIconButton(
                    icon = if (revealed) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                    contentDescription = if (revealed) "Hide password" else "Show password",
                    onClick = { revealed = !revealed },
                    size = 36.dp,
                )
                com.velora.vault.core.design.components.CopyIconAction(onCopy = viewModel::copyPassword)
            }
            androidx.compose.foundation.layout.Spacer(Modifier.height(VeloraSpacing.sm))
            StrengthBar(score = strength.score, label = strength.label)
        }

        androidx.compose.foundation.layout.Spacer(Modifier.height(VeloraSpacing.md))

        VeloraSurfaceCard(modifier = Modifier.fillMaxWidth().padding(horizontal = VeloraSpacing.xl)) {
            CopyRow(label = "Copy username", onCopy = viewModel::copyUsername, modifier = Modifier.fillMaxWidth().padding(vertical = VeloraSpacing.xs))
            if (!current.websiteUrl.isNullOrBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = VeloraSpacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Outlined.OpenInNew, contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(18.dp))
                    Text("Open website", style = VeloraType.bodySmall, color = colors.textSecondary, modifier = Modifier.padding(start = VeloraSpacing.xs))
                }
            }
        }

        androidx.compose.foundation.layout.Spacer(Modifier.height(VeloraSpacing.md))

        VeloraSurfaceCard(modifier = Modifier.fillMaxWidth().padding(horizontal = VeloraSpacing.xl)) {
            Text("Security", style = VeloraType.label, color = colors.textSecondary, modifier = Modifier.padding(bottom = VeloraSpacing.sm))
            SecurityInfoRow("Password age", "$ageDays day${if (ageDays == 1L) "" else "s"}")
            SecurityInfoRow("Strength") { StatusPill(text = com.velora.vault.core.design.components.strengthText(strength.label), tone = if (isReused) PillTone.Warn else PillTone.Safe) }
            SecurityInfoRow("Reuse status") {
                StatusPill(
                    text = if (isReused) "Reused" else "Unique",
                    tone = if (isReused) PillTone.Critical else PillTone.Safe,
                )
            }
            SecurityInfoRow("Last modified", SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(current.updatedAt)))
        }

        androidx.compose.foundation.layout.Spacer(Modifier.height(VeloraSpacing.xxxl))
    }

    if (showDeleteConfirm) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete this login?") },
            text = { Text("This can't be undone.") },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { viewModel.delete(onDeleted) }) {
                    Text("Delete", color = colors.critical)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun SecurityInfoRow(label: String, value: String) {
    SecurityInfoRow(label) { Text(value, style = VeloraType.bodySmall, color = Velora.colors.textPrimary) }
}

@Composable
private fun SecurityInfoRow(label: String, content: @Composable () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = VeloraSpacing.xs),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = VeloraType.bodySmall, color = Velora.colors.textSecondary)
        content()
    }
}
