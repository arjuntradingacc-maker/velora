package com.velora.vault.core.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.velora.vault.core.design.Velora
import com.velora.vault.core.design.VeloraMotion
import com.velora.vault.core.design.VeloraRadius
import com.velora.vault.core.design.VeloraSpacing
import com.velora.vault.core.design.VeloraType
import com.velora.vault.core.haptics.HapticStyle
import com.velora.vault.core.haptics.rememberVeloraHaptics

/** Shared 2–4% press-scale spring used by every tappable primitive in the system. */
@Composable
private fun pressScaleModifier(
    interactionSource: MutableInteractionSource,
    pressedScale: Float = 0.96f,
): Modifier {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale = remember { androidx.compose.animation.core.Animatable(1f) }
    androidx.compose.runtime.LaunchedEffect(pressed) {
        scale.animateTo(if (pressed) pressedScale else 1f, animationSpec = VeloraMotion.quickSpring)
    }
    return Modifier.graphicsLayer {
        scaleX = scale.value
        scaleY = scale.value
    }
}

@Composable
fun VeloraPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
) {
    val colors = Velora.colors
    val interaction = remember { MutableInteractionSource() }
    val haptics = rememberVeloraHaptics()
    Box(
        modifier = modifier
            .then(pressScaleModifier(interaction))
            .clip(RoundedCornerShape(VeloraRadius.control))
            .background(if (enabled) colors.accent else colors.accentMuted)
            .then(
                Modifier.veloraClickable(
                    interactionSource = interaction,
                    enabled = enabled,
                    role = Role.Button,
                ) {
                    haptics.perform(HapticStyle.Confirm)
                    onClick()
                },
            )
            .padding(horizontal = VeloraSpacing.xl, vertical = VeloraSpacing.md),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            leadingIcon?.let {
                it()
                androidx.compose.foundation.layout.Spacer(Modifier.size(VeloraSpacing.xs))
            }
            Text(text = text, style = VeloraType.titleSmall, color = colors.onAccent)
        }
    }
}

@Composable
fun VeloraSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = Velora.colors
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .then(pressScaleModifier(interaction))
            .clip(RoundedCornerShape(VeloraRadius.control))
            .background(colors.surfaceElevated)
            .then(
                Modifier.veloraClickable(interactionSource = interaction, enabled = enabled, role = Role.Button) {
                    onClick()
                },
            )
            .padding(horizontal = VeloraSpacing.xl, vertical = VeloraSpacing.md),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = text, style = VeloraType.titleSmall, color = colors.textPrimary)
    }
}

@Composable
fun VeloraTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = Velora.colors.accent,
) {
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .then(pressScaleModifier(interaction, pressedScale = 0.98f))
            .clip(RoundedCornerShape(VeloraRadius.chip))
            .then(Modifier.veloraClickable(interactionSource = interaction, role = Role.Button) { onClick() })
            .padding(horizontal = VeloraSpacing.sm, vertical = VeloraSpacing.xs),
    ) {
        Text(text = text, style = VeloraType.titleSmall, color = color)
    }
}

/** Circular icon button used for quick actions, top bar affordances, radial add menu items. */
@Composable
fun VeloraIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 44.dp,
    background: Color = Velora.colors.surfaceElevated,
    tint: Color = Velora.colors.textPrimary,
) {
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .size(size)
            .then(pressScaleModifier(interaction))
            .clip(CircleShape)
            .background(background)
            .then(Modifier.veloraClickable(interactionSource = interaction, role = Role.Button) { onClick() }),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = contentDescription, tint = tint, modifier = Modifier.size(size * 0.45f))
    }
}

@Composable
fun VeloraQuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = Velora.colors
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .then(pressScaleModifier(interaction))
            .clip(RoundedCornerShape(VeloraRadius.card))
            .background(colors.surface)
            .then(Modifier.veloraClickable(interactionSource = interaction, role = Role.Button) { onClick() })
            .padding(vertical = VeloraSpacing.md, horizontal = VeloraSpacing.xs),
    ) {
        androidx.compose.foundation.layout.Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(VeloraSpacing.xs),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(colors.accent.copy(alpha = if (colors.isDark) 0.16f else 0.1f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = colors.accent, modifier = Modifier.size(20.dp))
            }
            Text(label, style = VeloraType.labelSmall, color = colors.textSecondary)
        }
    }
}

/** Borderless, indication-less clickable — cards/buttons drive their own press-scale feedback instead. */
internal fun Modifier.veloraClickable(
    interactionSource: MutableInteractionSource,
    enabled: Boolean = true,
    role: Role? = null,
    onClick: () -> Unit,
): Modifier = this.clickable(
    interactionSource = interactionSource,
    indication = null,
    enabled = enabled,
    role = role,
    onClick = onClick,
)
