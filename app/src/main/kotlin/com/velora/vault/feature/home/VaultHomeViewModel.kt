package com.velora.vault.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.velora.vault.data.model.VaultItemSummary
import com.velora.vault.data.repository.AuthRepository
import com.velora.vault.data.repository.SecurityIssueGroup
import com.velora.vault.data.repository.SecurityOverview
import com.velora.vault.data.repository.SecurityRepository
import com.velora.vault.data.repository.VaultRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalTime
import javax.inject.Inject

data class VaultHomeUiState(
    val greeting: String = "Welcome",
    val accountInitial: String = "V",
    val recentItems: List<VaultItemSummary> = emptyList(),
    val security: SecurityOverview = SecurityOverview(100, 0, emptyList()),
    val isLoading: Boolean = true,
)

@HiltViewModel
class VaultHomeViewModel @Inject constructor(
    vaultRepository: VaultRepository,
    securityRepository: SecurityRepository,
    authRepository: AuthRepository,
) : ViewModel() {

    val uiState: StateFlow<VaultHomeUiState> = combine(
        vaultRepository.observeRecentSummaries(8),
        securityRepository.observeOverview(),
    ) { recent, security ->
        val name = authRepository.currentAccount()?.displayName
        VaultHomeUiState(
            greeting = greetingFor(name),
            accountInitial = (name?.firstOrNull() ?: 'V').uppercaseChar().toString(),
            recentItems = recent,
            security = security,
            isLoading = false,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), VaultHomeUiState())

    private fun greetingFor(name: String?): String {
        val hour = LocalTime.now().hour
        val timeGreeting = when {
            hour < 5 -> "Good night"
            hour < 12 -> "Good morning"
            hour < 18 -> "Good afternoon"
            else -> "Good evening"
        }
        return if (name.isNullOrBlank()) timeGreeting else "$timeGreeting, ${name.substringBefore(" ")}"
    }
}

val SecurityOverview.topIssues: List<SecurityIssueGroup> get() = issues.take(3)
