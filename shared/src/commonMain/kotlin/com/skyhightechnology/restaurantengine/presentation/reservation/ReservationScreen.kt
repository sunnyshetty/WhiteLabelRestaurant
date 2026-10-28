package com.skyhightechnology.restaurantengine.presentation.reservation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skyhightechnology.restaurantengine.presentation.component.AppIcons
import com.skyhightechnology.restaurantengine.domain.model.Reservation
import com.skyhightechnology.restaurantengine.presentation.component.ErrorBanner
import com.skyhightechnology.restaurantengine.presentation.component.LoadingView
import com.skyhightechnology.restaurantengine.resources.Res
import com.skyhightechnology.restaurantengine.resources.action_dismiss
import com.skyhightechnology.restaurantengine.resources.reservation_book_another
import com.skyhightechnology.restaurantengine.resources.reservation_confirm
import com.skyhightechnology.restaurantengine.resources.reservation_date_chip
import com.skyhightechnology.restaurantengine.resources.reservation_email_label
import com.skyhightechnology.restaurantengine.resources.reservation_existing_title
import com.skyhightechnology.restaurantengine.resources.reservation_guest_count
import com.skyhightechnology.restaurantengine.resources.reservation_guests_or_more
import com.skyhightechnology.restaurantengine.resources.reservation_name_label
import com.skyhightechnology.restaurantengine.resources.reservation_no_account
import com.skyhightechnology.restaurantengine.resources.reservation_phone_label
import com.skyhightechnology.restaurantengine.resources.reservation_pick_date_first
import com.skyhightechnology.restaurantengine.resources.reservation_requests_label
import com.skyhightechnology.restaurantengine.resources.reservation_requests_title
import com.skyhightechnology.restaurantengine.resources.reservation_section_date
import com.skyhightechnology.restaurantengine.resources.reservation_section_details
import com.skyhightechnology.restaurantengine.resources.reservation_section_guests
import com.skyhightechnology.restaurantengine.resources.reservation_section_time
import com.skyhightechnology.restaurantengine.resources.reservation_when
import com.skyhightechnology.restaurantengine.resources.reservation_when_label
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock

/**
 * Book a table (Figma "5 · Book a table"): date, guests, time and contact details, confirmed from a pinned bottom bar.
 * If the guest already has an upcoming booking at the branch, that booking is shown instead.
 */
@Composable
fun ReservationScreen(branchId: String, viewModel: ReservationViewModel = koinViewModel()) {
    LaunchedEffect(branchId) { viewModel.onAction(ReservationAction.SelectBranch(branchId)) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val timeZone = remember { TimeZone.currentSystemDefault() }
    val now = remember { Clock.System.now().toLocalDateTime(timeZone) }
    ReservationContent(state, branchId, now, timeZone, viewModel::onAction)
}

/**
 * Stateless body of [ReservationScreen] apart from the form fields, also used by the previews.
 * [now] decides which dates are offered and which slots have passed.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ReservationContent(
    state: ReservationState,
    branchId: String,
    now: LocalDateTime,
    timeZone: TimeZone,
    onAction: (ReservationAction) -> Unit,
) {
    if (state.isLoading) {
        LoadingView()
        return
    }
    state.reservation?.let { reservation ->
        BookingDetails(reservation, timeZone, onBookAnother = { onAction(ReservationAction.Reset) })
        return
    }

    val dates = remember(now.date) { BookingSlots.dates(now.date) }

    var dateIso by rememberSaveable { mutableStateOf<String?>(null) }
    var partySize by rememberSaveable { mutableStateOf<Int?>(null) }
    var time by rememberSaveable { mutableStateOf<String?>(null) }
    var name by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var requests by rememberSaveable { mutableStateOf("") }

    val date = dateIso?.let(LocalDate::parse)
    val selectedTime = time?.takeIf { date != null && BookingSlots.isAvailable(date, it, now) }
    val isComplete = date != null && partySize != null && selectedTime != null &&
        name.isNotBlank() && phone.isNotBlank() && email.isNotBlank()

    Column(modifier = Modifier.fillMaxSize().imePadding()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Section(Res.string.reservation_section_date) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    dates.forEach { day ->
                        ChoiceChip(
                            label = stringResource(Res.string.reservation_date_chip, stringResource(day.dayOfWeek.shortLabelRes), day.day),
                            selected = day == date,
                            onClick = { dateIso = day.toString() },
                        )
                    }
                }
            }

            Section(Res.string.reservation_section_guests) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BookingSlots.partySizes.forEach { size ->
                        ChoiceChip(
                            label = if (size == BookingSlots.largestPartySize) {
                                stringResource(Res.string.reservation_guests_or_more, size)
                            } else {
                                size.toString()
                            },
                            selected = partySize == size,
                            onClick = { partySize = size },
                        )
                    }
                }
            }

            Section(Res.string.reservation_section_time) {
                TimeChips(date = date, selected = selectedTime, now = now, onSelect = { time = it })
            }

            Section(Res.string.reservation_section_details, spacing = 12.dp) {
                DetailField(name, { name = it }, Res.string.reservation_name_label, KeyboardType.Text, KeyboardCapitalization.Words)
                DetailField(phone, { phone = it }, Res.string.reservation_phone_label, KeyboardType.Phone)
                DetailField(email, { email = it }, Res.string.reservation_email_label, KeyboardType.Email)
                OutlinedTextField(
                    value = requests,
                    onValueChange = { requests = it },
                    label = { Text(stringResource(Res.string.reservation_requests_label)) },
                    maxLines = 3,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        BottomBar(
            error = state.error,
            onDismissError = { onAction(ReservationAction.DismissError) },
            isSubmitting = state.isSubmitting,
            enabled = isComplete,
            onConfirm = {
                if (date != null && selectedTime != null) {
                    onAction(
                        ReservationAction.Submit(
                            branchId = branchId,
                            customerName = name,
                            customerPhone = phone,
                            customerEmail = email,
                            specialRequests = requests,
                            partySize = partySize ?: 0,
                            reservationTime = BookingSlots.reservationTime(date, selectedTime, timeZone),
                        ),
                    )
                }
            },
        )
    }
}

@Composable
private fun Section(title: StringResource, spacing: Dp = 8.dp, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(spacing)) {
        Text(stringResource(title), style = MaterialTheme.typography.titleSmall)
        content()
    }
}

/** Outlined filter chip with the design's leading check when selected. */
@Composable
private fun ChoiceChip(label: String, selected: Boolean, onClick: () -> Unit, enabled: Boolean = true) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        enabled = enabled,
        label = { Text(label) },
        leadingIcon = if (selected) {
            { Icon(AppIcons.Check, contentDescription = null, modifier = Modifier.size(FilterChipDefaults.IconSize)) }
        } else {
            null
        },
    )
}

