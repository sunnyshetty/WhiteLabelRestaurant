package com.skyhightechnology.restaurantengine.domain.model

/** Why a repository call failed, independent of the backend that produced the failure. */
sealed interface DataError {
    /** The backend couldn't be reached (no network, or the request timed out). */
    data object Offline : DataError

    /** The backend refused the request (security rules, disabled API, missing auth). */
    data object PermissionDenied : DataError

    /** The requested record doesn't exist. */
    data object NotFound : DataError

    data object Unknown : DataError
}

/** The only exception repositories throw; backend-specific exceptions never leave the data layer. */
class DataException(val error: DataError, cause: Throwable? = null) : Exception(error.toString(), cause)
