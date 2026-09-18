package com.velora.vault.core.design.components

import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.velora.vault.core.design.Velora
import com.velora.vault.core.design.VeloraSpacing
import com.velora.vault.core.design.VeloraType

/**
 * Coherent illustration system for empty states: a soft circular "aperture"
 * field behind a single thin-line glyph, echoing the vault mark rather than
 * generic stock art.
 */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(VeloraSpacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(VeloraSpacing.md),
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(Velora.colors.accent.copy(alpha = if (Velora.colors.isDark) 0.12f else 0.08f)),
            contentAlignment = Alignment.Center,
        ) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Velora.colors.surfaceElevated),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = Velora.colors.accent, modifier = Modifier.size(28.dp))
            }
        }
        Text(title, style = VeloraType.title, color = Velora.colors.textPrimary, textAlign = TextAlign.Center)
        Text(
            message,
            style = VeloraType.bodySmall,
            color = Velora.colors.textSecondary,
            textAlign = TextAlign.Center,
        )
        action?.let {
            androidx.compose.foundation.layout.Spacer(Modifier.height(VeloraSpacing.xs))
            it()
        }
    }
}

/** Human-readable error state — never a raw code. Always offers retry + detail. */
@Composable
fun ErrorState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
    onViewDetails: (() -> Unit)? = null,
    icon: ImageVector = Icons.Outlined.CloudOff,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(VeloraSpacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(VeloraSpacing.sm),
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Velora.colors.warn.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = Velora.colors.warn, modifier = Modifier.size(24.dp))
        }
        Text(title, style = VeloraType.titleSmall, color = Velora.colors.textPrimary, textAlign = TextAlign.Center)
        Text(message, style = VeloraType.bodySmall, color = Velora.colors.textSecondary, textAlign = TextAlign.Center)
        Column(
            verticalArrangement = Arrangement.spacedBy(VeloraSpacing.xs),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            onRetry?.let { VeloraSecondaryButton(text = "Try again", onClick = it) }
            onViewDetails?.let { VeloraTextButton(text = "View details", onClick = it) }
        }
    }
}

/** Shimmerless skeleton block — a static soft tonal placeholder, animated via [pulsingAlpha]. */
@Composable
fun SkeletonBlock(
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(12.dp),
) {
    val alpha = pulsingAlpha()
    androidx.compose.foundation.layout.Box(
        modifier = modifier
            .clip(shape)
            .background(Velora.colors.surfaceElevated.copy(alpha = alpha)),
    )
}

@Composable
fun SkeletonListItem(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth().padding(vertical = VeloraSpacing.xs)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(VeloraSpacing.sm),
        ) {
            SkeletonBlock(modifier = Modifier.size(44.dp), shape = CircleShape)
            Column(verticalArrangement = Arrangement.spacedBy(VeloraSpacing.xs)) {
                SkeletonBlock(modifier = Modifier.height(14.dp).width(140.dp))
                SkeletonBlock(modifier = Modifier.height(11.dp).width(96.dp))
            }
        }
    }
}

@Composable
private fun pulsingAlpha(): Float {
    val transition = rememberInfiniteTransition(label = "skeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.55f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "skeleton-alpha",
    )
    return alpha
}
