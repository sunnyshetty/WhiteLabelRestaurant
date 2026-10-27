package com.skyhightechnology.restaurantengine.presentation.reservation

data class ReservationState(
    val isSubmitting: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
)

sealed interface ReservationAction {
    data class Submit(
        val branchId: String?,
        val customerName: String,
        val partySize: Int,
        val reservationTime: String,
    ) : ReservationAction

    data object DismissError : ReservationAction

    /** Clears a completed booking so the form can be reused. */
    data object Reset : ReservationAction
}
