package com.skyhightechnology.restaurantengine

import androidx.compose.ui.window.ComposeUIViewController
import com.skyhightechnology.restaurantengine.presentation.App
import com.skyhightechnology.restaurantengine.di.initKoin

fun MainViewController() = ComposeUIViewController(
    configure = { initKoin() },
) { App() }
