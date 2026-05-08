@file:OptIn(ExperimentalMaterial3Api::class)

package ru.application.homemedkit.ui.screens.intake.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.maxLength
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.then
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.collectLatest
import ru.application.homemedkit.R
import ru.application.homemedkit.models.events.IntakeEvent
import ru.application.homemedkit.models.states.IntakeState
import ru.application.homemedkit.ui.elements.TextFieldListItemColors
import ru.application.homemedkit.ui.elements.VectorIcon
import ru.application.homemedkit.utils.DaysInputTransformation
import ru.application.homemedkit.utils.enums.Interval
import ru.application.homemedkit.utils.enums.Period
import ru.application.homemedkit.utils.enums.SchemaType
import ru.application.homemedkit.utils.extensions.defined
import java.time.DayOfWeek
import java.time.format.TextStyle
import androidx.compose.ui.text.intl.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchemaType(state: IntakeState, event: (IntakeEvent) -> Unit) = OutlinedCard {
    ExposedDropdownMenuBox(state.showSchemaTypePicker, {}) {
        ListItem(
            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
            headlineContent = { Text(stringResource(R.string.intake_text_schema_type)) },
            supportingContent = { Text(stringResource(state.schemaType.title)) },
            trailingContent = state.default.let {
                {
                  if (!it) {
                      IconButton(
                          onClick = { event(IntakeEvent.ShowSchemaTypePicker) },
                          content = { ExposedDropdownMenuDefaults.TrailingIcon(state.showSchemaTypePicker) }
                      )
                  }
                }
            }
        )
        ExposedDropdownMenu(state.showSchemaTypePicker, {}) {
            SchemaType.entries.forEach {
                DropdownMenuItem(
                    onClick = { event(IntakeEvent.SetSchemaType(it)) },
                    text = { Text(stringResource(it.title)) }
                )
            }
        }
    }
}

