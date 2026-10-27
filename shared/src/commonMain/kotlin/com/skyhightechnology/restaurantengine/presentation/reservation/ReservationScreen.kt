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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.skyhightechnology.restaurantengine.presentation.component.ErrorBanner
import com.skyhightechnology.restaurantengine.resources.Res
import com.skyhightechnology.restaurantengine.resources.action_dismiss
import com.skyhightechnology.restaurantengine.resources.reservation_book_another
import com.skyhightechnology.restaurantengine.resources.reservation_confirm
import com.skyhightechnology.restaurantengine.resources.reservation_date_chip
import com.skyhightechnology.restaurantengine.resources.reservation_email_label
import com.skyhightechnology.restaurantengine.resources.reservation_guests_or_more
import com.skyhightechnology.restaurantengine.resources.reservation_name_label
import com.skyhightechnology.restaurantengine.resources.reservation_no_account
import com.skyhightechnology.restaurantengine.resources.reservation_phone_label
import com.skyhightechnology.restaurantengine.resources.reservation_pick_date_first
import com.skyhightechnology.restaurantengine.resources.reservation_requests_label
import com.skyhightechnology.restaurantengine.resources.reservation_section_date
import com.skyhightechnology.restaurantengine.resources.reservation_section_details
import com.skyhightechnology.restaurantengine.resources.reservation_section_guests
import com.skyhightechnology.restaurantengine.resources.reservation_section_time
import com.skyhightechnology.restaurantengine.resources.reservation_success_body
import com.skyhightechnology.restaurantengine.resources.reservation_success_title
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock

/** Book a table (Figma "5 · Book a table"): date, guests, time and contact details, confirmed from a pinned bottom bar. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReservationScreen(branchId: String, viewModel: ReservationViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    if (state.isSuccess) {
        BookingConfirmed(onBookAnother = { viewModel.onAction(ReservationAction.Reset) })
        return
    }

    val timeZone = remember { TimeZone.currentSystemDefault() }
    val now = remember { Clock.System.now().toLocalDateTime(timeZone) }
    val today = remember { Clock.System.todayIn(timeZone) }
    val dates = remember(today) { BookingSlots.dates(today) }

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
            onDismissError = { viewModel.onAction(ReservationAction.DismissError) },
            isSubmitting = state.isSubmitting,
            enabled = isComplete,
            onConfirm = {
                if (date != null && selectedTime != null) {
                    viewModel.onAction(
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

@Composable
private fun BookingConfirmed(onBookAnother: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(stringResource(Res.string.reservation_success_title), style = MaterialTheme.typography.headlineSmall)
        Text(
            stringResource(Res.string.reservation_success_body),
            style = MaterialTheme.typography.bodyMedium,
        )
        Button(onClick = onBookAnother) { Text(stringResource(Res.string.reservation_book_another)) }
    }
}
