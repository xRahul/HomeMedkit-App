package ru.application.homemedkit.ui.screens.intake.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ru.application.homemedkit.R
import ru.application.homemedkit.models.events.IntakeEvent
import ru.application.homemedkit.models.states.IntakeState

@Composable
fun DialogDescription(state: IntakeState, event: (IntakeEvent) -> Unit) = AlertDialog(
    onDismissRequest = { event(IntakeEvent.ShowDialogDescription()) },
    title = { Text(stringResource(R.string.text_medicine_description)) },
    confirmButton = {},
    text = state.extraDesc?.let {
        {
            Text(
                text = stringResource(it),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    },
    dismissButton = {
        TextButton(
            onClick = { event(IntakeEvent.ShowDialogDescription()) },
            content = { Text(stringResource(R.string.text_dismiss)) }
        )
    }
)

@Composable
fun DialogDataLoss(event: (IntakeEvent) -> Unit, navigateUp: () -> Unit) = AlertDialog(
    onDismissRequest = { event(IntakeEvent.ShowDialogDataLoss(false)) },
    confirmButton = { TextButton(navigateUp) { Text(stringResource(R.string.text_exit)) } },
    text = {
        Text(
            text = stringResource(R.string.text_not_saved_intake),
            style = MaterialTheme.typography.bodyLarge
        )
    },
    dismissButton = {
        TextButton(
            onClick = { event(IntakeEvent.ShowDialogDataLoss(false)) },
            content = { Text(stringResource(R.string.text_stay)) }
        )
    }
)
