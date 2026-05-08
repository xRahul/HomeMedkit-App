package `in`.rahulja.medicinekit.utils.enums

import androidx.annotation.StringRes
import `in`.rahulja.medicinekit.R

enum class MedicineListView(@StringRes val title: Int) {
    LIST(R.string.tab_list),
    GROUPS(R.string.tab_groups)
}