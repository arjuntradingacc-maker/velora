package com.velora.vault.core.design.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.velora.vault.core.design.Velora
import com.velora.vault.core.design.VeloraSpacing
import com.velora.vault.core.design.VeloraType
import com.velora.vault.core.design.icons.VeloraIcons
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

data class RadialMenuAction(val label: String, val icon: ImageVector, val onClick: () -> Unit)

/**
 * The center "+" button's beautiful radial add menu: options fan out along
 * an arc above the button with a short staggered spring, and collapse back
 * on dismiss. A scrim behind them closes the menu on tap-outside.
 */
@Composable
fun RadialAddMenu(
    visible: Boolean,
    onDismiss: () -> Unit,
    actions: List<RadialMenuAction>,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(150)),
        exit = fadeOut(tween(150)),
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Velora.colors.overlay)
                .then(
                    Modifier.veloraClickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        role = Role.Button,
                        onClick = onDismiss,
                    ),
                ),
        ) {
            val radius = 128.dp
            val count = actions.size
            actions.forEachIndexed { index, action ->
                // Spread across a 180° arc above the button, centered.
                val spread = 150.0
                val startAngle = 90.0 + spread / 2.0
                val angleDeg = if (count == 1) 90.0 else startAngle - (spread / (count - 1)) * index
                val angle = Math.toRadians(angleDeg)
                val x = (radius.value * cos(angle)).dp
                val y = (radius.value * sin(angle)).dp

                RadialItem(
                    action = action,
                    visible = visible,
                    delayMillis = index * 35,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(x = x, y = -y - 28.dp),
                )
            }
        }
    }
}

@Composable
private fun RadialItem(
    action: RadialMenuAction,
    visible: Boolean,
    delayMillis: Int,
    modifier: Modifier = Modifier,
) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(visible) {
        if (visible) {
            delay(delayMillis.toLong())
            progress.animateTo(1f, animationSpec = tween(260))
        } else {
            progress.snapTo(0f)
        }
    }
    val scale = progress.value
    val colors = Velora.colors
    androidx.compose.foundation.layout.Column(
        modifier = modifier.scale(scale),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(colors.surfaceElevated)
                .then(
                    Modifier.veloraClickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        role = Role.Button,
                        onClick = action.onClick,
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(action.icon, contentDescription = action.label, tint = colors.accent, modifier = Modifier.size(22.dp))
        }
        Text(
            action.label,
            style = VeloraType.labelSmall,
            color = colors.textPrimary,
            modifier = Modifier.padding(top = VeloraSpacing.xxs),
        )
    }
}

/** The standard set of quick-create actions shown from the center "+" button. */
@Composable
fun defaultRadialActions(
    onPassword: () -> Unit,
    onPasskey: () -> Unit,
    onNote: () -> Unit,
    onCard: () -> Unit,
): List<RadialMenuAction> = listOf(
    RadialMenuAction("Password", VeloraIcons.Logins, onPassword),
    RadialMenuAction("Passkey", VeloraIcons.Passkeys, onPasskey),
    RadialMenuAction("Note", VeloraIcons.SecureNotes, onNote),
    RadialMenuAction("Card", VeloraIcons.PaymentCards, onCard),
)
