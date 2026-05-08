package `in`.rahulja.medicinekit.utils.enums

import androidx.annotation.StringRes
import `in`.rahulja.medicinekit.R

enum class Period(val days: Int, @StringRes val title: Int) {
    PICK(-1, R.string.intake_period_pick),
    OTHER(21, R.string.intake_period_other),
    INDEFINITE(1825, R.string.intake_period_indef);

    companion object {
        fun getValue(days: Int) = entries.find { it.days == days } ?: OTHER
    }
}