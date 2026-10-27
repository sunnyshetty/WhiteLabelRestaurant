package com.skyhightechnology.restaurantengine.domain.repository

import com.skyhightechnology.restaurantengine.domain.model.MenuItem
import com.skyhightechnology.restaurantengine.domain.model.Reservation
import com.skyhightechnology.restaurantengine.domain.model.RestaurantBranch
import com.skyhightechnology.restaurantengine.domain.model.RestaurantConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow

/** In-memory [RestaurantRepository]: push data through the flows, or set the `*Error` fields to fail calls. */
class FakeRestaurantRepository : RestaurantRepository {
    var config: RestaurantConfig? = null
    var configError: Exception? = null
    val requestedConfigIds = mutableListOf<String>()

    val branches = MutableStateFlow<List<RestaurantBranch>>(emptyList())
    private val menus = mutableMapOf<String, MutableStateFlow<List<MenuItem>>>()
    var streamError: Exception? = null

    var createError: Exception? = null
    val createdReservations = mutableListOf<Pair<String, Reservation>>()

    fun menu(branchId: String): MutableStateFlow<List<MenuItem>> =
        menus.getOrPut(branchId) { MutableStateFlow(emptyList()) }

    override suspend fun getCorporateConfig(restaurantId: String): RestaurantConfig {
        requestedConfigIds += restaurantId
        configError?.let { throw it }
        return checkNotNull(config) { "No config set on the fake" }
    }

    override fun streamBranches(restaurantId: String): Flow<List<RestaurantBranch>> =
        streamError?.let { error -> flow { throw error } } ?: branches

    override fun streamBranchMenu(restaurantId: String, branchId: String): Flow<List<MenuItem>> =
        streamError?.let { error -> flow { throw error } } ?: menu(branchId)

    override suspend fun createReservation(restaurantId: String, reservation: Reservation) {
        createError?.let { throw it }
        createdReservations += restaurantId to reservation
    }
}
