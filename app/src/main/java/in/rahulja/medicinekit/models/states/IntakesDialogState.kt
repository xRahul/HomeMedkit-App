package `in`.rahulja.medicinekit.models.states

import `in`.rahulja.medicinekit.data.model.IntakeModel

sealed interface IntakesDialogState {
    data object TakenAdd : IntakesDialogState
    data class TakenDelete(val takenId: Long = 0L) : IntakesDialogState
    data class TakenInfo(val takenId: Long) : IntakesDialogState
    data class ScheduleToTaken(val item: IntakeModel) : IntakesDialogState
    data object DatePicker : IntakesDialogState
}