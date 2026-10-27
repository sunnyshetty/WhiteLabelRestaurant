package com.skyhightechnology.restaurantengine.presentation.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.skyhightechnology.restaurantengine.domain.model.RestaurantConfig
import com.skyhightechnology.restaurantengine.presentation.LoadError
import com.skyhightechnology.restaurantengine.presentation.preview.BrandPreviewProvider
import com.skyhightechnology.restaurantengine.presentation.preview.PreviewData
import com.skyhightechnology.restaurantengine.presentation.preview.PreviewTheme
import com.skyhightechnology.restaurantengine.presentation.preview.ScreenPreview

@ScreenPreview
@Composable
private fun OnboardingBranchesPreview(@PreviewParameter(BrandPreviewProvider::class) brand: RestaurantConfig) {
    PreviewTheme(brand) { Onboarding(OnboardingState(branches = PreviewData.branches), brand.name) }
}

@ScreenPreview
@Composable
private fun OnboardingLoadingPreview() {
    PreviewTheme { Onboarding(OnboardingState(isLoading = true)) }
}

@ScreenPreview
@Composable
private fun OnboardingEmptyPreview() {
    PreviewTheme { Onboarding(OnboardingState()) }
}

@ScreenPreview
@Composable
private fun OnboardingErrorPreview() {
    PreviewTheme { Onboarding(OnboardingState(error = LoadError.Offline)) }
}

@Composable
private fun Onboarding(state: OnboardingState, brandName: String = PreviewData.burgerJoint.name) {
    OnboardingContent(brandName = brandName, state = state, onBranchSelected = {}, onRetry = {})
}
