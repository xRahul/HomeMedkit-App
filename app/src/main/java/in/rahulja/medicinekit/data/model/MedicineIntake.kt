package `in`.rahulja.medicinekit.data.model

import `in`.rahulja.medicinekit.utils.BLANK
import `in`.rahulja.medicinekit.utils.enums.DoseType

data class MedicineIntake(
    val productName: String = BLANK,
    val nameAlias: String = BLANK,
    val prodFormNormName: String = BLANK,
    val expDate: Long = -1L,
    val prodAmount: Double = 0.0,
    val doseType: DoseType = DoseType.MILLIGRAMS
)
