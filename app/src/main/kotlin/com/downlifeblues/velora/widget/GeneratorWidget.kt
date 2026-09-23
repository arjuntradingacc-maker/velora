package com.downlifeblues.velora.widget

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.compose.ui.unit.dp
import com.downlifeblues.velora.core.design.VeloraPalette
import com.downlifeblues.velora.core.util.GeneratorOptions
import com.downlifeblues.velora.core.util.PasswordGenerator

private val GENERATED_KEY = stringPreferencesKey("generated_password")

class GeneratorWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = currentState<Preferences>()
            val generated = prefs[GENERATED_KEY] ?: "Tap to generate"

            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(ColorProvider(VeloraPalette.SurfaceDark1))
                    .padding(16.dp),
                verticalAlignment = Alignment.Vertical.CenterVertically,
                horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
            ) {
                Text(
                    generated,
                    style = TextStyle(color = ColorProvider(VeloraPalette.TextPrimaryDark), fontWeight = FontWeight.Medium),
                )
                Text(
                    "Generate",
                    style = TextStyle(color = ColorProvider(VeloraPalette.AccentVioletDark)),
                    modifier = GlanceModifier.padding(top = 8.dp).clickable(actionRunCallback<GenerateAction>()),
                )
            }
        }
    }
}

class GenerateAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val password = PasswordGenerator.generate(GeneratorOptions(length = 16))
        updateAppWidgetState(context, glanceId) { prefs -> prefs[GENERATED_KEY] = password }
        GeneratorWidget().update(context, glanceId)
    }
}

class GeneratorWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = GeneratorWidget()
}
