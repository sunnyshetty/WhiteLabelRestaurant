package com.skyhightechnology.restaurantengine.presentation.reservation

data class ReservationState(
    val isSubmitting: Boolean = false,
    val isSuccess: Boolean = false,
    val error: ReservationError? = null,
)

/** Validation and submit failures. The UI maps each case to one string resource. */
sealed interface ReservationError {
    data object NoBranch : ReservationError
    data object MissingName : ReservationError
    data object MissingPhone : ReservationError
    data object InvalidEmail : ReservationError
    data object MissingPartySize : ReservationError
    data object MissingTime : ReservationError
    data object Offline : ReservationError
    data object PermissionDenied : ReservationError
    data object SubmitFailed : ReservationError
}

sealed interface ReservationAction {
    data class Submit(
        val branchId: String?,
        val customerName: String,
        val customerPhone: String,
        val customerEmail: String,
        val specialRequests: String,
        val partySize: Int,
        val reservationTime: String,
    ) : ReservationAction

    data object DismissError : ReservationAction

    /** Clears a completed booking so the form can be reused. */
    data object Reset : ReservationAction
}
