@file:OptIn(ExperimentalMaterial3Api::class)

package ru.application.homemedkit.ui.screens.intake.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.maxLength
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.then
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
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
import ru.application.homemedkit.utils.DecimalAmountInputTransformation
import ru.application.homemedkit.utils.DecimalAmountOutputTransformation
import ru.application.homemedkit.utils.EmptyInteractionSource
import ru.application.homemedkit.utils.Formatter
import ru.application.homemedkit.utils.enums.FoodType
import ru.application.homemedkit.utils.enums.IntakeExtra
import ru.application.homemedkit.utils.extensions.canUseFullScreenIntent

@Composable
fun Amount(state: IntakeState, event: (IntakeEvent) -> Unit) =
    OutlinedCard(Modifier.animateContentSize()) {
        ListItem(
            headlineContent = { Text(stringResource(R.string.intake_text_amount)) },
            supportingContent = {
                Text(
                    text = stringResource(
                        id = R.string.intake_text_in_stock_params,
                        formatArgs = arrayOf(
                            Formatter.decimalFormat(state.amountStock),
                            stringResource(state.doseType)
                        )
                    )
                )
            }
        )

        HorizontalDivider()

        ListItem(
            headlineContent = { Text(stringResource(R.string.text_same_amount)) },
            supportingContent = {
                Text(stringResource(if (state.sameAmount) R.string.text_on else R.string.text_off))
            },
            trailingContent = {
                Switch(
                    checked = state.sameAmount,
                    onCheckedChange = { if (!state.default) event(IntakeEvent.SetSameAmount(it)) }
                )
            }
        )

        if (state.sameAmount) {
            val textFieldState = rememberTextFieldState(state.pickedTime.first().amount)

            LaunchedEffect(textFieldState) {
                snapshotFlow { textFieldState.text.toString() }.collectLatest {
                    event(IntakeEvent.SetAmount(it))
                }
            }

            HorizontalDivider()
            ListItem(
                leadingContent = { VectorIcon(R.drawable.vector_medicine) },
                headlineContent = {
                    TextField(
                        state = textFieldState,
                        readOnly = state.default,
                        isError = state.amountError != null && state.pickedTime.first().amount.isEmpty(),
                        placeholder = { Text(stringResource(R.string.text_empty)) },
                        suffix = { Text(stringResource(state.doseType)) },
                        lineLimits = TextFieldLineLimits.SingleLine,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        outputTransformation = DecimalAmountOutputTransformation,
                        colors = TextFieldListItemColors,
                        inputTransformation = InputTransformation
                            .maxLength(8)
                            .then(DecimalAmountInputTransformation),
                    )
                }
            )
        }
    }

@Composable
fun Food(state: IntakeState, event: (IntakeEvent) -> Unit) = OutlinedCard {
    ListItem(
        headlineContent = { Text(stringResource(R.string.intake_text_food)) },
        supportingContent = {
            Text(
                text = stringResource(
                    id = if (state.foodType == -1) R.string.text_not_selected
                    else R.string.text_selected
                )
            )
        }
    )
    ListItem(
        headlineContent = {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceAround, Alignment.CenterVertically) {
                FoodType.entries.forEach { type ->
                    FilterChip(
                        modifier = Modifier.width(100.dp),
                        selected = type.value == state.foodType,
                        onClick = { if (!state.default) event(IntakeEvent.SetFoodType(type.value)) },
                        label = {
                            Text(
                                text = stringResource(type.title),
                                textAlign = TextAlign.Center,
                                minLines = 2,
                                maxLines = 2
                            )
                        }
                    )
                }
            }
        }
    )
}

