package ru.application.homemedkit.ui.screens.intake.components

import androidx.annotation.StringRes
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.application.homemedkit.R
import ru.application.homemedkit.data.model.MedicineIntake
import ru.application.homemedkit.ui.elements.MedicineImage
import ru.application.homemedkit.utils.Formatter

@Composable
fun MedicineInfo(medicine: MedicineIntake, image: String) {

    @Composable
    fun LocalInfo(@StringRes label: Int, text: String) = Column {
        Text(
            text = stringResource(label),
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.W400)
        )

        Text(
            text = text.ifEmpty { stringResource(R.string.text_unspecified) },
            style = MaterialTheme.typography.titleMedium,
            softWrap = false,
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
        )
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(156.dp)
    ) {
        MedicineImage(
            image = image,
            modifier = Modifier
                .fillMaxHeight()
                .width(128.dp)
                .border(1.dp, MaterialTheme.colorScheme.onSurface, MaterialTheme.shapes.medium)
                .padding(8.dp)
        )
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
        ) {
            LocalInfo(R.string.text_medicine_product_name, medicine.nameAlias.ifEmpty(medicine::productName))
            LocalInfo(R.string.text_medicine_form, Formatter.formFormat(medicine.prodFormNormName))
            LocalInfo(R.string.text_exp_date, Formatter.toExpDate(medicine.expDate))
        }
    }
}
