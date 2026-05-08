package `in`.rahulja.medicinekit.data.model

import `in`.rahulja.medicinekit.utils.enums.DoseType

data class MedicineMain(
    val id: Long,
    val productName: String,
    val nameAlias: String,
    val prodAmount: Double,
    val doseType: DoseType,
    val expDate: Long,
    val prodFormNormName: String,
    val image: String?,
    val kitIdsString: String?
) {
    val kitIds: Set<Long> by lazy {
        kitIdsString?.split(",")?.mapNotNullTo(hashSetOf()) { it.toLongOrNull() } ?: emptySet()
    }
}