package com.skyhightechnology.restaurantengine.presentation.reservation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skyhightechnology.restaurantengine.domain.model.DataError
import com.skyhightechnology.restaurantengine.domain.model.DataException
import com.skyhightechnology.restaurantengine.domain.model.Reservation
import com.skyhightechnology.restaurantengine.domain.repository.ReservationHistory
import com.skyhightechnology.restaurantengine.domain.repository.RestaurantRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Books a table, or shows the guest's upcoming booking at the branch instead. Guests have no account,
 * so [history] remembers on the device which reservation they made; the booking itself is streamed live
 * from the repository, so a status change by the branch shows straight away.
 */
class ReservationViewModel(
    private val restaurantId: String,
    private val repository: RestaurantRepository,
    private val history: ReservationHistory,
    private val clock: Clock = Clock.System,
) : ViewModel() {

    private val _state = MutableStateFlow(ReservationState())
    val state: StateFlow<ReservationState> = _state.asStateFlow()

    private var branchId: String? = null
    private var watchJob: Job? = null

    fun onAction(action: ReservationAction) {
        when (action) {
            is ReservationAction.SelectBranch -> selectBranch(action.branchId)
            is ReservationAction.Submit -> submit(action)
            ReservationAction.DismissError -> _state.update { it.copy(error = null) }
            ReservationAction.Reset -> {
                watchJob?.cancel()
                _state.value = ReservationState()
            }
        }
    }

    private fun selectBranch(branchId: String) {
        if (branchId == this.branchId) return
        this.branchId = branchId
        val reservationId = history.lastReservationId(restaurantId, branchId)
        if (reservationId == null) {
            watchJob?.cancel()
            _state.value = ReservationState()
        } else {
            _state.value = ReservationState(isLoading = true)
            watch(branchId, reservationId)
        }
    }

    /** Past, deleted or unreadable bookings leave the form in place. */
    private fun watch(branchId: String, reservationId: String) {
        watchJob?.cancel()
        watchJob = viewModelScope.launch {
            repository.streamReservation(restaurantId, branchId, reservationId)
                .catch { _state.update { it.copy(isLoading = false) } }
                .collect { reservation ->
                    _state.update { it.copy(isLoading = false, reservation = reservation?.takeIf(::isUpcoming)) }
                }
        }
    }

    private fun isUpcoming(reservation: Reservation): Boolean =
        runCatching { Instant.parse(reservation.reservationTime) > clock.now() }.getOrDefault(false)

    private fun submit(entry: ReservationAction.Submit) {
        if (_state.value.isSubmitting) return
        val validationError = validate(entry)
        if (validationError != null) {
            _state.update { it.copy(error = validationError) }
            return
        }

        _state.value = ReservationState(isSubmitting = true)
        val reservation = Reservation(
            id = "", // Firestore assigns the document id.
            branchId = entry.branchId.orEmpty(),
            customerName = entry.customerName.trim(),
            customerPhone = entry.customerPhone.trim(),
            customerEmail = entry.customerEmail.trim(),
            specialRequests = entry.specialRequests.trim(),
            partySize = entry.partySize,
            reservationTime = entry.reservationTime.trim(),
            status = STATUS_PENDING,
        )
        viewModelScope.launch {
            try {
                val id = repository.createReservation(restaurantId, reservation)
                history.saveReservationId(restaurantId, reservation.branchId, id)
                _state.value = ReservationState(reservation = reservation.copy(id = id))
                watch(reservation.branchId, id)
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
        entry.customerPhone.isBlank() -> ReservationError.MissingPhone
        !EMAIL.matches(entry.customerEmail.trim()) -> ReservationError.InvalidEmail
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

        /** Shape check only (something@domain.tld); the branch confirms by contacting the guest. */
        val EMAIL = Regex("""[^@\s]+@[^@\s]+\.[^@\s]+""")
    }
}
