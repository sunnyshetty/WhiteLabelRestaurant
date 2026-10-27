package com.skyhightechnology.restaurantengine.presentation.reservation

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BookingSlotsTest {
    private val today = LocalDate(2026, 10, 30) // a Friday

    @Test
    fun `offers a one week window starting today`() {
        val dates = BookingSlots.dates(today)

        assertEquals(7, dates.size)
        assertEquals(today, dates.first())
        assertEquals(LocalDate(2026, 11, 5), dates.last())
    }

    @Test
    fun `offers one to six guests with six as the open ended choice`() {
        assertEquals(listOf(1, 2, 3, 4, 5, 6), BookingSlots.partySizes)
        assertEquals(6, BookingSlots.largestPartySize)
    }

    @Test
    fun `only future slots are available`() {
        val now = LocalDateTime(2026, 10, 30, 18, 15)

        assertFalse(BookingSlots.isAvailable(today, "12:00", now))
        assertFalse(BookingSlots.isAvailable(today, "18:00", now))
        assertTrue(BookingSlots.isAvailable(today, "18:30", now))
        assertTrue(BookingSlots.isAvailable(LocalDate(2026, 10, 31), "12:00", now))
    }

    @Test
    fun `stores the local slot as a utc instant`() {
        assertEquals("2026-10-30T19:30:00Z", BookingSlots.reservationTime(today, "19:30", TimeZone.UTC))
        assertEquals(
            "2026-10-30T18:30:00Z",
            BookingSlots.reservationTime(today, "19:30", TimeZone.of("Europe/Amsterdam")),
        )
    }

    @Test
    fun `every configured time parses`() {
        val now = LocalDateTime(2000, 1, 1, 0, 0)
        BookingSlots.times.forEach { assertTrue(BookingSlots.isAvailable(today, it, now), it) }
    }
}
