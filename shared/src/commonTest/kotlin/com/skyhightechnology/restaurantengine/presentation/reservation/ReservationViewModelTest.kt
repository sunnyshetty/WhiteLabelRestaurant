package com.skyhightechnology.restaurantengine.presentation.reservation

import app.cash.turbine.test
import com.skyhightechnology.restaurantengine.domain.model.Reservation
import com.skyhightechnology.restaurantengine.domain.repository.FakeRestaurantRepository
import com.skyhightechnology.restaurantengine.domain.usecase.CreateReservationUseCase
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
        partySize = 4,
        reservationTime = "2026-10-30T19:30:00Z",
    )

    @BeforeTest fun setUp() { Dispatchers.setMain(testDispatcher) }
    @AfterTest fun tearDown() { Dispatchers.resetMain() }

    private fun viewModel() = ReservationViewModel("com.burgerjoint.app", CreateReservationUseCase(repository))

    @Test
    fun `rejects invalid entries without submitting`() = runTest {
        val viewModel = viewModel()
        val cases = mapOf(
            validEntry.copy(branchId = null) to "Choose a branch first",
            validEntry.copy(customerName = " ") to "Enter a name for the reservation",
            validEntry.copy(partySize = 0) to "Party size must be at least 1",
            validEntry.copy(reservationTime = "") to "Choose a reservation time",
        )

        for ((entry, message) in cases) {
            viewModel.onAction(entry)
            assertEquals(ReservationState(error = message), viewModel.state.value)
        }
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(repository.createdReservations.isEmpty())
    }

    @Test
    fun `submits a pending reservation for the tenant`() = runTest {
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
        repository.createError = IllegalStateException("UNAVAILABLE")
        val viewModel = viewModel()

        viewModel.state.test {
            skipItems(1)
            viewModel.onAction(validEntry)
            assertEquals(ReservationState(error = "UNAVAILABLE"), awaitItemMatching { !it.isSubmitting })

            viewModel.onAction(ReservationAction.DismissError)
            assertEquals(ReservationState(), awaitItem())
        }
    }
}
