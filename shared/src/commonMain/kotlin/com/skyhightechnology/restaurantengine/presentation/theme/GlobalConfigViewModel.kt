package com.skyhightechnology.restaurantengine.presentation.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skyhightechnology.restaurantengine.domain.usecase.GetCorporateConfigUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Loads the tenant's corporate config on creation. [restaurantId] is the platform application ID
 * (the `TenantId` Koin binding), which doubles as the tenant's `restaurants/{restaurantId}` document.
 */
class GlobalConfigViewModel(
    private val restaurantId: String,
    private val getCorporateConfig: GetCorporateConfigUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(GlobalThemeState(isLoading = true))
    val state: StateFlow<GlobalThemeState> = _state.asStateFlow()

    init {
        loadConfig()
    }

    fun onAction(action: GlobalConfigAction) {
        when (action) {
            GlobalConfigAction.Retry -> loadConfig()
            is GlobalConfigAction.SelectBranch -> _state.update { it.copy(selectedBranchId = action.branchId) }
        }
    }

    private fun loadConfig() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val config = getCorporateConfig(restaurantId)
                _state.update { it.copy(brandConfig = config, isLoading = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message ?: "Couldn't load restaurant config") }
            }
        }
    }
}
