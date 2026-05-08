package `in`.rahulja.medicinekit.data.model

import `in`.rahulja.medicinekit.utils.enums.DoseType

data class Schedule(
    val alarmId: Long,
    val productName: String,
    val nameAlias: String,
    val prodFormNormName: String,
    val doseType: DoseType,
    val amount: Double,
    val image: String,
    val trigger: Long
)
