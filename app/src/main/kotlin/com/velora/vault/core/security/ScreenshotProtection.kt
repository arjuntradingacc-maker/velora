package com.velora.vault.core.security

import android.app.Activity
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode

/**
 * Marks the host window FLAG_SECURE for as long as this composable is in
 * the composition, blocking screenshots/screen-recording and hiding the
 * screen from the recents thumbnail. Reference-counted via [flagUsers] so
 * two sensitive screens stacked in the back stack don't clear the flag out
 * from under one another; the flag only actually clears once the last
 * user leaves composition.
 */
@Composable
fun ScreenshotProtected() {
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current
    if (isPreview) return
    DisposableEffect(Unit) {
        val activity = context as? Activity
        flagUsers += 1
        activity?.window?.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        onDispose {
            flagUsers = (flagUsers - 1).coerceAtLeast(0)
            if (flagUsers == 0) {
                activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
            }
        }
    }
}

private var flagUsers = 0
