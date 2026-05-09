package `in`.rahulja.medicinekit.network.models.auth

import `in`.rahulja.medicinekit.data.dto.*
import kotlinx.serialization.Serializable

@Serializable
data class FullBackupData(
    val medicines: List<Medicine> = emptyList(),
    val intakes: List<Intake> = emptyList(),
    val kits: List<Kit> = emptyList(),
    val medicineKits: List<MedicineKit> = emptyList(),
    val intakeTimes: List<IntakeTime> = emptyList(),
    val images: List<Image> = emptyList(),
    val alarms: List<Alarm> = emptyList(),
    val intakeDays: List<IntakeDay> = emptyList(),
    val taken: List<IntakeTaken> = emptyList()
)
