package `in`.rahulja.medicinekit.models.states

import `in`.rahulja.medicinekit.data.dto.Kit
import `in`.rahulja.medicinekit.utils.BLANK
import `in`.rahulja.medicinekit.utils.enums.MedicineListView
import `in`.rahulja.medicinekit.utils.enums.Sorting

data class MedicinesState(
    val search: String = BLANK,
    val sorting: Sorting = Sorting.IN_NAME,
    val hideEmpty: Boolean = false,
    val listView: MedicineListView = MedicineListView.LIST,
    val kits: Set<Kit> = emptySet(),
    val showSorting: Boolean = false,
    val showFilter: Boolean = false,
    val showAdding: Boolean = false,
    val showExit: Boolean = false
)
