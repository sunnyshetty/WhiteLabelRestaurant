package com.skyhightechnology.restaurantengine.presentation.restaurant

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.skyhightechnology.restaurantengine.domain.model.RestaurantConfig
import com.skyhightechnology.restaurantengine.presentation.LoadError
import com.skyhightechnology.restaurantengine.presentation.preview.BrandPreviewProvider
import com.skyhightechnology.restaurantengine.presentation.preview.PreviewData
import com.skyhightechnology.restaurantengine.presentation.preview.PreviewTheme
import com.skyhightechnology.restaurantengine.presentation.preview.ScreenPreview

@ScreenPreview
@Composable
private fun RestaurantPreview(@PreviewParameter(BrandPreviewProvider::class) brand: RestaurantConfig) {
    PreviewTheme(brand) { Restaurant(RestaurantState(branch = PreviewData.branch), brand.name) }
}

/** Fully booked, and a branch that hasn't filled in its opening hours. */
@ScreenPreview
@Composable
private fun RestaurantFullyBookedPreview() {
    PreviewTheme {
        Restaurant(RestaurantState(branch = PreviewData.branch.copy(openingHours = "", hasTablesToday = false)))
    }
}

@ScreenPreview
@Composable
private fun RestaurantLoadingPreview() {
    PreviewTheme { Restaurant(RestaurantState(isLoading = true)) }
}

@ScreenPreview
@Composable
private fun RestaurantNotFoundPreview() {
    PreviewTheme { Restaurant(RestaurantState(error = LoadError.NotFound)) }
}

@Composable
private fun Restaurant(state: RestaurantState, brandName: String = PreviewData.burgerJoint.name) {
    RestaurantContent(brandName = brandName, state = state, onBack = {}, onBookTable = {}, onViewMenu = {})
}
