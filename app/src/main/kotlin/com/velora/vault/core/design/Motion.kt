package com.velora.vault.core.design

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween

/**
 * Velora's motion language: spring-first, 150–500ms, never a bare
 * linear/ease curve. Every screen should build transitions from these
 * tokens rather than inventing new curves so the app reads as one system.
 */
object VeloraMotion {
    /** Snappy feedback for presses, toggles, small icon morphs. */
    val quickSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium,
    )

    /** The default for card expansion, sheet entry, list item settle. */
    val standardSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMediumLow,
    )

    /** Slow, cinematic — vault unlock, onboarding hero transitions. */
    val cinematicSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessVeryLow,
    )

    val fast = tween<Float>(durationMillis = 150)
    val standard = tween<Float>(durationMillis = 280)
    val slow = tween<Float>(durationMillis = 450)

    const val PRESS_SCALE = 0.96f
    const val TOGGLE_SCALE = 0.98f
}
