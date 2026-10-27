package com.skyhightechnology.restaurantengine.domain.model

/** A table booking at one branch. An empty [id] means the backend assigns one on creation. */
data class Reservation(
    val id: String,
    val branchId: String,
    val customerName: String,
    val partySize: Int,
    val reservationTime: String,
    val status: String,
)
