package com.skyhightechnology.restaurantengine.presentation.reservation

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant

/** Bookable options shown by [ReservationScreen]. Fixed for now; per-branch opening hours would replace these. */
object BookingSlots {
    /** The last size is shown as "6+": larger groups book 6 and say so in special requests. */
    val partySizes = (1..6).toList()
    val largestPartySize = partySizes.last()

    val times = listOf("12:00", "12:30", "13:00", "13:30", "18:00", "18:30", "19:00", "19:30", "20:00", "20:30", "21:00")

    const val BOOKING_WINDOW_DAYS = 7

    fun dates(today: LocalDate): List<LocalDate> =
        List(BOOKING_WINDOW_DAYS) { today.plus(it, DateTimeUnit.DAY) }

    /** A slot is bookable only if it is still in the future. */
    fun isAvailable(date: LocalDate, time: String, now: LocalDateTime): Boolean =
        LocalDateTime(date, LocalTime.parse(time)) > now

    /** The stored `reservationTime`: the local slot as an ISO-8601 UTC instant, e.g. `2026-10-30T18:30:00Z`. */
    fun reservationTime(date: LocalDate, time: String, timeZone: TimeZone): String =
        LocalDateTime(date, LocalTime.parse(time)).toInstant(timeZone).toString()
}
