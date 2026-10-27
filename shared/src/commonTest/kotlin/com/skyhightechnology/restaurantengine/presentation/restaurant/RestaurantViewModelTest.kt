package com.skyhightechnology.restaurantengine.presentation.restaurant

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
class RestaurantViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val repository = FakeRestaurantRepository()

    private val downtown = RestaurantBranch("downtown", "Downtown", "Main St 1", "+31 20 000 0001")
    private val harbour = RestaurantBranch("harbour", "Harbour", "Pier 9", "+31 20 000 0002")

    @BeforeTest fun setUp() { Dispatchers.setMain(testDispatcher) }
    @AfterTest fun tearDown() { Dispatchers.resetMain() }

    private fun viewModel() = RestaurantViewModel("com.burgerjoint.app", repository)

    @Test
    fun `stays empty until a branch is selected`() = runTest {
        viewModel().state.test {
            assertEquals(RestaurantState(), awaitItem())
            expectNoEvents()
        }
    }

    @Test
    fun `shows the selected branch and follows live edits`() = runTest {
        repository.branches.value = listOf(downtown, harbour)
        val viewModel = viewModel()

        viewModel.state.test {
            skipItems(1)
            viewModel.onAction(RestaurantAction.SelectBranch("harbour"))
            assertEquals(RestaurantState(branch = harbour), awaitItemMatching { !it.isLoading })

            val renamed = harbour.copy(name = "Harbour Front")
            repository.branches.value = listOf(downtown, renamed)
            assertEquals(RestaurantState(branch = renamed), awaitItem())
        }
    }

    @Test
    fun `reports not found when the branch is removed`() = runTest {
        repository.branches.value = listOf(downtown)
        val viewModel = viewModel()

        viewModel.state.test {
            skipItems(1)
            viewModel.onAction(RestaurantAction.SelectBranch("harbour"))
            assertEquals(RestaurantState(error = LoadError.NotFound), awaitItemMatching { !it.isLoading })
        }
    }

    @Test
    fun `surfaces a stream failure`() = runTest {
        repository.streamError = DataException(DataError.Offline)
        val viewModel = viewModel()

        viewModel.state.test {
            skipItems(1)
            viewModel.onAction(RestaurantAction.SelectBranch("downtown"))
            assertEquals(RestaurantState(error = LoadError.Offline), awaitItemMatching { !it.isLoading })
        }
    }
}
