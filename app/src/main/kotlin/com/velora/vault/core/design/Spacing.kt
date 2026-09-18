package com.velora.vault.core.design

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** 4dp base spacing scale used everywhere instead of ad-hoc dp literals. */
object VeloraSpacing {
    val xxs: Dp = 4.dp
    val xs: Dp = 8.dp
    val sm: Dp = 12.dp
    val md: Dp = 16.dp
    val lg: Dp = 20.dp
    val xl: Dp = 24.dp
    val xxl: Dp = 32.dp
    val xxxl: Dp = 40.dp
    val huge: Dp = 56.dp
}

/** Corner-radius tokens: 20–28dp for primary components, smaller for chips/controls. */
object VeloraRadius {
    val chip: Dp = 12.dp
    val control: Dp = 16.dp
    val card: Dp = 20.dp
    val cardLarge: Dp = 24.dp
    val sheet: Dp = 28.dp
    val pill: Dp = 999.dp
}

/** Elevation hierarchy — used as tonal-overlay strength, not literal shadow depth. */
object VeloraElevation {
    val level0: Dp = 0.dp
    val level1: Dp = 1.dp
    val level2: Dp = 3.dp
    val level3: Dp = 6.dp
    val level4: Dp = 10.dp
}
