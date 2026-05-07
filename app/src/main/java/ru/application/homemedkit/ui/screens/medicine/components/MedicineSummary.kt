package ru.application.homemedkit.ui.screens.medicine.components

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import kotlinx.coroutines.flow.collectLatest
import ru.application.homemedkit.R
import ru.application.homemedkit.data.dto.Kit
import ru.application.homemedkit.models.events.MedicineEvent
import ru.application.homemedkit.models.events.MedicineEvent.SetProductName
import ru.application.homemedkit.models.states.MedicineDialogState
import ru.application.homemedkit.models.states.MedicineState

@Composable
fun Summary(state: MedicineState, onEvent: (MedicineEvent) -> Unit) {

    @Composable
    fun LocalLabel(@StringRes text: Int) =
        Text(
            text = stringResource(text),
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.W400)
        )

    @Composable
    fun LocalText(text: String, style: TextStyle = MaterialTheme.typography.titleMedium) {
        Text(
            text = text.ifEmpty { stringResource(R.string.text_unspecified) },
            style = style,
            softWrap = false,
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
        )
    }

    @Composable
    fun LocalTextField(
        value: String,
        onEvent: () -> Unit,
        @StringRes label: Int,
        @StringRes placeholder: Int,
        modifier: Modifier = Modifier,
    ) {
        val interactionSource = remember(::MutableInteractionSource)

        LaunchedEffect(interactionSource) {
            interactionSource.interactions.collectLatest { interaction ->
                if (interaction is PressInteraction.Release) {
                    onEvent()
                }
            }
        }

        OutlinedTextField(
            value = value,
            onValueChange = {},
            modifier = modifier.fillMaxWidth(),
            interactionSource = interactionSource,
            readOnly = true,
            singleLine = true,
            placeholder = { Text(stringResource(placeholder)) },
            label = {
                Text(
                    text = stringResource(label),
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    softWrap = false
                )
            }
        )
    }

    @Composable
    fun LocalAnimatedField(
        isDefault: Boolean,
        label: @Composable () -> Unit,
        text: @Composable () -> Unit,
        textField: @Composable () -> Unit,
    ) {
        AnimatedContent(isDefault, label = "SummaryField") { isDefault ->
            if (isDefault) {
                Column {
                    label()
                    text()
                }
            } else {
                textField()
            }
        }
    }

    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxHeight()
            .verticalScroll(rememberScrollState())
    ) {
        LocalAnimatedField(
            isDefault = state.default || state.technical.verified,
            label = { LocalLabel(R.string.text_medicine_product_name) },
            text = { LocalText(state.productName) },
            textField = {
                val textFieldState = rememberTextFieldState(state.productName)
                val focusRequester = remember(::FocusRequester)
                val viewRequester = remember(::BringIntoViewRequester)

                LaunchedEffect(textFieldState) {
                    snapshotFlow { textFieldState.text.toString() }.collectLatest {
                        onEvent(SetProductName(it))
                    }
                }

                LaunchedEffect(state.productNameError) {
                    if (state.productNameError != null) {
                        focusRequester.requestFocus()
                    }
                }

                LaunchedEffect(focusRequester) {
                    viewRequester.bringIntoView()
                }

                OutlinedTextField(
                    state = textFieldState,
                    label = { Text(stringResource(R.string.text_medicine_product_name)) },
                    supportingText = state.productNameError?.let { { Text(stringResource(it)) } },
                    isError = state.productNameError != null,
                    lineLimits = TextFieldLineLimits.SingleLine,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .bringIntoViewRequester(viewRequester)
                        .focusRequester(focusRequester)
                )
            }
        )

        LocalAnimatedField(
            isDefault = state.default,
            label = { LocalLabel(R.string.text_medicine_group) },
            text = { LocalText(state.kits.joinToString(transform = Kit::title)) },
            textField = {
                LocalTextField(
                    value = state.kits.joinToString(transform = Kit::title),
                    label = R.string.text_medicine_group,
                    placeholder = R.string.text_empty,
                    onEvent = { onEvent(MedicineEvent.ToggleDialog(MedicineDialogState.Kits)) }
                )
            }
        )

        LocalAnimatedField(
            isDefault = state.default || !state.isOpened && state.technical.verified,
            label = { LocalLabel(R.string.text_exp_date) },
            text = { LocalText(state.expDateString) },
            textField = {
                LocalTextField(
                    value = state.expDateString,
                    label = R.string.text_exp_date,
                    placeholder = R.string.text_unspecified,
                    onEvent = { onEvent(MedicineEvent.ToggleDialog(MedicineDialogState.Date)) }
                )
            }
        )

        LocalAnimatedField(
            isDefault = state.default,
            label = { LocalLabel(R.string.text_package_opened_date) },
            text = { LocalText(state.dateOpenedString) },
            textField = {
                LocalTextField(
                    value = state.dateOpenedString,
                    label = R.string.text_package_opened_date,
                    placeholder = R.string.text_unspecified,
                    onEvent = { onEvent(MedicineEvent.ToggleDialog(MedicineDialogState.PackageDate)) }
                )
            }
        )

        if (state.default) {
            Column {
                LocalLabel(R.string.text_status)

                LocalText(
                    text = stringResource(
                        id = if (state.technical.verified) R.string.text_medicine_status_checked
                        else if (state.technical.scanned && !state.technical.verified) R.string.text_medicine_status_scanned
                        else R.string.text_medicine_status_self_added
                    ),
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = if (state.technical.verified) MaterialTheme.colorScheme.primary
                        else if (state.technical.scanned && !state.technical.verified) MaterialTheme.colorScheme.onBackground
                        else MaterialTheme.colorScheme.error
                    )
                )
            }
        }
    }
}
