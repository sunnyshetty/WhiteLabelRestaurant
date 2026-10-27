package com.skyhightechnology.restaurantengine.presentation.onboarding

import com.skyhightechnology.restaurantengine.domain.model.RestaurantBranch
import com.skyhightechnology.restaurantengine.presentation.LoadError

data class OnboardingState(
    val branches: List<RestaurantBranch> = emptyList(),
    val isLoading: Boolean = false,
    val error: LoadError? = null,
)

sealed interface OnboardingAction {
    data object Retry : OnboardingAction
}
