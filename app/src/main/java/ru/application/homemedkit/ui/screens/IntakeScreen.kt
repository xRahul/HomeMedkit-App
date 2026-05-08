@file:OptIn(ExperimentalMaterial3Api::class)

package ru.application.homemedkit.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.application.homemedkit.R
import ru.application.homemedkit.dialogs.DatePicker
import ru.application.homemedkit.dialogs.DateRangePicker
import ru.application.homemedkit.dialogs.TimePickerDialog
import ru.application.homemedkit.models.events.IntakeEvent
import ru.application.homemedkit.models.viewModels.IntakeViewModel
import ru.application.homemedkit.ui.elements.DialogDelete
import ru.application.homemedkit.ui.elements.NavigationIcon
import ru.application.homemedkit.ui.elements.TopBarActions
import ru.application.homemedkit.ui.screens.intake.components.*
import ru.application.homemedkit.ui.screens.permissions.PermissionsScreen
import ru.application.homemedkit.utils.enums.SchemaType
import ru.application.homemedkit.utils.extensions.intake

@Composable
fun IntakeScreen(model: IntakeViewModel, onBack: () -> Unit) {
    val state by model.state.collectAsStateWithLifecycle()

    BackHandler(!state.isFirstLaunch) {
        if (state.default) onBack()
        else model.onEvent(IntakeEvent.ShowDialogDataLoss(true))
    }
    if (state.isFirstLaunch) PermissionsScreen(onBack, model::setExitFirstLaunch)
    else Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    NavigationIcon {
                        if (state.default) onBack()
                        else model.onEvent(IntakeEvent.ShowDialogDataLoss(true))
                    }
                },
                actions = {
                    TopBarActions(
                        isDefault = state.default,
                        setModifiable = model::setEditing,
                        onSave = if (state.adding) model::add else model::update,
                        onShowDialog = { model.onEvent(IntakeEvent.ShowDialogDelete) }
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onTertiaryContainer
                )
            )
        }
    ) { values ->
        Crossfade(state.isLoading, label = "IntakeLoading") { isLoading ->
            if (isLoading) {
                Box(Modifier.fillMaxSize())
            } else {
                LazyColumn(
                    verticalArrangement = spacedBy(24.dp),
                    contentPadding = values.intake()
                ) {
                    item { MedicineInfo(state.medicine, state.image) }

                    item { SchemaType(state, model::onEvent) }

                    if (state.schemaType == SchemaType.BY_DAYS) item {
                        DaysPicker(state, model::onEvent)
                    }

                    if (state.schemaType != SchemaType.BY_DAYS) item {
                        Interval(state, model::onEvent)
                    }

                    if (state.schemaType != SchemaType.INDEFINITELY) item {
                        Period(state, model::onEvent)
                    }

                    item { Amount(state, model::onEvent) }
                    item { Food(state, model::onEvent) }
                    item { Time(state, model::onEvent) }
                    item { Extra(state, model::onEvent) }
                }
            }
        }
    }

    IntakeDialogs(state, model, onBack)
}

@Composable
private fun IntakeDialogs(state: ru.application.homemedkit.models.states.IntakeState, model: IntakeViewModel, onBack: () -> Unit) {
    when {
        state.showDialogDescription -> DialogDescription(state, model::onEvent)
        state.showDialogDataLoss -> DialogDataLoss(model::onEvent, onBack)
        state.showDialogDelete -> DialogDelete(
            text = R.string.text_confirm_deletion_int,
            onCancel = { model.onEvent(IntakeEvent.ShowDialogDelete) },
            onConfirm = { model.delete(onBack) }
        )

        state.showDatePicker -> DatePicker(
            onDismiss = { model.onEvent(IntakeEvent.ShowDatePicker) },
            onSelect = { model.onEvent(IntakeEvent.SetStartDate(it)) }
        )

        state.showDateRangePicker -> DateRangePicker(
            startDate = state.startDate,
            finalDate = state.finalDate,
            onDismiss = { model.onEvent(IntakeEvent.ShowDatePicker) },
            onRangeSelected = { model.onEvent(IntakeEvent.SetPeriod(it)) }
        )

        state.showTimePicker -> TimePickerDialog(
            onCancel = { model.onEvent(IntakeEvent.ShowTimePicker()) },
            onConfirm = { model.onEvent(IntakeEvent.SetPickedTime) },
            content = { TimePicker(state.pickedTime[state.timePickerIndex].picker) }
        )
    }
}
