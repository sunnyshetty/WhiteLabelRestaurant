package com.skyhightechnology.restaurantengine.data.model

import com.skyhightechnology.restaurantengine.domain.model.RestaurantBranch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Firestore document `restaurants/{restaurantId}/branches/{branchId}`. */
@Serializable
data class BranchDto(
    @SerialName("branchName") val name: String = "",
    val address: String = "",
    val phoneNumber: String = "",
)

fun BranchDto.toDomain(id: String) = RestaurantBranch(
    id = id,
    name = name,
    address = address,
    phoneNumber = phoneNumber,
)
