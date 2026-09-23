package com.downlifeblues.velora.widget

import android.content.Context
import android.content.Intent
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.compose.ui.unit.dp
import com.downlifeblues.velora.MainActivity
import com.downlifeblues.velora.core.design.VeloraPalette
import com.downlifeblues.velora.core.security.VaultSession
import kotlinx.coroutines.flow.first

class SecurityStatusWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val deps = context.widgetDependencies()
        val session = deps.vaultSessionManager().session.value
        val scoreText = if (session is VaultSession.Unlocked) {
            "${deps.securityRepository().observeOverview().first().score}"
        } else {
            "--"
        }
        val subtitle = if (session is VaultSession.Unlocked) "Security score" else "Vault locked"

        provideContent {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(ColorProvider(VeloraPalette.SurfaceDark1))
                    .padding(16.dp)
                    .clickable(actionStartActivity(Intent(context, MainActivity::class.java))),
                verticalAlignment = Alignment.Vertical.CenterVertically,
                horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
            ) {
                Text(
                    scoreText,
                    style = TextStyle(color = ColorProvider(VeloraPalette.AccentVioletDark), fontWeight = FontWeight.Medium),
                )
                Text(subtitle, style = TextStyle(color = ColorProvider(VeloraPalette.TextSecondaryDark)))
            }
        }
    }
}

class SecurityStatusWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SecurityStatusWidget()
}
