package com.skyhightechnology.restaurantengine

import platform.Foundation.NSBundle

actual fun getApplicationId(): String =
    checkNotNull(NSBundle.mainBundle.bundleIdentifier) { "Main bundle has no bundle identifier" }
