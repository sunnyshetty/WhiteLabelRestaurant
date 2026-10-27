package com.skyhightechnology.restaurantengine.presentation.theme

import app.cash.turbine.test
import com.skyhightechnology.restaurantengine.domain.model.DataError
import com.skyhightechnology.restaurantengine.domain.model.DataException
import com.skyhightechnology.restaurantengine.domain.model.RestaurantConfig
import com.skyhightechnology.restaurantengine.domain.repository.FakeRestaurantRepository
import com.skyhightechnology.restaurantengine.presentation.LoadError
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
class GlobalConfigViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val repository = FakeRestaurantRepository()
    private val config = RestaurantConfig("com.burgerjoint.app", "Burger Joint", "#FF5722", "#212121")

    @BeforeTest fun setUp() { Dispatchers.setMain(testDispatcher) }
    @AfterTest fun tearDown() { Dispatchers.resetMain() }

    private fun viewModel() = GlobalConfigViewModel("com.burgerjoint.app", repository)

    @Test
    fun `loads the tenant config on creation`() = runTest {
        repository.config = config
        val viewModel = viewModel()

        viewModel.state.test {
            assertEquals(GlobalThemeState(isLoading = true), awaitItem())
            assertEquals(GlobalThemeState(brandConfig = config), awaitItem())
        }
        assertEquals(listOf("com.burgerjoint.app"), repository.requestedConfigIds)
    }

    @Test
    fun `surfaces a load failure and recovers on retry`() = runTest {
        repository.configError = DataException(DataError.NotFound)
        val viewModel = viewModel()

        viewModel.state.test {
            assertEquals(GlobalThemeState(isLoading = true), awaitItem())
            assertEquals(GlobalThemeState(error = LoadError.NotFound), awaitItem())

            repository.configError = null
            repository.config = config
            viewModel.onAction(GlobalConfigAction.Retry)

            assertEquals(GlobalThemeState(isLoading = true), awaitItem())
            assertEquals(GlobalThemeState(brandConfig = config), awaitItem())
        }
    }

    @Test
    fun `unexpected exceptions become an unknown load error`() = runTest {
        repository.configError = IllegalArgumentException("bad document shape")
        val viewModel = viewModel()

        viewModel.state.test {
            skipItems(1)
            assertEquals(GlobalThemeState(error = LoadError.Unknown), awaitItem())
        }
    }

    @Test
    fun `selecting and clearing a branch keeps the loaded config`() = runTest {
        repository.config = config
        val viewModel = viewModel()

        viewModel.state.test {
            skipItems(2)
            viewModel.onAction(GlobalConfigAction.SelectBranch("downtown"))
            assertEquals(GlobalThemeState(brandConfig = config, selectedBranchId = "downtown"), awaitItem())

            viewModel.onAction(GlobalConfigAction.ClearBranch)
            assertEquals(GlobalThemeState(brandConfig = config), awaitItem())
        }
    }
}
