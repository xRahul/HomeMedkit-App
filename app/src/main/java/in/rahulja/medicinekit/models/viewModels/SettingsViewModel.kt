package `in`.rahulja.medicinekit.models.viewModels

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import `in`.rahulja.medicinekit.data.dao.KitDAO
import `in`.rahulja.medicinekit.data.dto.Kit
import `in`.rahulja.medicinekit.models.events.SettingsEvent
import `in`.rahulja.medicinekit.models.states.SettingsState
import `in`.rahulja.medicinekit.utils.ActionResult
import `in`.rahulja.medicinekit.utils.Preferences
import `in`.rahulja.medicinekit.utils.enums.Page
import `in`.rahulja.medicinekit.utils.enums.Sorting
import `in`.rahulja.medicinekit.utils.enums.Theme

class SettingsViewModel(
    private val preferences: Preferences,
    private val kitDAO: KitDAO
) : BaseViewModel<SettingsState, SettingsEvent>() {
    override fun initState() = SettingsState()

    override fun loadData() = Unit

    override fun onEvent(event: SettingsEvent) = when (event) {
        SettingsEvent.ShowClearing -> updateState { it.copy(showClearing = !it.showClearing) }
        SettingsEvent.ShowExport -> updateState { it.copy(showExport = !it.showExport) }
        SettingsEvent.ShowFixing -> updateState { it.copy(showFixing = !it.showFixing) }
        SettingsEvent.ShowKits -> updateState { it.copy(showKits = !it.showKits) }
        SettingsEvent.ShowPermissions -> updateState { it.copy(showPermissions = !it.showPermissions) }
    }

    val startPage = preferences.startPageFlow.stateIn(viewModelScope, SharingStarted.Eagerly, Page.MEDICINES)

    val sortingType = preferences.sortingOrderFlow.stateIn(viewModelScope, SharingStarted.Eagerly, Sorting.IN_NAME)

    val checkExpiration = preferences.checkExpirationFlow.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val theme = preferences.theme.stateIn(viewModelScope, SharingStarted.Eagerly, Theme.SYSTEM)

    val useAi = preferences.useAiFlow.stateIn(viewModelScope, SharingStarted.Eagerly, true)
    val aiMode = preferences.aiModeFlow.stateIn(viewModelScope, SharingStarted.Eagerly, `in`.rahulja.medicinekit.utils.enums.AiMode.ML_KIT)
    val geminiApiKey: String get() = preferences.geminiApiKey

    val kits = kitDAO.getFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    fun upsertKit(kit: Kit) {
        viewModelScope.launch {
            kitDAO.upsert(kit)
        }
    }

    fun deleteKit(kit: Kit) {
        viewModelScope.launch {
            kitDAO.delete(kit)
        }
    }

    fun saveKitsPosition(kits: List<Kit>) {
        viewModelScope.launch {
            val newList = kits.mapIndexed { index, kit ->
                Kit(
                    kitId = kit.kitId,
                    title = kit.title,
                    position = index.toLong()
                )
            }

            kitDAO.updatePositions(newList)
        }
    }

    fun onDataAction(actionResult: ActionResult) {
        viewModelScope.launch {
            val isSuccess = actionResult.onAction()
            actionResult.onResult(isSuccess)
        }
    }
}