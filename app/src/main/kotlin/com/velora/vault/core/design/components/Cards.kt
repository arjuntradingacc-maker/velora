package com.velora.vault.core.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.velora.vault.core.design.Velora
import com.velora.vault.core.design.VeloraRadius
import com.velora.vault.core.design.VeloraSpacing
import com.velora.vault.core.design.VeloraType

/** The base surface every card in the app builds on: rounded, flat, no borders, no heavy shadow. */
@Composable
fun VeloraSurfaceCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    color: Color = Velora.colors.surface,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(VeloraRadius.card),
    contentPadding: androidx.compose.foundation.layout.PaddingValues =
        androidx.compose.foundation.layout.PaddingValues(VeloraSpacing.lg),
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val clickModifier = if (onClick != null) {
        Modifier.veloraClickable(interactionSource = interaction, role = Role.Button, onClick = onClick)
    } else Modifier
    Column(
        modifier = modifier
            .clip(shape)
            .background(color)
            .then(clickModifier)
            .padding(contentPadding),
        content = content,
    )
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    action: String? = null,
    onActionClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(title, style = VeloraType.title, color = Velora.colors.textPrimary)
        if (action != null) {
            VeloraTextButton(text = action, onClick = { onActionClick?.invoke() })
        }
    }
}

/** Small rounded status pill: safe / attention / critical, or a neutral category label. */
@Composable
fun StatusPill(
    text: String,
    modifier: Modifier = Modifier,
    tone: PillTone = PillTone.Neutral,
) {
    val colors = Velora.colors
    val (bg, fg) = when (tone) {
        PillTone.Safe -> colors.safe.copy(alpha = 0.16f) to colors.safe
        PillTone.Warn -> colors.warn.copy(alpha = 0.16f) to colors.warn
        PillTone.Critical -> colors.critical.copy(alpha = 0.16f) to colors.critical
        PillTone.Accent -> colors.accent.copy(alpha = 0.16f) to colors.accent
        PillTone.Neutral -> colors.surfaceElevated to colors.textSecondary
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(VeloraRadius.pill))
            .background(bg)
            .padding(horizontal = VeloraSpacing.sm, vertical = 6.dp),
    ) {
        Text(text, style = VeloraType.labelSmall, color = fg)
    }
}

enum class PillTone { Safe, Warn, Critical, Accent, Neutral }
