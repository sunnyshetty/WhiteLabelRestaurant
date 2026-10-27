package com.skyhightechnology.restaurantengine.presentation.reservation

import com.skyhightechnology.restaurantengine.resources.Res
import com.skyhightechnology.restaurantengine.resources.reservation_error_invalid_email
import com.skyhightechnology.restaurantengine.resources.reservation_error_missing_name
import com.skyhightechnology.restaurantengine.resources.reservation_error_missing_party_size
import com.skyhightechnology.restaurantengine.resources.reservation_error_missing_phone
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
        ReservationError.MissingPhone -> Res.string.reservation_error_missing_phone
        ReservationError.InvalidEmail -> Res.string.reservation_error_invalid_email
        ReservationError.MissingPartySize -> Res.string.reservation_error_missing_party_size
        ReservationError.MissingTime -> Res.string.reservation_error_missing_time
        ReservationError.Offline -> Res.string.reservation_error_offline
        ReservationError.PermissionDenied -> Res.string.reservation_error_permission_denied
        ReservationError.SubmitFailed -> Res.string.reservation_error_submit_failed
    }

/** Short weekday name for the date chips ("Thu"). */
val DayOfWeek.shortLabelRes: StringResource
    get() = when (this) {
        DayOfWeek.MONDAY -> Res.string.weekday_short_monday
        DayOfWeek.TUESDAY -> Res.string.weekday_short_tuesday
        DayOfWeek.WEDNESDAY -> Res.string.weekday_short_wednesday
        DayOfWeek.THURSDAY -> Res.string.weekday_short_thursday
        DayOfWeek.FRIDAY -> Res.string.weekday_short_friday
        DayOfWeek.SATURDAY -> Res.string.weekday_short_saturday
        DayOfWeek.SUNDAY -> Res.string.weekday_short_sunday
    }
