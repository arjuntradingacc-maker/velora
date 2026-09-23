package com.downlifeblues.velora.feature.onboarding

import androidx.lifecycle.ViewModel
import com.downlifeblues.velora.data.repository.AuthRepository
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
