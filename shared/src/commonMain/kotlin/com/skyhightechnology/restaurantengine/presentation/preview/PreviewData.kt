package com.skyhightechnology.restaurantengine.presentation.preview

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.skyhightechnology.restaurantengine.domain.model.MenuItem
import com.skyhightechnology.restaurantengine.domain.model.Reservation
import com.skyhightechnology.restaurantengine.domain.model.RestaurantBranch
import com.skyhightechnology.restaurantengine.domain.model.RestaurantConfig
import com.skyhightechnology.restaurantengine.presentation.menu.MenuCategory
import com.skyhightechnology.restaurantengine.presentation.theme.RestaurantEngineTheme
import kotlinx.datetime.LocalDateTime

/** Phone-sized preview frame shared by every screen preview. */
@Preview(widthDp = 360, heightDp = 760)
internal annotation class ScreenPreview

/** Sample brands and data for the `@Preview`s; the app never shows these. */
internal object PreviewData {
    val burgerJoint = RestaurantConfig(
        id = "burgerjoint",
        name = "De Burger Meesters",
        primaryColor = "#D9531E",
        secondaryColor = "#2B2B2B",
    )
    val cafeLocal = RestaurantConfig(
        id = "cafelocal",
        name = "Café De Koffieboon",
        primaryColor = "#1E3F20",
        secondaryColor = "#E6D5B8",
    )

    val branch = RestaurantBranch(
        id = "amsterdam_centrum",
        name = "Amsterdam Centrum",
        address = "Damrak 12, 1012 LG Amsterdam",
        phoneNumber = "+31 20 123 4567",
        openingHours = "Monday - Sunday: 12:00 - 22:00",
        hasTablesToday = true,
    )
    val branches = listOf(
        branch,
        RestaurantBranch(
            id = "utrecht_oudegracht",
            name = "Utrecht Oudegracht",
            address = "Oudegracht 150, 3511 AZ Utrecht",
            phoneNumber = "+31 30 765 4321",
        ),
        RestaurantBranch(
            id = "rotterdam_markthal",
            name = "Rotterdam Markthal",
            address = "Dominee Jan Scharpstraat 298, 3011 GZ Rotterdam",
            phoneNumber = "+31 10 246 8135",
        ),
    )

    val menu = listOf(
        MenuCategory(
            name = "Burgers",
            items = listOf(
                dish("classic", "Classic Burger", "Beef patty, aged cheddar, pickles and house sauce", 1650, "Burgers"),
                dish("smokehouse", "Smokehouse", "Smoked bacon, onion rings and BBQ glaze", 1850, "Burgers", isAvailable = false),
                dish("veggie", "Garden Burger", null, 1495, "Burgers"),
            ),
        ),
        MenuCategory(
            name = "Sides",
            items = listOf(dish("fries", "Fries", "Hand-cut, sea salt", 450, "Sides")),
        ),
        MenuCategory(
            name = "Drinks",
            items = listOf(dish("lemonade", "Lemonade", "Fresh lemon and mint", 395, "Drinks")),
        ),
    )

    /** A fixed midday "now" so the booking previews don't change from day to day. */
    val now = LocalDateTime(2026, 10, 27, 12, 15)

    /** A booking made earlier on this device, three days after [now] (previews render in UTC). */
    val reservation = Reservation(
        id = "r1",
        branchId = branch.id,
        customerName = "Sanne de Vries",
        customerPhone = "+31 6 1234 5678",
        customerEmail = "sanne@example.com",
        specialRequests = "A table by the window, please.",
        partySize = 4,
        reservationTime = "2026-10-30T19:30:00Z",
        status = "PENDING",
    )

    private fun dish(
        id: String,
        name: String,
        description: String?,
        priceInCents: Int,
        category: String,
        isAvailable: Boolean = true,
    ) = MenuItem(id, name, description, priceInCents, category, imageUrl = null, isAvailable = isAvailable)
}

/** Renders a preview once per brand, so each screen is checked against both palettes. */
internal class BrandPreviewProvider : PreviewParameterProvider<RestaurantConfig> {
    override val values = sequenceOf(PreviewData.burgerJoint, PreviewData.cafeLocal)
}

/** The app's theme and background surface, as [App][com.skyhightechnology.restaurantengine.presentation.App] provides them. */
@Composable
internal fun PreviewTheme(brand: RestaurantConfig = PreviewData.burgerJoint, content: @Composable () -> Unit) {
    RestaurantEngineTheme(brand) {
        Surface(modifier = Modifier.fillMaxSize(), content = content)
    }
}
