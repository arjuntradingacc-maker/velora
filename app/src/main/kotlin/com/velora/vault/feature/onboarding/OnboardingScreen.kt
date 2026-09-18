package com.velora.vault.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.velora.vault.core.design.Velora
import com.velora.vault.core.design.VeloraSpacing
import com.velora.vault.core.design.VeloraType
import com.velora.vault.core.design.components.VeloraPrimaryButton
import com.velora.vault.core.design.components.VeloraTextButton
import kotlinx.coroutines.launch

private const val PAGE_COUNT = 5

@Composable
fun OnboardingScreen(onDone: () -> Unit, viewModel: OnboardingViewModel = hiltViewModel()) {
    val colors = Velora.colors
    val pagerState = rememberPagerState(pageCount = { PAGE_COUNT })
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current

    fun finish() {
        viewModel.completeOnboarding()
        onDone()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .systemBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = VeloraSpacing.lg, vertical = VeloraSpacing.sm),
            horizontalArrangement = Arrangement.End,
        ) {
            if (pagerState.currentPage < PAGE_COUNT - 1) {
                VeloraTextButton(text = "Skip", onClick = { finish() }, color = colors.textSecondary)
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
        ) { page ->
            when (page) {
                0 -> VaultHeroPage()
                1 -> ItemsFlowingPage()
                2 -> BiometricPage()
                3 -> ZeroKnowledgePage()
                else -> GetStartedPage(onCreateAccount = { finish() }, onSignIn = { finish() })
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = VeloraSpacing.lg),
            horizontalArrangement = Arrangement.Center,
        ) {
            repeat(PAGE_COUNT) { index ->
                val selected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(if (selected) 8.dp else 6.dp)
                        .clip(CircleShape)
                        .background(if (selected) colors.accent else colors.surfaceElevated),
                )
            }
        }

        if (pagerState.currentPage < PAGE_COUNT - 1) {
            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = VeloraSpacing.xl, vertical = VeloraSpacing.md)) {
                VeloraPrimaryButton(
                    text = "Continue",
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
internal fun OnboardingHeadline(text: String) {
    Text(
        text = text,
        style = VeloraType.displayMedium,
        color = Velora.colors.textPrimary,
        modifier = Modifier.padding(horizontal = VeloraSpacing.xl),
    )
}

@Composable
internal fun OnboardingBody(text: String) {
    Text(
        text = text,
        style = VeloraType.body,
        color = Velora.colors.textSecondary,
        modifier = Modifier.padding(horizontal = VeloraSpacing.xl, vertical = VeloraSpacing.sm),
    )
}
