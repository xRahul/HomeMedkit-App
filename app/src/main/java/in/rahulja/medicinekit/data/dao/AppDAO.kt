package `in`.rahulja.medicinekit.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Transaction
import androidx.room.Upsert
import androidx.room.Update
import androidx.room.Delete
import androidx.sqlite.db.SupportSQLiteQuery
import `in`.rahulja.medicinekit.data.dto.Alarm
import `in`.rahulja.medicinekit.data.dto.Image
import `in`.rahulja.medicinekit.data.dto.Intake
import `in`.rahulja.medicinekit.data.dto.IntakeDay
import `in`.rahulja.medicinekit.data.dto.IntakeTaken
import `in`.rahulja.medicinekit.data.dto.IntakeTime
import `in`.rahulja.medicinekit.data.dto.Kit
import `in`.rahulja.medicinekit.data.dto.Medicine
import `in`.rahulja.medicinekit.data.dto.MedicineFTS
import `in`.rahulja.medicinekit.data.dto.MedicineKit
import `in`.rahulja.medicinekit.data.model.IntakeFull
import `in`.rahulja.medicinekit.data.model.IntakeList
import `in`.rahulja.medicinekit.data.model.IntakeTakenFull
import `in`.rahulja.medicinekit.data.model.MedicineFull
import `in`.rahulja.medicinekit.data.model.MedicineMain
import `in`.rahulja.medicinekit.data.model.Schedule
import kotlinx.coroutines.flow.Flow
import java.time.DayOfWeek

@Dao
interface AppDAO {

    // ============================== MedicineDAO ==============================
    @Query("SELECT * FROM medicines")
    suspend fun getAllMedicines(): List<Medicine>

    @Query("SELECT * FROM medicines WHERE expDate < :expDate AND prodAmount > 0")
    suspend fun getExpiredSoon(expDate: Long): List<Medicine>

    @Transaction
    @RawQuery(observedEntities = [Medicine::class, MedicineFTS::class, Image::class, Kit::class])
    fun getMedicineFlow(query: SupportSQLiteQuery): Flow<List<MedicineMain>>

    @Query("SELECT id FROM medicines WHERE cis LIKE '%' || :cis || '%' AND cis IS NOT NULL AND cis != '' LIMIT 1")
    suspend fun findDuplicateMedicine(cis: String): Long?

    @Transaction
    @Query("SELECT * FROM medicines WHERE id = :id")
    suspend fun getMedicineById(id: Long): MedicineFull?

    @Query("SELECT DISTINCT image FROM images")
    suspend fun getAllImageNames(): List<String>

    @Query("SELECT image FROM images WHERE medicineId = :medicineId AND position = 0")
    suspend fun getMedicineImage(medicineId: Long): String?

    @Query("SELECT COUNT(*) FROM images WHERE image = :fileName")
    suspend fun getImageCount(fileName: String): Int

    @Query("UPDATE medicines SET prodAmount = prodAmount - :amount WHERE id = :id")
    suspend fun intakeMedicine(id: Long, amount: Double)

    @Query("UPDATE medicines SET prodAmount = prodAmount + :amount WHERE id = :id")
    suspend fun untakeMedicine(id: Long, amount: Double)

    @Insert
    suspend fun insertImages(image: List<Image>)

    @Upsert
    suspend fun upsertMedicines(medicines: Iterable<Medicine>)

    @Transaction
    suspend fun updateImages(images: List<Image>) {
        images.firstOrNull()?.let { deleteImages(it.medicineId) }
        insertImages(images)
    }

    @Transaction
    suspend fun syncMedicines(medicines: Iterable<Medicine>) {
        upsertMedicines(medicines)
        val ids = medicines.map { it.id }
        deleteMissingMedicines(ids)
    }

    @Query("DELETE FROM images WHERE medicineId = :medicineId")
    suspend fun deleteImages(medicineId: Long)

    @Query("DELETE FROM medicines WHERE id NOT IN (:ids)")
    suspend fun deleteMissingMedicines(ids: List<Long>)

    @Query("SELECT * FROM images")
    suspend fun getAllImages(): List<Image>

    @Transaction
    suspend fun syncImages(images: Iterable<Image>) {
        insertImages(images.toList())
    }

    @Query("DELETE FROM images")
    suspend fun deleteAllImages()

    @Insert
    suspend fun insertMedicine(medicine: Medicine): Long

    @Update
    suspend fun updateMedicine(medicine: Medicine)

    @Delete
    suspend fun deleteMedicine(medicine: Medicine)

    // ============================== IntakeDAO ==============================
    @Transaction
    @Query("SELECT intakeId, medicineId, productName, nameAlias, interval, schemaType, finalDate FROM intakes JOIN medicines ON medicines.id = intakes.medicineId")
    fun getAllIntakesList(): List<IntakeList>

