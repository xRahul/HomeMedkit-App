package `in`.rahulja.medicinekit.data.repository

import `in`.rahulja.medicinekit.data.dao.AppDAO
import `in`.rahulja.medicinekit.data.dto.Image
import `in`.rahulja.medicinekit.data.dto.Medicine
import `in`.rahulja.medicinekit.data.dto.MedicineKit
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MedicineRepository(
    private val dao: AppDAO,
) {
    fun getKitsFlow() = dao.getKitsFlow()

    suspend fun saveMedicine(medicine: Medicine, kits: List<Long>, images: List<String>): Long = coroutineScope {
        val id = dao.insertMedicine(medicine)

        val jobOne = launch {
            val medicineKits = kits.map { MedicineKit(id, it) }
            dao.pinKit(medicineKits)
        }

        val jobTwo = launch {
            val medicineImages = images.mapIndexed { index, image ->
                Image(medicineId = id, position = index, image = image)
            }
            dao.updateImages(medicineImages)
        }

        joinAll(jobOne, jobTwo)
        id
    }

    suspend fun updateMedicine(medicine: Medicine, kits: List<Long>, images: List<String>) = coroutineScope {
        dao.deleteMedicineKitsByMedicineId(medicine.id)
        val jobOne = launch {
            val medicineKits = kits.map { MedicineKit(medicine.id, it) }
            dao.pinKit(medicineKits)
        }

        val jobTwo = launch {
            val medicineImages = images.mapIndexed { index, image ->
                Image(medicineId = medicine.id, position = index, image = image)
            }
            dao.updateImages(medicineImages)
        }

        joinAll(jobOne, jobTwo)
        dao.updateMedicine(medicine)
    }

    suspend fun deleteMedicine(medicine: Medicine, images: List<String>, directory: File) = withContext(Dispatchers.IO) {
        dao.deleteMedicine(medicine)
        images.forEach { image ->
            if (dao.getImageCount(image) == 0) {
                File(directory, image).delete()
            }
        }
    }
}
