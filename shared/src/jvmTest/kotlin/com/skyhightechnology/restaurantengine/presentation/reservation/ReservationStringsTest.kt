package com.skyhightechnology.restaurantengine.presentation.reservation

import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DayOfWeek
import org.jetbrains.compose.resources.getString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** JVM only: reading string resources needs Android's real resources on the Android host-test target. */
class ReservationStringsTest {

    @Test
    fun `every reservation error has its own string`() = runTest {
        val errors = listOf(
            ReservationError.NoBranch,
            ReservationError.MissingName,
            ReservationError.MissingPhone,
            ReservationError.InvalidEmail,
            ReservationError.MissingPartySize,
            ReservationError.MissingTime,
            ReservationError.Offline,
            ReservationError.PermissionDenied,
            ReservationError.SubmitFailed,
        )
        assertEquals(errors.size, errors.map { it.messageRes }.toSet().size)
        errors.forEach { assertTrue(getString(it.messageRes).isNotBlank(), it.toString()) }
    }

    @Test
    fun `every weekday has its own short label`() = runTest {
        val days = DayOfWeek.entries
        assertEquals(days.size, days.map { it.shortLabelRes }.toSet().size)
        days.forEach { assertTrue(getString(it.shortLabelRes).isNotBlank(), it.toString()) }
    }
}
