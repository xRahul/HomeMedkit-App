package `in`.rahulja.medicinekit.ui.screens.medicine.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collectLatest
import `in`.rahulja.medicinekit.R

@Composable
fun InfoTextField(
    title: String,
    isEditing: Boolean,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = stringResource(R.string.text_empty),
    emptyText: String = stringResource(R.string.text_empty),
    lineLimits: TextFieldLineLimits = TextFieldLineLimits.Default,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    inputTransformation: InputTransformation? = null,
    outputTransformation: OutputTransformation? = null,
    suffix: @Composable (() -> Unit)? = null
) = Column(modifier.animateContentSize(), Arrangement.spacedBy(8.dp)) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
    )

    AnimatedContent(isEditing) { editing ->
        if (editing) {
            val textFieldState = rememberTextFieldState(value)

            LaunchedEffect(textFieldState) {
                snapshotFlow { textFieldState.text.toString() }.collectLatest(onValueChange)
            }

            OutlinedTextField(
                state = textFieldState,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(placeholder) },
                inputTransformation = inputTransformation,
                outputTransformation = outputTransformation,
                lineLimits = lineLimits,
                keyboardOptions = keyboardOptions,
                suffix = suffix
            )
        } else {
            Text(value.ifEmpty { emptyText })
        }
    }
}
