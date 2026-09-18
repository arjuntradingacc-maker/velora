package com.velora.vault.core.design

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val LocalVeloraColors = compositionLocalOf { DarkVeloraColors }

/**
 * True when the user has requested reduced motion at the OS level
 * (Settings > Accessibility > Remove animations, or a battery saver
 * equivalent). Screens should collapse spring transitions to instant
 * fades or skip them entirely when this is true.
 */
val LocalReducedMotion = compositionLocalOf { false }

private fun veloraMaterialColorScheme(c: VeloraColorScheme) = if (c.isDark) {
    darkColorScheme(
        primary = c.accent,
        onPrimary = c.onAccent,
        secondary = c.accent,
        background = c.background,
        onBackground = c.textPrimary,
        surface = c.surface,
        onSurface = c.textPrimary,
        surfaceVariant = c.surfaceElevated,
        onSurfaceVariant = c.textSecondary,
        error = c.critical,
        outline = c.hairline,
        outlineVariant = c.hairline,
    )
} else {
    lightColorScheme(
        primary = c.accent,
        onPrimary = c.onAccent,
        secondary = c.accent,
        background = c.background,
        onBackground = c.textPrimary,
        surface = c.surface,
        onSurface = c.textPrimary,
        surfaceVariant = c.surfaceElevated,
        onSurfaceVariant = c.textSecondary,
        error = c.critical,
        outline = c.hairline,
        outlineVariant = c.hairline,
    )
}

@Composable
fun VeloraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    reducedMotion: Boolean = false,
    content: @Composable () -> Unit,
) {
    val veloraColors = if (darkTheme) DarkVeloraColors else LightVeloraColors
    val view = LocalView.current
    val isPreview = LocalInspectionMode.current
    if (!isPreview) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    CompositionLocalProvider(
        LocalVeloraColors provides veloraColors,
        LocalReducedMotion provides reducedMotion,
    ) {
        MaterialTheme(
            colorScheme = veloraMaterialColorScheme(veloraColors),
            typography = VeloraMaterialTypography,
            shapes = VeloraShapes,
            content = content,
        )
    }
}

/** Convenience accessor: `Velora.colors.accent`, mirroring `MaterialTheme.colorScheme`. */
object Velora {
    val colors: VeloraColorScheme
        @Composable
        get() = LocalVeloraColors.current
}
