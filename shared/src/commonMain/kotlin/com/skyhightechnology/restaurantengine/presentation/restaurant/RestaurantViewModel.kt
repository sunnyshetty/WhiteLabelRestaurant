package com.skyhightechnology.restaurantengine.presentation.restaurant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skyhightechnology.restaurantengine.domain.repository.RestaurantRepository
import com.skyhightechnology.restaurantengine.presentation.LoadError
import com.skyhightechnology.restaurantengine.presentation.toLoadError
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

/** Streams the selected branch live from the tenant's branch list, so edits in Firestore show immediately. */
class RestaurantViewModel(
    private val restaurantId: String,
    private val repository: RestaurantRepository,
) : ViewModel() {

    private val branchId = MutableStateFlow<String?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<RestaurantState> = branchId.flatMapLatest { id ->
        if (id == null) {
            flowOf(RestaurantState())
        } else {
            repository.streamBranches(restaurantId)
                .map { branches ->
                    branches.find { it.id == id }?.let { RestaurantState(branch = it) }
                        ?: RestaurantState(error = LoadError.NotFound)
                }
                .onStart { emit(RestaurantState(isLoading = true)) }
                .catch { emit(RestaurantState(error = it.toLoadError())) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RestaurantState())

    fun onAction(action: RestaurantAction) {
        when (action) {
            is RestaurantAction.SelectBranch -> branchId.value = action.branchId
        }
    }
}
