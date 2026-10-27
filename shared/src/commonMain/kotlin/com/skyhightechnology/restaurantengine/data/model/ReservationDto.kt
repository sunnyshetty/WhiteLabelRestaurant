package com.skyhightechnology.restaurantengine.data.model

import com.skyhightechnology.restaurantengine.domain.model.Reservation
import kotlinx.serialization.Serializable

/** Firestore document `restaurants/{restaurantId}/branches/{branchId}/reservations/{reservationId}`. */
@Serializable
data class ReservationDto(
    val branchId: String = "",
    val customerName: String = "",
    val partySize: Int = 0,
    val reservationTime: String = "",
    val status: String = "",
)

fun ReservationDto.toDomain(id: String) = Reservation(
    id = id,
    branchId = branchId,
    customerName = customerName,
    partySize = partySize,
    reservationTime = reservationTime,
    status = status,
)

fun Reservation.toDto() = ReservationDto(
    branchId = branchId,
    customerName = customerName,
    partySize = partySize,
    reservationTime = reservationTime,
    status = status,
)