@Composable
fun DaysPicker(state: IntakeState, event: (IntakeEvent) -> Unit) = OutlinedCard {
    val locale = Locale.current.platformLocale

    ListItem(
        headlineContent = { Text(stringResource(R.string.text_repeat)) },
        supportingContent = {
            Text(
                text = if (state.pickedDays.size == DayOfWeek.entries.size) stringResource(R.string.text_every_day)
                else state.pickedDays.joinToString {
                    it.getDisplayName(TextStyle.SHORT, locale)
                }
            )
        }
    )
    Row(
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(ListItemDefaults.containerColor)
    ) {
        DayOfWeek.entries.sorted().forEach { day ->
            FilterChip(
                shape = CircleShape,
                selected = day in state.pickedDays,
                onClick = { if (!state.default) event(IntakeEvent.SetPickedDay(day)) },
                label = {
                    Text(
                        text = day.getDisplayName(TextStyle.NARROW_STANDALONE, locale),
                        textAlign = TextAlign.Center
                    )
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Interval(state: IntakeState, event: (IntakeEvent) -> Unit) =
    OutlinedCard(Modifier.animateContentSize()) {
        ExposedDropdownMenuBox(state.showIntervalTypePicker, {}) {
            ListItem(
                modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                headlineContent = { Text(stringResource(R.string.intake_text_interval)) },
                supportingContent = { Text(stringResource(state.intervalType.title)) },
                trailingContent = state.default.let {
                    {
                        if (!it) {
                            IconButton(
                                onClick = { event(IntakeEvent.ShowIntervalTypePicker) },
                                content = { ExposedDropdownMenuDefaults.TrailingIcon(state.showIntervalTypePicker) }
                            )
                        }
                    }
                }
            )
            ExposedDropdownMenu(state.showIntervalTypePicker, {}) {
                Interval.entries.forEach {
                    DropdownMenuItem(
                        onClick = { event(IntakeEvent.SetInterval(it)) },
                        text = { Text(stringResource(it.title)) }
                    )
                }
            }
        }

        if (state.intervalType == Interval.CUSTOM) {
            val textFieldState = rememberTextFieldState(state.interval)

            LaunchedEffect(textFieldState) {
                snapshotFlow { textFieldState.text.toString() }.collectLatest {
                    event(IntakeEvent.SetInterval(it))
                }
            }

            HorizontalDivider()
            ListItem(
                leadingContent = { Text(stringResource(R.string.text_every)) },
                headlineContent = {
                    OutlinedTextField(
                        state = textFieldState,
                        readOnly = state.default,
                        isError = state.intervalError != null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        placeholder = { Text(stringResource(R.string.text_empty)) },
                        suffix = { Text(stringResource(R.string.text_days_short)) },
                        lineLimits = TextFieldLineLimits.SingleLine,
                        colors = TextFieldListItemColors,
                        inputTransformation = InputTransformation
                            .maxLength(2)
                            .then(DaysInputTransformation),
                    )
                },
                colors = ListItemDefaults.colors(
                    containerColor = if (state.intervalError == null) ListItemDefaults.containerColor
                    else MaterialTheme.colorScheme.errorContainer
                )
            )
        }
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Period(state: IntakeState, event: (IntakeEvent) -> Unit) =
    OutlinedCard(Modifier.animateContentSize()) {
        Row(Modifier.height(IntrinsicSize.Max), verticalAlignment = Alignment.CenterVertically) {
            ExposedDropdownMenuBox(state.showPeriodTypePicker, {}, Modifier.weight(1f)) {
                ListItem(
                    modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                    headlineContent = { Text(stringResource(R.string.intake_text_period)) },
                    supportingContent = { Text(stringResource(state.periodType.title)) },
                    trailingContent = state.default.let {
                        {
                            if (!it) {
                                IconButton(
                                    onClick = { event(IntakeEvent.ShowPeriodTypePicker) },
                                    content = { ExposedDropdownMenuDefaults.TrailingIcon(state.showPeriodTypePicker) }
                                )
                            }
                        }
                    }
                )
                ExposedDropdownMenu(state.showPeriodTypePicker, {}) {
                    Period.entries.forEach {
                        DropdownMenuItem(
                            onClick = { event(IntakeEvent.SetPeriod(it)) },
                            text = { Text(stringResource(it.title)) }
                        )                    }
                }
            }

            if (state.periodType == Period.OTHER) {
                val textFieldState = rememberTextFieldState(state.period)

                LaunchedEffect(textFieldState) {
                    snapshotFlow { textFieldState.text.toString() }.collectLatest {
                        event(IntakeEvent.SetPeriod(it))
                    }
                }

                TextField(
                    state = textFieldState,
                    readOnly = state.default,
                    isError = state.periodError != null,
                    lineLimits = TextFieldLineLimits.SingleLine,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = TextFieldListItemColors,
                    suffix = { Text(stringResource(R.string.text_days_short)) },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    placeholder = {
                        Text(
                            text = stringResource(R.string.text_empty),
                            maxLines = 1
                        )
                    },
                    prefix = {
                        VectorIcon(
                            icon = R.drawable.vector_period,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                    },
                    inputTransformation = InputTransformation
                        .maxLength(3)
                        .then(DaysInputTransformation),
                )
            }
        }

        if (state.periodType != Period.INDEFINITE) {
            HorizontalDivider()
            Row {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.intake_text_start)) },
                    supportingContent = {
                        Text(
                            softWrap = false,
                            overflow = TextOverflow.Visible,
                            text = if (state.periodType == Period.PICK) {
                                state.startDate.ifEmpty { stringResource(R.string.text_not_selected) }
                            } else {
                                state.startDate.ifEmpty { stringResource(R.string.text_today) }
                            }
                        )
                    },
                    trailingContent = state.let {
                        {
                            if (!it.default && it.periodType in Period.entries.defined)
                                VectorIcon(R.drawable.vector_keyboard_arrow_right)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            enabled = !state.default && state.periodType in Period.entries.defined,
                            onClick = { event(IntakeEvent.ShowDatePicker) }
                        ),
                    colors = ListItemDefaults.colors(
                        containerColor = if (state.periodError != null && state.startDate.isEmpty()) {
                            MaterialTheme.colorScheme.errorContainer
                        } else {
                            ListItemDefaults.containerColor
                        }
                    )
                )
                ListItem(
                    headlineContent = { Text(stringResource(R.string.intake_text_finish)) },
                    supportingContent = {
                        Text(
                            softWrap = false,
                            overflow = TextOverflow.Visible,
                            text = if (state.periodType == Period.PICK) {
                                state.finalDate.ifEmpty { stringResource(R.string.text_not_selected) }
                            } else {
                                state.finalDate.ifEmpty { stringResource(R.string.text_tomorrow) }
                            }
                        )
                    },
                    trailingContent = state.let {
                        {
                            if (!it.default && it.periodType == Period.PICK)
                                VectorIcon(R.drawable.vector_keyboard_arrow_right)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            enabled = !state.default && state.periodType == Period.PICK,
                            onClick = { event(IntakeEvent.ShowDatePicker) }
                        ),
                    colors = ListItemDefaults.colors(
                        containerColor = if (state.periodError != null && state.finalDate.isEmpty()) {
                            MaterialTheme.colorScheme.errorContainer
                        } else {
                            ListItemDefaults.containerColor
                        }
                    )
                )
            }
        }
    }
