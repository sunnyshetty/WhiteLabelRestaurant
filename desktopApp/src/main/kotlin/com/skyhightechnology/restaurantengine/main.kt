package com.skyhightechnology.restaurantengine

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.skyhightechnology.restaurantengine.di.initKoin

fun main() {
    initKoin()
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "WhiteLabelRestaurant",
        ) {
            App()
        }
    }
}
