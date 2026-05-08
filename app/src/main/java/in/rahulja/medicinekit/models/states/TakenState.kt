@file:OptIn(ExperimentalMaterial3Api::class)

package `in`.rahulja.medicinekit.models.states

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePickerState
import `in`.rahulja.medicinekit.R
import `in`.rahulja.medicinekit.data.dto.Medicine
import `in`.rahulja.medicinekit.utils.BLANK
import `in`.rahulja.medicinekit.utils.ResourceText

data class TakenState(
    val takenId: Long = 0L,
    val alarmId: Long = 0L,
    val medicine: Medicine? = null,
    val productName: String = BLANK,
    val amount: Double = 0.0,
    val date: String = BLANK,
    val scheduled: String = BLANK,
    val actual: ResourceText = ResourceText.StringResource(R.string.intake_text_not_taken),
    val inFact: Long = 0L,
    val pickerState: TimePickerState = TimePickerState(12, 0, true),
    val selection: Int = 0,
    val taken: Boolean = false,
    val notified: Boolean = false,
    val showPicker: Boolean = false
)