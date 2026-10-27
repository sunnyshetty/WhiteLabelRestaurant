package com.skyhightechnology.restaurantengine.data.model

import com.skyhightechnology.restaurantengine.domain.model.MenuItem
import com.skyhightechnology.restaurantengine.domain.model.Reservation
import com.skyhightechnology.restaurantengine.domain.model.RestaurantBranch
import com.skyhightechnology.restaurantengine.domain.model.RestaurantConfig
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class DtoMappersTest {

    @Test
    fun `config dto maps to domain with document id`() {
        val dto = RestaurantConfigDto(name = "Burger Joint", primaryColor = "#FF5722", secondaryColor = "#212121")

        assertEquals(
            RestaurantConfig(id = "burgerjoint", name = "Burger Joint", primaryColor = "#FF5722", secondaryColor = "#212121"),
            dto.toDomain("burgerjoint"),
        )
    }

    @Test
    fun `branch dto maps to domain with document id`() {
        val dto = BranchDto(name = "Downtown", address = "1 Main St", phoneNumber = "555-0100")

        assertEquals(
            RestaurantBranch(id = "b1", name = "Downtown", address = "1 Main St", phoneNumber = "555-0100"),
            dto.toDomain("b1"),
        )
    }

    @Test
    fun `menu item dto keeps nullable fields`() {
        val dto = MenuItemDto(name = "Classic", priceInCents = 1299, category = "Burgers", isAvailable = true)

        assertEquals(
            MenuItem(
                id = "m1",
                name = "Classic",
                description = null,
                priceInCents = 1299,
                category = "Burgers",
                imageUrl = null,
                isAvailable = true,
            ),
            dto.toDomain("m1"),
        )
    }

    @Test
    fun `reservation round trips through dto without id`() {
        val reservation = Reservation(
            id = "r1",
            branchId = "b1",
            customerName = "Ada",
            partySize = 4,
            reservationTime = "2026-10-26T19:30:00Z",
            status = "PENDING",
        )

        assertEquals(reservation, reservation.toDto().toDomain("r1"))
    }

    @Test
    fun `menu item dto decodes a document with missing optional fields`() {
        val dto = Json.decodeFromString<MenuItemDto>("""{"name":"Fries","priceInCents":399}""")

        assertEquals(MenuItemDto(name = "Fries", priceInCents = 399), dto)
    }
}
