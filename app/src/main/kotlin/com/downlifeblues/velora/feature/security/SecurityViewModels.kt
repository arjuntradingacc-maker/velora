package com.downlifeblues.velora.feature.security

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.downlifeblues.velora.data.repository.BreachCheckRepository
import com.downlifeblues.velora.data.repository.BreachCheckResult
import com.downlifeblues.velora.data.repository.SecurityIssueGroup
import com.downlifeblues.velora.data.repository.SecurityIssueType
import com.downlifeblues.velora.data.repository.SecurityOverview
import com.downlifeblues.velora.data.repository.SecurityRepository
import com.downlifeblues.velora.data.repository.VaultRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SecurityCenterViewModel @Inject constructor(
    securityRepository: SecurityRepository,
) : ViewModel() {
    val overview: StateFlow<SecurityOverview> = securityRepository.observeOverview()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SecurityOverview(100, 0, emptyList()))

    val scoreExplanation: String = SecurityRepository.SCORE_EXPLANATION
}

@HiltViewModel
class SecurityIssueDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    securityRepository: SecurityRepository,
) : ViewModel() {
    val issueType: SecurityIssueType? = savedStateHandle.get<String>("type")
        ?.let { runCatching { SecurityIssueType.valueOf(it) }.getOrNull() }

    val issue: StateFlow<SecurityIssueGroup?> = securityRepository.observeOverview()
        .map { overview -> overview.issues.firstOrNull { it.type == issueType } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}

@HiltViewModel
class BreachMonitoringViewModel @Inject constructor(
    private val breachCheckRepository: BreachCheckRepository,
    private val vaultRepository: VaultRepository,
) : ViewModel() {

    private val _checking = MutableStateFlow(false)
    val isChecking: StateFlow<Boolean> = _checking.asStateFlow()

    private val _lastResult = MutableStateFlow<BreachCheckResult?>(null)
    val lastResult: StateFlow<BreachCheckResult?> = _lastResult.asStateFlow()

    val breachRecords = vaultRepository.observeBreachRecords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun runCheck() {
        if (_checking.value) return
        viewModelScope.launch {
            _checking.value = true
            val logins = vaultRepository.allLoginsOnce()
            _lastResult.value = breachCheckRepository.checkAll(logins)
            _checking.value = false
        }
    }
}
