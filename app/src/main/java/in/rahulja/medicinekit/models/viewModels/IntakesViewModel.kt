@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalCoroutinesApi::class)

package `in`.rahulja.medicinekit.models.viewModels

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePickerState
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import `in`.rahulja.medicinekit.R
import `in`.rahulja.medicinekit.data.dao.AlarmDAO
import `in`.rahulja.medicinekit.data.dao.IntakeDAO
import `in`.rahulja.medicinekit.data.dao.MedicineDAO
import `in`.rahulja.medicinekit.data.dao.TakenDAO
import `in`.rahulja.medicinekit.data.dto.IntakeTaken
import `in`.rahulja.medicinekit.data.model.IntakeList
import `in`.rahulja.medicinekit.data.model.IntakeModel
import `in`.rahulja.medicinekit.data.queries.MedicinesQueryBuilder
import `in`.rahulja.medicinekit.models.events.IntakesEvent
import `in`.rahulja.medicinekit.models.events.NewTakenEvent
import `in`.rahulja.medicinekit.models.events.TakenEvent
import `in`.rahulja.medicinekit.models.states.IntakesDialogState
import `in`.rahulja.medicinekit.models.states.IntakesState
import `in`.rahulja.medicinekit.models.states.NewTakenState
import `in`.rahulja.medicinekit.models.states.ScheduledState
import `in`.rahulja.medicinekit.models.states.TakenState
import `in`.rahulja.medicinekit.utils.BLANK
import `in`.rahulja.medicinekit.utils.Formatter
import `in`.rahulja.medicinekit.utils.ResourceText
import `in`.rahulja.medicinekit.receivers.AlarmSetter
import `in`.rahulja.medicinekit.utils.enums.IntakeTab
import `in`.rahulja.medicinekit.utils.extensions.orDefault
import `in`.rahulja.medicinekit.utils.extensions.toIntake
import `in`.rahulja.medicinekit.utils.extensions.toIntakePast
import `in`.rahulja.medicinekit.utils.extensions.toIntakeSchedule
import `in`.rahulja.medicinekit.utils.extensions.toTakenState
import java.time.LocalDate
import kotlin.math.abs

