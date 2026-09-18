package com.velora.vault.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.velora.vault.core.design.Velora
import com.velora.vault.core.design.VeloraSpacing
import com.velora.vault.core.design.VeloraType
import com.velora.vault.core.design.components.EmptyState
import com.velora.vault.core.design.components.PillTone
import com.velora.vault.core.design.components.SectionHeader
import com.velora.vault.core.design.components.StatusPill
import com.velora.vault.core.design.components.VaultItemRow
import com.velora.vault.core.design.components.VeloraIconButton
import com.velora.vault.core.design.components.VeloraQuickActionButton
import com.velora.vault.core.design.components.VeloraSurfaceCard
import com.velora.vault.core.design.icons.VeloraIcons
import com.velora.vault.data.model.VaultCategory

@Composable
fun VaultHomeScreen(
    onOpenSearch: () -> Unit,
    onOpenCategory: (String) -> Unit,
    onOpenItem: (String, String) -> Unit,
    onOpenSecurityCenter: () -> Unit,
    onQuickAdd: (String) -> Unit,
    viewModel: VaultHomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val colors = Velora.colors

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .systemBarsPadding(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = VeloraSpacing.xxxl),
    ) {
        item {
            HomeTopBar(
                greeting = state.greeting,
                accountInitial = state.accountInitial,
                securityScore = state.security.score,
                onOpenSearch = onOpenSearch,
            )
        }
        item {
            VaultHealthHeroCard(
                score = state.security.score,
                issueCount = state.security.issues.sumOf { it.affected.size },
                onClick = onOpenSecurityCenter,
                modifier = Modifier.padding(horizontal = VeloraSpacing.xl, vertical = VeloraSpacing.md),
            )
        }
        item {
            QuickActionsRow(
                onQuickAdd = onQuickAdd,
                modifier = Modifier.padding(horizontal = VeloraSpacing.xl, vertical = VeloraSpacing.sm),
            )
        }
        item {
            SectionHeader(
                title = "Categories",
                modifier = Modifier.padding(horizontal = VeloraSpacing.xl, vertical = VeloraSpacing.md),
            )
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = VeloraSpacing.xl),
                horizontalArrangement = Arrangement.spacedBy(VeloraSpacing.sm),
            ) {
                VaultCategory.entries.forEach { category ->
                    CategoryChip(category = category, onClick = { onOpenCategory(category.name) })
                }
            }
        }
        item {
            SectionHeader(
                title = "Recent",
                modifier = Modifier.padding(horizontal = VeloraSpacing.xl, vertical = VeloraSpacing.md),
            )
        }
        if (state.recentItems.isEmpty() && !state.isLoading) {
            item {
                EmptyState(
                    icon = VeloraIcons.Logins,
                    title = "No secrets here yet.",
                    message = "Add your first login and your vault starts working for you.",
                )
            }
        } else {
            items(state.recentItems, key = { it.id }) { item ->
                VaultItemRow(
                    icon = item.category.icon,
                    title = item.title,
                    subtitle = item.subtitle,
                    categoryLabel = item.category.displayName,
                    isFavorite = item.isFavorite,
                    onClick = { onOpenItem(item.category.name, item.id) },
                    modifier = Modifier.padding(horizontal = VeloraSpacing.xl),
                )
            }
        }
        if (state.security.issues.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Security",
                    action = "See all",
                    onActionClick = onOpenSecurityCenter,
                    modifier = Modifier.padding(horizontal = VeloraSpacing.xl, vertical = VeloraSpacing.md),
                )
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = VeloraSpacing.xl),
                    horizontalArrangement = Arrangement.spacedBy(VeloraSpacing.sm),
                ) {
                    state.security.issues.forEach { issue ->
                        SecurityIssuePreviewCard(
                            title = issue.title,
                            count = issue.affected.size,
                            onClick = onOpenSecurityCenter,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeTopBar(
    greeting: String,
    accountInitial: String,
    securityScore: Int,
    onOpenSearch: () -> Unit,
) {
    val colors = Velora.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = VeloraSpacing.xl, vertical = VeloraSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(greeting, style = VeloraType.title, color = colors.textPrimary)
            StatusPill(
                text = if (securityScore >= 80) "Vault protected" else "Needs attention",
                tone = if (securityScore >= 80) PillTone.Safe else PillTone.Warn,
                modifier = Modifier.padding(top = VeloraSpacing.xxs),
            )
        }
        VeloraIconButton(icon = Icons.Outlined.Search, contentDescription = "Search", onClick = onOpenSearch)
        androidx.compose.foundation.layout.Spacer(Modifier.width(VeloraSpacing.xs))
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(colors.accent),
            contentAlignment = Alignment.Center,
        ) {
            Text(accountInitial, style = VeloraType.titleSmall, color = colors.onAccent)
        }
    }
}

