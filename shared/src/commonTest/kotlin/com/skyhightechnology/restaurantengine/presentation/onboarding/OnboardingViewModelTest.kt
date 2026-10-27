package com.skyhightechnology.restaurantengine.presentation.onboarding

import app.cash.turbine.test
import com.skyhightechnology.restaurantengine.domain.model.DataError
import com.skyhightechnology.restaurantengine.domain.model.DataException
import com.skyhightechnology.restaurantengine.domain.model.RestaurantBranch
import com.skyhightechnology.restaurantengine.domain.repository.FakeRestaurantRepository
import com.skyhightechnology.restaurantengine.presentation.LoadError
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

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val repository = FakeRestaurantRepository()
    private val downtown = RestaurantBranch("downtown", "Downtown", "1 Main St", "555-0100")
    private val harbour = RestaurantBranch("harbour", "Harbour", "2 Pier Rd", "555-0200")

    @BeforeTest fun setUp() { Dispatchers.setMain(testDispatcher) }
    @AfterTest fun tearDown() { Dispatchers.resetMain() }

    private fun viewModel() = OnboardingViewModel("com.burgerjoint.app", repository)

    @Test
    fun `streams branch updates live`() = runTest {
        repository.branches.value = listOf(downtown)
        val viewModel = viewModel()

        viewModel.state.test {
            assertEquals(OnboardingState(isLoading = true), awaitItem())
            assertEquals(OnboardingState(branches = listOf(downtown)), awaitItem())

            repository.branches.value = listOf(downtown, harbour)
            assertEquals(OnboardingState(branches = listOf(downtown, harbour)), awaitItem())
        }
    }

    @Test
    fun `surfaces a stream failure`() = runTest {
        repository.streamError = DataException(DataError.PermissionDenied)
        val viewModel = viewModel()

        viewModel.state.test {
            assertEquals(OnboardingState(isLoading = true), awaitItem())
            assertEquals(OnboardingState(error = LoadError.PermissionDenied), awaitItem())
        }
    }

    @Test
    fun `retry resubscribes after a failure`() = runTest {
        repository.streamError = DataException(DataError.Offline)
        repository.branches.value = listOf(downtown)
        val viewModel = viewModel()

        viewModel.state.test {
            assertEquals(OnboardingState(error = LoadError.Offline), awaitItemMatching { !it.isLoading })

            repository.streamError = null
            viewModel.onAction(OnboardingAction.Retry)
            assertEquals(OnboardingState(branches = listOf(downtown)), awaitItemMatching { !it.isLoading })
        }
    }
}
