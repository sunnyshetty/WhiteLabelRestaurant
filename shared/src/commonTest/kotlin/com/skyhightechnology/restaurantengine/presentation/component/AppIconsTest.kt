package com.skyhightechnology.restaurantengine.presentation.component

import kotlin.test.Test
import kotlin.test.assertEquals

/** Icon paths are parsed lazily at first draw; a typo would crash the bottom bar, so parse them here. */
class AppIconsTest {

    @Test
    fun `icon paths parse into 24dp vectors`() {
        listOf(AppIcons.RestaurantMenu, AppIcons.Event).forEach {
            assertEquals(24f, it.viewportWidth, it.name)
            assertEquals(1, it.root.size, it.name)
        }
    }
}
