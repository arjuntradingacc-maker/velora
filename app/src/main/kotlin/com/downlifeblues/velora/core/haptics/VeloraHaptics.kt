package com.downlifeblues.velora.core.haptics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

/**
 * Velora uses haptics sparingly and only for meaningful confirmations —
 * never on every tap. This maps a small semantic vocabulary onto the
 * platform's [HapticFeedback] so screens never reach for raw constants.
 */
enum class HapticStyle { Confirm, Success, Warning, Selection }

class VeloraHaptics(private val feedback: HapticFeedback) {
    fun perform(style: HapticStyle) {
        when (style) {
            HapticStyle.Confirm -> feedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            HapticStyle.Success -> feedback.performHapticFeedback(HapticFeedbackType.LongPress)
            HapticStyle.Warning -> feedback.performHapticFeedback(HapticFeedbackType.LongPress)
            HapticStyle.Selection -> feedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }
}

@Composable
fun rememberVeloraHaptics(): VeloraHaptics {
    val feedback = LocalHapticFeedback.current
    return remember(feedback) { VeloraHaptics(feedback) }
}
