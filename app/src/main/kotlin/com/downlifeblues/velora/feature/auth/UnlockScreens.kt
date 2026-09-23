package com.downlifeblues.velora.feature.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.downlifeblues.velora.core.design.Velora
import com.downlifeblues.velora.core.design.VeloraSpacing
import com.downlifeblues.velora.core.design.VeloraType
import com.downlifeblues.velora.core.design.components.VeloraPasswordField
import com.downlifeblues.velora.core.design.components.VeloraPrimaryButton
import com.downlifeblues.velora.core.design.components.VeloraTextButton
import com.downlifeblues.velora.core.design.components.VeloraTextField
import com.downlifeblues.velora.core.design.icons.VeloraIcons
import com.downlifeblues.velora.core.security.BiometricAuthManager
import com.downlifeblues.velora.core.security.BiometricResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun UnlockScreen(
    onUnlocked: () -> Unit,
    onForgotPassword: () -> Unit,
    viewModel: UnlockViewModel = hiltViewModel(),
) {
    val colors = Velora.colors
    val context = androidx.compose.ui.platform.LocalContext.current
    val biometricManager = remember { BiometricAuthManager() }
    var password by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var usePin by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var unlocking by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun onSuccess() {
        unlocking = true
        scope.launch {
            delay(520)
            onUnlocked()
        }
    }

    LaunchedEffect(Unit) {
        if (viewModel.isBiometricEnabled()) {
            val cipher = viewModel.biometricCipher()
            val activity = context as? FragmentActivity
            if (cipher != null && activity != null) {
                biometricManager.authenticate(activity, title = "Unlock Velora", cipher = cipher) { result ->
                    if (result is BiometricResult.Success && result.cipher != null) {
                        if (viewModel.unlockWithBiometric(result.cipher)) onSuccess()
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .systemBarsPadding()
            .padding(horizontal = VeloraSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        androidx.compose.foundation.layout.Spacer(Modifier.height(VeloraSpacing.huge))
        UnlockVaultGlyph(unlocking = unlocking)
        Text(
            if (unlocking) "Vault unlocked" else "Welcome back",
            style = VeloraType.headline,
            color = colors.textPrimary,
            modifier = Modifier.padding(top = VeloraSpacing.lg),
        )
        viewModel.accountEmail()?.let {
            Text(it, style = VeloraType.bodySmall, color = colors.textSecondary)
        }

        androidx.compose.foundation.layout.Spacer(Modifier.height(VeloraSpacing.xxl))

        AnimatedContent(targetState = usePin, label = "unlock-mode") { pinMode ->
            Column(modifier = Modifier.fillMaxWidth()) {
                if (pinMode) {
                    VeloraTextField(
                        value = pin,
                        onValueChange = { pin = it.take(8); error = null },
                        label = "PIN",
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.NumberPassword,
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        supportingText = error,
                        isError = error != null,
                    )
                } else {
                    VeloraPasswordField(
                        value = password,
                        onValueChange = { password = it; error = null },
                        label = "Master password",
                        supportingText = error,
                    )
                }
            }
        }

        androidx.compose.foundation.layout.Spacer(Modifier.height(VeloraSpacing.md))
        VeloraPrimaryButton(
            text = "Unlock",
            onClick = {
                val success = if (usePin) viewModel.unlockWithPin(pin) else viewModel.unlockWithMasterPassword(password)
                if (success) onSuccess() else error = if (usePin) "Incorrect PIN." else "Incorrect master password."
            },
            modifier = Modifier.fillMaxWidth(),
        )

        androidx.compose.foundation.layout.Spacer(Modifier.height(VeloraSpacing.sm))
        androidx.compose.foundation.layout.Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            if (viewModel.isPinEnabled()) {
                VeloraTextButton(text = if (usePin) "Use master password" else "Use PIN", onClick = { usePin = !usePin; error = null })
            } else {
                Box(modifier = Modifier)
            }
            VeloraTextButton(text = "Forgot password?", onClick = onForgotPassword)
        }

        if (viewModel.isBiometricEnabled()) {
            androidx.compose.foundation.layout.Spacer(Modifier.height(VeloraSpacing.md))
            VeloraTextButton(
                text = "Use biometric unlock",
                onClick = {
                    val cipher = viewModel.biometricCipher()
                    val activity = context as? FragmentActivity
                    if (cipher != null && activity != null) {
                        biometricManager.authenticate(activity, title = "Unlock Velora", cipher = cipher) { result ->
                            if (result is BiometricResult.Success && result.cipher != null) {
                                if (viewModel.unlockWithBiometric(result.cipher)) onSuccess()
                            }
                        }
                    }
                },
            )
        }
    }
}

/** The cinematic unlock moment: the vault glyph rotates, a ring completes around it, then it "opens". */
@Composable
private fun UnlockVaultGlyph(unlocking: Boolean) {
    val colors = Velora.colors
    val ringProgress = remember { Animatable(0f) }
    val scale = remember { Animatable(1f) }
    LaunchedEffect(unlocking) {
        if (unlocking) {
            launch { ringProgress.animateTo(1f, animationSpec = tween(420)) }
            launch { scale.animateTo(1.12f, animationSpec = tween(420)) }
        }
    }
    Box(modifier = Modifier.size(96.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(96.dp)) {
            drawArc(
                color = colors.accent,
                startAngle = -90f,
                sweepAngle = 360f * ringProgress.value,
                useCenter = false,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
                size = Size(size.width, size.height),
            )
        }
        Icon(
            VeloraIcons.ShieldFilled,
            contentDescription = null,
            tint = colors.accent,
            modifier = Modifier
                .size(52.dp)
                .then(Modifier.graphicsLayerScale(scale.value)),
        )
    }
}

private fun Modifier.graphicsLayerScale(scale: Float): Modifier = this.scale(scale)

@Composable
fun ForgotPasswordScreen(
    onRecovered: () -> Unit,
    onBack: () -> Unit,
    viewModel: ForgotPasswordViewModel = hiltViewModel(),
) {
    val colors = Velora.colors
    var code by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .systemBarsPadding()
            .padding(horizontal = VeloraSpacing.xl, vertical = VeloraSpacing.xxl),
    ) {
        VeloraTextButton(text = "← Back", onClick = onBack, color = colors.textSecondary)
        androidx.compose.foundation.layout.Spacer(Modifier.height(VeloraSpacing.lg))
        Text("Recover your vault", style = VeloraType.displayMedium, color = colors.textPrimary)
        Text(
            "Enter the recovery code you saved when you set up your vault. It's the only other way in — Velora cannot reset your master password for you.",
            style = VeloraType.body,
            color = colors.textSecondary,
            modifier = Modifier.padding(top = VeloraSpacing.sm, bottom = VeloraSpacing.xl),
        )
        VeloraTextField(
            value = code,
            onValueChange = { code = it; error = null },
            label = "Recovery code",
            supportingText = error ?: "Format: XXXXX-XXXXX-XXXXX-XXXXX",
            isError = error != null,
        )
        androidx.compose.foundation.layout.Spacer(Modifier.height(VeloraSpacing.xl))
        VeloraPrimaryButton(
            text = "Recover vault",
            onClick = {
                if (viewModel.unlockWithRecoveryCode(code)) onRecovered()
                else error = "That code didn't match. Double-check and try again."
            },
            modifier = Modifier.fillMaxWidth(),
        )
        androidx.compose.foundation.layout.Spacer(Modifier.height(VeloraSpacing.lg))
        Text(
            "If you've lost your recovery code too, you'll need to reset your vault and start fresh — your existing entries can't be decrypted without either.",
            style = VeloraType.bodySmall,
            color = colors.textSecondary,
        )
    }
}
