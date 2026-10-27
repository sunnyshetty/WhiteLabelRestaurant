package com.skyhightechnology.restaurantengine.domain.usecase

import com.skyhightechnology.restaurantengine.domain.model.Reservation
import com.skyhightechnology.restaurantengine.domain.repository.RestaurantRepository

class CreateReservationUseCase(private val repository: RestaurantRepository) {
    suspend operator fun invoke(restaurantId: String, reservation: Reservation) =
        repository.createReservation(restaurantId, reservation)
}
