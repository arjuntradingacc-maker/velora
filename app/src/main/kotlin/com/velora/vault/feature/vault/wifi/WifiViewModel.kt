package com.velora.vault.feature.vault.wifi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.velora.vault.data.local.entity.WifiEntity
import com.velora.vault.data.repository.VaultRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class WifiFormState(
    val id: String? = null,
    val ssid: String = "",
    val password: String = "",
    val security: String = "WPA2",
    val notes: String = "",
)

@HiltViewModel
class AddEditWifiViewModel @Inject constructor(
    private val vaultRepository: VaultRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(WifiFormState())
    val state: StateFlow<WifiFormState> = _state
    private var createdAt = System.currentTimeMillis()

    fun load(wifiId: String?) {
        if (wifiId == null) return
        viewModelScope.launch {
            val existing = vaultRepository.observeWifi().map { it.firstOrNull { w -> w.id == wifiId } }.first()
            existing?.let {
                createdAt = it.createdAt
                _state.value = WifiFormState(it.id, it.ssid, it.password, it.security, it.notes.orEmpty())
            }
        }
    }

    fun update(block: (WifiFormState) -> WifiFormState) { _state.value = block(_state.value) }

    fun save(onDone: () -> Unit) {
        val s = _state.value
        if (s.ssid.isBlank()) return
        viewModelScope.launch {
            vaultRepository.saveWifi(
                WifiEntity(
                    id = s.id ?: UUID.randomUUID().toString(),
                    ssid = s.ssid.trim(),
                    password = s.password,
                    security = s.security,
                    notes = s.notes.ifBlank { null },
                    createdAt = if (s.id == null) System.currentTimeMillis() else createdAt,
                    updatedAt = System.currentTimeMillis(),
                ),
            )
            onDone()
        }
    }

    fun delete(onDeleted: () -> Unit) {
        val s = _state.value
        val id = s.id ?: return
        viewModelScope.launch {
            val existing = vaultRepository.observeWifi().map { it.firstOrNull { w -> w.id == id } }.first()
            existing?.let { vaultRepository.deleteWifi(it) }
            onDeleted()
        }
    }
}
