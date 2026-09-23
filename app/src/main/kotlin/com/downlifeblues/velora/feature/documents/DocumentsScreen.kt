package com.downlifeblues.velora.feature.documents

import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.downlifeblues.velora.core.design.Velora
import com.downlifeblues.velora.core.design.VeloraSpacing
import com.downlifeblues.velora.core.design.VeloraType
import com.downlifeblues.velora.core.design.components.EmptyState
import com.downlifeblues.velora.core.design.components.VaultItemRow
import com.downlifeblues.velora.core.design.components.VeloraDetailTopBar
import com.downlifeblues.velora.core.design.components.VeloraIconButton
import com.downlifeblues.velora.core.design.components.VeloraPrimaryButton
import com.downlifeblues.velora.core.design.components.VeloraSurfaceCard
import com.downlifeblues.velora.core.design.icons.VeloraIcons
import com.downlifeblues.velora.core.security.BiometricAuthManager
import com.downlifeblues.velora.core.security.BiometricResult
import com.downlifeblues.velora.data.local.entity.DocumentEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DocumentsScreen(
    onBack: () -> Unit,
    onOpenDocument: (String) -> Unit,
    viewModel: DocumentsViewModel = hiltViewModel(),
) {
    val colors = Velora.colors
    val documents by viewModel.documents.collectAsState()
    val context = LocalContext.current

    val pickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            val name = queryDisplayName(uri, context) ?: "Document"
            viewModel.importDocument(uri, name)
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(colors.background).systemBarsPadding()) {
        VeloraDetailTopBar(
            title = "Documents",
            onBack = onBack,
            actions = { VeloraIconButton(icon = Icons.Outlined.Add, contentDescription = "Add document", onClick = { pickerLauncher.launch("*/*") }) },
        )
        if (documents.isEmpty()) {
            EmptyState(
                icon = VeloraIcons.Documents,
                title = "No documents yet.",
                message = "Import a PDF, image, or text file to keep it encrypted in your vault.",
                action = { VeloraPrimaryButton(text = "Import document", onClick = { pickerLauncher.launch("*/*") }) },
            )
        } else {
            LazyColumn(contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = VeloraSpacing.lg, vertical = VeloraSpacing.sm)) {
                items(documents, key = { it.id }) { document ->
                    VaultItemRow(
                        icon = VeloraIcons.Documents,
                        title = document.name,
                        subtitle = "${formatSize(document.sizeBytes)} · ${document.type.name}",
                        categoryLabel = "Document",
                        isFavorite = document.isFavorite,
                        onClick = { onOpenDocument(document.id) },
                    )
                }
            }
        }
    }
}

private fun formatSize(bytes: Long): String = when {
    bytes < 1024 -> "$bytes B"
    bytes < 1024 * 1024 -> "${bytes / 1024} KB"
    else -> "${bytes / (1024 * 1024)} MB"
}

private fun queryDisplayName(uri: android.net.Uri, context: android.content.Context): String? {
    return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (index >= 0 && cursor.moveToFirst()) cursor.getString(index) else null
    }
}

@Composable
fun DocumentDetailScreen(
    documentId: String,
    onBack: () -> Unit,
    viewModel: DocumentDetailViewModel = hiltViewModel(),
) {
    val colors = Velora.colors
    val document by viewModel.document.collectAsState()
    val context = LocalContext.current
    val biometricManager = remember { BiometricAuthManager() }
    var unlocked by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val current = document

    Column(modifier = Modifier.fillMaxSize().background(colors.background).systemBarsPadding()) {
        VeloraDetailTopBar(
            title = current?.name ?: "Document",
            onBack = onBack,
            actions = { VeloraIconButton(icon = Icons.Outlined.Delete, contentDescription = "Delete", onClick = { showDeleteConfirm = true }) },
        )
        if (current != null) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = VeloraSpacing.xl)) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = VeloraSpacing.lg),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier.size(88.dp).clip(CircleShape).background(colors.surfaceElevated),
                        contentAlignment = Alignment.Center,
                    ) {
                        androidx.compose.material3.Icon(VeloraIcons.Documents, contentDescription = null, tint = colors.accent, modifier = Modifier.size(36.dp))
                    }
                }
                VeloraSurfaceCard(modifier = Modifier.fillMaxWidth()) {
                    DetailRow("Type", current.type.name)
                    DetailRow("Size", formatSize(current.sizeBytes))
                    DetailRow("Uploaded", SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(current.createdAt)))
                    current.expiresAt?.let { DetailRow("Expires", SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(it))) }
                }
                androidx.compose.foundation.layout.Spacer(Modifier.height(VeloraSpacing.lg))
                if (!unlocked) {
                    VeloraPrimaryButton(
                        text = "Authenticate to open",
                        onClick = {
                            val activity = context as? FragmentActivity
                            if (activity != null) {
                                biometricManager.authenticate(activity, title = "Confirm it's you") { result ->
                                    if (result is BiometricResult.Success) unlocked = true
                                }
                            } else unlocked = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else {
                    Text(
                        "This document is decrypted only in memory for viewing and never written back to disk unencrypted.",
                        style = VeloraType.bodySmall,
                        color = colors.textSecondary,
                    )
                }
            }
        }
    }

    if (showDeleteConfirm) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete this document?") },
            confirmButton = { androidx.compose.material3.TextButton(onClick = { viewModel.delete(onBack) }) { Text("Delete", color = colors.critical) } },
            dismissButton = { androidx.compose.material3.TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    val colors = Velora.colors
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = VeloraSpacing.xs), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = VeloraType.bodySmall, color = colors.textSecondary)
        Text(value, style = VeloraType.bodySmall, color = colors.textPrimary)
    }
}
