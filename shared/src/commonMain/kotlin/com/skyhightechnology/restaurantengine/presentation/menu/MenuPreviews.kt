package com.skyhightechnology.restaurantengine.presentation.menu

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.skyhightechnology.restaurantengine.domain.model.RestaurantConfig
import com.skyhightechnology.restaurantengine.presentation.BackScaffold
import com.skyhightechnology.restaurantengine.presentation.LoadError
import com.skyhightechnology.restaurantengine.presentation.preview.BrandPreviewProvider
import com.skyhightechnology.restaurantengine.presentation.preview.PreviewData
import com.skyhightechnology.restaurantengine.presentation.preview.PreviewTheme
import com.skyhightechnology.restaurantengine.presentation.preview.ScreenPreview
import com.skyhightechnology.restaurantengine.resources.Res
import com.skyhightechnology.restaurantengine.resources.nav_menu

/** Includes a sold-out dish and one without a description. */
@ScreenPreview
@Composable
private fun MenuPreview(@PreviewParameter(BrandPreviewProvider::class) brand: RestaurantConfig) {
    PreviewTheme(brand) { Menu(MenuState(categories = PreviewData.menu)) }
}

@ScreenPreview
@Composable
private fun MenuLoadingPreview() {
    PreviewTheme { Menu(MenuState(isLoading = true)) }
}

@ScreenPreview
@Composable
private fun MenuEmptyPreview() {
    PreviewTheme { Menu(MenuState()) }
}

@ScreenPreview
@Composable
private fun MenuErrorPreview() {
    PreviewTheme { Menu(MenuState(error = LoadError.Offline)) }
}

/** With the app bar the navigation graph puts around the menu. */
@Composable
private fun Menu(state: MenuState) {
    BackScaffold(title = Res.string.nav_menu, subtitle = PreviewData.branch.name, onBack = {}) {
        MenuContent(state)
    }
}
