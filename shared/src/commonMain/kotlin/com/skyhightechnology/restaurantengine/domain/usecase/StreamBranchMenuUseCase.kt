package com.skyhightechnology.restaurantengine.domain.usecase

import com.skyhightechnology.restaurantengine.domain.model.MenuItem
import com.skyhightechnology.restaurantengine.domain.repository.RestaurantRepository
import kotlinx.coroutines.flow.Flow

class StreamBranchMenuUseCase(private val repository: RestaurantRepository) {
    operator fun invoke(restaurantId: String, branchId: String): Flow<List<MenuItem>> =
        repository.streamBranchMenu(restaurantId, branchId)
}
