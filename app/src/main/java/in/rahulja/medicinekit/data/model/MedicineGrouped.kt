package `in`.rahulja.medicinekit.data.model

data class MedicineGrouped(
    val kit: KitModel,
    val medicines: List<MedicineList>
)
