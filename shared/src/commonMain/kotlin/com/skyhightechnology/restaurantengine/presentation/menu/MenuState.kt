package com.skyhightechnology.restaurantengine.presentation.menu

import com.skyhightechnology.restaurantengine.domain.model.MenuItem
import com.skyhightechnology.restaurantengine.presentation.LoadError

/** The branch menu grouped into [categories], in the order each category first appears on the menu. */
data class MenuState(
    val categories: List<MenuCategory> = emptyList(),
    val isLoading: Boolean = false,
    val error: LoadError? = null,
)

/** One tab/page of the menu. */
data class MenuCategory(val name: String, val items: List<MenuItem>)

sealed interface MenuAction {
    data class SelectBranch(val branchId: String) : MenuAction
}
