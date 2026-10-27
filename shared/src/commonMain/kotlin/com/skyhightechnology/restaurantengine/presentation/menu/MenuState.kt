package com.skyhightechnology.restaurantengine.presentation.menu

import com.skyhightechnology.restaurantengine.domain.model.MenuItem
import com.skyhightechnology.restaurantengine.presentation.LoadError

/**
 * [items] is already filtered to [selectedCategory]; `null` means the "All" tab.
 * [categories] lists every category on the branch menu, in menu order, for the tab row.
 */
data class MenuState(
    val items: List<MenuItem> = emptyList(),
    val categories: List<String> = emptyList(),
    val selectedCategory: String? = null,
    val isLoading: Boolean = false,
    val error: LoadError? = null,
)

sealed interface MenuAction {
    data class SelectBranch(val branchId: String) : MenuAction
    data class SelectCategory(val category: String?) : MenuAction
}
