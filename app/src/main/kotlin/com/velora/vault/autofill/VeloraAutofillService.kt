package com.velora.vault.autofill

import android.os.CancellationSignal
import android.service.autofill.AutofillService
import android.service.autofill.Dataset
import android.service.autofill.FillCallback
import android.service.autofill.FillRequest
import android.service.autofill.FillResponse
import android.service.autofill.SaveCallback
import android.service.autofill.SaveInfo
import android.service.autofill.SaveRequest
import android.view.autofill.AutofillId
import android.view.autofill.AutofillValue
import android.widget.RemoteViews
import com.velora.vault.core.security.VaultSessionManager
import com.velora.vault.data.local.entity.LoginEntity
import com.velora.vault.data.repository.VaultRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * Android Autofill Framework integration. Deliberately does not fall back to
 * an Accessibility Service — if the OS-level Autofill APIs aren't available
 * for a given field, Velora simply offers nothing there rather than reading
 * the screen through a broader, more invasive API.
 *
 * Suggestions are only ever offered while the vault is already unlocked in
 * the foreground app process; a locked vault yields no datasets rather than
 * attempting a cross-process unlock handshake.
 */
@AndroidEntryPoint
class VeloraAutofillService : AutofillService() {

    @Inject lateinit var vaultRepository: VaultRepository
    @Inject lateinit var vaultSessionManager: VaultSessionManager

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onFillRequest(request: FillRequest, cancellationSignal: CancellationSignal, callback: FillCallback) {
        val structure = request.fillContexts.lastOrNull()?.structure ?: return callback.onSuccess(null)
        val fields = AutofillStructureParser.parse(structure)

        if (fields.usernameId == null && fields.passwordId == null) {
            callback.onSuccess(null)
            return
        }
        if (!vaultSessionManager.isUnlocked()) {
            // Locked: offer nothing rather than a suggestion we can't back with real data.
            callback.onSuccess(null)
            return
        }

        serviceScope.launch {
            val logins = vaultRepository.allLoginsOnce()
            val domain = fields.webDomain?.lowercase()
            val candidates = if (domain != null) {
                logins.filter { it.websiteUrl?.lowercase()?.contains(domain) == true }.ifEmpty { logins }
            } else {
                logins
            }.take(8)

            if (candidates.isEmpty()) {
                callback.onSuccess(null)
                return@launch
            }

            val responseBuilder = FillResponse.Builder()
            candidates.forEach { login ->
                responseBuilder.addDataset(buildDataset(login, fields))
            }
            fields.buildSaveInfo()?.let { responseBuilder.setSaveInfo(it) }
            callback.onSuccess(responseBuilder.build())
        }
    }

    private fun buildDataset(login: LoginEntity, fields: ParsedAutofillFields): Dataset {
        val presentation = RemoteViews(packageName, android.R.layout.simple_list_item_2).apply {
            setTextViewText(android.R.id.text1, login.name)
            setTextViewText(android.R.id.text2, login.username ?: "")
        }
        val builder = Dataset.Builder(presentation)
        fields.usernameId?.let { id -> login.username?.let { builder.setValue(id, AutofillValue.forText(it), presentation) } }
        fields.passwordId?.let { id -> builder.setValue(id, AutofillValue.forText(login.password), presentation) }
        return builder.build()
    }

    private fun ParsedAutofillFields.buildSaveInfo(): SaveInfo? {
        val ids = listOfNotNull(usernameId, passwordId)
        if (ids.isEmpty() || passwordId == null) return null
        return SaveInfo.Builder(SaveInfo.SAVE_DATA_TYPE_USERNAME or SaveInfo.SAVE_DATA_TYPE_PASSWORD, ids.toTypedArray())
            .build()
    }

    override fun onSaveRequest(request: SaveRequest, callback: SaveCallback) {
        if (!vaultSessionManager.isUnlocked()) {
            callback.onSuccess()
            return
        }
        val structure = request.fillContexts.lastOrNull()?.structure ?: return callback.onSuccess()
        val fields = AutofillStructureParser.parse(structure)

        var username: String? = null
        var password: String? = null
        findValues(structure) { id, value ->
            if (id == fields.usernameId) username = value
            if (id == fields.passwordId) password = value
        }

        if (password.isNullOrBlank()) {
            callback.onSuccess()
            return
        }

        serviceScope.launch {
            val domain = fields.webDomain
            val existing = vaultRepository.observeLogins().first().firstOrNull { it.websiteUrl == domain && it.username == username }
            val now = System.currentTimeMillis()
            vaultRepository.saveLogin(
                existing?.copy(password = password!!, passwordUpdatedAt = now, updatedAt = now) ?: LoginEntity(
                    id = UUID.randomUUID().toString(),
                    name = domain ?: "Saved login",
                    websiteUrl = domain,
                    username = username,
                    password = password!!,
                    notes = null,
                    passwordUpdatedAt = now,
                    createdAt = now,
                    updatedAt = now,
                ),
            )
            callback.onSuccess()
        }
    }

    private fun findValues(structure: android.app.assist.AssistStructure, action: (AutofillId, String?) -> Unit) {
        for (i in 0 until structure.windowNodeCount) {
            visit(structure.getWindowNodeAt(i).rootViewNode, action)
        }
    }

    private fun visit(node: android.app.assist.AssistStructure.ViewNode, action: (AutofillId, String?) -> Unit) {
        node.autofillId?.let { id -> action(id, node.autofillValue?.let { if (it.isText) it.textValue?.toString() else null }) }
        for (i in 0 until node.childCount) visit(node.getChildAt(i), action)
    }
}
