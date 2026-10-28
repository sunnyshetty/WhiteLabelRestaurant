package com.skyhightechnology.restaurantengine.presentation.menu

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import com.skyhightechnology.restaurantengine.presentation.preview.PreviewData
import com.skyhightechnology.restaurantengine.presentation.preview.PreviewTheme
import com.skyhightechnology.restaurantengine.presentation.text
import com.skyhightechnology.restaurantengine.resources.Res
import com.skyhightechnology.restaurantengine.resources.menu_empty
import com.skyhightechnology.restaurantengine.resources.menu_sold_out
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class MenuContentTest {

    @Test
    fun `shows the first category with prices and sold-out dishes tagged`() = runComposeUiTest {
        setContent { PreviewTheme { MenuContent(MenuState(categories = PreviewData.menu)) } }

        onNodeWithText("Classic Burger").assertIsDisplayed()
        onNodeWithText("€16.50").assertIsDisplayed()
        onNodeWithText(text(Res.string.menu_sold_out)).assertIsDisplayed()
        onNodeWithText("Fries").assertDoesNotExist()
    }

    @Test
    fun `tapping a category tab shows its dishes`() = runComposeUiTest {
        setContent { PreviewTheme { MenuContent(MenuState(categories = PreviewData.menu)) } }

        onNodeWithText("Sides").performClick()

        onNodeWithText("Fries").assertIsDisplayed()
        onNodeWithText("€4.50").assertIsDisplayed()
    }

    @Test
    fun `says so when the menu is empty`() = runComposeUiTest {
        setContent { PreviewTheme { MenuContent(MenuState()) } }

        onNodeWithText(text(Res.string.menu_empty)).assertIsDisplayed()
    }
}
