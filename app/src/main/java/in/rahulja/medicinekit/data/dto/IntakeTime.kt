package `in`.rahulja.medicinekit.data.dto

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import `in`.rahulja.medicinekit.utils.BLANK

@Serializable
@Entity(
    tableName = "intake_time",
    foreignKeys = [
        ForeignKey(
            entity = Intake::class,
            parentColumns = ["intakeId"],
            childColumns = ["intakeId"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["intakeId"])
    ]
)
data class IntakeTime(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val intakeId: Long = 0L,
    val time: String = BLANK,
    val amount: Double = 0.0
)
