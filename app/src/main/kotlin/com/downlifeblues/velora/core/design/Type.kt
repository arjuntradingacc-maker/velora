package com.downlifeblues.velora.core.design

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Velora type scale. Large, generously-spaced display sizes for hero moments
 * (onboarding, vault health, the generator) and a calmer body scale for
 * dense lists. Built on the platform system font, distinguished purely
 * through scale, weight and letter-spacing rather than a bundled typeface.
 */
object VeloraType {
    private val sans = FontFamily.Default

    val displayLarge = TextStyle(
        fontFamily = sans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 40.sp,
        lineHeight = 46.sp,
        letterSpacing = (-0.5).sp,
    )
    val displayMedium = TextStyle(
        fontFamily = sans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 38.sp,
        letterSpacing = (-0.3).sp,
    )
    val headline = TextStyle(
        fontFamily = sans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 26.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.2).sp,
    )
    val title = TextStyle(
        fontFamily = sans,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
        lineHeight = 26.sp,
    )
    val titleSmall = TextStyle(
        fontFamily = sans,
        fontWeight = FontWeight.Medium,
        fontSize = 17.sp,
        lineHeight = 23.sp,
    )
    val body = TextStyle(
        fontFamily = sans,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 23.sp,
    )
    val bodySmall = TextStyle(
        fontFamily = sans,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    )
    val label = TextStyle(
        fontFamily = sans,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.2.sp,
    )
    val labelSmall = TextStyle(
        fontFamily = sans,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.3.sp,
    )
    val monoLarge = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
        fontSize = 30.sp,
        lineHeight = 38.sp,
        letterSpacing = 2.sp,
    )
    val monoMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 1.sp,
    )
}

/** Material3 typography derived from the Velora scale, for components that read it directly. */
val VeloraMaterialTypography = Typography(
    displayLarge = VeloraType.displayLarge,
    displayMedium = VeloraType.displayMedium,
    headlineLarge = VeloraType.headline,
    headlineMedium = VeloraType.title,
    titleLarge = VeloraType.title,
    titleMedium = VeloraType.titleSmall,
    bodyLarge = VeloraType.body,
    bodyMedium = VeloraType.bodySmall,
    labelLarge = VeloraType.label,
    labelMedium = VeloraType.labelSmall,
)
