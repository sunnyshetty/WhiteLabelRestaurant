package com.skyhightechnology.restaurantengine.presentation.theme

import com.skyhightechnology.restaurantengine.domain.model.RestaurantConfig

/** App-wide tenant state: the brand's corporate config and the branch the guest chose. */
data class GlobalThemeState(
    val brandConfig: RestaurantConfig? = null,
    val selectedBranchId: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)

sealed interface GlobalConfigAction {
    data object Retry : GlobalConfigAction
    data class SelectBranch(val branchId: String) : GlobalConfigAction
}
