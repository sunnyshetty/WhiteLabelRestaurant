package com.skyhightechnology.restaurantengine.domain.model

data class RestaurantBranch(
    val id: String,
    val name: String,
    val address: String,
    val phoneNumber: String,
    /** Free text as entered by the restaurant, e.g. "Monday - Sunday: 12:00 - 22:00"; empty when not set. */
    val openingHours: String = "",
    /** Whether tables can still be booked today; `null` when the branch doesn't say. */
    val hasTablesToday: Boolean? = null,
)
