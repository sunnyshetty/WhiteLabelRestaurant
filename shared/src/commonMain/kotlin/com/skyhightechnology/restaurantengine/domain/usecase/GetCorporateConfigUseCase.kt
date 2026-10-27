package com.skyhightechnology.restaurantengine.domain.usecase

import com.skyhightechnology.restaurantengine.domain.model.RestaurantConfig
import com.skyhightechnology.restaurantengine.domain.repository.RestaurantRepository

class GetCorporateConfigUseCase(private val repository: RestaurantRepository) {
    suspend operator fun invoke(restaurantId: String): RestaurantConfig =
        repository.getCorporateConfig(restaurantId)
}
