@file:OptIn(ExperimentalCoroutinesApi::class)

package `in`.rahulja.medicinekit.models.viewModels

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import `in`.rahulja.medicinekit.R
import `in`.rahulja.medicinekit.data.dao.KitDAO
import `in`.rahulja.medicinekit.data.dao.MedicineDAO
import `in`.rahulja.medicinekit.data.dto.Kit
import `in`.rahulja.medicinekit.data.model.KitModel
import `in`.rahulja.medicinekit.data.model.MedicineGrouped
import `in`.rahulja.medicinekit.data.model.MedicineList
import `in`.rahulja.medicinekit.data.queries.MedicinesQueryBuilder
import `in`.rahulja.medicinekit.models.states.MedicinesState
import `in`.rahulja.medicinekit.utils.BLANK
import `in`.rahulja.medicinekit.utils.Preferences
import `in`.rahulja.medicinekit.utils.ResourceText
import `in`.rahulja.medicinekit.utils.enums.MedicineListView
import `in`.rahulja.medicinekit.utils.enums.Sorting
import `in`.rahulja.medicinekit.utils.extensions.toMedicineList
import `in`.rahulja.medicinekit.utils.extensions.toModel
import `in`.rahulja.medicinekit.utils.extensions.toggle

class MedicinesViewModel(
    private val medicineDAO: MedicineDAO,
    private val kitDAO: KitDAO,
    private val preferences: Preferences
) : BaseViewModel<MedicinesState, Unit>() {
    private val currentMillis by lazy { System.currentTimeMillis() }

    val kits = kitDAO.getFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    private data class QueryParams(
        val search: String,
        val sorting: Sorting,
        val hideEmpty: Boolean,
        val kits: Set<Kit>
    )

    private val queryParams = state.map { QueryParams(it.search, it.sorting, it.hideEmpty, it.kits) }.distinctUntilChanged()
    private val _medicines = queryParams.flatMapLatest { query ->
        medicineDAO.getFlow(
            query = MedicinesQueryBuilder.selectBy(
                search = query.search,
                order = query.sorting,
                hideEmpty = query.hideEmpty,
                kits = query.kits
            )
        )
    }

    val medicines = _medicines
        .map { list -> list.map { main -> main.toMedicineList(currentMillis) } }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val grouped = combine(medicines, kits, state.map { it.kits }.distinctUntilChanged()) { medicinesList, kitsList, stateKits ->
        val selectedKits = stateKits.mapTo(mutableSetOf(), Kit::kitId)
        val kitsMap = kitsList.associateBy(Kit::kitId)

        val filterIsEmpty = selectedKits.isEmpty()
        val groups = mutableMapOf<Long, MutableList<MedicineList>>()
        val noGroup = mutableListOf<MedicineList>()

        for (medicine in medicinesList) {
            var anyGroup = false

            for (kitId in medicine.kitIds) {
                if (filterIsEmpty || kitId in selectedKits) {
                    if (kitsMap.containsKey(kitId)) {
                        groups.getOrPut(kitId) { mutableListOf() }.add(medicine)
                        anyGroup = true
                    }
                }
            }

            if (!anyGroup) {
                noGroup.add(medicine)
            }
        }

        val result = ArrayList<MedicineGrouped>(groups.size + 1)

        groups.forEach { (kitId, items) ->
            val kit = kitsMap[kitId]
            if (kit != null) {
                result.add(MedicineGrouped(kit.toModel(), items))
            }
        }

        if (noGroup.isNotEmpty()) {
            result.add(
                MedicineGrouped(
                    medicines = noGroup,
                    kit = KitModel(
                        id = if (kitsList.isEmpty()) -2L else -1L,
                        position = kitsList.size.toLong(),
                        title = ResourceText.StringResource(R.string.text_no_group)
                    )
                )
            )
        }

        result.sortedBy { it.kit.position }
    }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    override fun onEvent(event: Unit) = Unit

    override fun initState() = MedicinesState()

    override fun loadData() {
        val kitIds = preferences.kitsFilter
            .orEmpty()
            .mapNotNullTo(mutableSetOf(), String::toLongOrNull)

        if (kitIds.isNotEmpty()) {
            viewModelScope.launch {
                val kits = kitDAO.getKitList(kitIds).toSet()

                updateState { it.copy(kits = kits) }
            }
        }
    }

    fun pickView(view: MedicineListView) {
        if (currentState.listView != view) {
            updateState { it.copy(listView = view) }
            preferences.setMedicinesListView(view)        }
    }

    fun toggleAdding() = updateState { it.copy(showAdding = !it.showAdding) }
    fun showExit(flag: Boolean = false) = updateState { it.copy(showExit = flag) }

    fun onSearch(text: String = BLANK) = updateState { it.copy(search = text) }

    fun showSorting() = updateState { it.copy(showSorting = !it.showSorting) }
    fun setSorting(sorting: Sorting) = updateState { it.copy(sorting = sorting) }

    fun toggleFilter() {
        updateState { it.copy(showFilter = !it.showFilter) }

        if (!currentState.showFilter) {
            preferences.saveKitsFilter(currentState.kits.mapTo(mutableSetOf()) { it.kitId.toString() })
        }
    }

    fun clearFilter() {
        updateState {
            it.copy(
                showFilter = false,
                kits = emptySet()
            )
        }

        preferences.saveKitsFilter(emptySet())
    }

    fun pickFilter(kit: Kit) = updateState { it.copy(kits = it.kits.toggle(kit)) }

    fun hideEmpty(hide: Boolean) {
        updateState { it.copy(hideEmpty = hide) }
        preferences.setHideEmptyMedicines(hide)
    }
}