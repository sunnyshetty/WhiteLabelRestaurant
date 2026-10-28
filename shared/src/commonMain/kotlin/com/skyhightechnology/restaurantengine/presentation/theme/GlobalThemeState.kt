package com.skyhightechnology.restaurantengine.presentation.theme

import com.skyhightechnology.restaurantengine.domain.model.RestaurantConfig
import com.skyhightechnology.restaurantengine.presentation.LoadError

/** App-wide tenant state: the brand's corporate config. The chosen branch lives in the navigation back stack. */
data class GlobalThemeState(
    val brandConfig: RestaurantConfig? = null,
    val isLoading: Boolean = false,
    val error: LoadError? = null,
)

sealed interface GlobalConfigAction {
    data object Retry : GlobalConfigAction
}
