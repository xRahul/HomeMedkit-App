package `in`.rahulja.medicinekit.models.states

import `in`.rahulja.medicinekit.data.model.MedicineMain
import `in`.rahulja.medicinekit.utils.BLANK
import `in`.rahulja.medicinekit.utils.ResourceText

data class NewTakenState(
    val title: String = BLANK,
    val amount: String = BLANK,
    val doseType: ResourceText = ResourceText.StaticString(BLANK),
    val inStock: String = BLANK,
    val date: String = BLANK,
    val time: String = BLANK,
    val medicine: MedicineMain? = null
)
