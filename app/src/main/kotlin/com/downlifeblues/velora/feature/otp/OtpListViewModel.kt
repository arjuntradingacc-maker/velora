package com.downlifeblues.velora.feature.otp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.downlifeblues.velora.core.security.ClipboardTimeoutManager
import com.downlifeblues.velora.core.security.OtpSecretCrypto
import com.downlifeblues.velora.core.util.TotpGenerator
import com.downlifeblues.velora.data.local.entity.OtpEntity
import com.downlifeblues.velora.data.repository.VaultRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class OtpAccountDisplay(
    val id: String,
    val issuer: String,
    val accountName: String,
    val code: String,
    val secondsRemaining: Int,
    val periodProgress: Float,
)

@HiltViewModel
class OtpListViewModel @Inject constructor(
    private val vaultRepository: VaultRepository,
    private val otpSecretCrypto: OtpSecretCrypto,
    private val clipboardTimeoutManager: ClipboardTimeoutManager,
) : ViewModel() {

    private val ticker = MutableStateFlow(System.currentTimeMillis())

    init {
        viewModelScope.launch {
            while (true) {
                delay(1000)
                ticker.value = System.currentTimeMillis()
            }
        }
    }

    val accounts: StateFlow<List<OtpAccountDisplay>> = combine(vaultRepository.observeOtp(), ticker) { entities, now ->
        entities.map { entity -> entity.toDisplay(now) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun OtpEntity.toDisplay(now: Long): OtpAccountDisplay {
        val secret = runCatching { otpSecretCrypto.decryptSecret(encryptedSecret) }.getOrDefault("")
        return OtpAccountDisplay(
            id = id,
            issuer = issuer,
            accountName = accountName,
            code = if (secret.isBlank()) "------" else TotpGenerator.currentCode(secret, digits, periodSeconds, algorithm, now),
            secondsRemaining = TotpGenerator.secondsRemaining(periodSeconds, now),
            periodProgress = TotpGenerator.periodProgress(periodSeconds, now),
        )
    }

    fun copyCode(id: String) {
        val account = accounts.value.firstOrNull { it.id == id } ?: return
        clipboardTimeoutManager.copy("Authenticator code", account.code)
    }

    fun addAccount(issuer: String, accountName: String, secretBase32: String) {
        if (issuer.isBlank() || secretBase32.isBlank()) return
        viewModelScope.launch {
            vaultRepository.saveOtp(
                OtpEntity(
                    id = UUID.randomUUID().toString(),
                    issuer = issuer.trim(),
                    accountName = accountName.trim(),
                    encryptedSecret = otpSecretCrypto.encryptSecret(secretBase32.trim().replace(" ", "")),
                    createdAt = System.currentTimeMillis(),
                ),
            )
        }
    }

    fun deleteAccount(id: String) {
        viewModelScope.launch {
            val existing = vaultRepository.observeOtp().map { it.firstOrNull { o -> o.id == id } }.first()
            existing?.let { vaultRepository.deleteOtp(it) }
        }
    }
}
