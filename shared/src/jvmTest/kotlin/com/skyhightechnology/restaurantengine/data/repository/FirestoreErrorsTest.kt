package com.skyhightechnology.restaurantengine.data.repository

import com.skyhightechnology.restaurantengine.domain.model.DataError
import com.skyhightechnology.restaurantengine.domain.model.DataException
import dev.gitlive.firebase.firestore.FirestoreExceptionCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

/** JVM only: building GitLive Firestore exceptions needs the real Firebase SDK on Android and iOS. */
class FirestoreErrorsTest {

    @Test
    fun `maps firestore codes to domain errors`() {
        val expected = mapOf(
            FirestoreExceptionCode.UNAVAILABLE to DataError.Offline,
            FirestoreExceptionCode.DEADLINE_EXCEEDED to DataError.Offline,
            FirestoreExceptionCode.PERMISSION_DENIED to DataError.PermissionDenied,
            FirestoreExceptionCode.UNAUTHENTICATED to DataError.PermissionDenied,
            FirestoreExceptionCode.NOT_FOUND to DataError.NotFound,
        )
        FirestoreExceptionCode.entries.forEach { code ->
            assertEquals(expected[code] ?: DataError.Unknown, code.toDataError(), code.name)
        }
    }

    @Test
    fun `keeps domain exceptions and wraps anything else as unknown`() {
        val domain = DataException(DataError.NotFound)
        assertSame(domain, domain.toDataException())

        val wrapped = IllegalStateException("decode failed").toDataException()
        assertEquals(DataError.Unknown, wrapped.error)
    }
}
