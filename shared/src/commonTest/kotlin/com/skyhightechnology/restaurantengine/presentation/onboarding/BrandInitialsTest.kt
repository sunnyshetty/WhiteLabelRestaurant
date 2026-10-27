package com.skyhightechnology.restaurantengine.presentation.onboarding

import kotlin.test.Test
import kotlin.test.assertEquals

class BrandInitialsTest {

    @Test
    fun `takes the first letter of each word and keeps ampersands`() {
        assertEquals("BJ", brandInitials("Burger Joint"))
        assertEquals("CL", brandInitials("café  local"))
        assertEquals("O&E", brandInitials("Olive & Ember"))
        assertEquals("N", brandInitials("Nobu"))
    }

    @Test
    fun `caps at three letters and handles blank names`() {
        assertEquals("TLB", brandInitials("The Little Blue Door"))
        assertEquals("", brandInitials("  "))
    }
}
