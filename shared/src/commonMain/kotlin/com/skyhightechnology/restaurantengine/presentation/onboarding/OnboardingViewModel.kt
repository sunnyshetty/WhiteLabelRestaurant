package com.skyhightechnology.restaurantengine.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skyhightechnology.restaurantengine.domain.usecase.StreamBranchesUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** Streams the tenant's branches live. Picking one is a [GlobalConfigAction.SelectBranch][com.skyhightechnology.restaurantengine.presentation.theme.GlobalConfigAction.SelectBranch]. */
class OnboardingViewModel(
    restaurantId: String,
    streamBranches: StreamBranchesUseCase,
) : ViewModel() {

    val state: StateFlow<OnboardingState> = streamBranches(restaurantId)
        .map { OnboardingState(branches = it) }
        .catch { emit(OnboardingState(error = it.message ?: "Couldn't load branches")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OnboardingState(isLoading = true))
}
