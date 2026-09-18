package com.velora.vault.core.design

import androidx.compose.ui.graphics.Color

/**
 * Velora color tokens. This is the single source of truth for brand color —
 * screens should never reference [Color] literals directly, only
 * [VeloraColorScheme] via [LocalVeloraColors].
 */
object VeloraPalette {
    // Neutrals — dark
    val Ink = Color(0xFF0B0B0F)
    val SurfaceDark1 = Color(0xFF14141A)
    val SurfaceDark2 = Color(0xFF1B1B22)
    val SurfaceDark3 = Color(0xFF232330)
    val TextPrimaryDark = Color(0xFFF5F5F7)
    val TextSecondaryDark = Color(0xFFA7A7B0)

    // Neutrals — light
    val Cloud = Color(0xFFF7F7F5)
    val SurfaceLight1 = Color(0xFFFFFFFF)
    val SurfaceLight2 = Color(0xFFF0F0ED)
    val SurfaceLight3 = Color(0xFFE7E7E3)
    val TextPrimaryLight = Color(0xFF17171A)
    val TextSecondaryLight = Color(0xFF6E6E75)

    // Brand accent — "Signal Violet", a muted electric violet tuned for both
    // themes. Deliberately not the generic cybersecurity blue.
    val AccentVioletDark = Color(0xFF9C8CFF)
    val AccentVioletLight = Color(0xFF6153D6)
    val AccentVioletMuted = Color(0xFF4A4166)

    // Semantic security signal colors — used sparingly, never as decoration.
    val SignalSafeDark = Color(0xFF6FD8A3)
    val SignalSafeLight = Color(0xFF1F9D63)
    val SignalWarnDark = Color(0xFFF3C56B)
    val SignalWarnLight = Color(0xFFA6741A)
    val SignalCriticalDark = Color(0xFFF08A8A)
    val SignalCriticalLight = Color(0xFFC23B3B)

    val Overlay = Color(0x99000000)
    val HairlineDark = Color(0x1FFFFFFF)
    val HairlineLight = Color(0x1A17171A)
}

/**
 * Semantic color roles consumed by screens. Distinct from Material3's
 * ColorScheme so the brand identity survives dynamic color and any future
 * theming work without every screen re-deriving roles from primary/tertiary.
 */
data class VeloraColorScheme(
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val surfaceSunken: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val accent: Color,
    val onAccent: Color,
    val accentMuted: Color,
    val safe: Color,
    val warn: Color,
    val critical: Color,
    val hairline: Color,
    val overlay: Color,
    val isDark: Boolean,
)

val DarkVeloraColors = VeloraColorScheme(
    background = VeloraPalette.Ink,
    surface = VeloraPalette.SurfaceDark1,
    surfaceElevated = VeloraPalette.SurfaceDark2,
    surfaceSunken = VeloraPalette.SurfaceDark3,
    textPrimary = VeloraPalette.TextPrimaryDark,
    textSecondary = VeloraPalette.TextSecondaryDark,
    accent = VeloraPalette.AccentVioletDark,
    onAccent = VeloraPalette.Ink,
    accentMuted = VeloraPalette.AccentVioletMuted,
    safe = VeloraPalette.SignalSafeDark,
    warn = VeloraPalette.SignalWarnDark,
    critical = VeloraPalette.SignalCriticalDark,
    hairline = VeloraPalette.HairlineDark,
    overlay = VeloraPalette.Overlay,
    isDark = true,
)

val LightVeloraColors = VeloraColorScheme(
    background = VeloraPalette.Cloud,
    surface = VeloraPalette.SurfaceLight1,
    surfaceElevated = VeloraPalette.SurfaceLight2,
    surfaceSunken = VeloraPalette.SurfaceLight3,
    textPrimary = VeloraPalette.TextPrimaryLight,
    textSecondary = VeloraPalette.TextSecondaryLight,
    accent = VeloraPalette.AccentVioletLight,
    onAccent = VeloraPalette.Cloud,
    accentMuted = VeloraPalette.SurfaceLight3,
    safe = VeloraPalette.SignalSafeLight,
    warn = VeloraPalette.SignalWarnLight,
    critical = VeloraPalette.SignalCriticalLight,
    hairline = VeloraPalette.HairlineLight,
    overlay = VeloraPalette.Overlay,
    isDark = false,
)
