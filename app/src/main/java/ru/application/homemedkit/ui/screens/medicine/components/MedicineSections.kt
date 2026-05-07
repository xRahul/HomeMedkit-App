package ru.application.homemedkit.ui.screens.medicine.components

import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ru.application.homemedkit.R
import ru.application.homemedkit.data.dto.Kit
import ru.application.homemedkit.models.events.MedicineEvent
import ru.application.homemedkit.models.states.MedicineDialogState
import ru.application.homemedkit.models.states.MedicineState
import ru.application.homemedkit.ui.elements.IconButton
import ru.application.homemedkit.ui.elements.VectorIcon
import ru.application.homemedkit.utils.DecimalAmountInputTransformation
import ru.application.homemedkit.utils.DecimalAmountOutputTransformation
import ru.application.homemedkit.utils.Formatter

@Composable
fun MedicineHeaderSection(state: MedicineState, event: (MedicineEvent) -> Unit) {
    Row(Modifier.height(256.dp), Arrangement.spacedBy(12.dp)) {
        ProductImage(
            isDefault = state.default,
            images = state.images,
            onShow = { event(MedicineEvent.ToggleDialog(MedicineDialogState.FullImage(it))) },
            onDismiss = { event(MedicineEvent.ToggleDialog(MedicineDialogState.PictureGrid)) }
        )

        Summary(state, event)
    }
}

@Composable
fun MedicineGeneralInfoSection(state: MedicineState, event: (MedicineEvent) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(32.dp)) {
        InfoTextField(
            isEditing = state.adding || state.editing,
            title = stringResource(R.string.text_medicine_display_name),
            value = state.nameAlias,
            onValueChange = { event(MedicineEvent.SetNameAlias(it)) },
            emptyText = state.productName,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Next
            )
        )

        InfoTextField(
            isEditing = !state.default && !state.technical.verified,
            title = stringResource(R.string.text_medicine_form),
            value = state.prodFormNormName,
            onValueChange = { event(MedicineEvent.SetFormName(it)) },
            modifier = Modifier.fillMaxWidth(),
            emptyText = stringResource(R.string.text_empty),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Default
            )
        )

        Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(16.dp)) {
            InfoTextField(
                isEditing = state.adding || state.editing && !state.technical.verified,
                value = state.prodDNormName,
                onValueChange = { event(MedicineEvent.SetDoseName(it)) },
                modifier = Modifier.weight(0.5f),
                title = stringResource(R.string.text_medicine_dose),
                placeholder = stringResource(R.string.placeholder_dose),
                lineLimits = TextFieldLineLimits.SingleLine,
                keyboardOptions = KeyboardOptions(KeyboardCapitalization.Sentences)
            )

            InfoTextField(
                isEditing = !state.default,
                title = stringResource(R.string.text_amount),
                value = if (!state.default) state.prodAmount
                else "${Formatter.decimalFormat(state.prodAmount)} ${stringResource(state.doseType.title)}",
                onValueChange = { event(MedicineEvent.SetAmount(it)) },
                modifier = Modifier.weight(0.5f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                emptyText = stringResource(R.string.text_empty),
                lineLimits = TextFieldLineLimits.SingleLine,
                inputTransformation = DecimalAmountInputTransformation,
                outputTransformation = DecimalAmountOutputTransformation,
                suffix = {
                    DoseDropdownMenu(
                        doseTitle = stringResource(state.doseType.title),
                        setDoseType = { event(MedicineEvent.SetDoseType(it)) }
                    )
                }
            )
        }
    }
}

@Composable
fun MedicineAdditionalInfoSection(state: MedicineState, event: (MedicineEvent) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(32.dp)) {
        if (state.adding || state.editing || state.salts.isNotEmpty()) {
            InfoTextField(
                isEditing = !state.default,
                title = stringResource(R.string.text_medicine_salts),
                value = state.salts,
                onValueChange = { event(MedicineEvent.SetSalts(it)) },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Default
                )
            )
        }
        if (state.default && state.structure.isNotEmpty()) {
            InfoTextField(
                isEditing = false,
                title = stringResource(R.string.text_medicine_composition),
                value = state.structure,
                onValueChange = {}
            )
        }
        if (state.adding || state.editing || state.phKinetics.isNotEmpty()) {
            InfoTextField(
                isEditing = !state.default,
                title = stringResource(R.string.text_indications_for_use),
                value = state.phKinetics,
                onValueChange = { event(MedicineEvent.SetPhKinetics(it)) },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    keyboardType = KeyboardType.Text
                )
            )
        }
        if (state.default && state.recommendations.isNotEmpty()) {
            InfoTextField(
                isEditing = false,
                title = stringResource(R.string.text_medicine_recommendations),
                value = state.recommendations,
                onValueChange = {}
            )
        }
        if (state.default && state.storageConditions.isNotEmpty()) {
            InfoTextField(
                isEditing = false,
                title = stringResource(R.string.text_medicine_storage_conditions),
                value = state.storageConditions,
                onValueChange = {}
            )
        }
        if (state.adding || state.editing || state.comment.isNotEmpty()) {
            InfoTextField(
                isEditing = !state.default,
                title = stringResource(R.string.text_medicine_comment),
                value = state.comment,
                onValueChange = { event(MedicineEvent.SetComment(it)) },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Default
                )
            )
        }
    }
}

@Composable
fun MedicineKitsSection(state: MedicineState, event: (MedicineEvent) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = stringResource(R.string.preference_kits_group),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
        )

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (state.adding || state.editing) {
                IconButton(
                    onClick = { event(MedicineEvent.ToggleDialog(MedicineDialogState.Kits)) },
                    modifier = Modifier
                        .size(48.dp)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = CircleShape
                        ),
                    content = { VectorIcon(R.drawable.vector_add) }
                )
            }

            state.kits.forEach { kit ->
                Surface(
                    onClick = { if (state.adding || state.editing) event(MedicineEvent.PickKit(kit)) },
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ) {
                    Text(
                        text = kit.title,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}
