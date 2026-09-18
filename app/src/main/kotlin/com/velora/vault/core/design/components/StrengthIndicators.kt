package com.velora.vault.core.design.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.velora.vault.core.design.Velora
import com.velora.vault.core.design.VeloraMotion
import com.velora.vault.core.design.VeloraType
import com.velora.vault.core.util.StrengthLabel

@Composable
fun strengthColor(label: StrengthLabel): Color {
    val c = Velora.colors
    return when (label) {
        StrengthLabel.VeryWeak, StrengthLabel.Weak -> c.critical
        StrengthLabel.Fair -> c.warn
        StrengthLabel.Good, StrengthLabel.Strong -> c.safe
    }
}

fun strengthText(label: StrengthLabel): String = when (label) {
    StrengthLabel.VeryWeak -> "Very weak"
    StrengthLabel.Weak -> "Weak"
    StrengthLabel.Fair -> "Fair"
    StrengthLabel.Good -> "Good"
    StrengthLabel.Strong -> "Strong"
}

@Composable
fun StrengthBar(
    score: Float,
    label: StrengthLabel,
    modifier: Modifier = Modifier,
    showLabel: Boolean = true,
) {
    val animatedScore by animateFloatAsState(targetValue = score, animationSpec = VeloraMotion.standardSpring, label = "strength")
    val color = strengthColor(label)
    androidx.compose.foundation.layout.Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Velora.colors.surfaceElevated),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedScore.coerceIn(0.04f, 1f))
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(color),
            )
        }
        if (showLabel) {
            androidx.compose.foundation.layout.Spacer(Modifier.height(6.dp))
            Text(strengthText(label), style = VeloraType.labelSmall, color = color)
        }
    }
}

/** The generator's signature large circular strength ring. */
@Composable
fun StrengthRing(
    score: Float,
    label: StrengthLabel,
    modifier: Modifier = Modifier,
    strokeWidth: androidx.compose.ui.unit.Dp = 8.dp,
    content: @Composable () -> Unit = {},
) {
    val animatedScore by animateFloatAsState(targetValue = score, animationSpec = VeloraMotion.standardSpring, label = "ring")
    val color = strengthColor(label)
    val track = Velora.colors.surfaceElevated
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(240.dp)) {
            val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            drawArc(
                color = track,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = stroke,
                size = Size(size.width, size.height),
            )
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * animatedScore.coerceIn(0f, 1f),
                useCenter = false,
                style = stroke,
                size = Size(size.width, size.height),
            )
        }
        content()
    }
}
