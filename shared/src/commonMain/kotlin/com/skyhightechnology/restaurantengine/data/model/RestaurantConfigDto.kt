package com.skyhightechnology.restaurantengine.data.model

import com.skyhightechnology.restaurantengine.domain.model.RestaurantConfig
import kotlinx.serialization.Serializable

/** Firestore document `restaurants/{restaurantId}`. Defaults tolerate documents missing a field. */
@Serializable
data class RestaurantConfigDto(
    val name: String = "",
    val primaryColor: String = "",
    val secondaryColor: String = "",
)

fun RestaurantConfigDto.toDomain(id: String) = RestaurantConfig(
    id = id,
    name = name,
    primaryColor = primaryColor,
    secondaryColor = secondaryColor,
)
