package `in`.rahulja.medicinekit.utils.extensions

import `in`.rahulja.medicinekit.utils.enums.Period
import kotlin.enums.EnumEntries

val EnumEntries<Period>.defined: List<Period>
    get() = dropLast(1)