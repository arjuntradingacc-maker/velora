package com.downlifeblues.velora.feature.generator

import androidx.lifecycle.ViewModel
import com.downlifeblues.velora.core.security.ClipboardTimeoutManager
import com.downlifeblues.velora.core.util.GeneratorOptions
import com.downlifeblues.velora.core.util.PassphraseOptions
import com.downlifeblues.velora.core.util.PasswordGenerator
import com.downlifeblues.velora.core.util.PasswordStrength
import com.downlifeblues.velora.core.util.PasswordStrengthResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

enum class GeneratorMode { PASSWORD, PASSPHRASE }

data class GeneratorUiState(
    val mode: GeneratorMode = GeneratorMode.PASSWORD,
    val value: String = "",
    val options: GeneratorOptions = GeneratorOptions(),
    val passphraseOptions: PassphraseOptions = PassphraseOptions(),
    val strength: PasswordStrengthResult = PasswordStrength.evaluate(""),
    val generationTick: Int = 0,
)

@HiltViewModel
class GeneratorViewModel @Inject constructor(
    private val clipboardTimeoutManager: ClipboardTimeoutManager,
) : ViewModel() {

    private val _state = MutableStateFlow(GeneratorUiState())
    val state: StateFlow<GeneratorUiState> = _state.asStateFlow()

    init {
        regenerate()
    }

    fun setMode(mode: GeneratorMode) {
        _state.value = _state.value.copy(mode = mode)
        regenerate()
    }

    fun setLength(length: Int) {
        _state.value = _state.value.copy(options = _state.value.options.copy(length = length))
        regenerate()
    }

    fun toggleUppercase(enabled: Boolean) = updateOptions { it.copy(useUppercase = enabled) }
    fun toggleLowercase(enabled: Boolean) = updateOptions { it.copy(useLowercase = enabled) }
    fun toggleNumbers(enabled: Boolean) = updateOptions { it.copy(useNumbers = enabled) }
    fun toggleSymbols(enabled: Boolean) = updateOptions { it.copy(useSymbols = enabled) }
    fun toggleExcludeAmbiguous(enabled: Boolean) = updateOptions { it.copy(excludeAmbiguous = enabled) }

    private inline fun updateOptions(block: (GeneratorOptions) -> GeneratorOptions) {
        _state.value = _state.value.copy(options = block(_state.value.options))
        regenerate()
    }

    fun regenerate() {
        val s = _state.value
        val value = when (s.mode) {
            GeneratorMode.PASSWORD -> PasswordGenerator.generate(s.options)
            GeneratorMode.PASSPHRASE -> PasswordGenerator.generatePassphrase(s.passphraseOptions)
        }
        _state.value = s.copy(
            value = value,
            strength = PasswordStrength.evaluate(value),
            generationTick = s.generationTick + 1,
        )
    }

    fun copy() {
        clipboardTimeoutManager.copy("Generated password", _state.value.value)
    }
}
