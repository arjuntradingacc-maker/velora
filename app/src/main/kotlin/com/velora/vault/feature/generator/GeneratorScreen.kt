package com.velora.vault.feature.generator

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.velora.vault.core.design.Velora
import com.velora.vault.core.design.VeloraSpacing
import com.velora.vault.core.design.VeloraType
import com.velora.vault.core.design.components.CopyIconAction
import com.velora.vault.core.design.components.PillTone
import com.velora.vault.core.design.components.SettingsSwitchRow
import com.velora.vault.core.design.components.StatusPill
import com.velora.vault.core.design.components.StrengthRing
import com.velora.vault.core.design.components.VeloraPrimaryButton
import com.velora.vault.core.design.components.strengthText
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun GeneratorScreen(viewModel: GeneratorViewModel = hiltViewModel()) {
    val colors = Velora.colors
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .systemBarsPadding()
            .padding(horizontal = VeloraSpacing.xl)
            .verticalScroll(rememberScrollState()),
    ) {
        Text("Generator", style = VeloraType.headline, color = colors.textPrimary, modifier = Modifier.padding(top = VeloraSpacing.md))

        androidx.compose.foundation.layout.Spacer(Modifier.height(VeloraSpacing.xl))

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            StrengthRing(score = state.strength.score, label = state.strength.label, strokeWidth = 8.dp) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(VeloraSpacing.lg)) {
                    Box(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                        ScramblingPasswordText(
                            target = state.value,
                            tick = state.generationTick,
                            style = VeloraType.monoLarge,
                            color = colors.textPrimary,
                        )
                    }
                    StatusPill(
                        text = strengthText(state.strength.label),
                        tone = when {
                            state.strength.score > 0.7f -> PillTone.Safe
                            state.strength.score > 0.4f -> PillTone.Warn
                            else -> PillTone.Critical
                        },
                        modifier = Modifier.padding(top = VeloraSpacing.sm),
                    )
                }
            }
        }

        androidx.compose.foundation.layout.Spacer(Modifier.height(VeloraSpacing.xl))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(VeloraSpacing.sm)) {
            CopyIconAction(onCopy = viewModel::copy)
            VeloraPrimaryButton(
                text = "Generate",
                onClick = viewModel::regenerate,
                modifier = Modifier.weight(1f),
            )
        }

        androidx.compose.foundation.layout.Spacer(Modifier.height(VeloraSpacing.xl))

        ModeSwitch(mode = state.mode, onModeChange = viewModel::setMode)

        androidx.compose.foundation.layout.Spacer(Modifier.height(VeloraSpacing.lg))

        if (state.mode == GeneratorMode.PASSWORD) {
            Text("Length: ${state.options.length}", style = VeloraType.body, color = colors.textPrimary)
            Slider(
                value = state.options.length.toFloat(),
                onValueChange = { viewModel.setLength(it.toInt()) },
                valueRange = 8f..64f,
                colors = SliderDefaults.colors(thumbColor = colors.accent, activeTrackColor = colors.accent),
            )
            SettingsSwitchRow("Uppercase (A-Z)", state.options.useUppercase, viewModel::toggleUppercase)
            SettingsSwitchRow("Lowercase (a-z)", state.options.useLowercase, viewModel::toggleLowercase)
            SettingsSwitchRow("Numbers (0-9)", state.options.useNumbers, viewModel::toggleNumbers)
            SettingsSwitchRow("Symbols (!@#$)", state.options.useSymbols, viewModel::toggleSymbols)
            SettingsSwitchRow("Exclude ambiguous characters", state.options.excludeAmbiguous, viewModel::toggleExcludeAmbiguous)
        } else {
            Text(
                "A passphrase of memorable words is easier to type and just as strong, given enough words.",
                style = VeloraType.bodySmall,
                color = colors.textSecondary,
            )
        }

        androidx.compose.foundation.layout.Spacer(Modifier.height(VeloraSpacing.xxxl))
    }
}

@Composable
private fun ModeSwitch(mode: GeneratorMode, onModeChange: (GeneratorMode) -> Unit) {
    val colors = Velora.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surfaceElevated, RoundedCornerShape(14.dp))
            .padding(4.dp),
    ) {
        listOf(GeneratorMode.PASSWORD to "Password", GeneratorMode.PASSPHRASE to "Passphrase").forEach { (m, label) ->
            val selected = mode == m
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(if (selected) colors.accent else Color.Transparent, RoundedCornerShape(12.dp))
                    .padding(vertical = 10.dp)
                    .then(
                        Modifier.clickableNoRipple { onModeChange(m) },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(label, style = VeloraType.titleSmall, color = if (selected) colors.onAccent else colors.textSecondary)
            }
        }
    }
}

@Composable
private fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier {
    val interaction = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    return this.clickable(interactionSource = interaction, indication = null, onClick = onClick)
}

/** The generator's signature reveal: characters scramble left-to-right before settling on the real value. */
@Composable
private fun ScramblingPasswordText(target: String, tick: Int, style: TextStyle, color: Color) {
    var displayed by remember { mutableStateOf(target) }
    val pool = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%"
    LaunchedEffect(tick) {
        val steps = 10
        repeat(steps) { step ->
            val settledUpTo = (target.length * (step + 1) / steps)
            displayed = target.mapIndexed { index, char ->
                if (index < settledUpTo) char else pool[Random.nextInt(pool.length)]
            }.joinToString("")
            delay(28)
        }
        displayed = target
    }
    Text(displayed, style = style, color = color)
}
