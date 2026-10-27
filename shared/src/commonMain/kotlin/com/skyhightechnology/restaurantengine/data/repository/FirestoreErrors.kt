package com.skyhightechnology.restaurantengine.data.repository

import com.skyhightechnology.restaurantengine.domain.model.DataError
import com.skyhightechnology.restaurantengine.domain.model.DataException
import dev.gitlive.firebase.firestore.FirebaseFirestoreException
import dev.gitlive.firebase.firestore.FirestoreExceptionCode
import dev.gitlive.firebase.firestore.code
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch

internal fun FirestoreExceptionCode.toDataError(): DataError = when (this) {
    FirestoreExceptionCode.UNAVAILABLE, FirestoreExceptionCode.DEADLINE_EXCEEDED -> DataError.Offline
    FirestoreExceptionCode.PERMISSION_DENIED, FirestoreExceptionCode.UNAUTHENTICATED -> DataError.PermissionDenied
    FirestoreExceptionCode.NOT_FOUND -> DataError.NotFound
    else -> DataError.Unknown
}

internal fun Throwable.toDataException(): DataException = when (this) {
    is DataException -> this
    is FirebaseFirestoreException -> DataException(code.toDataError(), this)
    else -> DataException(DataError.Unknown, this)
}

/** Runs a one-shot Firestore call, rethrowing failures as [DataException]. */
internal suspend fun <T> firestoreCall(block: suspend () -> T): T =
    try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        throw e.toDataException()
    }

/** Rethrows stream failures as [DataException]. */
internal fun <T> Flow<T>.mapFirestoreErrors(): Flow<T> = catch { e ->
    if (e is CancellationException) throw e
    throw e.toDataException()
}
