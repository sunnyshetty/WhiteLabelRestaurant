package com.skyhightechnology.restaurantengine.domain.usecase

import com.skyhightechnology.restaurantengine.domain.model.RestaurantBranch
import com.skyhightechnology.restaurantengine.domain.repository.RestaurantRepository
import kotlinx.coroutines.flow.Flow

class StreamBranchesUseCase(private val repository: RestaurantRepository) {
    operator fun invoke(restaurantId: String): Flow<List<RestaurantBranch>> =
        repository.streamBranches(restaurantId)
}
