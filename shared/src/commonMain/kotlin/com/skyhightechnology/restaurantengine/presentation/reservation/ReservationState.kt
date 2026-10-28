package com.skyhightechnology.restaurantengine.presentation.reservation

import com.skyhightechnology.restaurantengine.domain.model.Reservation

data class ReservationState(
    /** Looking up the booking this device made earlier at the branch. */
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    /** The guest's upcoming booking at this branch, shown instead of the form. Also set by a successful submit. */
    val reservation: Reservation? = null,
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
    /** Shows the guest's upcoming booking at [branchId], if this device made one, instead of the form. */
    data class SelectBranch(val branchId: String) : ReservationAction

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

    /** Puts the form back in place of the booking, to book another table. */
    data object Reset : ReservationAction
}
