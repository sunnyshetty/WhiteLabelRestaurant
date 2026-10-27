package com.skyhightechnology.restaurantengine.presentation.menu

import kotlin.test.Test
import kotlin.test.assertEquals

class PricePartsTest {

    @Test
    fun `splits cents into whole units and two digit cents`() {
        assertEquals("12" to "99", priceParts(1299))
        assertEquals("3" to "05", priceParts(305))
        assertEquals("0" to "50", priceParts(50))
        assertEquals("7" to "00", priceParts(700))
    }
}
