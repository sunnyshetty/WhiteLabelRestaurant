package com.skyhightechnology.restaurantengine.presentation.reservation

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.skyhightechnology.restaurantengine.domain.model.RestaurantConfig
import com.skyhightechnology.restaurantengine.presentation.BackScaffold
import com.skyhightechnology.restaurantengine.presentation.preview.BrandPreviewProvider
import com.skyhightechnology.restaurantengine.presentation.preview.PreviewData
import com.skyhightechnology.restaurantengine.presentation.preview.PreviewTheme
import com.skyhightechnology.restaurantengine.presentation.preview.ScreenPreview
import com.skyhightechnology.restaurantengine.resources.Res
import com.skyhightechnology.restaurantengine.resources.restaurant_book_table
import kotlinx.datetime.TimeZone

@ScreenPreview
@Composable
private fun ReservationFormPreview(@PreviewParameter(BrandPreviewProvider::class) brand: RestaurantConfig) {
    PreviewTheme(brand) { Reservation(ReservationState()) }
}

@ScreenPreview
@Composable
private fun ReservationErrorPreview() {
    PreviewTheme { Reservation(ReservationState(error = ReservationError.Offline)) }
}

@ScreenPreview
@Composable
private fun ReservationSubmittingPreview() {
    PreviewTheme { Reservation(ReservationState(isSubmitting = true)) }
}

@ScreenPreview
@Composable
private fun ReservationExistingPreview(@PreviewParameter(BrandPreviewProvider::class) brand: RestaurantConfig) {
    PreviewTheme(brand) { Reservation(ReservationState(reservation = PreviewData.reservation)) }
}

@ScreenPreview
@Composable
private fun ReservationConfirmedByBranchPreview() {
    PreviewTheme { Reservation(ReservationState(reservation = PreviewData.reservation.copy(status = "CONFIRMED"))) }
}

@ScreenPreview
@Composable
private fun ReservationLoadingPreview() {
    PreviewTheme { Reservation(ReservationState(isLoading = true)) }
}

/** With the app bar the navigation graph puts around the booking form. */
@Composable
private fun Reservation(state: ReservationState) {
    BackScaffold(title = Res.string.restaurant_book_table, subtitle = PreviewData.branch.name, onBack = {}) {
        ReservationContent(
            state = state,
            branchId = PreviewData.branch.id,
            now = PreviewData.now,
            timeZone = TimeZone.UTC,
            onAction = {},
        )
    }
}
