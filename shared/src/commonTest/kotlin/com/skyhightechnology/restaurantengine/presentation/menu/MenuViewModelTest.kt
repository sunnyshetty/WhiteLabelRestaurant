package com.skyhightechnology.restaurantengine.presentation.menu

import app.cash.turbine.test
import com.skyhightechnology.restaurantengine.domain.model.MenuItem
import com.skyhightechnology.restaurantengine.domain.repository.FakeRestaurantRepository
import com.skyhightechnology.restaurantengine.domain.usecase.StreamBranchMenuUseCase
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
class MenuViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val repository = FakeRestaurantRepository()

    private val burger = item("burger", "Burgers")
    private val fries = item("fries", "Sides")
    private val shake = item("shake", "Drinks")
    private val espresso = item("espresso", "Drinks")

    @BeforeTest fun setUp() { Dispatchers.setMain(testDispatcher) }
    @AfterTest fun tearDown() { Dispatchers.resetMain() }

    private fun viewModel() = MenuViewModel("com.burgerjoint.app", StreamBranchMenuUseCase(repository))

    @Test
    fun `stays empty until a branch is selected`() = runTest {
        viewModel().state.test {
            assertEquals(MenuState(), awaitItem())
            expectNoEvents()
        }
    }

    @Test
    fun `lists categories in menu order and filters by the selected tab`() = runTest {
        repository.menu("downtown").value = listOf(burger, fries, shake)
        val viewModel = viewModel()

        viewModel.state.test {
            skipItems(1)
            viewModel.onAction(MenuAction.SelectBranch("downtown"))
            assertEquals(
                MenuState(items = listOf(burger, fries, shake), categories = listOf("Burgers", "Sides", "Drinks")),
                awaitItemMatching { !it.isLoading },
            )

            viewModel.onAction(MenuAction.SelectCategory("Drinks"))
            assertEquals(listOf(shake), awaitItem().items)

            viewModel.onAction(MenuAction.SelectCategory(null))
            assertEquals(listOf(burger, fries, shake), awaitItem().items)
        }
    }

    @Test
    fun `switching branch resets the tab and stops the previous branch stream`() = runTest {
        repository.menu("downtown").value = listOf(burger, shake)
        repository.menu("harbour").value = listOf(espresso)
        val viewModel = viewModel()

        viewModel.state.test {
            skipItems(1)
            viewModel.onAction(MenuAction.SelectBranch("downtown"))
            awaitItemMatching { !it.isLoading }
            viewModel.onAction(MenuAction.SelectCategory("Drinks"))
            awaitItem()

            viewModel.onAction(MenuAction.SelectBranch("harbour"))
            assertEquals(
                MenuState(items = listOf(espresso), categories = listOf("Drinks")),
                awaitItemMatching { !it.isLoading && it.items == listOf(espresso) },
            )

            repository.menu("downtown").value = listOf(burger)
            expectNoEvents()
        }
    }

    @Test
    fun `surfaces a stream failure`() = runTest {
        repository.streamError = IllegalStateException("PERMISSION_DENIED")
        val viewModel = viewModel()

        viewModel.state.test {
            skipItems(1)
            viewModel.onAction(MenuAction.SelectBranch("downtown"))
            assertEquals(MenuState(error = "PERMISSION_DENIED"), awaitItemMatching { !it.isLoading })
        }
    }

    private fun item(id: String, category: String) =
        MenuItem(id, id, description = null, priceInCents = 500, category = category, imageUrl = null, isAvailable = true)
}
