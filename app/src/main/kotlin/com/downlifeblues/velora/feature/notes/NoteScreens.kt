package com.downlifeblues.velora.feature.notes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.downlifeblues.velora.core.design.Velora
import com.downlifeblues.velora.core.design.VeloraSpacing
import com.downlifeblues.velora.core.design.VeloraType
import com.downlifeblues.velora.core.design.components.VeloraDetailTopBar
import com.downlifeblues.velora.core.design.components.VeloraIconButton
import com.downlifeblues.velora.core.design.components.VeloraPrimaryButton
import com.downlifeblues.velora.core.design.components.VeloraSurfaceCard
import com.downlifeblues.velora.core.design.components.VeloraTextField
import com.downlifeblues.velora.core.security.ScreenshotProtected

@Composable
fun NoteDetailScreen(
    noteId: String,
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    viewModel: NoteDetailViewModel = hiltViewModel(),
) {
    ScreenshotProtected()
    val colors = Velora.colors
    val note by viewModel.note.collectAsState()
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val current = note

    Column(modifier = Modifier.fillMaxSize().background(colors.background).systemBarsPadding()) {
        VeloraDetailTopBar(
            title = current?.title ?: "Note",
            onBack = onBack,
            actions = {
                VeloraIconButton(icon = Icons.Outlined.Edit, contentDescription = "Edit", onClick = { onEdit(noteId) })
                VeloraIconButton(icon = Icons.Outlined.Delete, contentDescription = "Delete", onClick = { showDeleteConfirm = true })
            },
        )
        if (current != null) {
            VeloraSurfaceCard(modifier = Modifier.fillMaxWidth().padding(horizontal = VeloraSpacing.xl)) {
                Text(current.body, style = VeloraType.body, color = colors.textPrimary)
            }
        }
    }

    if (showDeleteConfirm) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete this note?") },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { viewModel.delete(onBack) }) { Text("Delete", color = colors.critical) }
            },
            dismissButton = { androidx.compose.material3.TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") } },
        )
    }
}

@Composable
fun AddEditNoteScreen(
    noteId: String?,
    onDone: () -> Unit,
    onBack: () -> Unit,
    viewModel: AddEditNoteViewModel = hiltViewModel(),
) {
    val colors = Velora.colors
    val state by viewModel.state.collectAsState()
    LaunchedEffect(noteId) { viewModel.load(noteId) }

    Column(
        modifier = Modifier.fillMaxSize().background(colors.background).systemBarsPadding().verticalScroll(rememberScrollState()),
    ) {
        VeloraDetailTopBar(title = if (noteId == null) "Add note" else "Edit note", onBack = onBack)
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = VeloraSpacing.xl)) {
            VeloraTextField(value = state.title, onValueChange = viewModel::updateTitle, label = "Title", modifier = Modifier.padding(bottom = VeloraSpacing.md))
            VeloraTextField(
                value = state.body,
                onValueChange = viewModel::updateBody,
                label = "Note",
                singleLine = false,
                minLines = 8,
                modifier = Modifier.padding(bottom = VeloraSpacing.xl),
            )
            VeloraPrimaryButton(text = "Save", onClick = { viewModel.save(onDone) }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(VeloraSpacing.xxl))
        }
    }
}
