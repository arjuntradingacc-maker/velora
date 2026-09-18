package com.velora.vault.core.security

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.PersistableBundle
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Copies sensitive values to the clipboard, flags them as sensitive on API
 * 33+ (so the platform itself hides clipboard previews), and automatically
 * clears them again after a configurable timeout — but only if the
 * clipboard still holds *our* value, so we never stomp something the user
 * copied from elsewhere in the meantime.
 */
@Singleton
class ClipboardTimeoutManager @Inject constructor(@ApplicationContext private val context: Context) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var pendingClear: Job? = null
    private var lastCopiedValue: String? = null

    var timeoutSeconds: Int = 30

    fun copy(label: String, value: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, value)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val extras = PersistableBundle()
            extras.putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, true)
            clip.description.extras = extras
        }
        clipboard.setPrimaryClip(clip)
        lastCopiedValue = value

        pendingClear?.cancel()
        if (timeoutSeconds > 0) {
            pendingClear = scope.launch {
                delay(timeoutSeconds * 1000L)
                clearIfUnchanged(clipboard, value)
            }
        }
    }

    private fun clearIfUnchanged(clipboard: ClipboardManager, expected: String) {
        val current = runCatching {
            clipboard.primaryClip?.takeIf { it.itemCount > 0 }?.getItemAt(0)?.text?.toString()
        }.getOrNull()
        if (current == expected) {
            clipboard.setPrimaryClip(ClipData.newPlainText("", ""))
        }
        lastCopiedValue = null
    }
}