    @Transaction
    @Query(
        """
        SELECT intakeId, medicineId, productName, nameAlias, interval, schemaType, finalDate
        FROM intakes
        JOIN medicines ON medicines.id = intakes.medicineId
        WHERE (:searchQuery = '' OR LOWER(productName) LIKE '%' || LOWER(:searchQuery) || '%' OR LOWER(nameAlias) LIKE '%' || LOWER(:searchQuery) || '%')
    """
    )
    fun getIntakesFlow(searchQuery: String): Flow<List<IntakeList>>

    @Transaction
    @Query("SELECT * FROM intakes WHERE intakeId = :intakeId")
    suspend fun getIntakeById(intakeId: Long): IntakeFull?

    @Insert
    suspend fun addIntakeTime(intakeTime: IntakeTime): Long

    @Query("DELETE FROM intake_time WHERE intakeId = :intakeId")
    suspend fun deleteIntakeTime(intakeId: Long)

    @Query("SELECT * FROM intakes")
    suspend fun getAllIntakes(): List<Intake>

    @Query("SELECT * FROM intake_time")
    suspend fun getAllIntakeTimes(): List<IntakeTime>

    @Upsert
    suspend fun upsertAllIntakes(intakes: Iterable<Intake>)

    @Upsert
    suspend fun upsertAllIntakeTimes(intakeTimes: Iterable<IntakeTime>)

    @Transaction
    suspend fun syncIntakes(intakes: Iterable<Intake>, intakeTimes: Iterable<IntakeTime>) {
        upsertAllIntakes(intakes)
        upsertAllIntakeTimes(intakeTimes)
        val ids = intakes.map { it.intakeId }
        if (ids.isNotEmpty()) deleteMissingIntakes(ids)
    }

    @Query("DELETE FROM intakes WHERE intakeId NOT IN (:ids)")
    suspend fun deleteMissingIntakes(ids: List<Long>)

    @Insert
    suspend fun insertIntake(intake: Intake): Long

    @Update
    suspend fun updateIntake(intake: Intake)

    @Delete
    suspend fun deleteIntake(intake: Intake)

    // ============================== AlarmDAO ==============================
    @Transaction
    @Query(
        """
        SELECT alarms.alarmId, alarms.`trigger`, alarms.amount, images.image, 
        medicines.nameAlias, medicines.productName, medicines.prodFormNormName, medicines.doseType
        FROM alarms
        JOIN intakes ON intakes.intakeId = alarms.intakeId 
        JOIN medicines ON medicines.id = intakes.medicineId 
        LEFT JOIN images ON images.medicineId = medicines.id
        WHERE (:search = '' OR LOWER(medicines.productName) LIKE '%' || LOWER(:search) || '%')
        GROUP BY alarms.alarmId
        ORDER BY alarms.`trigger`
    """
    )
    fun getAlarmsFlow(search: String): Flow<List<Schedule>>

    @Query("SELECT * FROM alarms")
    suspend fun getAllAlarms(): List<Alarm>

    @Query("SELECT * FROM alarms WHERE `trigger` < :millis ORDER BY `trigger` ASC")
    suspend fun getExpiredAlarms(millis: Long): List<Alarm>

    @Query("SELECT * FROM alarms WHERE alarmId = :alarmId")
    suspend fun getAlarmById(alarmId: Long): Alarm?

    @Query("SELECT * FROM alarms WHERE intakeId = :intakeId ORDER BY `trigger` LIMIT 1")
    suspend fun getNextAlarmByIntakeId(intakeId: Long): Alarm?

    @Query("DELETE FROM alarms WHERE intakeId = :intakeId")
    suspend fun deleteAlarmsByIntakeId(intakeId: Long)

    @Upsert
    suspend fun upsertAllAlarms(alarms: Iterable<Alarm>)

    @Transaction
    suspend fun syncAlarms(alarms: Iterable<Alarm>) {
        upsertAllAlarms(alarms)
        val ids = alarms.map { it.alarmId }
        if (ids.isNotEmpty()) deleteMissingAlarms(ids)
    }

    @Query("DELETE FROM alarms WHERE alarmId NOT IN (:ids)")
    suspend fun deleteMissingAlarms(ids: List<Long>)

    @Insert
    suspend fun insertAlarm(alarm: Alarm): Long

    @Insert
    suspend fun insertAlarms(alarms: List<Alarm>)

    @Update
    suspend fun updateAlarm(alarm: Alarm)

    @Delete
    suspend fun deleteAlarm(alarm: Alarm)

    // ============================== IntakeDayDAO ==============================
    @Query("DELETE FROM intake_days WHERE intakeId = :intakeId")
    suspend fun deleteIntakeDaysByIntakeId(intakeId: Long)

