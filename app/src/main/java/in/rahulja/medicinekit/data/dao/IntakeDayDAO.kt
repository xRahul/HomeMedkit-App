package `in`.rahulja.medicinekit.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import `in`.rahulja.medicinekit.data.dto.IntakeDay

@Dao
interface IntakeDayDAO {
    @Query("DELETE FROM intake_days WHERE intakeId = :intakeId")
    suspend fun deleteByIntakeId(intakeId: Long)

    @Transaction
    suspend fun insert(days: Iterable<IntakeDay>) {
        days.forEach { (intakeId, day) ->
            insert(intakeId, day.name)
        }
    }

    @Query("INSERT INTO intake_days (`intakeId`,`day`) VALUES (:intakeId, :day)")
    suspend fun insert(intakeId: Long, day: String)
}