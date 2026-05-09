package `in`.rahulja.medicinekit.data.model

import `in`.rahulja.medicinekit.utils.ResourceText

data class IntakeUI(
    val intakeId: Long,
    val title: String,
    val interval: ResourceText,
    val days: ResourceText,
    val time: String,
    val image: String,
    val active: Boolean
)
