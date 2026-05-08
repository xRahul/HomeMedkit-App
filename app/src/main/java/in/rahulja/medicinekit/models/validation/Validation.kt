package `in`.rahulja.medicinekit.models.validation

import `in`.rahulja.medicinekit.R.string.text_fill_field
import `in`.rahulja.medicinekit.data.model.IntakeAmountTime

object Validation {
    fun checkAmount(list: List<IntakeAmountTime>) = if (list.all { it.amount.toDoubleOrNull() != null }) ValidationResult(successful = true)
    else ValidationResult(
        successful = false,
        errorMessage = text_fill_field
    )

    fun checkTime(list: List<IntakeAmountTime>) = when {
        list.all { it.time.isNotEmpty() } -> ValidationResult(successful = true)
        else -> ValidationResult(
            successful = false,
            errorMessage = text_fill_field
        )
    }

    fun textNotEmpty(text: String) = if (text.isNotEmpty()) ValidationResult(successful = true)
    else ValidationResult(
        successful = false,
        errorMessage = text_fill_field
    )
}
