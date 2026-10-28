package com.skyhightechnology.restaurantengine.domain.repository

/**
 * Remembers, on this device, the last reservation the guest made at each branch.
 * Guests have no account, so this is how the app finds their booking again.
 */
interface ReservationHistory {
    fun lastReservationId(restaurantId: String, branchId: String): String?

    fun saveReservationId(restaurantId: String, branchId: String, reservationId: String)
}
