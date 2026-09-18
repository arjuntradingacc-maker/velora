package com.velora.vault.feature.otp

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.velora.vault.core.design.Velora
import com.velora.vault.core.design.VeloraSpacing
import com.velora.vault.core.design.VeloraType
import com.velora.vault.core.design.components.CopyIconAction
import com.velora.vault.core.design.components.EmptyState
import com.velora.vault.core.design.components.VeloraDetailTopBar
import com.velora.vault.core.design.components.VeloraIconButton
import com.velora.vault.core.design.components.VeloraPrimaryButton
import com.velora.vault.core.design.components.VeloraSurfaceCard
import com.velora.vault.core.design.components.VeloraTextField
import com.velora.vault.core.design.icons.VeloraIcons

@Composable
fun OtpListScreen(onBack: () -> Unit, viewModel: OtpListViewModel = hiltViewModel()) {
    val colors = Velora.colors
    val accounts by viewModel.accounts.collectAsState()
    var showAddSheet by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().background(colors.background).systemBarsPadding()) {
        VeloraDetailTopBar(
            title = "Authenticator",
            onBack = onBack,
            actions = { VeloraIconButton(icon = Icons.Outlined.Add, contentDescription = "Add account", onClick = { showAddSheet = true }) },
        )
        if (accounts.isEmpty()) {
            EmptyState(
                icon = VeloraIcons.Otp,
                title = "No authenticator codes yet.",
                message = "Add an account's secret key to generate rotating codes here.",
                action = { VeloraPrimaryButton(text = "Add account", onClick = { showAddSheet = true }) },
            )
        } else {
            LazyColumn(contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = VeloraSpacing.lg, vertical = VeloraSpacing.sm)) {
                items(accounts, key = { it.id }) { account ->
                    OtpAccountCard(
                        account = account,
                        onCopy = { viewModel.copyCode(account.id) },
                        onDelete = { viewModel.deleteAccount(account.id) },
                        modifier = Modifier.fillMaxWidth().padding(bottom = VeloraSpacing.sm).animateItem(),
                    )
                }
            }
        }
    }

    if (showAddSheet) {
        AddOtpSheet(onDismiss = { showAddSheet = false }, onAdd = { issuer, name, secret -> viewModel.addAccount(issuer, name, secret); showAddSheet = false })
    }
}

@Composable
private fun OtpAccountCard(account: OtpAccountDisplay, onCopy: () -> Unit, onDelete: () -> Unit, modifier: Modifier = Modifier) {
    val colors = Velora.colors
    VeloraSurfaceCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(account.issuer, style = VeloraType.titleSmall, color = colors.textPrimary)
                Text(account.accountName, style = VeloraType.bodySmall, color = colors.textSecondary)
                Text(
                    account.code.chunked(3).joinToString(" "),
                    style = VeloraType.monoLarge,
                    color = colors.textPrimary,
                    modifier = Modifier.padding(top = VeloraSpacing.xs),
                )
            }
            Box(modifier = Modifier.size(36.dp), contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.size(36.dp)) {
                    drawArc(
                        color = colors.surfaceSunken,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
                    )
                    drawArc(
                        color = colors.accent,
                        startAngle = -90f,
                        sweepAngle = 360f * (1f - account.periodProgress),
                        useCenter = false,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
                    )
                }
                Text("${account.secondsRemaining}", style = VeloraType.labelSmall, color = colors.textSecondary)
            }
            CopyIconAction(onCopy = onCopy)
            VeloraIconButton(icon = Icons.Outlined.Delete, contentDescription = "Delete", onClick = onDelete, size = 36.dp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddOtpSheet(onDismiss: () -> Unit, onAdd: (String, String, String) -> Unit) {
    var issuer by remember { mutableStateOf("") }
    var account by remember { mutableStateOf("") }
    var secret by remember { mutableStateOf("") }
    val colors = Velora.colors

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = colors.surface) {
        Column(modifier = Modifier.fillMaxWidth().padding(VeloraSpacing.xl)) {
            Text("Add authenticator account", style = VeloraType.title, color = colors.textPrimary)
            Text(
                "Enter the secret key shown when the service sets up two-factor authentication.",
                style = VeloraType.bodySmall,
                color = colors.textSecondary,
                modifier = Modifier.padding(top = VeloraSpacing.xs, bottom = VeloraSpacing.lg),
            )
            VeloraTextField(value = issuer, onValueChange = { issuer = it }, label = "Service", modifier = Modifier.padding(bottom = VeloraSpacing.md))
            VeloraTextField(value = account, onValueChange = { account = it }, label = "Account name", modifier = Modifier.padding(bottom = VeloraSpacing.md))
            VeloraTextField(value = secret, onValueChange = { secret = it }, label = "Secret key", modifier = Modifier.padding(bottom = VeloraSpacing.lg))
            VeloraPrimaryButton(
                text = "Add",
                onClick = { if (issuer.isNotBlank() && secret.isNotBlank()) onAdd(issuer, account, secret) },
                modifier = Modifier.fillMaxWidth(),
            )
            androidx.compose.foundation.layout.Spacer(Modifier.height(VeloraSpacing.lg))
        }
    }
}
