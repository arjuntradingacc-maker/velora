package com.velora.vault.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.velora.vault.core.design.Velora
import com.velora.vault.core.design.VeloraSpacing
import com.velora.vault.core.design.VeloraType
import com.velora.vault.core.design.components.StrengthBar
import com.velora.vault.core.design.components.VeloraPrimaryButton
import com.velora.vault.core.design.components.VeloraSecondaryButton
import com.velora.vault.core.design.components.VeloraTextButton
import com.velora.vault.core.design.components.VeloraTextField
import com.velora.vault.core.design.icons.VeloraIcons
import com.velora.vault.core.security.BiometricAuthManager
import com.velora.vault.core.security.BiometricAvailability
import com.velora.vault.core.security.BiometricResult

@Composable
fun SignUpScreen(onAccountCreated: () -> Unit, viewModel: SignUpViewModel = hiltViewModel()) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val colors = Velora.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = VeloraSpacing.xl, vertical = VeloraSpacing.xxl),
    ) {
        Text("Create your account", style = VeloraType.displayMedium, color = colors.textPrimary)
        Text(
            "This identifies you for sync across devices — it never unlocks your vault by itself.",
            style = VeloraType.body,
            color = colors.textSecondary,
            modifier = Modifier.padding(top = VeloraSpacing.sm, bottom = VeloraSpacing.xl),
        )
        VeloraTextField(value = name, onValueChange = { name = it }, label = "Name", modifier = Modifier.padding(bottom = VeloraSpacing.md))
        VeloraTextField(
            value = email,
            onValueChange = { email = it; error = null },
            label = "Email",
            keyboardType = KeyboardType.Email,
            supportingText = error,
            isError = error != null,
        )
        androidx.compose.foundation.layout.Spacer(Modifier.height(VeloraSpacing.xl))
        VeloraPrimaryButton(
            text = "Continue",
            onClick = {
                if (viewModel.createAccount(email, name)) onAccountCreated()
                else error = "Enter a valid email address."
            },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun CreateMasterPasswordScreen(
    onMasterPasswordCreated: () -> Unit,
    viewModel: MasterPasswordViewModel = hiltViewModel(),
) {
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val colors = Velora.colors
    val strength = remember(password) { viewModel.evaluate(password) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = VeloraSpacing.xl, vertical = VeloraSpacing.xxl),
    ) {
        Text("Choose your master password", style = VeloraType.displayMedium, color = colors.textPrimary)
        Text(
            "This is the one password you'll need to remember. We never store it — only you can unlock your vault with it.",
            style = VeloraType.body,
            color = colors.textSecondary,
            modifier = Modifier.padding(top = VeloraSpacing.sm, bottom = VeloraSpacing.xl),
        )
        com.velora.vault.core.design.components.VeloraPasswordField(
            value = password,
            onValueChange = { password = it; error = null },
            label = "Master password",
        )
        if (password.isNotEmpty()) {
            StrengthBar(score = strength.score, label = strength.label, modifier = Modifier.padding(top = VeloraSpacing.sm))
        }
        androidx.compose.foundation.layout.Spacer(Modifier.height(VeloraSpacing.md))
        com.velora.vault.core.design.components.VeloraPasswordField(
            value = confirm,
            onValueChange = { confirm = it; error = null },
            label = "Confirm master password",
            supportingText = error,
        )
        androidx.compose.foundation.layout.Spacer(Modifier.height(VeloraSpacing.xl))
        VeloraPrimaryButton(
            text = "Secure my vault",
            onClick = {
                when {
                    password.length < 10 -> error = "Use at least 10 characters."
                    password != confirm -> error = "Passwords don't match."
                    else -> {
                        viewModel.createMasterPassword(password)
                        onMasterPasswordCreated()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun BiometricSetupScreen(onDone: () -> Unit, viewModel: BiometricSetupViewModel = hiltViewModel()) {
    val colors = Velora.colors
    val context = androidx.compose.ui.platform.LocalContext.current
    val biometricManager = remember { BiometricAuthManager() }
    val availability = remember {
        (context as? FragmentActivity)?.let { biometricManager.availability(it) } ?: BiometricAvailability.UNSUPPORTED
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .systemBarsPadding()
            .padding(horizontal = VeloraSpacing.xl, vertical = VeloraSpacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        androidx.compose.foundation.layout.Spacer(Modifier.height(VeloraSpacing.xxxl))
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .height(120.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            androidx.compose.material3.Icon(
                VeloraIcons.Fingerprint,
                contentDescription = null,
                tint = colors.accent,
                modifier = Modifier.height(72.dp),
            )
        }
        Text("Unlock faster next time", style = VeloraType.displayMedium, color = colors.textPrimary, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Text(
            if (availability == BiometricAvailability.AVAILABLE) {
                "Turn on biometric unlock so you can open your vault with your fingerprint or face instead of typing your master password every time."
            } else {
                "Biometric unlock isn't available on this device right now — you can always turn it on later from Settings."
            },
            style = VeloraType.body,
            color = colors.textSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(vertical = VeloraSpacing.md),
        )
        androidx.compose.foundation.layout.Spacer(Modifier.weight(1f))
        if (availability == BiometricAvailability.AVAILABLE) {
            VeloraPrimaryButton(
                text = "Enable biometric unlock",
                onClick = {
                    (context as? FragmentActivity)?.let { activity ->
                        biometricManager.authenticate(activity, title = "Confirm it's you") { result ->
                            if (result is BiometricResult.Success) {
                                viewModel.enableBiometric()
                                onDone()
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )
            androidx.compose.foundation.layout.Spacer(Modifier.height(VeloraSpacing.sm))
        }
        VeloraTextButton(text = "Not now", onClick = onDone)
    }
}

@Composable
fun RecoveryCodeRevealScreen(onDone: () -> Unit, viewModel: RecoveryRevealViewModel = hiltViewModel()) {
    val colors = Velora.colors
    val clipboard = LocalClipboardManager.current
    var code by remember { mutableStateOf<String?>(null) }
    var confirmed by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { code = viewModel.generateCode() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .systemBarsPadding()
            .padding(horizontal = VeloraSpacing.xl, vertical = VeloraSpacing.xxl),
    ) {
        Text("Save your recovery code", style = VeloraType.displayMedium, color = colors.textPrimary)
        Text(
            "If you ever forget your master password, this code is the only way back into your vault. Store it somewhere safe — we can't show it to you again.",
            style = VeloraType.body,
            color = colors.textSecondary,
            modifier = Modifier.padding(top = VeloraSpacing.sm, bottom = VeloraSpacing.xl),
        )
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.surfaceElevated, androidx.compose.foundation.shape.RoundedCornerShape(20.dp))
                .padding(VeloraSpacing.xl),
            contentAlignment = Alignment.Center,
        ) {
            Text(code ?: "", style = VeloraType.monoMedium, color = colors.textPrimary, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
        androidx.compose.foundation.layout.Spacer(Modifier.height(VeloraSpacing.md))
        VeloraSecondaryButton(
            text = "Copy code",
            onClick = { code?.let { clipboard.setText(AnnotatedString(it)) } },
            modifier = Modifier.fillMaxWidth(),
        )
        androidx.compose.foundation.layout.Spacer(Modifier.height(VeloraSpacing.xl))
        androidx.compose.foundation.layout.Row(verticalAlignment = Alignment.CenterVertically) {
            androidx.compose.material3.Checkbox(checked = confirmed, onCheckedChange = { confirmed = it })
            Text("I've saved this code somewhere safe", style = VeloraType.bodySmall, color = colors.textSecondary)
        }
        androidx.compose.foundation.layout.Spacer(Modifier.weight(1f))
        VeloraPrimaryButton(text = "Enter my vault", onClick = onDone, enabled = confirmed, modifier = Modifier.fillMaxWidth())
    }
}
