package com.downlifeblues.velora.feature.cards

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.downlifeblues.velora.core.design.Velora
import com.downlifeblues.velora.core.design.VeloraSpacing
import com.downlifeblues.velora.core.design.VeloraType
import com.downlifeblues.velora.core.design.components.CopyRow
import com.downlifeblues.velora.core.design.components.VeloraDetailTopBar
import com.downlifeblues.velora.core.design.components.VeloraIconButton
import com.downlifeblues.velora.core.design.components.VeloraPrimaryButton
import com.downlifeblues.velora.core.design.components.VeloraSecondaryButton
import com.downlifeblues.velora.core.design.components.VeloraTextField
import com.downlifeblues.velora.core.security.BiometricAuthManager
import com.downlifeblues.velora.core.security.BiometricResult
import com.downlifeblues.velora.core.security.ScreenshotProtected
import com.downlifeblues.velora.core.util.CardOcrParser
import com.downlifeblues.velora.data.local.entity.PaymentCardEntity

@Composable
fun CardDetailScreen(
    cardId: String,
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    viewModel: CardDetailViewModel = hiltViewModel(),
) {
    ScreenshotProtected()
    val colors = Velora.colors
    val card by viewModel.card.collectAsState()
    val context = LocalContext.current
    val biometricManager = remember { BiometricAuthManager() }
    var revealed by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val current = card

    Column(modifier = Modifier.fillMaxSize().background(colors.background).systemBarsPadding().verticalScroll(rememberScrollState())) {
        VeloraDetailTopBar(
            title = current?.nickname ?: "Card",
            onBack = onBack,
            actions = {
                VeloraIconButton(icon = Icons.Outlined.Edit, contentDescription = "Edit", onClick = { onEdit(cardId) })
                VeloraIconButton(icon = Icons.Outlined.Delete, contentDescription = "Delete", onClick = { showDeleteConfirm = true })
            },
        )
        if (current != null) {
            VirtualCardVisual(card = current, revealed = revealed, modifier = Modifier.fillMaxWidth().padding(horizontal = VeloraSpacing.xl))
            Spacer(Modifier.height(VeloraSpacing.lg))
            if (!revealed) {
                VeloraPrimaryButton(
                    text = "Authenticate to reveal",
                    onClick = {
                        val activity = context as? FragmentActivity
                        if (activity != null) {
                            biometricManager.authenticate(activity, title = "Confirm it's you") { result ->
                                if (result is BiometricResult.Success) revealed = true
                            }
                        } else {
                            revealed = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = VeloraSpacing.xl),
                )
            } else {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = VeloraSpacing.xl)) {
                    CopyRow(label = "Copy card number", onCopy = {}, modifier = Modifier.fillMaxWidth().padding(vertical = VeloraSpacing.xs))
                    Text("CVV: ${current.cvv}", style = VeloraType.body, color = colors.textPrimary, modifier = Modifier.padding(vertical = VeloraSpacing.xs))
                    current.pin?.let { Text("PIN: $it", style = VeloraType.body, color = colors.textPrimary) }
                }
            }
            current.notes?.let {
                Text(it, style = VeloraType.bodySmall, color = colors.textSecondary, modifier = Modifier.padding(horizontal = VeloraSpacing.xl, vertical = VeloraSpacing.md))
            }
        }
    }

    if (showDeleteConfirm) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete this card?") },
            confirmButton = { androidx.compose.material3.TextButton(onClick = { viewModel.delete(onBack) }) { Text("Delete", color = colors.critical) } },
            dismissButton = { androidx.compose.material3.TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") } },
        )
    }
}

/** An original, abstract virtual-card visual — deliberately not modeled on any real issuer's card design. */
@Composable
private fun VirtualCardVisual(card: PaymentCardEntity, revealed: Boolean, modifier: Modifier = Modifier) {
    val colors = Velora.colors
    Column(
        modifier = modifier
            .aspectRatio(1.6f)
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(colors.accentMuted, colors.surfaceSunken, colors.background),
                ),
            )
            .padding(VeloraSpacing.lg),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(card.nickname, style = VeloraType.titleSmall, color = colors.textPrimary)
            if (!revealed) {
                androidx.compose.material3.Icon(Icons.Outlined.Lock, contentDescription = null, tint = colors.textSecondary)
            }
        }
        Spacer(Modifier.weight(1f))
        Text(
            if (revealed) formatCardNumber(card.fullNumber) else "•••• •••• •••• ${card.lastFour}",
            style = VeloraType.monoMedium,
            color = colors.textPrimary,
        )
        Spacer(Modifier.height(VeloraSpacing.sm))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("CARDHOLDER", style = VeloraType.labelSmall, color = colors.textSecondary)
                Text(card.cardholderName.ifBlank { "—" }, style = VeloraType.bodySmall, color = colors.textPrimary)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("EXPIRES", style = VeloraType.labelSmall, color = colors.textSecondary)
                Text("%02d/%02d".format(card.expiryMonth, card.expiryYear % 100), style = VeloraType.bodySmall, color = colors.textPrimary)
            }
            Text(card.network.name, style = VeloraType.label, color = colors.accent)
        }
    }
}

