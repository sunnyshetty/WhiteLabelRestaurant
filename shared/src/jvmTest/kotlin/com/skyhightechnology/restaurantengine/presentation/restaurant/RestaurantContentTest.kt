package com.skyhightechnology.restaurantengine.presentation.restaurant

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import com.skyhightechnology.restaurantengine.presentation.LoadError
import com.skyhightechnology.restaurantengine.presentation.messageRes
import com.skyhightechnology.restaurantengine.presentation.preview.PreviewData
import com.skyhightechnology.restaurantengine.presentation.preview.PreviewTheme
import com.skyhightechnology.restaurantengine.presentation.text
import com.skyhightechnology.restaurantengine.resources.Res
import com.skyhightechnology.restaurantengine.resources.action_back
import com.skyhightechnology.restaurantengine.resources.restaurant_book_table
import com.skyhightechnology.restaurantengine.resources.restaurant_opening_hours
import com.skyhightechnology.restaurantengine.resources.restaurant_tables_available
import com.skyhightechnology.restaurantengine.resources.restaurant_tables_full
import com.skyhightechnology.restaurantengine.resources.restaurant_view_menu
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class RestaurantContentTest {

    @Test
    fun `shows the branch details`() = runComposeUiTest {
        setContent { PreviewTheme { RestaurantContent("De Burger Meesters", RestaurantState(branch = PreviewData.branch), {}, {}, {}) } }

        onNodeWithText("Damrak 12, 1012 LG Amsterdam").assertIsDisplayed()
        onNodeWithText("Monday - Sunday: 12:00 - 22:00").assertIsDisplayed()
        onNodeWithText(text(Res.string.restaurant_tables_available)).assertIsDisplayed()
        onNodeWithText("+31 20 123 4567").assertIsDisplayed()
    }

    @Test
    fun `buttons open booking, menu and go back`() = runComposeUiTest {
        val events = mutableListOf<String>()
        setContent {
            PreviewTheme {
                RestaurantContent(
                    brandName = "Brand",
                    state = RestaurantState(branch = PreviewData.branch),
                    onBack = { events += "back" },
                    onBookTable = { events += "book $it" },
                    onViewMenu = { events += "menu $it" },
                )
            }
        }

        onNodeWithText(text(Res.string.restaurant_book_table)).performClick()
        onNodeWithText(text(Res.string.restaurant_view_menu)).performClick()
        onNodeWithContentDescription(text(Res.string.action_back)).performClick()

        assertEquals(listOf("book Amsterdam Centrum", "menu Amsterdam Centrum", "back"), events)
    }

    @Test
    fun `a fully booked branch without hours says so and hides the hours`() = runComposeUiTest {
        val branch = PreviewData.branch.copy(openingHours = "", hasTablesToday = false)
        setContent { PreviewTheme { RestaurantContent("Brand", RestaurantState(branch = branch), {}, {}, {}) } }

        onNodeWithText(text(Res.string.restaurant_tables_full)).assertIsDisplayed()
        onNodeWithText(text(Res.string.restaurant_opening_hours)).assertDoesNotExist()
    }

    @Test
    fun `a removed branch shows the not-found message`() = runComposeUiTest {
        setContent { PreviewTheme { RestaurantContent("Brand", RestaurantState(error = LoadError.NotFound), {}, {}, {}) } }

        onNodeWithText(text(LoadError.NotFound.messageRes)).assertIsDisplayed()
    }
}
