package com.skyhightechnology.restaurantengine.domain.model

/** Corporate branding for a restaurant tenant. Colors are hex strings, e.g. `#FF5722`. */
data class RestaurantConfig(
    val id: String,
    val name: String,
    val primaryColor: String,
    val secondaryColor: String,
)
