package com.skyhightechnology.restaurantengine.presentation.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import com.skyhightechnology.restaurantengine.domain.model.RestaurantConfig

/** Engine-wide theme, tinted with the tenant's brand colors once [config] has loaded. */
@Composable
fun RestaurantEngineTheme(config: RestaurantConfig? = null, content: @Composable () -> Unit) {
    val colorScheme = remember(config) { brandColorScheme(config) }
    MaterialTheme(colorScheme = colorScheme, content = content)
}

/** Material3 light scheme with brand primary/secondary; falls back to the default palette for missing or bad hex. */
fun brandColorScheme(config: RestaurantConfig?): ColorScheme {
    val base = lightColorScheme()
    val primary = config?.primaryColor?.let(::parseHexColor) ?: return base
    val secondary = parseHexColor(config.secondaryColor) ?: primary
    return base.copy(
        primary = primary,
        onPrimary = contentColorOn(primary),
        primaryContainer = lerp(primary, Color.White, 0.85f),
        onPrimaryContainer = lerp(primary, Color.Black, 0.6f),
        secondary = secondary,
        onSecondary = contentColorOn(secondary),
        secondaryContainer = lerp(secondary, Color.White, 0.85f),
        onSecondaryContainer = lerp(secondary, Color.Black, 0.6f),
    )
}

/** Parses `#RRGGBB` or `#AARRGGBB` (leading `#` optional); returns null for anything else. */
fun parseHexColor(hex: String): Color? {
    val digits = hex.trim().removePrefix("#")
    if (digits.length != 6 && digits.length != 8) return null
    val value = digits.toLongOrNull(16) ?: return null
    return Color(if (digits.length == 6) value or 0xFF000000 else value)
}

private fun contentColorOn(background: Color): Color =
    if (background.luminance() > 0.5f) Color.Black else Color.White
