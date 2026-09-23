package com.downlifeblues.velora.core.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.downlifeblues.velora.core.design.Velora
import com.downlifeblues.velora.core.design.VeloraRadius
import com.downlifeblues.velora.core.design.VeloraSpacing
import com.downlifeblues.velora.core.design.VeloraType

/** The single row used everywhere an item appears in a list: vault, search results, recents. */
@Composable
fun VaultItemRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    categoryLabel: String,
    isFavorite: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onToggleFavorite: (() -> Unit)? = null,
    trailingBadge: (@Composable () -> Unit)? = null,
) {
    val colors = Velora.colors
    val interaction = remember { MutableInteractionSource() }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(VeloraRadius.control))
            .veloraClickable(interactionSource = interaction, role = Role.Button, onClick = onClick)
            .padding(vertical = VeloraSpacing.sm, horizontal = VeloraSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(VeloraSpacing.sm),
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(colors.surfaceElevated),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = colors.textPrimary, modifier = Modifier.size(20.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = VeloraType.titleSmall,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    subtitle,
                    style = VeloraType.bodySmall,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                Text(" · $categoryLabel", style = VeloraType.labelSmall, color = colors.textSecondary)
            }
        }
        trailingBadge?.invoke()
        if (onToggleFavorite != null) {
            VeloraIconButton(
                icon = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                onClick = onToggleFavorite,
                size = 36.dp,
                background = androidx.compose.ui.graphics.Color.Transparent,
                tint = if (isFavorite) colors.accent else colors.textSecondary,
            )
        }
    }
}

@Composable
fun SettingsSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    val colors = Velora.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = VeloraSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = VeloraType.body, color = colors.textPrimary)
            subtitle?.let { Text(it, style = VeloraType.bodySmall, color = colors.textSecondary) }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = colors.onAccent,
                checkedTrackColor = colors.accent,
                uncheckedThumbColor = colors.textSecondary,
                uncheckedTrackColor = colors.surfaceElevated,
            ),
        )
    }
}

@Composable
fun SettingsNavRow(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leadingIcon: ImageVector? = null,
) {
    val colors = Velora.colors
    val interaction = remember { MutableInteractionSource() }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .veloraClickable(interactionSource = interaction, role = Role.Button, onClick = onClick)
            .padding(vertical = VeloraSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(VeloraSpacing.sm),
    ) {
        leadingIcon?.let {
            Icon(it, contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(20.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = VeloraType.body, color = colors.textPrimary)
            subtitle?.let { Text(it, style = VeloraType.bodySmall, color = colors.textSecondary) }
        }
        Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = colors.textSecondary)
    }
}
