package com.skyhightechnology.restaurantengine.data.repository

import com.russhwolf.settings.Settings
import com.skyhightechnology.restaurantengine.domain.repository.ReservationHistory

/** [ReservationHistory] in the platform's key-value store (SharedPreferences, NSUserDefaults, java.util.prefs). */
class SettingsReservationHistory(
    private val settings: Settings,
) : ReservationHistory {

    override fun lastReservationId(restaurantId: String, branchId: String): String? =
        settings.getStringOrNull(key(restaurantId, branchId))

    override fun saveReservationId(restaurantId: String, branchId: String, reservationId: String) {
        settings.putString(key(restaurantId, branchId), reservationId)
    }

    private fun key(restaurantId: String, branchId: String) = "reservation.$restaurantId.$branchId"
}
