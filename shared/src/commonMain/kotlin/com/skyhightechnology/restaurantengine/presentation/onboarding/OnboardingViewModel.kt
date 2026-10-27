package com.skyhightechnology.restaurantengine.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skyhightechnology.restaurantengine.domain.repository.RestaurantRepository
import com.skyhightechnology.restaurantengine.presentation.toLoadError
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

/** Streams the tenant's branches live. Picking one is a [GlobalConfigAction.SelectBranch][com.skyhightechnology.restaurantengine.presentation.theme.GlobalConfigAction.SelectBranch]. */
class OnboardingViewModel(
    restaurantId: String,
    repository: RestaurantRepository,
) : ViewModel() {

    private val attempt = MutableStateFlow(0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<OnboardingState> = attempt
        .flatMapLatest {
            repository.streamBranches(restaurantId)
                .map { OnboardingState(branches = it) }
                .onStart { emit(OnboardingState(isLoading = true)) }
                .catch { emit(OnboardingState(error = it.toLoadError())) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OnboardingState(isLoading = true))

    fun onAction(action: OnboardingAction) {
        when (action) {
            OnboardingAction.Retry -> attempt.value++
        }
    }
}
