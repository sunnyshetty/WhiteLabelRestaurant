package com.skyhightechnology.restaurantengine.presentation.reservation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skyhightechnology.restaurantengine.presentation.component.ErrorBanner
import com.skyhightechnology.restaurantengine.resources.Res
import com.skyhightechnology.restaurantengine.resources.action_dismiss
import com.skyhightechnology.restaurantengine.resources.reservation_book_another
import com.skyhightechnology.restaurantengine.resources.reservation_confirm
import com.skyhightechnology.restaurantengine.resources.reservation_name_label
import com.skyhightechnology.restaurantengine.resources.reservation_party_size
import com.skyhightechnology.restaurantengine.resources.reservation_pick_date_first
import com.skyhightechnology.restaurantengine.resources.reservation_step_date
import com.skyhightechnology.restaurantengine.resources.reservation_step_name
import com.skyhightechnology.restaurantengine.resources.reservation_step_party_size
import com.skyhightechnology.restaurantengine.resources.reservation_step_time
import com.skyhightechnology.restaurantengine.resources.reservation_success_body
import com.skyhightechnology.restaurantengine.resources.reservation_success_title
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock

/** Step-by-step booking: party size, date, time, name, then confirm. */
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

    var partySize by rememberSaveable { mutableStateOf<Int?>(null) }
    var dateIso by rememberSaveable { mutableStateOf<String?>(null) }
    var time by rememberSaveable { mutableStateOf<String?>(null) }
    var name by rememberSaveable { mutableStateOf("") }

    val date = dateIso?.let(LocalDate::parse)
    val selectedTime = time?.takeIf { date != null && BookingSlots.isAvailable(date, it, now) }
    val isComplete = partySize != null && date != null && selectedTime != null && name.isNotBlank()

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Step(stringResource(Res.string.reservation_step_party_size)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BookingSlots.partySizes.forEach { size ->
                    FilterChip(
                        selected = partySize == size,
                        onClick = { partySize = size },
                        label = { Text(pluralStringResource(Res.plurals.reservation_party_size, size, size)) },
                    )
                }
            }
        }

        Step(stringResource(Res.string.reservation_step_date)) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(end = 16.dp)) {
                items(dates, key = { it.toString() }) { day ->
                    DateCell(
                        label = BookingSlots.dayLabel(day, today),
                        day = day.day,
                        selected = day == date,
                        onClick = { dateIso = day.toString() },
                    )
                }
            }
        }

        Step(stringResource(Res.string.reservation_step_time)) {
            TimeSlotMatrix(date = date, selected = selectedTime, now = now, onSelect = { time = it })
        }

        Step(stringResource(Res.string.reservation_step_name)) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(Res.string.reservation_name_label)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        state.error?.let { error ->
            ErrorBanner(
                message = stringResource(error.messageRes),
                actionLabel = stringResource(Res.string.action_dismiss),
                onAction = { viewModel.onAction(ReservationAction.DismissError) },
            )
        }

        Button(
            onClick = {
                if (date != null && selectedTime != null) {
                    viewModel.onAction(
                        ReservationAction.Submit(
                            branchId = branchId,
                            customerName = name,
                            partySize = partySize ?: 0,
                            reservationTime = BookingSlots.reservationTime(date, selectedTime, timeZone),
                        ),
                    )
                }
            },
            enabled = isComplete && !state.isSubmitting,
            modifier = Modifier.fillMaxWidth().height(56.dp),
        ) {
            if (state.isSubmitting) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            } else {
                Text(stringResource(Res.string.reservation_confirm))
            }
        }
    }
}

@Composable
private fun Step(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        content()
    }
}

@Composable
private fun DateCell(label: DayLabel, day: Int, selected: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(
        selected = selected,
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = if (selected) colors.primary else colors.surface,
        contentColor = if (selected) colors.onPrimary else colors.onSurface,
        border = if (selected) null else BorderStroke(1.dp, colors.outline),
        modifier = Modifier.width(64.dp),
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(stringResource(label.labelRes), style = MaterialTheme.typography.labelMedium, maxLines = 1)
            Text(day.toString(), style = MaterialTheme.typography.titleLarge)
        }
    }
}

/** Every slot, with past ones disabled. SuggestionChip has no selected state, so selection is drawn with colors. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TimeSlotMatrix(
    date: LocalDate?,
    selected: String?,
    now: LocalDateTime,
    onSelect: (String) -> Unit,
) {
    if (date == null) {
        Text(
            stringResource(Res.string.reservation_pick_date_first),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        BookingSlots.times.forEach { slot ->
            val isSelected = slot == selected
            SuggestionChip(
                onClick = { onSelect(slot) },
                label = { Text(slot) },
                enabled = BookingSlots.isAvailable(date, slot, now),
                colors = if (isSelected) {
                    SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        labelColor = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    SuggestionChipDefaults.suggestionChipColors()
                },
                border = if (isSelected) null else SuggestionChipDefaults.suggestionChipBorder(enabled = true),
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
