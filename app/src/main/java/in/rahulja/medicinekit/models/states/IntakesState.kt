package `in`.rahulja.medicinekit.models.states

import `in`.rahulja.medicinekit.utils.BLANK

data class IntakesState(
    val search: String = BLANK,
    val dialogState: IntakesDialogState? = null
)