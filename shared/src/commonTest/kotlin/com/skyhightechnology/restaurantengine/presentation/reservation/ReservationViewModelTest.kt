package com.skyhightechnology.restaurantengine.presentation.reservation

import app.cash.turbine.test
import com.skyhightechnology.restaurantengine.domain.model.DataError
import com.skyhightechnology.restaurantengine.domain.model.DataException
import com.skyhightechnology.restaurantengine.domain.model.Reservation
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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ReservationViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val repository = FakeRestaurantRepository()

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

    private fun viewModel() = ReservationViewModel("com.burgerjoint.app", repository)

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

        assertEquals(ReservationState(isSuccess = true), viewModel.state.value)
        assertEquals("", repository.createdReservations.single().second.specialRequests)
    }

    @Test
    fun `submits a trimmed pending reservation for the tenant`() = runTest {
        val viewModel = viewModel()

        viewModel.state.test {
            skipItems(1)
            viewModel.onAction(validEntry)
            assertEquals(ReservationState(isSubmitting = true), viewModel.state.value)
            assertEquals(ReservationState(isSuccess = true), awaitItemMatching { !it.isSubmitting })
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
