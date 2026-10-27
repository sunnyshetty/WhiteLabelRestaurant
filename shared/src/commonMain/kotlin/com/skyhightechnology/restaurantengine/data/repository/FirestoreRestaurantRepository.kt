package com.skyhightechnology.restaurantengine.data.repository

import com.skyhightechnology.restaurantengine.data.model.BranchDto
import com.skyhightechnology.restaurantengine.data.model.MenuItemDto
import com.skyhightechnology.restaurantengine.data.model.RestaurantConfigDto
import com.skyhightechnology.restaurantengine.data.model.toDomain
import com.skyhightechnology.restaurantengine.data.model.toDto
import com.skyhightechnology.restaurantengine.domain.model.DataError
import com.skyhightechnology.restaurantengine.domain.model.DataException
import com.skyhightechnology.restaurantengine.domain.model.MenuItem
import com.skyhightechnology.restaurantengine.domain.model.Reservation
import com.skyhightechnology.restaurantengine.domain.model.RestaurantBranch
import com.skyhightechnology.restaurantengine.domain.model.RestaurantConfig
import com.skyhightechnology.restaurantengine.domain.repository.RestaurantRepository
import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Multi-tenant Firestore layout: everything for a tenant lives under `restaurants/{restaurantId}`,
 * with branches as a sub-collection and each branch owning its `menu` and `reservations`.
 * Every failure surfaces as a [DataException]; Firestore exceptions never escape.
 */
class FirestoreRestaurantRepository(
    private val firestore: FirebaseFirestore,
) : RestaurantRepository {

    override suspend fun getCorporateConfig(restaurantId: String): RestaurantConfig = firestoreCall {
        val snapshot = restaurant(restaurantId).get()
        if (!snapshot.exists) throw DataException(DataError.NotFound)
        snapshot.data<RestaurantConfigDto>().toDomain(snapshot.id)
    }

    override fun streamBranches(restaurantId: String): Flow<List<RestaurantBranch>> =
        restaurant(restaurantId).collection(BRANCHES).snapshots
            .map { query -> query.documents.map { it.data<BranchDto>().toDomain(it.id) } }
            .mapFirestoreErrors()

    override fun streamBranchMenu(restaurantId: String, branchId: String): Flow<List<MenuItem>> =
        branch(restaurantId, branchId).collection(MENU).snapshots
            .map { query -> query.documents.map { it.data<MenuItemDto>().toDomain(it.id) } }
            .mapFirestoreErrors()

    override suspend fun createReservation(restaurantId: String, reservation: Reservation) {
        firestoreCall {
            val reservations = branch(restaurantId, reservation.branchId).collection(RESERVATIONS)
            if (reservation.id.isBlank()) {
                reservations.add(reservation.toDto())
            } else {
                reservations.document(reservation.id).set(reservation.toDto())
            }
        }
    }

    private fun restaurant(restaurantId: String) = firestore.collection(RESTAURANTS).document(restaurantId)

    private fun branch(restaurantId: String, branchId: String) =
        restaurant(restaurantId).collection(BRANCHES).document(branchId)

    private companion object {
        const val RESTAURANTS = "restaurants"
        const val BRANCHES = "branches"
        const val MENU = "menu"
        const val RESERVATIONS = "reservations"
    }
}
