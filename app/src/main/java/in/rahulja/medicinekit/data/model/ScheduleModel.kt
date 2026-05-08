package `in`.rahulja.medicinekit.data.model

import `in`.rahulja.medicinekit.utils.ResourceText

data class ScheduleModel(
    override val id: Long,
    override val alarmId: Long,
    override val title: String,
    override val doseAmount: ResourceText.StringResource,
    override val image: String,
    override val time: String,
    override val taken: Boolean = true
) : IntakeModel
