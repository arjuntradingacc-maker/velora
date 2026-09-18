package com.velora.vault.core.design.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.runtime.getValue
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.velora.vault.core.design.Velora
import com.velora.vault.core.design.VeloraMotion
import com.velora.vault.core.design.VeloraType
import com.velora.vault.core.design.icons.VeloraIcons
import com.velora.vault.core.navigation.Routes

data class BottomNavTab(val label: String, val icon: ImageVector, val route: String)

val VeloraBottomNavTabs = listOf(
    BottomNavTab("Vault", VeloraIcons.VaultTab, Routes.VAULT_HOME),
    BottomNavTab("Security", VeloraIcons.SecurityTab, Routes.SECURITY_CENTER),
    BottomNavTab("Generator", VeloraIcons.GeneratorTab, Routes.GENERATOR),
    BottomNavTab("Settings", Icons.Outlined.Settings, Routes.SETTINGS),
)

@Composable
fun VeloraBottomBar(
    currentRoute: String?,
    onTabSelected: (BottomNavTab) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = Velora.colors
    Box(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.surface)
                .navigationBarsPadding()
                .height(64.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            VeloraBottomNavTabs.forEachIndexed { index, tab ->
                if (index == 2) {
                    Box(modifier = Modifier.size(56.dp))
                }
                NavTabItem(
                    tab = tab,
                    selected = currentRoute == tab.route,
                    onClick = { onTabSelected(tab) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(56.dp)
                .offset(y = (-18).dp),
        ) {
            CenterAddButton(onClick = onAddClick)
        }
    }
}

@Composable
private fun NavTabItem(
    tab: BottomNavTab,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = Velora.colors
    val interaction = remember { MutableInteractionSource() }
    val indicatorWidth by animateDpAsState(
        targetValue = if (selected) 20.dp else 0.dp,
        label = "nav-indicator",
    )
    Column(
        modifier = modifier
            .veloraClickable(interactionSource = interaction, role = Role.Tab, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            tab.icon,
            contentDescription = tab.label,
            tint = if (selected) colors.accent else colors.textSecondary,
            modifier = Modifier.size(22.dp),
        )
        Text(
            tab.label,
            style = VeloraType.labelSmall,
            color = if (selected) colors.accent else colors.textSecondary,
            modifier = Modifier.padding(top = 2.dp),
        )
        Box(
            modifier = Modifier
                .padding(top = 3.dp)
                .height(2.dp)
                .width(indicatorWidth)
                .clip(RoundedCornerShape(1.dp))
                .background(colors.accent),
        )
    }
}

@Composable
private fun CenterAddButton(onClick: () -> Unit) {
    val colors = Velora.colors
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(colors.accent)
            .veloraClickable(interactionSource = interaction, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Outlined.Add, contentDescription = "Add to vault", tint = colors.onAccent)
    }
}