/** Every slot for the chosen date; slots already in the past are disabled. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TimeChips(date: LocalDate?, selected: String?, now: LocalDateTime, onSelect: (String) -> Unit) {
    if (date == null) {
        Text(
            stringResource(Res.string.reservation_pick_date_first),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        BookingSlots.times.forEach { slot ->
            ChoiceChip(
                label = slot,
                selected = slot == selected,
                onClick = { onSelect(slot) },
                enabled = BookingSlots.isAvailable(date, slot, now),
            )
        }
    }
}

@Composable
private fun DetailField(
    value: String,
    onValueChange: (String) -> Unit,
    label: StringResource,
    keyboardType: KeyboardType,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.None,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(stringResource(label)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(capitalization = capitalization, keyboardType = keyboardType, imeAction = ImeAction.Next),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun BottomBar(
    error: ReservationError?,
    onDismissError: () -> Unit,
    isSubmitting: Boolean,
    enabled: Boolean,
    onConfirm: () -> Unit,
) {
    Surface(color = MaterialTheme.colorScheme.surfaceContainer, modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.navigationBarsPadding().padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            error?.let {
                ErrorBanner(
                    message = stringResource(it.messageRes),
                    actionLabel = stringResource(Res.string.action_dismiss),
                    onAction = onDismissError,
                )
            }
            Button(
                onClick = onConfirm,
                enabled = enabled && !isSubmitting,
                modifier = Modifier.heightIn(min = 56.dp),
                contentPadding = PaddingValues(horizontal = 24.dp),
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = LocalContentColor.current)
                } else {
                    Text(stringResource(Res.string.reservation_confirm), style = MaterialTheme.typography.titleMedium)
                }
            }
            Text(
                stringResource(Res.string.reservation_no_account),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** The guest's booking, with the status the branch has set. */
@Composable
private fun BookingDetails(reservation: Reservation, timeZone: TimeZone, onBookAnother: () -> Unit) {
    val time = Instant.parse(reservation.reservationTime).toLocalDateTime(timeZone)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(stringResource(Res.string.reservation_existing_title), style = MaterialTheme.typography.headlineSmall)
        Text(
            stringResource(reservation.statusRes),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                DetailRow(
                    Res.string.reservation_when_label,
                    stringResource(Res.string.reservation_when, stringResource(time.dayOfWeek.shortLabelRes), time.day, time.time.toString()),
                )
                DetailRow(
                    Res.string.reservation_section_guests,
                    pluralStringResource(Res.plurals.reservation_guest_count, reservation.partySize, reservation.partySize),
                )
                DetailRow(Res.string.reservation_name_label, reservation.customerName)
                DetailRow(Res.string.reservation_phone_label, reservation.customerPhone)
                if (reservation.specialRequests.isNotBlank()) {
                    DetailRow(Res.string.reservation_requests_title, reservation.specialRequests)
                }
            }
        }
        FilledTonalButton(
            onClick = onBookAnother,
            modifier = Modifier.heightIn(min = 56.dp).align(Alignment.CenterHorizontally),
            contentPadding = PaddingValues(horizontal = 24.dp),
        ) {
            Text(stringResource(Res.string.reservation_book_another), style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun DetailRow(label: StringResource, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(stringResource(label), style = MaterialTheme.typography.labelLarge)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
