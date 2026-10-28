package com.skyhightechnology.restaurantengine.presentation.reservation

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runComposeUiTest
import com.skyhightechnology.restaurantengine.presentation.preview.PreviewData
import com.skyhightechnology.restaurantengine.presentation.preview.PreviewTheme
import com.skyhightechnology.restaurantengine.presentation.text
import com.skyhightechnology.restaurantengine.resources.Res
import com.skyhightechnology.restaurantengine.resources.action_dismiss
import com.skyhightechnology.restaurantengine.resources.reservation_book_another
import com.skyhightechnology.restaurantengine.resources.reservation_confirm
import com.skyhightechnology.restaurantengine.resources.reservation_email_label
import com.skyhightechnology.restaurantengine.resources.reservation_existing_title
import com.skyhightechnology.restaurantengine.resources.reservation_name_label
import com.skyhightechnology.restaurantengine.resources.reservation_phone_label
import com.skyhightechnology.restaurantengine.resources.reservation_pick_date_first
import com.skyhightechnology.restaurantengine.resources.reservation_status_confirmed
import com.skyhightechnology.restaurantengine.resources.reservation_status_pending
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals

/** "Now" is Tue 27 Oct 2026, 12:15 ([PreviewData.now]), in UTC. */
@OptIn(ExperimentalTestApi::class)
class ReservationContentTest {

    private fun ComposeUiTest.show(state: ReservationState, onAction: (ReservationAction) -> Unit = {}) {
        setContent {
            PreviewTheme { ReservationContent(state, PreviewData.branch.id, PreviewData.now, TimeZone.UTC, onAction) }
        }
    }

    /** Scrolls the form to [label] first: the test window is shorter than the whole form. */
    private fun ComposeUiTest.tap(label: String) = onNodeWithText(label).performScrollTo().performClick()

    private fun ComposeUiTest.type(label: String, value: String) =
        onNode(hasSetTextAction() and hasText(label)).performScrollTo().performTextInput(value)

    private fun ComposeUiTest.confirmButton() = onNodeWithText(text(Res.string.reservation_confirm))

    @Test
    fun `times appear only after a date is picked`() = runComposeUiTest {
        show(ReservationState())

        onNodeWithText(text(Res.string.reservation_pick_date_first)).assertIsDisplayed()
        onNodeWithText("19:00").assertDoesNotExist()

        tap("Wed 28")
        onNodeWithText("19:00").assertExists()
    }

    @Test
    fun `slots that have passed today can't be picked`() = runComposeUiTest {
        show(ReservationState())

        tap("Tue 27")
        onNodeWithText("12:00").assertIsNotEnabled()
        onNodeWithText("12:30").assertIsEnabled()

        tap("Wed 28")
        onNodeWithText("12:00").assertIsEnabled()
    }

    @Test
    fun `confirm is enabled once the form is complete and sends the booking`() = runComposeUiTest {
        val actions = mutableListOf<ReservationAction>()
        show(ReservationState()) { actions += it }

        tap("Wed 28")
        tap("2")
        tap("19:00")
        type(text(Res.string.reservation_name_label), "Ada Lovelace")
        type(text(Res.string.reservation_phone_label), "0612345678")
        confirmButton().assertIsNotEnabled()

        type(text(Res.string.reservation_email_label), "ada@example.com")
        confirmButton().assertIsEnabled().performClick()

        assertEquals(
            listOf<ReservationAction>(
                ReservationAction.Submit(
                    branchId = PreviewData.branch.id,
                    customerName = "Ada Lovelace",
                    customerPhone = "0612345678",
                    customerEmail = "ada@example.com",
                    specialRequests = "",
                    partySize = 2,
                    reservationTime = "2026-10-28T19:00:00Z",
                ),
            ),
            actions,
        )
    }

    @Test
    fun `a submit error is shown and can be dismissed`() = runComposeUiTest {
        val actions = mutableListOf<ReservationAction>()
        show(ReservationState(error = ReservationError.Offline)) { actions += it }

        onNodeWithText(text(ReservationError.Offline.messageRes)).assertIsDisplayed()
        onNodeWithText(text(Res.string.action_dismiss)).performClick() // in the pinned bottom bar, not the scrolling form

        assertEquals(listOf<ReservationAction>(ReservationAction.DismissError), actions)
    }

    @Test
    fun `an existing booking is shown instead of the form`() = runComposeUiTest {
        show(ReservationState(reservation = PreviewData.reservation))

        onNodeWithText(text(Res.string.reservation_existing_title)).assertIsDisplayed()
        onNodeWithText(text(Res.string.reservation_status_pending)).assertIsDisplayed()
        onNodeWithText("Fri 30, 19:30").assertIsDisplayed()
        onNodeWithText("4 guests").assertIsDisplayed()
        onNodeWithText("Sanne de Vries").assertIsDisplayed()
        confirmButton().assertDoesNotExist()
    }

    @Test
    fun `a booking confirmed by the branch says so`() = runComposeUiTest {
        show(ReservationState(reservation = PreviewData.reservation.copy(status = "CONFIRMED")))

        onNodeWithText(text(Res.string.reservation_status_confirmed)).assertIsDisplayed()
    }

    @Test
    fun `book another asks for the form`() = runComposeUiTest {
        val actions = mutableListOf<ReservationAction>()
        show(ReservationState(reservation = PreviewData.reservation)) { actions += it }

        tap(text(Res.string.reservation_book_another))

        assertEquals(listOf<ReservationAction>(ReservationAction.Reset), actions)
    }

    @Test
    fun `nothing is shown while the booking loads`() = runComposeUiTest {
        show(ReservationState(isLoading = true))

        confirmButton().assertDoesNotExist()
        onNodeWithText(text(Res.string.reservation_existing_title)).assertDoesNotExist()
        onNodeWithText("Wed 28").assertDoesNotExist()
    }
}
