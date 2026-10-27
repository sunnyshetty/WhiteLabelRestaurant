package com.skyhightechnology.restaurantengine.presentation

import com.skyhightechnology.restaurantengine.resources.Res
import com.skyhightechnology.restaurantengine.resources.load_error_not_found
import com.skyhightechnology.restaurantengine.resources.load_error_offline
import com.skyhightechnology.restaurantengine.resources.load_error_permission_denied
import com.skyhightechnology.restaurantengine.resources.load_error_unknown
import org.jetbrains.compose.resources.StringResource

/** One string per case; the exhaustive `when` makes a new [LoadError] without a string a compile error. */
val LoadError.messageRes: StringResource
    get() = when (this) {
        LoadError.Offline -> Res.string.load_error_offline
        LoadError.PermissionDenied -> Res.string.load_error_permission_denied
        LoadError.NotFound -> Res.string.load_error_not_found
        LoadError.Unknown -> Res.string.load_error_unknown
    }