@Composable
private fun VaultHealthHeroCard(score: Int, issueCount: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = Velora.colors
    VeloraSurfaceCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        color = colors.surfaceElevated,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    if (issueCount == 0) "Your vault is protected" else "Your vault needs attention",
                    style = VeloraType.titleSmall,
                    color = colors.textPrimary,
                )
                Text(
                    if (issueCount == 0) {
                        "No issues found in your last check."
                    } else {
                        "$issueCount item${if (issueCount == 1) "" else "s"} could use a fix."
                    },
                    style = VeloraType.bodySmall,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(top = VeloraSpacing.xxs),
                )
            }
            AnimatedShieldBadge(score = score)
        }
    }
}

@Composable
private fun AnimatedShieldBadge(score: Int) {
    val colors = Velora.colors
    val transition = androidx.compose.animation.core.rememberInfiniteTransition(label = "vault-health")
    val pulse by transition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            androidx.compose.animation.core.tween(1600),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse,
        ),
        label = "pulse",
    )
    Box(modifier = Modifier.size(64.dp), contentAlignment = Alignment.Center) {
        androidx.compose.foundation.Canvas(modifier = Modifier.size(64.dp)) {
            drawArc(
                color = colors.accent.copy(alpha = 0.25f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round),
            )
            drawArc(
                color = colors.accent,
                startAngle = -90f,
                sweepAngle = 360f * (score / 100f),
                useCenter = false,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round),
            )
        }
        androidx.compose.material3.Icon(
            VeloraIcons.ShieldFilled,
            contentDescription = "Security score $score",
            tint = colors.accent,
            modifier = Modifier.size(24.dp * pulse),
        )
    }
}

@Composable
private fun QuickActionsRow(onQuickAdd: (String) -> Unit, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(VeloraSpacing.sm)) {
        VeloraQuickActionButton(VeloraIcons.Logins, "Password", { onQuickAdd(VaultCategory.LOGIN.name) }, Modifier.weight(1f))
        VeloraQuickActionButton(VeloraIcons.Passkeys, "Passkey", { onQuickAdd(VaultCategory.PASSKEY.name) }, Modifier.weight(1f))
        VeloraQuickActionButton(VeloraIcons.SecureNotes, "Note", { onQuickAdd(VaultCategory.SECURE_NOTE.name) }, Modifier.weight(1f))
        VeloraQuickActionButton(VeloraIcons.PaymentCards, "Card", { onQuickAdd(VaultCategory.PAYMENT_CARD.name) }, Modifier.weight(1f))
    }
}

@Composable
private fun CategoryChip(category: VaultCategory, onClick: () -> Unit) {
    val colors = Velora.colors
    VeloraSurfaceCard(
        onClick = onClick,
        color = colors.surface,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = VeloraSpacing.md, vertical = VeloraSpacing.sm),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(VeloraSpacing.xs)) {
            androidx.compose.material3.Icon(category.icon, contentDescription = null, tint = colors.accent, modifier = Modifier.size(16.dp))
            Text(category.displayName, style = VeloraType.labelSmall, color = colors.textPrimary)
        }
    }
}

@Composable
private fun SecurityIssuePreviewCard(title: String, count: Int, onClick: () -> Unit) {
    val colors = Velora.colors
    VeloraSurfaceCard(
        modifier = Modifier.width(160.dp),
        onClick = onClick,
        color = colors.surface,
    ) {
        StatusPill(text = "$count", tone = PillTone.Warn)
        Text(title, style = VeloraType.titleSmall, color = colors.textPrimary, modifier = Modifier.padding(top = VeloraSpacing.sm))
    }
}
