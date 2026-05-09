package `in`.rahulja.medicinekit.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import `in`.rahulja.medicinekit.data.dao.AppDAO
import `in`.rahulja.medicinekit.data.dto.Alarm
import `in`.rahulja.medicinekit.data.dto.Converters
import `in`.rahulja.medicinekit.data.dto.Image
import `in`.rahulja.medicinekit.data.dto.Intake
import `in`.rahulja.medicinekit.data.dto.IntakeDay
import `in`.rahulja.medicinekit.data.dto.IntakeTaken
import `in`.rahulja.medicinekit.data.dto.IntakeTime
import `in`.rahulja.medicinekit.data.dto.Kit
import `in`.rahulja.medicinekit.data.dto.Medicine
import `in`.rahulja.medicinekit.data.dto.MedicineFTS
import `in`.rahulja.medicinekit.data.dto.MedicineKit
import `in`.rahulja.medicinekit.utils.DATABASE_NAME
import kotlinx.coroutines.Dispatchers

@Database(
    entities = [
        Medicine::class,
        Intake::class,
        Alarm::class,
        IntakeDay::class,
        Kit::class,
        MedicineKit::class,
        IntakeTime::class,
        IntakeTaken::class,
        Image::class,
        MedicineFTS::class,
    ],
    version = 38,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class MedicineDatabase : RoomDatabase() {

    abstract fun appDAO(): AppDAO

    companion object {
        @Volatile
        private var INSTANCE: MedicineDatabase? = null

        fun getInstance(context: Context): MedicineDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context = context.applicationContext,
                klass = MedicineDatabase::class.java,
                name = DATABASE_NAME,
            )
                .fallbackToDestructiveMigration()
                .setQueryCoroutineContext(Dispatchers.IO)
                .build()

        fun close() {
            INSTANCE?.close()
            INSTANCE = null
        }
    }
}
