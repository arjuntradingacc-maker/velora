package com.downlifeblues.velora.feature.security

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
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.downlifeblues.velora.core.design.Velora
import com.downlifeblues.velora.core.design.VeloraSpacing
import com.downlifeblues.velora.core.design.VeloraType
import com.downlifeblues.velora.core.design.components.PillTone
import com.downlifeblues.velora.core.design.components.StatusPill
import com.downlifeblues.velora.core.design.components.VeloraDetailTopBar
import com.downlifeblues.velora.core.design.components.VeloraPrimaryButton
import com.downlifeblues.velora.core.design.components.VeloraSurfaceCard
import com.downlifeblues.velora.data.local.entity.BreachStatus
import com.downlifeblues.velora.data.repository.BreachCheckResult

@Composable
fun SecurityIssueDetailScreen(
    issueType: String,
    onBack: () -> Unit,
    onOpenLogin: (String) -> Unit,
    viewModel: SecurityIssueDetailViewModel = hiltViewModel(),
) {
    val colors = Velora.colors
    val issue by viewModel.issue.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(colors.background).systemBarsPadding()) {
        VeloraDetailTopBar(title = issue?.title ?: "Security issue", onBack = onBack)

        val current = issue
        if (current == null) {
            com.downlifeblues.velora.core.design.components.EmptyState(
                icon = Icons.Outlined.CheckCircle,
                title = "This issue is resolved.",
                message = "Nothing left to fix here.",
            )
        } else {
            Column(modifier = Modifier.padding(horizontal = VeloraSpacing.xl)) {
                Text(current.riskExplanation, style = VeloraType.body, color = colors.textSecondary)
                Text(
                    "${current.affected.size} affected account${if (current.affected.size == 1) "" else "s"}",
                    style = VeloraType.label,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(vertical = VeloraSpacing.md),
                )
            }
            LazyColumn(contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = VeloraSpacing.xl)) {
                items(current.affected, key = { it.id }) { login ->
                    VeloraSurfaceCard(
                        onClick = { onOpenLogin(login.id) },
                        modifier = Modifier.fillMaxWidth().padding(bottom = VeloraSpacing.sm).animateItem(),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(login.name, style = VeloraType.titleSmall, color = colors.textPrimary)
                                login.username?.let { Text(it, style = VeloraType.bodySmall, color = colors.textSecondary) }
                            }
                            StatusPill(text = "Review", tone = PillTone.Warn)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BreachMonitoringScreen(
    onBack: () -> Unit,
    viewModel: BreachMonitoringViewModel = hiltViewModel(),
) {
    val colors = Velora.colors
    val isChecking by viewModel.isChecking.collectAsState()
    val lastResult by viewModel.lastResult.collectAsState()
    val records by viewModel.breachRecords.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(colors.background).systemBarsPadding()) {
        VeloraDetailTopBar(title = "Breach monitoring", onBack = onBack)

        Column(modifier = Modifier.padding(horizontal = VeloraSpacing.xl)) {
            Text(
                "Velora checks your saved passwords against known breach datasets using a privacy-preserving lookup — only a partial hash ever leaves your device, never your actual password. This is a point-in-time check you run yourself, not continuous real-time monitoring.",
                style = VeloraType.bodySmall,
                color = colors.textSecondary,
                modifier = Modifier.padding(bottom = VeloraSpacing.lg),
            )
            VeloraPrimaryButton(
                text = if (isChecking) "Checking…" else "Check for breaches",
                onClick = viewModel::runCheck,
                enabled = !isChecking,
                modifier = Modifier.fillMaxWidth(),
            )
            when (val result = lastResult) {
                is BreachCheckResult.Offline -> com.downlifeblues.velora.core.design.components.ErrorState(
                    title = "Something interrupted the sync.",
                    message = "Your vault is still safe. Try again.",
                    onRetry = viewModel::runCheck,
                    modifier = Modifier.padding(top = VeloraSpacing.md),
                )
                is BreachCheckResult.Completed -> Text(
                    "Checked ${result.checkedCount} logins — ${result.flaggedCount} need attention.",
                    style = VeloraType.bodySmall,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(top = VeloraSpacing.md),
                )
                null -> Unit
            }
        }

        androidx.compose.foundation.layout.Spacer(Modifier.height(VeloraSpacing.lg))

        LazyColumn(contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = VeloraSpacing.xl)) {
            items(records, key = { it.loginId }) { record ->
                VeloraSurfaceCard(modifier = Modifier.fillMaxWidth().padding(bottom = VeloraSpacing.sm)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val (icon, tone) = when (record.status) {
                            BreachStatus.SAFE -> Icons.Outlined.CheckCircle to PillTone.Safe
                            BreachStatus.ATTENTION -> Icons.Outlined.WarningAmber to PillTone.Warn
                            BreachStatus.CRITICAL -> Icons.Outlined.ErrorOutline to PillTone.Critical
                        }
                        Icon(icon, contentDescription = null, tint = colors.textPrimary)
                        Column(modifier = Modifier.weight(1f).padding(start = VeloraSpacing.sm)) {
                            Text(record.serviceName, style = VeloraType.titleSmall, color = colors.textPrimary)
                            Text(record.recommendedAction, style = VeloraType.bodySmall, color = colors.textSecondary)
                        }
                        StatusPill(text = record.status.name.lowercase().replaceFirstChar(Char::uppercase), tone = tone)
                    }
                }
            }
        }
    }
}
