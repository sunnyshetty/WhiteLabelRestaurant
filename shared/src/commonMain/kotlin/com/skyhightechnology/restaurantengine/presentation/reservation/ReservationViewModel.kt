package com.skyhightechnology.restaurantengine.presentation.reservation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skyhightechnology.restaurantengine.domain.model.DataError
import com.skyhightechnology.restaurantengine.domain.model.DataException
import com.skyhightechnology.restaurantengine.domain.model.Reservation
import com.skyhightechnology.restaurantengine.domain.repository.RestaurantRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ReservationViewModel(
    private val restaurantId: String,
    private val repository: RestaurantRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ReservationState())
    val state: StateFlow<ReservationState> = _state.asStateFlow()

    fun onAction(action: ReservationAction) {
        when (action) {
            is ReservationAction.Submit -> submit(action)
            ReservationAction.DismissError -> _state.update { it.copy(error = null) }
            ReservationAction.Reset -> _state.value = ReservationState()
        }
    }

    private fun submit(entry: ReservationAction.Submit) {
        if (_state.value.isSubmitting) return
        val validationError = validate(entry)
        if (validationError != null) {
            _state.update { it.copy(isSuccess = false, error = validationError) }
            return
        }

        _state.value = ReservationState(isSubmitting = true)
        val reservation = Reservation(
            id = "", // Firestore assigns the document id.
            branchId = entry.branchId.orEmpty(),
            customerName = entry.customerName.trim(),
            partySize = entry.partySize,
            reservationTime = entry.reservationTime.trim(),
            status = STATUS_PENDING,
        )
        viewModelScope.launch {
            try {
                repository.createReservation(restaurantId, reservation)
                _state.value = ReservationState(isSuccess = true)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.value = ReservationState(error = e.toReservationError())
            }
        }
    }

    private fun validate(entry: ReservationAction.Submit): ReservationError? = when {
        entry.branchId.isNullOrBlank() -> ReservationError.NoBranch
        entry.customerName.isBlank() -> ReservationError.MissingName
        entry.partySize < 1 -> ReservationError.MissingPartySize
        entry.reservationTime.isBlank() -> ReservationError.MissingTime
        else -> null
    }

    private fun Exception.toReservationError(): ReservationError = when ((this as? DataException)?.error) {
        DataError.Offline -> ReservationError.Offline
        DataError.PermissionDenied -> ReservationError.PermissionDenied
        DataError.NotFound, DataError.Unknown, null -> ReservationError.SubmitFailed
    }

    private companion object {
        const val STATUS_PENDING = "PENDING"
    }
}