private fun formatCardNumber(number: String): String = number.chunked(4).joinToString(" ")

@Composable
fun AddEditCardScreen(
    cardId: String?,
    onDone: () -> Unit,
    onBack: () -> Unit,
    viewModel: AddEditCardViewModel = hiltViewModel(),
) {
    val colors = Velora.colors
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    LaunchedEffect(cardId) { viewModel.load(cardId) }

    val recognizer = remember { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }
    DisposableEffect(Unit) { onDispose { recognizer.close() } }
    var scanError by remember { mutableStateOf<String?>(null) }
    var scanning by remember { mutableStateOf(false) }

    val captureLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
        if (bitmap == null) {
            scanning = false
            return@rememberLauncherForActivityResult
        }
        recognizer.process(InputImage.fromBitmap(bitmap, 0))
            .addOnSuccessListener { visionText ->
                scanning = false
                val lines = visionText.textBlocks.flatMap { it.lines }.map { it.text }
                val details = CardOcrParser.parse(lines)
                if (details.number == null && details.expiryMonth == null && details.cardholderName == null) {
                    scanError = "Couldn't read that card. Try better lighting and a flatter angle."
                } else {
                    viewModel.applyScan(details)
                }
            }
            .addOnFailureListener {
                scanning = false
                scanError = "Couldn't read that card. Try again."
            }
    }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            scanning = true
            scanError = null
            captureLauncher.launch(null)
        } else {
            scanError = "Camera permission is needed to scan a card."
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(colors.background).systemBarsPadding().verticalScroll(rememberScrollState())) {
        VeloraDetailTopBar(title = if (cardId == null) "Add card" else "Edit card", onBack = onBack)
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = VeloraSpacing.xl)) {
            VeloraSecondaryButton(
                text = if (scanning) "Scanning…" else "Scan card with camera",
                enabled = !scanning,
                onClick = {
                    val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                    if (granted) {
                        scanning = true
                        scanError = null
                        captureLauncher.launch(null)
                    } else {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(bottom = VeloraSpacing.xs),
            )
            scanError?.let {
                Text(it, style = VeloraType.bodySmall, color = colors.critical, modifier = Modifier.padding(bottom = VeloraSpacing.md))
            }
            if (scanError == null) {
                Text(
                    "The CVV never gets scanned — enter it yourself once the rest is filled in.",
                    style = VeloraType.bodySmall,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(bottom = VeloraSpacing.md),
                )
            }
            VeloraTextField(state.nickname, { v -> viewModel.update { it.copy(nickname = v) } }, "Card nickname", modifier = Modifier.padding(bottom = VeloraSpacing.md))
            VeloraTextField(state.cardholderName, { v -> viewModel.update { it.copy(cardholderName = v) } }, "Cardholder name", modifier = Modifier.padding(bottom = VeloraSpacing.md))
            VeloraTextField(
                state.fullNumber, { v -> viewModel.update { it.copy(fullNumber = v) } }, "Card number",
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
                modifier = Modifier.padding(bottom = VeloraSpacing.md),
            )
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = VeloraSpacing.md)) {
                VeloraTextField(state.expiryMonth, { v -> viewModel.update { it.copy(expiryMonth = v) } }, "MM", keyboardType = androidx.compose.ui.text.input.KeyboardType.Number, modifier = Modifier.weight(1f).padding(end = VeloraSpacing.xs))
                VeloraTextField(state.expiryYear, { v -> viewModel.update { it.copy(expiryYear = v) } }, "YYYY", keyboardType = androidx.compose.ui.text.input.KeyboardType.Number, modifier = Modifier.weight(1f).padding(start = VeloraSpacing.xs))
            }
            VeloraTextField(
                state.cvv, { v -> viewModel.update { it.copy(cvv = v) } }, "CVV",
                keyboardType = androidx.compose.ui.text.input.KeyboardType.NumberPassword,
                modifier = Modifier.padding(bottom = VeloraSpacing.xl),
            )
            VeloraPrimaryButton(text = "Save", onClick = { viewModel.save(onDone) }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(VeloraSpacing.xxl))
        }
    }
}
