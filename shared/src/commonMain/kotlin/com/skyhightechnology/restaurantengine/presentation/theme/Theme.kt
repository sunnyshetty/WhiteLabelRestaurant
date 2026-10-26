package com.skyhightechnology.restaurantengine.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/** Engine-wide theme. Per-tenant branding colors will be injected here once the data layer exists. */
@Composable
fun RestaurantEngineTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(),
        content = content,
    )
}
