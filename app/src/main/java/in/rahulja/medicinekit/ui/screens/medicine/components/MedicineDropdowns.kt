@file:OptIn(ExperimentalMaterial3Api::class)

package `in`.rahulja.medicinekit.ui.screens.medicine.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import `in`.rahulja.medicinekit.utils.enums.DoseType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoseDropdownMenu(doseTitle: String, setDoseType: (DoseType) -> Unit) {
    var isExpanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = isExpanded,
        onExpandedChange = { isExpanded = !isExpanded },
        modifier = Modifier.width(64.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        ) {
            Text(doseTitle)
            ExposedDropdownMenuDefaults.TrailingIcon(isExpanded)
        }
        ExposedDropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false }
        ) {
            DoseType.entries.forEach { item ->
                DropdownMenuItem(
                    modifier = Modifier.wrapContentSize(),
                    text = {
                        Text(
                            text = stringResource(item.title),
                            softWrap = false
                        )
                    },
                    onClick = {
                        setDoseType(item)
                        isExpanded = false
                    }
                )
            }
        }
    }
}
