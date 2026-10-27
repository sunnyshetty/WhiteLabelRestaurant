package com.skyhightechnology.restaurantengine.presentation.menu

import app.cash.turbine.test
import com.skyhightechnology.restaurantengine.domain.model.DataError
import com.skyhightechnology.restaurantengine.domain.model.DataException
import com.skyhightechnology.restaurantengine.domain.model.MenuItem
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
class MenuViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val repository = FakeRestaurantRepository()

    private val burger = item("burger", "Burgers")
    private val shake = item("shake", "Drinks")
    private val fries = item("fries", "Sides")
    private val cheeseburger = item("cheeseburger", "Burgers")
    private val espresso = item("espresso", "Drinks")

    @BeforeTest fun setUp() { Dispatchers.setMain(testDispatcher) }
    @AfterTest fun tearDown() { Dispatchers.resetMain() }

    private fun viewModel() = MenuViewModel("com.burgerjoint.app", repository)

    @Test
    fun `stays empty until a branch is selected`() = runTest {
        viewModel().state.test {
            assertEquals(MenuState(), awaitItem())
            expectNoEvents()
        }
    }

    @Test
    fun `groups items by category in the order categories first appear`() = runTest {
        repository.menu("downtown").value = listOf(burger, shake, fries, cheeseburger)
        val viewModel = viewModel()

        viewModel.state.test {
            skipItems(1)
            viewModel.onAction(MenuAction.SelectBranch("downtown"))
            assertEquals(
                MenuState(
                    categories = listOf(
                        MenuCategory("Burgers", listOf(burger, cheeseburger)),
                        MenuCategory("Drinks", listOf(shake)),
                        MenuCategory("Sides", listOf(fries)),
                    ),
                ),
                awaitItemMatching { !it.isLoading },
            )
        }
    }

    @Test
    fun `switching branch stops the previous branch stream`() = runTest {
        repository.menu("downtown").value = listOf(burger)
        repository.menu("harbour").value = listOf(espresso)
        val viewModel = viewModel()

        viewModel.state.test {
            skipItems(1)
            viewModel.onAction(MenuAction.SelectBranch("downtown"))
            awaitItemMatching { !it.isLoading }

            viewModel.onAction(MenuAction.SelectBranch("harbour"))
            assertEquals(
                MenuState(categories = listOf(MenuCategory("Drinks", listOf(espresso)))),
                awaitItemMatching { !it.isLoading },
            )

            repository.menu("downtown").value = listOf(burger, fries)
            expectNoEvents()
        }
    }

    @Test
    fun `surfaces a stream failure`() = runTest {
        repository.streamError = DataException(DataError.PermissionDenied)
        val viewModel = viewModel()

        viewModel.state.test {
            skipItems(1)
            viewModel.onAction(MenuAction.SelectBranch("downtown"))
            assertEquals(MenuState(error = LoadError.PermissionDenied), awaitItemMatching { !it.isLoading })
        }
    }

    private fun item(id: String, category: String) =
        MenuItem(id, id, description = null, priceInCents = 500, category = category, imageUrl = null, isAvailable = true)
}
