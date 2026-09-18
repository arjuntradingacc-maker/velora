package com.velora.vault.autofill

import android.app.assist.AssistStructure
import android.view.View
import android.view.autofill.AutofillId

/** What we found worth filling in one screen's view hierarchy. */
data class ParsedAutofillFields(
    val usernameId: AutofillId? = null,
    val passwordId: AutofillId? = null,
    val webDomain: String? = null,
)

/**
 * Walks an [AssistStructure] looking for username/email and password fields
 * using the platform's own autofill hints first (the reliable signal), then
 * falling back to input-type/hint-text heuristics for apps that never
 * adopted `android:autofillHints`.
 */
object AutofillStructureParser {

    fun parse(structure: AssistStructure): ParsedAutofillFields {
        var usernameId: AutofillId? = null
        var passwordId: AutofillId? = null
        var webDomain: String? = null

        val windowCount = structure.windowNodeCount
        for (i in 0 until windowCount) {
            val root = structure.getWindowNodeAt(i).rootViewNode
            visit(root) { node ->
                if (webDomain == null) webDomain = node.webDomain
                val hints = node.autofillHints
                if (hints != null) {
                    if (hints.any { it == View.AUTOFILL_HINT_PASSWORD }) {
                        passwordId = passwordId ?: node.autofillId
                    }
                    if (hints.any { it == View.AUTOFILL_HINT_USERNAME || it == View.AUTOFILL_HINT_EMAIL_ADDRESS }) {
                        usernameId = usernameId ?: node.autofillId
                    }
                } else {
                    val inputType = node.inputType
                    val isPasswordField = (inputType and android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD) != 0 ||
                        (inputType and android.text.InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD) != 0 ||
                        (inputType and android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD) != 0
                    val looksLikeUsername = node.hint?.contains("user", ignoreCase = true) == true ||
                        node.hint?.contains("email", ignoreCase = true) == true
                    if (isPasswordField) {
                        passwordId = passwordId ?: node.autofillId
                    } else if (looksLikeUsername) {
                        usernameId = usernameId ?: node.autofillId
                    }
                }
            }
        }

        return ParsedAutofillFields(usernameId, passwordId, webDomain)
    }

    private inline fun visit(node: AssistStructure.ViewNode, action: (AssistStructure.ViewNode) -> Unit) {
        action(node)
        for (i in 0 until node.childCount) {
            visit(node.getChildAt(i), action)
        }
    }
}
