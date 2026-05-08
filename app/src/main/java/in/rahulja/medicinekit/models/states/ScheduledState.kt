package `in`.rahulja.medicinekit.models.states

import `in`.rahulja.medicinekit.R
import `in`.rahulja.medicinekit.data.model.IntakeModel
import `in`.rahulja.medicinekit.utils.BLANK
import `in`.rahulja.medicinekit.utils.ResourceText

data class ScheduledState(
    override val id: Long = 0L,
    override val alarmId: Long = 0L,
    override val title: String = BLANK,
    override val doseAmount: ResourceText.StringResource = ResourceText.StringResource(R.string.blank),
    override val image: String = BLANK,
    override val time: String = BLANK,
    val date: String = BLANK,
    override val taken: Boolean = false
) : IntakeModel
