package com.velora.vault.core.design.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.velora.vault.core.design.Velora
import com.velora.vault.core.design.VeloraSpacing
import com.velora.vault.core.design.VeloraType

/** Shared back + title + trailing-actions bar used by every detail/add/edit screen. */
@Composable
fun VeloraDetailTopBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable () -> Unit = {},
) {
    val colors = Velora.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = VeloraSpacing.md, vertical = VeloraSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        VeloraIconButton(icon = Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back", onClick = onBack)
        Text(
            title,
            style = VeloraType.title,
            color = colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f).padding(horizontal = VeloraSpacing.sm),
        )
        actions()
    }
}
