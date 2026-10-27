package com.skyhightechnology.restaurantengine.presentation.reservation

import com.skyhightechnology.restaurantengine.resources.Res
import com.skyhightechnology.restaurantengine.resources.day_today
import com.skyhightechnology.restaurantengine.resources.day_tomorrow
import com.skyhightechnology.restaurantengine.resources.reservation_error_missing_name
import com.skyhightechnology.restaurantengine.resources.reservation_error_missing_party_size
import com.skyhightechnology.restaurantengine.resources.reservation_error_missing_time
import com.skyhightechnology.restaurantengine.resources.reservation_error_no_branch
import com.skyhightechnology.restaurantengine.resources.reservation_error_offline
import com.skyhightechnology.restaurantengine.resources.reservation_error_permission_denied
import com.skyhightechnology.restaurantengine.resources.reservation_error_submit_failed
import com.skyhightechnology.restaurantengine.resources.weekday_short_friday
import com.skyhightechnology.restaurantengine.resources.weekday_short_monday
import com.skyhightechnology.restaurantengine.resources.weekday_short_saturday
import com.skyhightechnology.restaurantengine.resources.weekday_short_sunday
import com.skyhightechnology.restaurantengine.resources.weekday_short_thursday
import com.skyhightechnology.restaurantengine.resources.weekday_short_tuesday
import com.skyhightechnology.restaurantengine.resources.weekday_short_wednesday
import kotlinx.datetime.DayOfWeek
import org.jetbrains.compose.resources.StringResource

/** One string per case; the exhaustive `when` makes a new [ReservationError] without a string a compile error. */
val ReservationError.messageRes: StringResource
    get() = when (this) {
        ReservationError.NoBranch -> Res.string.reservation_error_no_branch
        ReservationError.MissingName -> Res.string.reservation_error_missing_name
        ReservationError.MissingPartySize -> Res.string.reservation_error_missing_party_size
        ReservationError.MissingTime -> Res.string.reservation_error_missing_time
        ReservationError.Offline -> Res.string.reservation_error_offline
        ReservationError.PermissionDenied -> Res.string.reservation_error_permission_denied
        ReservationError.SubmitFailed -> Res.string.reservation_error_submit_failed
    }

val DayLabel.labelRes: StringResource
    get() = when (this) {
        DayLabel.Today -> Res.string.day_today
        DayLabel.Tomorrow -> Res.string.day_tomorrow
        is DayLabel.Weekday -> when (day) {
            DayOfWeek.MONDAY -> Res.string.weekday_short_monday
            DayOfWeek.TUESDAY -> Res.string.weekday_short_tuesday
            DayOfWeek.WEDNESDAY -> Res.string.weekday_short_wednesday
            DayOfWeek.THURSDAY -> Res.string.weekday_short_thursday
            DayOfWeek.FRIDAY -> Res.string.weekday_short_friday
            DayOfWeek.SATURDAY -> Res.string.weekday_short_saturday
            DayOfWeek.SUNDAY -> Res.string.weekday_short_sunday
        }
    }
