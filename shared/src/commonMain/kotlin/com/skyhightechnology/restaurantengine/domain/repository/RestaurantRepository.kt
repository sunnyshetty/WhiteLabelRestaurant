package com.skyhightechnology.restaurantengine.domain.repository

import com.skyhightechnology.restaurantengine.domain.model.MenuItem
import com.skyhightechnology.restaurantengine.domain.model.Reservation
import com.skyhightechnology.restaurantengine.domain.model.RestaurantBranch
import com.skyhightechnology.restaurantengine.domain.model.RestaurantConfig
import kotlinx.coroutines.flow.Flow

/** Implementations report every failure as a [com.skyhightechnology.restaurantengine.domain.model.DataException]. */
interface RestaurantRepository {
    suspend fun getCorporateConfig(restaurantId: String): RestaurantConfig

    fun streamBranches(restaurantId: String): Flow<List<RestaurantBranch>>

    fun streamBranchMenu(restaurantId: String, branchId: String): Flow<List<MenuItem>>

    /** Writes [reservation] under its branch, [Reservation.branchId]. */
    suspend fun createReservation(restaurantId: String, reservation: Reservation)
}
