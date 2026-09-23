package com.downlifeblues.velora.feature.security

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.downlifeblues.velora.core.design.Velora
import com.downlifeblues.velora.core.design.VeloraSpacing
import com.downlifeblues.velora.core.design.VeloraType
import com.downlifeblues.velora.core.design.components.PillTone
import com.downlifeblues.velora.core.design.components.StatusPill
import com.downlifeblues.velora.core.design.components.StrengthRing
import com.downlifeblues.velora.core.design.components.VeloraSurfaceCard
import com.downlifeblues.velora.data.repository.SecurityIssueGroup

@Composable
fun SecurityCenterScreen(
    onOpenIssue: (String) -> Unit,
    onOpenBreachMonitoring: () -> Unit,
    onOpenLogin: (String) -> Unit,
    viewModel: SecurityCenterViewModel = hiltViewModel(),
) {
    val colors = Velora.colors
    val overview by viewModel.overview.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(colors.background).systemBarsPadding(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = VeloraSpacing.xl, vertical = VeloraSpacing.md),
    ) {
        item {
            Text("Security", style = VeloraType.headline, color = colors.textPrimary, modifier = Modifier.padding(vertical = VeloraSpacing.md))
        }
        item {
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = VeloraSpacing.lg), contentAlignment = Alignment.Center) {
                StrengthRing(
                    score = overview.score / 100f,
                    label = com.downlifeblues.velora.core.util.StrengthLabel.entries[(overview.score / 21).coerceIn(0, 4)],
                    strokeWidth = 10.dp,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${overview.score}", style = VeloraType.displayLarge, color = colors.textPrimary)
                        Text("Security score", style = VeloraType.bodySmall, color = colors.textSecondary)
                    }
                }
            }
        }
        item {
            Text(
                viewModel.scoreExplanation,
                style = VeloraType.bodySmall,
                color = colors.textSecondary,
                modifier = Modifier.padding(bottom = VeloraSpacing.lg),
            )
        }
        item {
            VeloraSurfaceCard(onClick = onOpenBreachMonitoring, modifier = Modifier.fillMaxWidth().padding(bottom = VeloraSpacing.md)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Shield, contentDescription = null, tint = colors.accent)
                    Column(modifier = Modifier.weight(1f).padding(start = VeloraSpacing.sm)) {
                        Text("Breach monitoring", style = VeloraType.titleSmall, color = colors.textPrimary)
                        Text("Check your saved logins against known breach data", style = VeloraType.bodySmall, color = colors.textSecondary)
                    }
                    Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = colors.textSecondary)
                }
            }
        }
        if (overview.issues.isEmpty()) {
            item {
                com.downlifeblues.velora.core.design.components.EmptyState(
                    icon = Icons.Outlined.Shield,
                    title = "Nothing needs attention.",
                    message = "Your vault looks healthy. We'll let you know if that changes.",
                )
            }
        } else {
            items(overview.issues, key = { it.type }) { issue ->
                SecurityIssueCard(
                    issue = issue,
                    onClick = { onOpenIssue(issue.type.name) },
                    modifier = Modifier.fillMaxWidth().padding(bottom = VeloraSpacing.md).animateItem(),
                )
            }
        }
    }
}

@Composable
private fun SecurityIssueCard(issue: SecurityIssueGroup, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = Velora.colors
    VeloraSurfaceCard(onClick = onClick, modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(issue.title, style = VeloraType.titleSmall, color = colors.textPrimary)
                    StatusPill(text = "${issue.affected.size}", tone = PillTone.Warn, modifier = Modifier.padding(start = VeloraSpacing.xs))
                }
                Text(
                    issue.riskExplanation,
                    style = VeloraType.bodySmall,
                    color = colors.textSecondary,
                    maxLines = 2,
                    modifier = Modifier.padding(top = VeloraSpacing.xxs),
                )
            }
            Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = colors.textSecondary)
        }
    }
}
