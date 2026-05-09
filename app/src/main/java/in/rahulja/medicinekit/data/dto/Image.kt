package `in`.rahulja.medicinekit.data.dto

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import `in`.rahulja.medicinekit.utils.BLANK

@Serializable
@Entity(
    tableName = "images",
    foreignKeys = [
        ForeignKey(
            entity = Medicine::class,
            parentColumns = ["id"],
            childColumns = ["medicineId"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["medicineId"])
    ]
)
data class Image(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val medicineId: Long = 0L,
    val position: Int = 0,
    val image: String = BLANK
)
