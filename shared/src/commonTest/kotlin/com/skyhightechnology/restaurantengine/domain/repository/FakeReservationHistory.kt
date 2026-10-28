package com.skyhightechnology.restaurantengine.domain.repository

/** In-memory [ReservationHistory], keyed like the real one by restaurant and branch. */
class FakeReservationHistory : ReservationHistory {
    val saved = mutableMapOf<Pair<String, String>, String>()

    override fun lastReservationId(restaurantId: String, branchId: String): String? = saved[restaurantId to branchId]

    override fun saveReservationId(restaurantId: String, branchId: String, reservationId: String) {
        saved[restaurantId to branchId] = reservationId
    }
}
