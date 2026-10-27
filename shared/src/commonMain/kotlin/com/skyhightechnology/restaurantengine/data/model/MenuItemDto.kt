package com.skyhightechnology.restaurantengine.data.model

import com.skyhightechnology.restaurantengine.domain.model.MenuItem
import kotlinx.serialization.Serializable

/** Firestore document `restaurants/{restaurantId}/branches/{branchId}/menu/{itemId}`. */
@Serializable
data class MenuItemDto(
    val name: String = "",
    val description: String? = null,
    val priceInCents: Int = 0,
    val category: String = "",
    val imageUrl: String? = null,
    val isAvailable: Boolean = false,
)

fun MenuItemDto.toDomain(id: String) = MenuItem(
    id = id,
    name = name,
    description = description,
    priceInCents = priceInCents,
    category = category,
    imageUrl = imageUrl,
    isAvailable = isAvailable,
)
