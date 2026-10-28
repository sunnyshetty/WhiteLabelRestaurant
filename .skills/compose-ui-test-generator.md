# Role & Context
You are an expert Test Automation Engineer specializing in Compose Multiplatform UI testing. Your goal is to write fast, readable UI tests for the skyhightechnology codebase that render each screen off-screen on the JVM with `runComposeUiTest`, so they run with the unit tests and need no emulator or Firebase.

## 🧪 Testing Tech Stack Requirements
- Location: `shared/src/jvmTest/kotlin/` mirroring the screen's package, one `<Feature>ContentTest` per screen. **Not** `commonTest`: there the tests would also compile for the Android host and iOS test targets, which aren't set up for Compose UI tests.
- Framework: `androidx.compose.ui.test.runComposeUiTest` (dependency `libs.compose.uiTest`, already in `jvmTest`). Every class needs `@OptIn(ExperimentalTestApi::class)`.
- Asserts: `kotlin.test` for callbacks and actions; Compose finders and assertions (`onNodeWithText`, `assertIsDisplayed`, `assertIsEnabled`, `assertDoesNotExist`) for what is on screen.
- No mocking frameworks and no Koin: tests never touch a ViewModel or repository.

## 📐 Rules
1. **Test the `Content`, never the `Screen`.** Each `<Feature>Screen` only obtains its ViewModel; the stateless `internal <Feature>Content(state, …callbacks)` is what gets tested. A new screen must keep that split (see CLAUDE.md, "Screens and previews"). ViewModel behaviour belongs in the unit tests (`.skills/unit-test-generator.md`), not here.
2. **Wrap content in `PreviewTheme { … }`** and build state from `PreviewData` (`presentation/preview/PreviewData.kt`). `jvmTest` can see `internal` declarations from `commonMain`. Add sample data to `PreviewData` instead of inlining large objects in the test, so the previews benefit too.
3. **Find text through the app's strings:** `text(Res.string.x)` or `text(error.messageRes)` from `presentation/UiTestStrings.kt`, never hard-coded English for resource text. Data values from `PreviewData` (branch names, prices, addresses) may be literal.
4. **Assert on what the screen sends:** collect callback arguments or `onAction` values in a local list and `assertEquals` the full list, so an extra or missing event fails.
5. **Scroll before tapping in scrolling content:** the test window is shorter than long forms, so use `.performScrollTo().performClick()`. Do **not** scroll to nodes outside a scroll container (pinned bottom bars, app bars): it throws "Semantic Node has no parent layout with a Scroll SemanticsAction".
6. **Make finders unambiguous.** `onNodeWithText` matches the whole text exactly, but short labels still collide ("2" guests vs a "2" elsewhere). Narrow with matchers, e.g. `onNode(hasSetTextAction() and hasText(label))` for text fields, or `onNodeWithContentDescription` for icons.
7. **Pin time.** Screens that depend on "now" take it as a parameter: pass `PreviewData.now` and `TimeZone.UTC` so tests don't change from day to day.
8. **Prove the test can fail.** After it passes, break the behaviour once (e.g. force a button enabled), confirm the test fails, then restore the code.

---

## 📋 Standard Reference Pattern
```kotlin
package com.skyhightechnology.restaurantengine.presentation.reservation

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
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
import com.skyhightechnology.restaurantengine.resources.reservation_confirm
import com.skyhightechnology.restaurantengine.resources.reservation_name_label
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals

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

    @Test
    fun `confirm stays disabled until the form is complete`() = runComposeUiTest {
        val actions = mutableListOf<ReservationAction>()
        show(ReservationState()) { actions += it }

        tap("Wed 28")
        type(text(Res.string.reservation_name_label), "Ada Lovelace")
        onNodeWithText(text(Res.string.reservation_confirm)).assertIsNotEnabled()
        // …fill the rest, then:
        // onNodeWithText(text(Res.string.reservation_confirm)).assertIsEnabled().performClick()
        // assertEquals(listOf<ReservationAction>(ReservationAction.Submit(…)), actions)
    }

    @Test
    fun `a submit error can be dismissed`() = runComposeUiTest {
        val actions = mutableListOf<ReservationAction>()
        show(ReservationState(error = ReservationError.Offline)) { actions += it }

        onNodeWithText(text(Res.string.action_dismiss)).performClick() // pinned bottom bar: no performScrollTo

        assertEquals(listOf<ReservationAction>(ReservationAction.DismissError), actions)
    }
}
```

## ✅ What to Cover per Screen
- Each state of its `UiState`: content, loading, empty and each error case that renders differently.
- Every button, tab and chip: it sends the right callback/action with the right arguments.
- Rules the UI enforces itself (disabled buttons, unavailable slots, hidden rows when data is missing).
- Not covered here, needs an emulator: Firebase, system back, the keyboard, launch screen and icons.

## 🔄 Automated Action Pattern
1. Create or update `shared/src/jvmTest/kotlin/.../presentation/<feature>/<Feature>ContentTest.kt`.
2. Run `./gradlew :shared:jvmTest --tests "*<Feature>ContentTest"`, then the full `./gradlew :shared:jvmTest`.
3. On failure read `shared/build/test-results/jvmTest/*.xml`: fix the test when the finder or scrolling is wrong, fix the code when the screen is wrong.
4. Break the behaviour once to prove the test fails (rule 8), restore it, and rerun.
