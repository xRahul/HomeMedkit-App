package `in`.rahulja.medicinekit.data.model

import androidx.compose.runtime.Stable
import `in`.rahulja.medicinekit.utils.ResourceText

@Stable
data class MedicineList(
    val id: Long,
    val title: String,
    val prodAmountDoseType: ResourceText,
    val expDateS: String,
    val formName: String,
    val image: String,
    val inStock: Boolean,
    val isExpired: Boolean,
    val kitIds: Set<Long> = emptySet()
)
