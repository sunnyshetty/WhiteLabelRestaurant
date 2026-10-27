package com.skyhightechnology.restaurantengine.presentation.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skyhightechnology.restaurantengine.domain.model.MenuItem
import com.skyhightechnology.restaurantengine.domain.repository.RestaurantRepository
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

/** Streams the selected branch's menu live, grouped by category; switching branch cancels the previous stream. */
class MenuViewModel(
    private val restaurantId: String,
    private val repository: RestaurantRepository,
) : ViewModel() {

    private val branchId = MutableStateFlow<String?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<MenuState> = branchId.flatMapLatest { id ->
        if (id == null) {
            flowOf(MenuState())
        } else {
            repository.streamBranchMenu(restaurantId, id)
                .map { MenuState(categories = it.groupByCategory()) }
                .onStart { emit(MenuState(isLoading = true)) }
                .catch { emit(MenuState(error = it.toLoadError())) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MenuState())

    fun onAction(action: MenuAction) {
        when (action) {
            is MenuAction.SelectBranch -> branchId.value = action.branchId
        }
    }

    private fun List<MenuItem>.groupByCategory(): List<MenuCategory> =
        groupBy { it.category }.map { (name, items) -> MenuCategory(name, items) }
}
