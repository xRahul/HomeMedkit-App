package `in`.rahulja.medicinekit.data.model

import `in`.rahulja.medicinekit.utils.ResourceText

interface IntakeModel {
    val id: Long
    val alarmId: Long
    val title: String
    val doseAmount: ResourceText.StringResource
    val image: String
    val time: String
    val taken: Boolean
}