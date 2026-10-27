package com.skyhightechnology.restaurantengine.domain.model

data class MenuItem(
    val id: String,
    val name: String,
    val description: String?,
    val priceInCents: Int,
    val category: String,
    val imageUrl: String?,
    val isAvailable: Boolean,
)
