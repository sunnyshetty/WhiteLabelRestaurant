package com.skyhightechnology.restaurantengine

import app.cash.turbine.test
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** Verifies the coroutines-test + Turbine toolchain is wired on every target. Replace once real tests exist. */
class TestToolingSmokeTest {

    @Test
    fun `turbine observes StateFlow emissions`() = runTest {
        val state = MutableStateFlow(0)

        state.test {
            assertEquals(0, awaitItem())
            state.value = 1
            assertEquals(1, awaitItem())
        }
    }
}
