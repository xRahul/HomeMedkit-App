package `in`.rahulja.medicinekit.data.dto

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "medicines_kits",
    primaryKeys = ["medicineId", "kitId"],
    foreignKeys = [
        ForeignKey(
            entity = Medicine::class,
            parentColumns = ["id"],
            childColumns = ["medicineId"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Kit::class,
            parentColumns = ["kitId"],
            childColumns = ["kitId"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["kitId"])
    ]
)
data class MedicineKit(
    val medicineId: Long,
    val kitId: Long
)