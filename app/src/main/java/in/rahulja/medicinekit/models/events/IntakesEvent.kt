package `in`.rahulja.medicinekit.models.events

import androidx.compose.foundation.lazy.LazyListState
import `in`.rahulja.medicinekit.models.states.IntakesDialogState
import `in`.rahulja.medicinekit.utils.enums.IntakeTab

sealed interface IntakesEvent {
    data class SetSearch(val search: String) : IntakesEvent
    data class ToggleDialog(val state: IntakesDialogState? = null) : IntakesEvent
    data class ScrollToDate(val tab: IntakeTab, val listState: LazyListState, val time: Long) : IntakesEvent
}