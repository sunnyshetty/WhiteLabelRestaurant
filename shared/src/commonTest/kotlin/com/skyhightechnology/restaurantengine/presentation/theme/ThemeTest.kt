package com.skyhightechnology.restaurantengine.presentation.theme

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.skyhightechnology.restaurantengine.domain.model.RestaurantConfig
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ThemeTest {

    @Test
    fun `parses rgb and argb hex with or without hash`() {
        assertEquals(Color(0xFFFF5722), parseHexColor("#FF5722"))
        assertEquals(Color(0xFFFF5722), parseHexColor(" ff5722 "))
        assertEquals(Color(0x80FF5722), parseHexColor("#80FF5722"))
    }

    @Test
    fun `rejects malformed hex`() {
        listOf("", "#FFF", "#GG5722", "red", "#FF57221").forEach { assertNull(parseHexColor(it), it) }
    }

    @Test
    fun `brand colors drive primary and secondary with readable content colors`() {
        val scheme = brandColorScheme(RestaurantConfig("t", "Burger Joint", "#212121", "#FFEB3B"))

        assertEquals(Color(0xFF212121), scheme.primary)
        assertEquals(Color.White, scheme.onPrimary)
        assertEquals(Color(0xFFFFEB3B), scheme.secondary)
        assertEquals(Color.Black, scheme.onSecondary)
    }

    @Test
    fun `falls back to defaults when config is missing or primary is invalid`() {
        val default = lightColorScheme()
        assertEquals(default.primary, brandColorScheme(null).primary)
        assertEquals(default.primary, brandColorScheme(RestaurantConfig("t", "X", "not-a-color", "#000000")).primary)
    }

    @Test
    fun `invalid secondary reuses primary`() {
        val scheme = brandColorScheme(RestaurantConfig("t", "X", "#FF5722", "oops"))
        assertEquals(scheme.primary, scheme.secondary)
    }
}
