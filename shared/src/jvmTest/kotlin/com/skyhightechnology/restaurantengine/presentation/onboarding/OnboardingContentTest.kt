package com.skyhightechnology.restaurantengine.presentation.onboarding

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import com.skyhightechnology.restaurantengine.presentation.LoadError
import com.skyhightechnology.restaurantengine.presentation.messageRes
import com.skyhightechnology.restaurantengine.presentation.preview.PreviewData
import com.skyhightechnology.restaurantengine.presentation.preview.PreviewTheme
import com.skyhightechnology.restaurantengine.presentation.text
import com.skyhightechnology.restaurantengine.resources.Res
import com.skyhightechnology.restaurantengine.resources.action_retry
import com.skyhightechnology.restaurantengine.resources.onboarding_no_branches
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class OnboardingContentTest {

    @Test
    fun `lists the branches and opens the one tapped`() = runComposeUiTest {
        var selected: String? = null
        setContent {
            PreviewTheme {
                OnboardingContent("De Burger Meesters", OnboardingState(branches = PreviewData.branches), { selected = it }, {})
            }
        }

        onNodeWithText("De Burger Meesters").assertIsDisplayed()
        onNodeWithText("Damrak 12, 1012 LG Amsterdam").assertIsDisplayed()
        onNodeWithText("Utrecht Oudegracht").performClick()

        assertEquals("utrecht_oudegracht", selected)
    }

    @Test
    fun `a load error offers retry`() = runComposeUiTest {
        var retries = 0
        setContent {
            PreviewTheme { OnboardingContent("Brand", OnboardingState(error = LoadError.Offline), {}, { retries++ }) }
        }

        onNodeWithText(text(LoadError.Offline.messageRes)).assertIsDisplayed()
        onNodeWithText(text(Res.string.action_retry)).performClick()

        assertEquals(1, retries)
    }

    @Test
    fun `says so when there are no branches`() = runComposeUiTest {
        setContent { PreviewTheme { OnboardingContent("Brand", OnboardingState(), {}, {}) } }

        onNodeWithText(text(Res.string.onboarding_no_branches)).assertIsDisplayed()
    }
}
