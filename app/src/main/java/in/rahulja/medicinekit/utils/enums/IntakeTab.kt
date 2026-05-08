package `in`.rahulja.medicinekit.utils.enums

import androidx.annotation.StringRes
import `in`.rahulja.medicinekit.R

enum class IntakeTab(@StringRes val title: Int) {
    LIST(R.string.tab_list),
    CURRENT(R.string.tab_current),
    PAST(R.string.tab_past)
}