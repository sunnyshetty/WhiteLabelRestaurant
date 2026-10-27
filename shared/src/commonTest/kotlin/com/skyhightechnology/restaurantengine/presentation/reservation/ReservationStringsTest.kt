package com.skyhightechnology.restaurantengine.presentation.reservation

import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DayOfWeek
import org.jetbrains.compose.resources.getString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ReservationStringsTest {

    @Test
    fun `every reservation error has its own string`() = runTest {
        val errors = listOf(
            ReservationError.NoBranch,
            ReservationError.MissingName,
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
    fun `every day label has its own string`() = runTest {
        val labels = listOf(DayLabel.Today, DayLabel.Tomorrow) + DayOfWeek.entries.map(DayLabel::Weekday)
        assertEquals(labels.size, labels.map { it.labelRes }.toSet().size)
        labels.forEach { assertTrue(getString(it.labelRes).isNotBlank(), it.toString()) }
    }
}
