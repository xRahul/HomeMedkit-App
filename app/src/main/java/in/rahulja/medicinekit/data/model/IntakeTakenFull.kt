package `in`.rahulja.medicinekit.data.model

import androidx.room.Relation
import `in`.rahulja.medicinekit.data.dto.Medicine

data class IntakeTakenFull(
    val takenId: Long,
    val medicineId: Long, // needed for relation
    val intakeId: Long,
    val alarmId: Long,
    val productName: String,
    val amount: Double,
    val trigger: Long,
    val inFact: Long,
    val taken: Boolean,
    val notified: Boolean,

    @Relation(
        parentColumn = "medicineId",
        entityColumn = "id"
    )
    val medicine: Medicine?
)
