package com.velora.vault.feature.onboarding

import androidx.lifecycle.ViewModel
import com.velora.vault.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {
    fun completeOnboarding() {
        authRepository.setOnboardingCompleted()
    }
}
