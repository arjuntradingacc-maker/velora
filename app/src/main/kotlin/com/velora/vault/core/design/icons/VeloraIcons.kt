package com.velora.vault.core.design.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Velora's original thin-line icon set, hand-built as [ImageVector]s rather
 * than imported artwork. Every icon shares the same stroke language (1.5–1.7
 * unit round-cap strokes on a 24x24 grid) so the category system reads as
 * one coherent family instead of a bag of mismatched glyphs. Colors here are
 * neutral — [androidx.compose.material3.Icon] re-tints via its `tint` param.
 */
object VeloraIcons {

    private fun icon(name: String, block: ImageVector.Builder.() -> Unit): ImageVector =
        ImageVector.Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply(block).build()

    private fun ImageVector.Builder.line(
        strokeWidth: Float = 1.6f,
        block: androidx.compose.ui.graphics.vector.PathBuilder.() -> Unit,
    ) {
        path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = strokeWidth,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
            pathFillType = PathFillType.NonZero,
            pathBuilder = block,
        )
    }

    private fun ImageVector.Builder.dot(cx: Float, cy: Float, r: Float) {
        path(fill = SolidColor(Color.Black)) {
            moveTo(cx - r, cy)
            arcTo(r, r, 0f, true, true, cx + r, cy)
            arcTo(r, r, 0f, true, true, cx - r, cy)
            close()
        }
    }

    val Logins: ImageVector by lazy {
        icon("Logins") {
            // Key: circular bow + shaft with two teeth.
            line {
                moveTo(15.2f, 8.8f)
                arcTo(3.3f, 3.3f, 0f, true, true, 11.9f, 5.5f)
                arcTo(3.3f, 3.3f, 0f, true, true, 15.2f, 8.8f)
                close()
            }
            line {
                moveTo(13.6f, 10.4f)
                lineTo(5.2f, 18.8f)
            }
            line {
                moveTo(6.6f, 17.4f)
                lineTo(8.4f, 19.2f)
            }
            line {
                moveTo(8.9f, 15.1f)
                lineTo(10.4f, 16.6f)
            }
        }
    }

    val Passkeys: ImageVector by lazy {
        icon("Passkeys") {
            line(1.4f) {
                moveTo(12f, 4.2f)
                arcTo(7.2f, 7.2f, 0f, true, true, 4.8f, 11.4f)
            }
            line(1.4f) {
                moveTo(12f, 7.4f)
                arcTo(4f, 4f, 0f, true, true, 8f, 11.4f)
            }
            dot(12f, 11.4f, 1.5f)
            line(1.4f) {
                moveTo(12f, 12.9f)
                lineTo(12f, 19.8f)
            }
            line(1.4f) {
                moveTo(12f, 16.4f)
                lineTo(14.2f, 16.4f)
            }
        }
    }

    val SecureNotes: ImageVector by lazy {
        icon("SecureNotes") {
            line {
                moveTo(6.2f, 3.6f)
                lineTo(17.8f, 3.6f)
                arcTo(1f, 1f, 0f, false, true, 18.8f, 4.6f)
                lineTo(18.8f, 19.4f)
                arcTo(1f, 1f, 0f, false, true, 17.8f, 20.4f)
                lineTo(6.2f, 20.4f)
                arcTo(1f, 1f, 0f, false, true, 5.2f, 19.4f)
                lineTo(5.2f, 4.6f)
                arcTo(1f, 1f, 0f, false, true, 6.2f, 3.6f)
                close()
            }
            line(1.3f) {
                moveTo(8.2f, 8.4f)
                lineTo(15.8f, 8.4f)
            }
            line(1.3f) {
                moveTo(8.2f, 12f)
                lineTo(15.8f, 12f)
            }
            line(1.3f) {
                moveTo(8.2f, 15.6f)
                lineTo(13f, 15.6f)
            }
        }
    }

    val PaymentCards: ImageVector by lazy {
        icon("PaymentCards") {
            line {
                moveTo(4.2f, 6.4f)
                lineTo(19.8f, 6.4f)
                arcTo(1.2f, 1.2f, 0f, false, true, 21f, 7.6f)
                lineTo(21f, 16.4f)
                arcTo(1.2f, 1.2f, 0f, false, true, 19.8f, 17.6f)
                lineTo(4.2f, 17.6f)
                arcTo(1.2f, 1.2f, 0f, false, true, 3f, 16.4f)
                lineTo(3f, 7.6f)
                arcTo(1.2f, 1.2f, 0f, false, true, 4.2f, 6.4f)
                close()
            }
            line(2f) {
                moveTo(3f, 10.2f)
                lineTo(21f, 10.2f)
            }
            line(1.3f) {
                moveTo(6f, 14.4f)
                lineTo(10f, 14.4f)
            }
        }
    }

    val PersonalInfo: ImageVector by lazy {
        icon("PersonalInfo") {
            line {
                moveTo(15.4f, 8.4f)
                arcTo(3.4f, 3.4f, 0f, true, true, 8.6f, 8.4f)
                arcTo(3.4f, 3.4f, 0f, true, true, 15.4f, 8.4f)
                close()
            }
            line {
                moveTo(5.2f, 19.6f)
                arcTo(6.8f, 5.6f, 0f, false, true, 18.8f, 19.6f)
            }
        }
    }

    val Wifi: ImageVector by lazy {
        icon("Wifi") {
            line(1.7f) {
                moveTo(4.6f, 10.2f)
                arcTo(10.4f, 10.4f, 0f, false, true, 19.4f, 10.2f)
            }
            line(1.6f) {
                moveTo(7.4f, 13.4f)
                arcTo(6.6f, 6.6f, 0f, false, true, 16.6f, 13.4f)
            }
            line(1.5f) {
                moveTo(10.2f, 16.6f)
                arcTo(2.8f, 2.8f, 0f, false, true, 13.8f, 16.6f)
            }
            dot(12f, 19.2f, 1.15f)
        }
    }

    val Otp: ImageVector by lazy {
        icon("Otp") {
            line {
                moveTo(19.4f, 12f)
                arcTo(7.4f, 7.4f, 0f, true, true, 12f, 4.6f)
            }
            line(1.4f) {
                moveTo(12f, 7.6f)
                lineTo(12f, 12f)
                lineTo(15f, 13.4f)
            }
            line(1.3f) {
                moveTo(12f, 3.2f)
                lineTo(12f, 4.6f)
            }
        }
    }

    val Documents: ImageVector by lazy {
        icon("Documents") {
            line {
                moveTo(7.4f, 3.6f)
                lineTo(14.4f, 3.6f)
                lineTo(18.4f, 7.6f)
                lineTo(18.4f, 19.4f)
                arcTo(1f, 1f, 0f, false, true, 17.4f, 20.4f)
                lineTo(7.4f, 20.4f)
                arcTo(1f, 1f, 0f, false, true, 6.4f, 19.4f)
                lineTo(6.4f, 4.6f)
                arcTo(1f, 1f, 0f, false, true, 7.4f, 3.6f)
                close()
            }
            line {
                moveTo(14.2f, 3.6f)
                lineTo(14.2f, 7.6f)
                lineTo(18.3f, 7.6f)
            }
            line(1.2f) {
                moveTo(9f, 12.2f)
                lineTo(15.4f, 12.2f)
            }
            line(1.2f) {
                moveTo(9f, 15.6f)
                lineTo(15.4f, 15.6f)
            }
        }
    }

    val ApiKeys: ImageVector by lazy {
        icon("ApiKeys") {
            line(1.7f) {
                moveTo(9.6f, 6.4f)
                lineTo(4.6f, 12f)
                lineTo(9.6f, 17.6f)
            }
            line(1.7f) {
                moveTo(14.4f, 6.4f)
                lineTo(19.4f, 12f)
                lineTo(14.4f, 17.6f)
            }
            line(1.3f) {
                moveTo(13.2f, 8.2f)
                lineTo(10.8f, 15.8f)
            }
        }
    }

    val RecoveryCodes: ImageVector by lazy {
        val s = 4.6f
        icon("RecoveryCodes") {
            for (row in 0..1) {
                for (col in 0..2) {
                    val x = 5.4f + col * (s + 1.6f)
                    val y = 6.4f + row * (s + 1.6f)
                    line(1.3f) {
                        moveTo(x, y)
                        lineTo(x + s, y)
                        lineTo(x + s, y + s)
                        lineTo(x, y + s)
                        close()
                    }
                }
            }
        }
    }

    val CustomItems: ImageVector by lazy {
        icon("CustomItems") {
            line(1.5f) {
                moveTo(12f, 3.6f)
                lineTo(19.6f, 8f)
                lineTo(19.6f, 16f)
                lineTo(12f, 20.4f)
                lineTo(4.4f, 16f)
                lineTo(4.4f, 8f)
                close()
            }
            line(1.3f) {
                moveTo(12f, 3.6f)
                lineTo(12f, 12f)
                lineTo(19.6f, 8f)
            }
            line(1.3f) {
                moveTo(12f, 12f)
                lineTo(4.4f, 8f)
            }
            line(1.3f) {
                moveTo(12f, 12f)
                lineTo(12f, 20.4f)
            }
        }
    }

    /** Bottom-nav "Vault" glyph — an abbreviated version of the brand mark. */
    val VaultTab: ImageVector by lazy {
        icon("VaultTab") {
            line(1.5f) {
                moveTo(12f, 3.4f)
                lineTo(19f, 6.2f)
                lineTo(19f, 11.6f)
                arcTo(8.4f, 8.4f, 0f, false, true, 12f, 20.4f)
                arcTo(8.4f, 8.4f, 0f, false, true, 5f, 11.6f)
                lineTo(5f, 6.2f)
                close()
            }
            dot(12f, 12f, 1.35f)
        }
    }

    val SecurityTab: ImageVector by lazy {
        icon("SecurityTab") {
            line(1.5f) {
                moveTo(19.2f, 12f)
                arcTo(7.2f, 7.2f, 0f, true, true, 4.8f, 12f)
                arcTo(7.2f, 7.2f, 0f, true, true, 19.2f, 12f)
                close()
            }
            line(1.5f) {
                moveTo(9f, 12.1f)
                lineTo(11.1f, 14.2f)
                lineTo(15.2f, 9.9f)
            }
        }
    }

    val GeneratorTab: ImageVector by lazy {
        icon("GeneratorTab") {
            line(1.5f) {
                moveTo(12f, 4f)
                lineTo(13.2f, 9.4f)
                lineTo(12f, 12f)
                lineTo(10.8f, 9.4f)
                close()
            }
            line(1.5f) {
                moveTo(12f, 20f)
                lineTo(10.8f, 14.6f)
                lineTo(12f, 12f)
                lineTo(13.2f, 14.6f)
                close()
            }
            line(1.3f) {
                moveTo(5.2f, 12f)
                lineTo(9.4f, 12f)
            }
            line(1.3f) {
                moveTo(14.6f, 12f)
                lineTo(18.8f, 12f)
            }
        }
    }

    val Fingerprint: ImageVector by lazy {
        icon("Fingerprint") {
            for (r in floatArrayOf(3.2f, 5.2f, 7.2f)) {
                line(1.3f) {
                    moveTo(12f, 20.6f - r)
                    arcTo(r, r, 0f, true, true, 12f + 0.001f, 20.6f - r)
                }
            }
            line(1.3f) {
                moveTo(12f, 13.4f)
                lineTo(12f, 18f)
            }
        }
    }

    val ShieldFilled: ImageVector by lazy {
        icon("ShieldFilled") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 2.6f)
                lineTo(19.4f, 5.6f)
                lineTo(19.4f, 11.6f)
                curveTo(19.4f, 16.4f, 16.2f, 20f, 12f, 21.4f)
                curveTo(7.8f, 20f, 4.6f, 16.4f, 4.6f, 11.6f)
                lineTo(4.6f, 5.6f)
                close()
            }
        }
    }
}
