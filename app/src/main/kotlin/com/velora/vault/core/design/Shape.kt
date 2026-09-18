package com.velora.vault.core.design

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes

val VeloraShapes = Shapes(
    extraSmall = RoundedCornerShape(VeloraRadius.chip),
    small = RoundedCornerShape(VeloraRadius.control),
    medium = RoundedCornerShape(VeloraRadius.card),
    large = RoundedCornerShape(VeloraRadius.cardLarge),
    extraLarge = RoundedCornerShape(VeloraRadius.sheet),
)
