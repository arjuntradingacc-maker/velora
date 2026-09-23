package com.downlifeblues.velora.core.design.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.downlifeblues.velora.core.design.Velora
import com.downlifeblues.velora.core.design.VeloraSpacing
import com.downlifeblues.velora.core.design.VeloraType
import com.downlifeblues.velora.core.haptics.HapticStyle
import com.downlifeblues.velora.core.haptics.rememberVeloraHaptics
import kotlinx.coroutines.delay

/**
 * The system's signature "copy" micro-interaction: the icon morphs into a
 * checkmark, a small "Copied" label appears, then both revert on their own
 * after ~1200ms. Used for password/username/OTP copy across the app.
 */
@Composable
fun CopyIconAction(
    onCopy: () -> Unit,
    modifier: Modifier = Modifier,
    idleLabel: String = "Copy",
    copiedLabel: String = "Copied",
) {
    var copied by remember { mutableStateOf(false) }
    val haptics = rememberVeloraHaptics()

    LaunchedEffect(copied) {
        if (copied) {
            delay(1200)
            copied = false
        }
    }

    VeloraIconButton(
        icon = if (copied) Icons.Outlined.Check else Icons.Outlined.ContentCopy,
        contentDescription = if (copied) copiedLabel else idleLabel,
        onClick = {
            if (!copied) {
                onCopy()
                haptics.perform(HapticStyle.Confirm)
                copied = true
            }
        },
        tint = if (copied) Velora.colors.safe else Velora.colors.textPrimary,
        modifier = modifier,
    )
}

/** A full-width tappable row (e.g. "Copy username") that performs the same morph. */
@Composable
fun CopyRow(
    label: String,
    onCopy: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var copied by remember { mutableStateOf(false) }
    val haptics = rememberVeloraHaptics()
    val interaction = remember { MutableInteractionSource() }

    LaunchedEffect(copied) {
        if (copied) {
            delay(1200)
            copied = false
        }
    }

    Row(
        modifier = modifier.veloraClickable(interactionSource = interaction, role = Role.Button) {
            if (!copied) {
                onCopy()
                haptics.perform(HapticStyle.Confirm)
                copied = true
            }
        },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AnimatedContent(
            targetState = copied,
            transitionSpec = {
                (scaleIn(initialScale = 0.7f, animationSpec = tween(180)) + fadeIn(tween(180)))
                    .togetherWith(scaleOut(targetScale = 0.7f, animationSpec = tween(150)) + fadeOut(tween(150)))
            },
            label = "copy-row",
        ) { isCopied ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isCopied) Icons.Outlined.Check else Icons.Outlined.ContentCopy,
                    contentDescription = null,
                    tint = if (isCopied) Velora.colors.safe else Velora.colors.textSecondary,
                    modifier = Modifier.size(18.dp),
                )
                androidx.compose.foundation.layout.Spacer(Modifier.size(VeloraSpacing.xs))
                Text(
                    text = if (isCopied) "Copied" else label,
                    style = VeloraType.bodySmall,
                    color = if (isCopied) Velora.colors.safe else Velora.colors.textSecondary,
                )
            }
        }
    }
}
