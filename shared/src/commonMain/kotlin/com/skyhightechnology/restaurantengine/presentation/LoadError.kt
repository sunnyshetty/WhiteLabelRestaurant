package com.skyhightechnology.restaurantengine.presentation

import com.skyhightechnology.restaurantengine.domain.model.DataError
import com.skyhightechnology.restaurantengine.domain.model.DataException

/** Why a screen couldn't load its data. The UI maps each case to one string resource. */
sealed interface LoadError {
    data object Offline : LoadError
    data object PermissionDenied : LoadError
    data object NotFound : LoadError
    data object Unknown : LoadError
}

/** Anything that isn't a repository [DataException] is a bug or an unexpected failure: [LoadError.Unknown]. */
fun Throwable.toLoadError(): LoadError = when ((this as? DataException)?.error) {
    DataError.Offline -> LoadError.Offline
    DataError.PermissionDenied -> LoadError.PermissionDenied
    DataError.NotFound -> LoadError.NotFound
    DataError.Unknown, null -> LoadError.Unknown
}
