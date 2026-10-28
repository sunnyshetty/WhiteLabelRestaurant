package com.skyhightechnology.restaurantengine.presentation.reservation

import app.cash.turbine.test
import com.skyhightechnology.restaurantengine.domain.model.DataError
import com.skyhightechnology.restaurantengine.domain.model.DataException
import com.skyhightechnology.restaurantengine.domain.model.Reservation
import com.skyhightechnology.restaurantengine.domain.repository.FakeReservationHistory
import com.skyhightechnology.restaurantengine.domain.repository.FakeRestaurantRepository
import com.skyhightechnology.restaurantengine.presentation.awaitItemMatching
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class ReservationViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val repository = FakeRestaurantRepository()
    private val history = FakeReservationHistory()
    private val clock = object : Clock {
        override fun now() = Instant.parse("2026-10-28T12:00:00Z")
    }

    private val validEntry = ReservationAction.Submit(
        branchId = "downtown",
        customerName = "  Ada Lovelace ",
        customerPhone = " +31 6 1234 5678 ",
        customerEmail = " ada@example.com ",
        specialRequests = " Window seat ",
        partySize = 4,
        reservationTime = "2026-10-30T19:30:00Z",
    )

    @BeforeTest fun setUp() { Dispatchers.setMain(testDispatcher) }
    @AfterTest fun tearDown() { Dispatchers.resetMain() }

    private val tenant = "com.burgerjoint.app"

    private val earlierBooking = Reservation(
        id = "r1",
        branchId = "downtown",
        customerName = "Ada Lovelace",
        customerPhone = "+31 6 1234 5678",
        customerEmail = "ada@example.com",
        specialRequests = "",
        partySize = 2,
        reservationTime = "2026-10-29T18:00:00Z",
        status = "PENDING",
    )

    private fun viewModel() = ReservationViewModel(tenant, repository, history, clock)

    /** Puts [booking] in the repository and remembers it on the "device", as an earlier submit would. */
    private fun bookedEarlier(booking: Reservation = earlierBooking) {
        repository.reservation(booking.id).value = booking
        history.saved[tenant to booking.branchId] = booking.id
    }

    private fun ReservationViewModel.openBranch(branchId: String = "downtown") {
        onAction(ReservationAction.SelectBranch(branchId))
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @Test
    fun `shows the form when this device has no booking at the branch`() = runTest {
        val viewModel = viewModel()
        viewModel.openBranch()

        assertEquals(ReservationState(), viewModel.state.value)
    }

    @Test
    fun `shows an upcoming booking instead of the form`() = runTest {
        bookedEarlier()
        val viewModel = viewModel()

        viewModel.onAction(ReservationAction.SelectBranch("downtown"))
        assertEquals(ReservationState(isLoading = true), viewModel.state.value)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(ReservationState(reservation = earlierBooking), viewModel.state.value)
    }

    @Test
    fun `only shows the booking made at the selected branch`() = runTest {
        bookedEarlier()
        val viewModel = viewModel()
        viewModel.openBranch("harbour")

        assertNull(viewModel.state.value.reservation)
    }

    @Test
    fun `shows the form for a past deleted or unreadable booking`() = runTest {
        val cases = listOf(
            earlierBooking.copy(reservationTime = "2026-10-28T11:30:00Z"),
            earlierBooking.copy(reservationTime = "not a time"),
        )
        for (booking in cases) {
            bookedEarlier(booking)
            val viewModel = viewModel()
            viewModel.openBranch()
            assertEquals(ReservationState(), viewModel.state.value, booking.reservationTime)
        }

        bookedEarlier()
        repository.reservation(earlierBooking.id).value = null // deleted by staff
        val viewModel = viewModel()
        viewModel.openBranch()
        assertEquals(ReservationState(), viewModel.state.value)
    }

    @Test
    fun `shows the form when the booking can't be loaded`() = runTest {
        bookedEarlier()
        repository.streamError = DataException(DataError.Offline)
        val viewModel = viewModel()
        viewModel.openBranch()

        assertEquals(ReservationState(), viewModel.state.value)
    }

    @Test
    fun `a status change by the branch shows straight away`() = runTest {
        bookedEarlier()
        val viewModel = viewModel()
        viewModel.openBranch()

        repository.reservation(earlierBooking.id).value = earlierBooking.copy(status = "CONFIRMED")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("CONFIRMED", viewModel.state.value.reservation?.status)
    }

    @Test
    fun `book another shows the form and stops following the old booking`() = runTest {
        bookedEarlier()
        val viewModel = viewModel()
        viewModel.openBranch()

        viewModel.onAction(ReservationAction.Reset)
        repository.reservation(earlierBooking.id).value = earlierBooking.copy(status = "CONFIRMED")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(ReservationState(), viewModel.state.value)
    }

    @Test
    fun `a new booking is remembered and shown`() = runTest {
        val viewModel = viewModel()
        viewModel.openBranch()
        viewModel.onAction(validEntry)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(mapOf((tenant to "downtown") to "created-1"), history.saved)
        assertEquals("created-1", viewModel.state.value.reservation?.id)

        // Leaving and coming back finds it again.
        val reopened = viewModel()
        reopened.openBranch()
        assertEquals("created-1", reopened.state.value.reservation?.id)
    }

    @Test
    fun `rejects invalid entries without submitting`() = runTest {
        val viewModel = viewModel()
        val cases = mapOf(
            validEntry.copy(branchId = null) to ReservationError.NoBranch,
            validEntry.copy(customerName = " ") to ReservationError.MissingName,
            validEntry.copy(customerPhone = " ") to ReservationError.MissingPhone,
            validEntry.copy(customerEmail = "") to ReservationError.InvalidEmail,
            validEntry.copy(customerEmail = "ada@example") to ReservationError.InvalidEmail,
            validEntry.copy(customerEmail = "ada example.com") to ReservationError.InvalidEmail,
            validEntry.copy(partySize = 0) to ReservationError.MissingPartySize,
            validEntry.copy(reservationTime = "") to ReservationError.MissingTime,
        )

        for ((entry, error) in cases) {
            viewModel.onAction(entry)
            assertEquals(ReservationState(error = error), viewModel.state.value, entry.toString())
        }
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(repository.createdReservations.isEmpty())
    }

    @Test
    fun `special requests are optional`() = runTest {
        val viewModel = viewModel()
        viewModel.onAction(validEntry.copy(specialRequests = ""))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("", viewModel.state.value.reservation?.specialRequests)
        assertEquals("", repository.createdReservations.single().second.specialRequests)
    }

    @Test
    fun `submits a trimmed pending reservation for the tenant`() = runTest {
        val viewModel = viewModel()

        viewModel.state.test {
            skipItems(1)
            viewModel.onAction(validEntry)
            assertEquals(ReservationState(isSubmitting = true), viewModel.state.value)
            assertEquals("created-1", awaitItemMatching { !it.isSubmitting }.reservation?.id)
        }
        assertEquals(
            listOf(
                "com.burgerjoint.app" to Reservation(
                    id = "",
                    branchId = "downtown",
                    customerName = "Ada Lovelace",
                    customerPhone = "+31 6 1234 5678",
                    customerEmail = "ada@example.com",
                    specialRequests = "Window seat",
                    partySize = 4,
                    reservationTime = "2026-10-30T19:30:00Z",
                    status = "PENDING",
                ),
            ),
            repository.createdReservations,
        )
    }

    @Test
    fun `surfaces a submit failure and dismisses it`() = runTest {
        repository.createError = DataException(DataError.Offline)
        val viewModel = viewModel()

        viewModel.state.test {
            skipItems(1)
            viewModel.onAction(validEntry)
            assertEquals(ReservationState(error = ReservationError.Offline), awaitItemMatching { !it.isSubmitting })

            viewModel.onAction(ReservationAction.DismissError)
            assertEquals(ReservationState(), awaitItem())
        }
    }

    @Test
    fun `maps submit failures to reservation errors`() = runTest {
        val cases = mapOf(
            DataException(DataError.PermissionDenied) to ReservationError.PermissionDenied,
            DataException(DataError.NotFound) to ReservationError.SubmitFailed,
            DataException(DataError.Unknown) to ReservationError.SubmitFailed,
            IllegalStateException("not a repository error") to ReservationError.SubmitFailed,
        )

        for ((failure, expected) in cases) {
            repository.createError = failure
            val viewModel = viewModel()
            viewModel.onAction(validEntry)
            testDispatcher.scheduler.advanceUntilIdle()
            assertEquals(ReservationState(error = expected), viewModel.state.value)
        }
    }
}
