package com.velora.vault

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import com.velora.vault.core.design.VeloraTheme
import com.velora.vault.core.navigation.VeloraApp
import dagger.hilt.android.AndroidEntryPoint

/**
 * A single [FragmentActivity] (rather than the more common [androidx.activity.ComponentActivity])
 * so [androidx.biometric.BiometricPrompt] has the Fragment host it requires,
 * while still hosting the entire UI in Compose via [setContent].
 */
@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val startAction = intent?.action

        setContent {
            VeloraTheme {
                VeloraApp(launchAction = startAction)
            }
        }
    }
}
