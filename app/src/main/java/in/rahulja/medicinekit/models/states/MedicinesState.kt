package `in`.rahulja.medicinekit.models.states

import `in`.rahulja.medicinekit.data.dto.Kit
import `in`.rahulja.medicinekit.utils.BLANK
import `in`.rahulja.medicinekit.utils.di.Preferences
import `in`.rahulja.medicinekit.utils.enums.MedicineListView
import `in`.rahulja.medicinekit.utils.enums.Sorting

data class MedicinesState(
    val search: String = BLANK,
    val sorting: Sorting = Preferences.sortingOrder,
    val hideEmpty: Boolean = Preferences.hideEmptyMedicines,
    val listView: MedicineListView = Preferences.medicinesListView,
    val kits: Set<Kit> = emptySet(),
    val showSorting: Boolean = false,
    val showFilter: Boolean = false,
    val showAdding: Boolean = false,
    val showExit: Boolean = false
)