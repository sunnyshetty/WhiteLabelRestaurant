package com.skyhightechnology.restaurantengine.presentation.onboarding

import com.skyhightechnology.restaurantengine.domain.model.RestaurantBranch

data class OnboardingState(
    val branches: List<RestaurantBranch> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)