    @Transaction
    suspend fun insertIntakeDays(days: Iterable<IntakeDay>) {
        days.forEach { (intakeId, day) ->
            insertIntakeDay(intakeId, day)
        }
    }

    @Query("INSERT INTO intake_days (`intakeId`,`day`) VALUES (:intakeId, :day)")
    suspend fun insertIntakeDay(intakeId: Long, day: DayOfWeek)

    @Query("SELECT * FROM intake_days")
    suspend fun getAllIntakeDays(): List<IntakeDay>

    @Upsert
    suspend fun upsertAllIntakeDays(days: Iterable<IntakeDay>)

    @Transaction
    suspend fun syncIntakeDays(days: Iterable<IntakeDay>) {
        upsertAllIntakeDays(days)
    }

    // ============================== KitDAO ==============================
    @Query("SELECT * FROM kits ORDER BY position ASC, kitId ASC")
    fun getKitsFlow(): Flow<List<Kit>>

    @Query("SELECT * FROM kits WHERE kitId IN (:kitIds)")
    suspend fun getKitList(kitIds: Set<Long>): List<Kit>

    @Query("DELETE FROM medicines_kits WHERE medicineId = :medicineId")
    suspend fun deleteMedicineKitsByMedicineId(medicineId: Long)

    @Query("SELECT * FROM kits")
    suspend fun getAllKits(): List<Kit>

    @Query("SELECT * FROM medicines_kits")
    suspend fun getAllMedicineKits(): List<MedicineKit>

    @Upsert
    suspend fun upsertAllKits(kits: Iterable<Kit>)

    @Upsert
    suspend fun upsertAllMedicineKits(medicineKits: Iterable<MedicineKit>)

    @Transaction
    suspend fun syncKits(kits: Iterable<Kit>, medicineKits: Iterable<MedicineKit>) {
        upsertAllKits(kits)
        upsertAllMedicineKits(medicineKits)
        val kitIds = kits.map { it.kitId }
        if (kitIds.isNotEmpty()) deleteMissingKits(kitIds)
    }

    @Query("DELETE FROM kits WHERE kitId NOT IN (:ids)")
    suspend fun deleteMissingKits(ids: List<Long>)

    @Upsert
    suspend fun upsertKit(item: Kit)

    @Insert
    suspend fun pinKit(kits: Iterable<MedicineKit>)

    @Upsert
    suspend fun updateKitPositions(kits: Iterable<Kit>)

    @Delete
    suspend fun deleteKit(kit: Kit)

    // ============================== TakenDAO ==============================
    @Query(
        """
        SELECT * FROM intakes_taken
        WHERE (:search = '' OR LOWER(productName) LIKE '%' || LOWER(:search) || '%')
        ORDER BY `trigger` DESC
    """
    )
    fun getTakenFlow(search: String): Flow<List<IntakeTaken>>

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM intakes_taken WHERE takenId = :takenId")
    suspend fun getTakenById(takenId: Long): IntakeTakenFull?

    @Query(
        """
        SELECT amount FROM intakes_taken 
        WHERE medicineId = :medicineId
        ORDER BY `trigger` DESC
        LIMIT 1
    """
    )
    suspend fun getSimilarAmount(medicineId: Long): Double?

    @Query("SELECT `trigger` FROM intakes_taken WHERE intakeId = :intakeId")
    suspend fun getTakenTriggers(intakeId: Long): List<Long>

    @Query("UPDATE intakes_taken SET taken = :taken, inFact = :inFact WHERE takenId = :id")
    suspend fun setTaken(id: Long, taken: Boolean, inFact: Long)

    @Query("UPDATE intakes_taken SET notified = 1 WHERE takenId = :id")
    suspend fun setNotified(id: Long)

    @Query("UPDATE intakes_taken SET notified = 1 WHERE notified = 0")
    suspend fun setAllNotified()

    @Query("SELECT * FROM intakes_taken")
    suspend fun getAllTaken(): List<IntakeTaken>

    @Upsert
    suspend fun upsertAllTaken(taken: Iterable<IntakeTaken>)

    @Transaction
    suspend fun syncTaken(taken: Iterable<IntakeTaken>) {
        upsertAllTaken(taken)
        val ids = taken.map { it.takenId }
        if (ids.isNotEmpty()) deleteMissingTaken(ids)
    }

    @Query("DELETE FROM intakes_taken WHERE takenId NOT IN (:ids)")
    suspend fun deleteMissingTaken(ids: List<Long>)

    @Insert
    suspend fun insertTaken(taken: IntakeTaken): Long

    @Update
    suspend fun updateTaken(taken: IntakeTaken)

    @Delete
    suspend fun deleteTaken(taken: IntakeTaken)
}
