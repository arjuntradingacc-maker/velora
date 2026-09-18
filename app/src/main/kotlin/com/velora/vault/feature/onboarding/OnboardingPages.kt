package com.velora.vault.feature.onboarding

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.velora.vault.core.design.Velora
import com.velora.vault.core.design.VeloraMotion
import com.velora.vault.core.design.VeloraSpacing
import com.velora.vault.core.design.VeloraType
import com.velora.vault.core.design.components.VeloraPrimaryButton
import com.velora.vault.core.design.components.VeloraSecondaryButton
import com.velora.vault.core.design.icons.VeloraIcons
import kotlin.math.cos
import kotlin.math.sin

@Composable
internal fun VaultHeroPage() {
    val colors = Velora.colors
    val transition = rememberInfiniteTransition(label = "hero")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(18000, easing = LinearEasing)),
        label = "hero-rotate",
    )
    val ringProgress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2600), repeatMode = RepeatMode.Reverse),
        label = "hero-ring",
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(top = VeloraSpacing.xxxl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(modifier = Modifier.size(220.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawArc(
                    color = colors.accent.copy(alpha = 0.35f),
                    startAngle = -90f,
                    sweepAngle = 360f * ringProgress,
                    useCenter = false,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
                )
            }
            Icon(
                VeloraIcons.ShieldFilled,
                contentDescription = null,
                tint = colors.accent,
                modifier = Modifier.size(96.dp).rotate(rotation * 0.02f),
            )
        }
        androidx.compose.foundation.layout.Spacer(Modifier.height(VeloraSpacing.xxl))
        OnboardingHeadline("Everything important.\nOne private place.")
        OnboardingBody("Velora keeps your passwords, passkeys, and private documents in a single vault only you can open.")
    }
}

private data class FlowCard(val icon: androidx.compose.ui.graphics.vector.ImageVector, val angleDeg: Float)

@Composable
internal fun ItemsFlowingPage() {
    val colors = Velora.colors
    val cards = remember {
        listOf(
            FlowCard(VeloraIcons.Logins, -120f),
            FlowCard(VeloraIcons.PaymentCards, -60f),
            FlowCard(VeloraIcons.SecureNotes, 0f),
            FlowCard(VeloraIcons.Passkeys, 60f),
            FlowCard(VeloraIcons.Documents, 120f),
        )
    }
    var progress by remember { mutableStateOf(0f) }
    LaunchedEffect(Unit) {
        androidx.compose.animation.core.Animatable(0f).animateTo(
            1f,
            animationSpec = tween(1400, easing = androidx.compose.animation.core.FastOutSlowInEasing),
        ) { progress = value }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(top = VeloraSpacing.xxxl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(modifier = Modifier.size(220.dp), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(colors.accent.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(VeloraIcons.VaultTab, contentDescription = null, tint = colors.accent, modifier = Modifier.size(30.dp))
            }
            cards.forEach { card ->
                // Cards start scattered on the outer ring and settle in toward the vault as progress -> 1.
                val radius = 96f * (1f - progress)
                val angle = Math.toRadians(card.angleDeg.toDouble())
                val x = (radius * cos(angle)).dp
                val y = (radius * sin(angle)).dp
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .graphicsOffset(x, y)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.surfaceElevated),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(card.icon, contentDescription = null, tint = colors.textPrimary, modifier = Modifier.size(18.dp))
                }
            }
        }
        androidx.compose.foundation.layout.Spacer(Modifier.height(VeloraSpacing.xxl))
        OnboardingHeadline("Every kind of secret,\nin its place.")
        OnboardingBody("Logins, passkeys, notes, cards, and documents each get their own home — never a messy pile.")
    }
}

private fun Modifier.graphicsOffset(x: androidx.compose.ui.unit.Dp, y: androidx.compose.ui.unit.Dp): Modifier =
    this.offset(x = x, y = y)

@Composable
internal fun BiometricPage() {
    val colors = Velora.colors
    val transition = rememberInfiniteTransition(label = "bio")
    val scanY by transition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing), repeatMode = RepeatMode.Reverse),
        label = "scan",
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(top = VeloraSpacing.xxxl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(modifier = Modifier.size(220.dp), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(colors.surfaceElevated),
                contentAlignment = Alignment.Center,
            ) {
                Icon(VeloraIcons.Fingerprint, contentDescription = null, tint = colors.accent, modifier = Modifier.size(64.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .graphicsOffset(0.dp, (scanY * 56).dp)
                        .background(colors.accent.copy(alpha = 0.7f)),
                )
            }
        }
        androidx.compose.foundation.layout.Spacer(Modifier.height(VeloraSpacing.xxl))
        OnboardingHeadline("Unlock with a touch\nor a glance.")
        OnboardingBody("Where your device supports it, your fingerprint or face is all it takes to get back in.")
    }
}

@Composable
internal fun ZeroKnowledgePage() {
    val colors = Velora.colors
    val transition = rememberInfiniteTransition(label = "zk")
    val dashPhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 40f,
        animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing)),
        label = "dash",
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(top = VeloraSpacing.xxxl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(modifier = Modifier.size(220.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(160.dp)) {
                val stroke = Stroke(
                    width = 2.dp.toPx(),
                    cap = StrokeCap.Round,
                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(14f, 10f), dashPhase),
                )
                drawCircle(color = colors.accent.copy(alpha = 0.5f), style = stroke, radius = size.minDimension / 2)
            }
            Icon(VeloraIcons.ShieldFilled, contentDescription = null, tint = colors.accent, modifier = Modifier.size(72.dp))
        }
        androidx.compose.foundation.layout.Spacer(Modifier.height(VeloraSpacing.xxl))
        OnboardingHeadline("Locked before it\never leaves your phone.")
        OnboardingBody("Your vault is encrypted on your device with a key only you hold. Velora is built so it never sees your unencrypted data.")
    }
}

@Composable
internal fun GetStartedPage(onCreateAccount: () -> Unit, onSignIn: () -> Unit) {
    val colors = Velora.colors
    Column(
        modifier = Modifier.fillMaxSize().padding(top = VeloraSpacing.xxxl, start = VeloraSpacing.xl, end = VeloraSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(colors.accent.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(VeloraIcons.ShieldFilled, contentDescription = null, tint = colors.accent, modifier = Modifier.size(44.dp))
        }
        androidx.compose.foundation.layout.Spacer(Modifier.height(VeloraSpacing.xxl))
        Text("Your vault is ready\nwhen you are.", style = VeloraType.displayMedium, color = colors.textPrimary, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        OnboardingBody("Create your Velora account to get started, or sign in if you already have one.")
        androidx.compose.foundation.layout.Spacer(Modifier.height(VeloraSpacing.xl))
        VeloraPrimaryButton(text = "Create account", onClick = onCreateAccount, modifier = Modifier.fillMaxWidth())
        androidx.compose.foundation.layout.Spacer(Modifier.height(VeloraSpacing.sm))
        VeloraSecondaryButton(text = "I already have an account", onClick = onSignIn, modifier = Modifier.fillMaxWidth())
    }
}
