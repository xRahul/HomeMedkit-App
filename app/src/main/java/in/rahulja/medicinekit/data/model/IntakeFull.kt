package `in`.rahulja.medicinekit.data.model

import androidx.room.Relation
import `in`.rahulja.medicinekit.data.dto.Image
import `in`.rahulja.medicinekit.data.dto.IntakeDay
import `in`.rahulja.medicinekit.data.dto.IntakeTime
import `in`.rahulja.medicinekit.data.dto.Medicine
import `in`.rahulja.medicinekit.utils.BLANK
import `in`.rahulja.medicinekit.utils.enums.SchemaType
import java.time.DayOfWeek

data class IntakeFull(
    val intakeId: Long = 0L,
    val medicineId: Long = 0L,
    val interval: Int = 0,
    val foodType: Int = -1,
    val period: Int = 0,
    val startDate: String = BLANK,
    val finalDate: String = BLANK,
    val schemaType: SchemaType = SchemaType.BY_DAYS,
    val sameAmount: Boolean = true,
    val fullScreen: Boolean = false,
    val noSound: Boolean = false,
    val preAlarm: Boolean = false,
    val cancellable: Boolean = true,

    @Relation(
        entity = Medicine::class,
        parentColumn = "medicineId",
        entityColumn = "id",
        projection = ["productName", "nameAlias", "prodFormNormName", "expDate", "prodAmount", "doseType"]
    )
    val medicine: MedicineIntake,

    @Relation(
        entity = IntakeDay::class,
        parentColumn = "intakeId",
        entityColumn = "intakeId",
        projection = ["day"]
    )
    val pickedDays: List<DayOfWeek>,

    @Relation(
        entity = IntakeTime::class,
        parentColumn = "intakeId",
        entityColumn = "intakeId"
    )
    val pickedTime: List<IntakeTime>,

    @Relation(
        entity = Image::class,
        parentColumn = "medicineId",
        entityColumn = "medicineId",
        projection = ["image"]
    )
    val images: List<String>
)
