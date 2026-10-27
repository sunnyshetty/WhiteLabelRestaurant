package com.skyhightechnology.restaurantengine.presentation.restaurant

import com.skyhightechnology.restaurantengine.domain.model.RestaurantBranch
import com.skyhightechnology.restaurantengine.presentation.LoadError

/** The selected branch's details; [error] is [LoadError.NotFound] when the branch no longer exists. */
data class RestaurantState(
    val branch: RestaurantBranch? = null,
    val isLoading: Boolean = false,
    val error: LoadError? = null,
)

sealed interface RestaurantAction {
    data class SelectBranch(val branchId: String) : RestaurantAction
}
