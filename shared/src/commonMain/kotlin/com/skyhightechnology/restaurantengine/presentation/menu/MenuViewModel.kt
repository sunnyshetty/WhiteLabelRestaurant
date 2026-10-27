package com.skyhightechnology.restaurantengine.presentation.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skyhightechnology.restaurantengine.domain.model.MenuItem
import com.skyhightechnology.restaurantengine.domain.usecase.StreamBranchMenuUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

/** Streams the selected branch's menu live; switching branch cancels the previous stream. */
class MenuViewModel(
    private val restaurantId: String,
    private val streamBranchMenu: StreamBranchMenuUseCase,
) : ViewModel() {

    private val branchId = MutableStateFlow<String?>(null)
    private val selectedCategory = MutableStateFlow<String?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val menu: Flow<MenuLoad> = branchId.flatMapLatest { id ->
        if (id == null) {
            flowOf(MenuLoad.Idle)
        } else {
            streamBranchMenu(restaurantId, id)
                .map<List<MenuItem>, MenuLoad> { MenuLoad.Loaded(it) }
                .onStart { emit(MenuLoad.Loading) }
                .catch { emit(MenuLoad.Failed(it.message ?: "Couldn't load the menu")) }
        }
    }

    val state: StateFlow<MenuState> = combine(menu, selectedCategory) { load, category ->
        when (load) {
            MenuLoad.Idle -> MenuState()
            MenuLoad.Loading -> MenuState(selectedCategory = category, isLoading = true)
            is MenuLoad.Failed -> MenuState(selectedCategory = category, error = load.message)
            is MenuLoad.Loaded -> MenuState(
                items = if (category == null) load.items else load.items.filter { it.category == category },
                categories = load.items.map { it.category }.distinct(),
                selectedCategory = category,
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MenuState())

    fun onAction(action: MenuAction) {
        when (action) {
            is MenuAction.SelectBranch -> {
                selectedCategory.value = null
                branchId.value = action.branchId
            }
            is MenuAction.SelectCategory -> selectedCategory.value = action.category
        }
    }

    private sealed interface MenuLoad {
        data object Idle : MenuLoad
        data object Loading : MenuLoad
        data class Loaded(val items: List<MenuItem>) : MenuLoad
        data class Failed(val message: String) : MenuLoad
    }
}
