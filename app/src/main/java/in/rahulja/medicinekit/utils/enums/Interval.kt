package `in`.rahulja.medicinekit.utils.enums

import androidx.annotation.StringRes
import `in`.rahulja.medicinekit.R

enum class Interval(val days: Int, @StringRes val title: Int) {
    DAILY(1, R.string.intake_interval_daily),
    WEEKLY(7, R.string.intake_interval_weekly),
    CUSTOM(10, R.string.intake_interval_other);

    companion object {
        fun getValue(days: Int) = entries.find { it.days == days } ?: CUSTOM
    }
}