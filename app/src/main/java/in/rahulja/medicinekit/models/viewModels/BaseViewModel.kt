package `in`.rahulja.medicinekit.models.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

abstract class BaseViewModel<State, Event>(initialState: State) : ViewModel() {
    protected val _state = MutableStateFlow(initialState)
    val state = _state.asStateFlow()

    init {
        @Suppress("LeakingThis")
        loadData()
    }

    protected val currentState: State
        get() = _state.value

    protected fun updateState(update: (State) -> State) = _state.update(update)

    internal abstract fun loadData()
    abstract fun onEvent(event: Event)
}