@Composable
fun Time(state: IntakeState, event: (IntakeEvent) -> Unit) =
    OutlinedCard(Modifier.animateContentSize()) {
        ListItem(
            headlineContent = { Text(stringResource(R.string.intake_text_time)) },
            supportingContent = {
                Text(
                    text = pluralStringResource(
                        id = R.plurals.intake_times_a_day,
                        count = state.pickedTime.size,
                        formatArgs = arrayOf(state.pickedTime.size)
                    )
                )
            },
            trailingContent = {
                AnimatedVisibility(!state.default) {
                    Row {
                        FilledTonalIconButton(
                            enabled = state.pickedTime.size > 1,
                            onClick = { event(IntakeEvent.DecTime) },
                            content = { VectorIcon(R.drawable.vector_remove) }
                        )

                        FilledTonalIconButton(
                            onClick = { event(IntakeEvent.IncTime) },
                            content = { VectorIcon(R.drawable.vector_add) }
                        )
                    }
                }
            }
        )

        HorizontalDivider()

        state.pickedTime.forEachIndexed { index, amountTime ->
            Row {
                ListItem(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(enabled = !state.default) { event(IntakeEvent.ShowTimePicker(index)) },
                    leadingContent = { VectorIcon(R.drawable.vector_time) },
                    headlineContent = {
                        Text(
                            text = stringResource(R.string.placeholder_time, index + 1),
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1
                        )
                    },
                    supportingContent = {
                        Text(
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            text = amountTime.time.ifEmpty {
                                stringResource(R.string.text_not_selected)
                            }
                        )
                    },
                    colors = ListItemDefaults.colors(
                        containerColor = if (state.timesError != null && amountTime.time.isEmpty()) {
                            MaterialTheme.colorScheme.errorContainer
                        } else {
                            ListItemDefaults.containerColor
                        }
                    )
                )

                if (!state.sameAmount) {
                    val textFieldState = rememberTextFieldState(amountTime.amount)

                    LaunchedEffect(textFieldState) {
                        snapshotFlow { textFieldState.text.toString() }.collectLatest {
                            event(IntakeEvent.SetAmount(it, index))
                        }
                    }

                    ListItem(
                        modifier = Modifier.weight(1f),
                        headlineContent = {
                            TextField(
                                state = textFieldState,
                                modifier = Modifier.fillMaxHeight(),
                                readOnly = state.default,
                                isError = state.amountError != null && amountTime.amount.isEmpty(),
                                placeholder = { Text(stringResource(R.string.text_empty)) },
                                suffix = { Text(stringResource(state.doseType)) },
                                lineLimits = TextFieldLineLimits.SingleLine,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                outputTransformation = DecimalAmountOutputTransformation,
                                colors = TextFieldListItemColors,
                                inputTransformation = InputTransformation
                                    .maxLength(8)
                                    .then(DecimalAmountInputTransformation)
                            )
                        },
                        colors = ListItemDefaults.colors(
                            containerColor = if (state.amountError != null && amountTime.amount.isEmpty()) {
                                MaterialTheme.colorScheme.errorContainer
                            } else {
                                ListItemDefaults.containerColor
                            }
                        )
                    )
                }
            }
        }
    }

@Composable
fun Extra(state: IntakeState, event: (IntakeEvent) -> Unit) {
    val context = LocalContext.current

    val extrasFiltered = remember {
        IntakeExtra.entries.filter { extra ->
            !(extra == IntakeExtra.FULLSCREEN && !context.canUseFullScreenIntent())
        }
    }

    val extraAssociated = mapOf(
        IntakeExtra.CANCELLABLE to state.cancellable,
        IntakeExtra.FULLSCREEN to state.fullScreen,
        IntakeExtra.NO_SOUND to state.noSound,
        IntakeExtra.PREALARM to state.preAlarm,
    )

    OutlinedCard {
        ListItem(
            headlineContent = { Text(stringResource(R.string.intake_text_extra)) },
            supportingContent = {
                Text(
                    text = stringResource(
                        id = R.string.text_selected_of,
                        formatArgs = arrayOf(state.selectedExtras.size, extrasFiltered.size)
                    )
                )
            }
        )

        HorizontalDivider()

        extrasFiltered.forEach { extra ->
            val checked = extraAssociated.getOrDefault(extra, false)
            ListItem(
                modifier = Modifier.clickable(enabled = !state.default) {
                    event(IntakeEvent.SetIntakeExtra(extra))
                },
                headlineContent = {
                    Text(
                        text = stringResource(extra.title),
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 2
                    )
                },
                leadingContent = {
                    Checkbox(
                        onCheckedChange = null,
                        checked = checked
                    )
                },
                trailingContent = {
                    IconButton(
                        onClick = { event(IntakeEvent.ShowDialogDescription(extra.description)) },
                        content = { VectorIcon(R.drawable.vector_info) }
                    )
                }
            )
        }
    }
}