class IntakesViewModel(
    private val intakeDAO: IntakeDAO,
    private val medicineDAO: MedicineDAO,
    private val takenDAO: TakenDAO,
    private val alarmDAO: AlarmDAO,
    private val alarmManager: AlarmSetter
) : BaseViewModel<IntakesState, IntakesEvent>() {

    private val currentYear by lazy { LocalDate.now().year }

    val scheduledManager by lazy(::ScheduledManager)
    val takenManager by lazy(::TakenManager)
    val newTakenManager by lazy(::NewTakenManager)

    val medicines = medicineDAO.getFlow(MedicinesQueryBuilder.selectAll)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val intakes = state.map { it.search }.distinctUntilChanged().flatMapLatest { search ->
        intakeDAO.getFlow(search)
            .map { list -> list.map(IntakeList::toIntake) }
            .flowOn(Dispatchers.Default)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val schedule = state.map { it.search }.distinctUntilChanged().flatMapLatest { search ->
        alarmDAO.getFlow(search)
            .map { list ->
                list.groupBy { Formatter.getDateTime(it.trigger).toLocalDate().toEpochDay() }
                    .entries
                    .sortedBy { it.key }
                    .map { it.toIntakeSchedule(currentYear) }
            }
            .flowOn(Dispatchers.Default)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val taken = state.map { it.search }.distinctUntilChanged().flatMapLatest { search ->
        takenDAO.getFlow(search)
            .map { list ->
                list.groupBy { Formatter.getDateTime(it.trigger).toLocalDate().toEpochDay() }
                    .entries
                    .sortedByDescending { it.key }
                    .map { it.toIntakePast(currentYear) }
            }
            .flowOn(Dispatchers.Default)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    override fun initState() = IntakesState()

    override fun loadData() = Unit

    override fun onEvent(event: IntakesEvent) {
        when (event) {
            is IntakesEvent.SetSearch -> updateState { it.copy(search = event.search) }

            is IntakesEvent.ScrollToDate -> scrollToClosest(event.tab, event.listState, event.time)

            is IntakesEvent.ToggleDialog -> viewModelScope.launch {
                when (val data = event.state) {
                    IntakesDialogState.TakenAdd -> newTakenManager.tryEmit(NewTakenState())

                    is IntakesDialogState.TakenDelete -> takenManager.updateState { it.copy(takenId = data.takenId) }

                    is IntakesDialogState.TakenInfo -> takenManager.getTaken(data.takenId)

                    is IntakesDialogState.ScheduleToTaken -> scheduledManager.getScheduled(data.item)

                    else -> Unit
                }

                updateState { it.copy(dialogState = event.state) }
            }
        }
    }

    private fun closeDialog() = updateState { it.copy(dialogState = null) }

    private fun scrollToClosest(tab: IntakeTab, listState: LazyListState, time: Long) {
        val list = if (tab == IntakeTab.CURRENT) schedule.value else taken.value

        if (list.isEmpty()) {
            closeDialog()
            return
        }

        val day = Formatter.getDateTime(time).toLocalDate().toEpochDay()
        val value = list.minByOrNull { abs(day - it.epochDay) } ?: list.first()
        val itemsIndex = list.indexOf(value)

        val itemsBefore = list.take(itemsIndex).sumOf { it.intakes.size }

        listState.requestScrollToItem(itemsBefore + itemsIndex)

        closeDialog()
    }

    abstract inner class Manager<State, Event> {
        private val _state = MutableStateFlow(initState())
        val state = _state.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), initState())

        internal val currentState: State
            get() = _state.value

        internal fun updateState(update: (State) -> State) = _state.update(update)
        internal fun tryEmit(state: State) = _state.tryEmit(state)

        abstract fun initState(): State
        abstract fun onEvent(event: Event)
    }

    inner class ScheduledManager : Manager<ScheduledState, Unit>() {
        override fun initState() = ScheduledState()
        override fun onEvent(event: Unit) = Unit

        internal suspend fun getScheduled(item: IntakeModel) {
            val alarm = alarmDAO.getById(item.alarmId) ?: return
            val date = Formatter.getDateTime(alarm.trigger).toLocalDate().format(Formatter.FORMAT_DD_MM)
            val inStock = intakeDAO.getById(alarm.intakeId)?.let {
                it.medicine.prodAmount >= alarm.amount
            } ?: false

            updateState {
                it.copy(
                    id = item.id,
                    alarmId = item.alarmId,
                    title = item.title,
                    date = date,
                    time = item.time,
                    taken = inStock
                )
            }
        }

        fun scheduleToTaken() {
            viewModelScope.launch {
                val alarm = alarmDAO.getById(currentState.alarmId) ?: return@launch
                val intake = intakeDAO.getById(alarm.intakeId) ?: return@launch
                val medicine = medicineDAO.getById(intake.medicineId) ?: return@launch
                val image = medicineDAO.getMedicineImage(medicine.id).orEmpty()

                alarmDAO.delete(alarm)
                takenDAO.insert(
                    IntakeTaken(
                        medicineId = medicine.id,
                        intakeId = alarm.intakeId,
                        alarmId = alarm.alarmId,
                        productName = medicine.nameAlias.ifEmpty(medicine::productName),
                        formName = medicine.prodFormNormName,
                        amount = alarm.amount,
                        doseType = medicine.doseType,
                        image = image,
                        trigger = alarm.trigger,
                        inFact = alarm.trigger,
                        taken = true,
                        notified = true
                    )
                )

                medicineDAO.intakeMedicine(medicine.id, alarm.amount)

                alarmManager.setPreAlarm(intake.intakeId)
            }

            closeDialog()
        }
    }

    inner class TakenManager : Manager<TakenState, TakenEvent>() {
        override fun initState() = TakenState()

        override fun onEvent(event: TakenEvent) = when (event) {
            is TakenEvent.Save -> saveTaken(event.manager)

            TakenEvent.Delete -> {
                viewModelScope.launch {
                    takenDAO.delete(IntakeTaken(currentState.takenId))
                }

                closeDialog()
            }

            is TakenEvent.SetSelection -> {
                if (event.index == 0) {
                    updateState {
                        it.copy(
                            selection = 0,
                            inFact = 0L,
                            actual = ResourceText.StringResource(R.string.intake_text_not_taken)
                        )
                    }
                } else {
                    val current = System.currentTimeMillis()
                    val dateTime = Formatter.getDateTime(current)

                    updateState {
                        it.copy(
                            selection = 1,
                            inFact = current,
                            actual = ResourceText.StaticString(dateTime.format(Formatter.FORMAT_H_MM)),
                            pickerState = TimePickerState(dateTime.hour, dateTime.minute, true)
                        )
                    }
                }
            }

            TakenEvent.SetFactTime -> {
                val trigger = Formatter.toTimestamp(
                    hour = currentState.pickerState.hour,
                    minute = currentState.pickerState.minute
                )

                updateState {
                    it.copy(
                        showPicker = false,
                        inFact = trigger,
                        actual = ResourceText.StaticString(Formatter.timeFormat(trigger))
                    )
                }
            }

            is TakenEvent.ShowTimePicker -> updateState { it.copy(showPicker = event.flag) }
        }

        internal suspend fun getTaken(takenId: Long) {
            val taken = takenDAO.getById(takenId)
            val state = withContext(Dispatchers.Main) {
                taken?.toTakenState().orDefault()
            }

            updateState { state }
        }

        private fun saveTaken(manager: NotificationManagerCompat) {
            val takenId = currentState.takenId
            val takenNow = currentState.selection == 1
            val takenOld = currentState.taken

            with(manager) {
                cancel(takenId.toInt())
                cancel(currentState.alarmId.toInt())
            }

            viewModelScope.launch {
                takenDAO.setTaken(takenId, takenNow, if (takenNow) currentState.inFact else 0L)
                takenDAO.setNotified(takenId)

                currentState.medicine?.let { medicine ->
                    with(medicineDAO) {
                        getById(medicine.id)?.let {
                            when {
                                takenNow && !takenOld -> intakeMedicine(it.id, currentState.amount)
                                !takenNow && takenOld -> untakeMedicine(it.id, currentState.amount)
                            }
                        }
                    }
                }
            }

            closeDialog()
        }
    }

    inner class NewTakenManager : Manager<NewTakenState, NewTakenEvent>() {
        override fun initState() = NewTakenState()

        override fun onEvent(event: NewTakenEvent) {
            when (event) {
                NewTakenEvent.AddNewTaken -> {
                    val trigger = Formatter.toTimestamp(currentState.date, currentState.time)

                    val medicine = currentState.medicine ?: return

                    val amount = currentState.amount.let {
                        it.toDoubleOrNull() ?: it.replace(',', '.').toDoubleOrNull() ?: 1.0
                    }

                    val taken = IntakeTaken(
                        medicineId = medicine.id,
                        productName = currentState.title,
                        formName = medicine.prodFormNormName,
                        amount = amount,
                        doseType = medicine.doseType,
                        image = medicine.image.orEmpty(),
                        trigger = trigger,
                        inFact = trigger,
                        taken = true,
                        notified = true
                    )

                    viewModelScope.launch {
                        takenDAO.insert(taken)
                        medicineDAO.intakeMedicine(taken.medicineId, taken.amount)
                    }

                    closeDialog()
                }

                is NewTakenEvent.PickMedicine -> {
                    updateState {
                        it.copy(
                            title = event.medicine.nameAlias.ifEmpty(event.medicine::productName),
                            amount = BLANK,
                            doseType = ResourceText.StringResource(event.medicine.doseType.title),
                            inStock = Formatter.decimalFormat(event.medicine.prodAmount),
                            medicine = event.medicine
                        )
                    }

                    viewModelScope.launch {
                        takenDAO.getSimilarAmount(event.medicine.id)?.let { amount ->
                            updateState {
                                it.copy(amount = Formatter.decimalFormat(amount))
                            }
                        }
                    }
                }

                is NewTakenEvent.SetAmount -> updateState { it.copy(amount = event.amount) }

                is NewTakenEvent.SetDate -> event.pickerState.selectedDateMillis?.let { millis ->
                    updateState {
                        it.copy(date = Formatter.dateFormat(millis, Formatter.FORMAT_DD_MM_YYYY))
                    }
                }

                is NewTakenEvent.SetTime -> event.pickerState.run {
                    updateState {
                        it.copy(time = Formatter.timeFormat(hour, minute))
                    }
                }
            }
        }
    }
}