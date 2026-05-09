package `in`.rahulja.medicinekit.data.dto

import androidx.room.TypeConverter
import `in`.rahulja.medicinekit.utils.enums.DoseType
import `in`.rahulja.medicinekit.utils.enums.SchemaType
import java.time.DayOfWeek

class Converters {
    @TypeConverter
    fun fromDoseType(value: DoseType): String = value.name

    @TypeConverter
    fun toDoseType(value: String): DoseType = DoseType.valueOf(value)

    @TypeConverter
    fun fromSchemaType(value: SchemaType): String = value.name

    @TypeConverter
    fun toSchemaType(value: String): SchemaType = SchemaType.valueOf(value)

    @TypeConverter
    fun fromDayOfWeek(value: DayOfWeek): String = value.name

    @TypeConverter
    fun toDayOfWeek(value: String): DayOfWeek = DayOfWeek.valueOf(value)
}
