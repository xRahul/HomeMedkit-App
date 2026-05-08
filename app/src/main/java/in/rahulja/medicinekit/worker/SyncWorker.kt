@file:OptIn(ExperimentalSerializationApi::class)

package `in`.rahulja.medicinekit.worker

import android.content.Context
import android.webkit.MimeTypeMap
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import kotlinx.io.IOException
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import `in`.rahulja.medicinekit.data.MedicineDatabase
import `in`.rahulja.medicinekit.data.dto.Medicine
import `in`.rahulja.medicinekit.network.GoogleDriveApi
import `in`.rahulja.medicinekit.network.Network
import `in`.rahulja.medicinekit.network.models.auth.BackupData
import `in`.rahulja.medicinekit.network.models.auth.FileMetadata
import `in`.rahulja.medicinekit.utils.MimeType
import `in`.rahulja.medicinekit.utils.Preferences
import `in`.rahulja.medicinekit.utils.SYNC_MODE
import `in`.rahulja.medicinekit.utils.enums.SyncMode
import `in`.rahulja.medicinekit.utils.extensions.md5
import java.io.File

class SyncWorker(
    val context: Context,
    params: WorkerParameters,
    private val preferences: Preferences
) : CoroutineWorker(context, params) {

    private val isYandex = preferences.authIsYandex

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val mode = when (val name = inputData.getString(SYNC_MODE)) {
            null -> SyncMode.AUTO
            else -> SyncMode.valueOf(name)
        }

        if (!isYandex) {
            GoogleDriveApi.init(context)
        }

        return@withContext try {
            when (mode) {
                SyncMode.FORCE_DOWNLOAD -> download()
                SyncMode.FORCE_UPLOAD -> upload(serializeData())
                SyncMode.AUTO -> {
                    coroutineScope {
                        val fileLocal = serializeData()

                        if (isYandex) {
                            val fileRemote = Network.Yandex.getFileMetadata("/homemeds/data/medicines.json")
                            if (fileRemote != null) {
                                val localMd5 = fileLocal.md5()
                                if (localMd5.equals(fileRemote.md5, ignoreCase = true)) {
                                    fileLocal.delete()
                                    return@coroutineScope
                                }
                                if (fileRemote.modified > preferences.lastSyncMillis) {
                                    fileLocal.delete()
                                    download()
                                } else {
                                    upload(fileLocal)
                                }
                            } else {
                                upload(fileLocal)
                            }
                        } else {
                            // Google Drive sync disabled
                            fileLocal.delete()
                        }
                    }
                }
            }

            preferences.updateSyncMillis()
            Result.success()
        } catch (_: Exception) {
            Result.failure()
        }
    }

    private suspend fun upload(file: File) {
        if (isYandex) {
            if (!Network.Yandex.checkConnection()) throw Exception()
        } else {
            if (!GoogleDriveApi.isReady()) throw Exception()
        }

        var imagesSizeBytes = 0L
        val mimeTypeMap = MimeTypeMap.getSingleton()
        val images = context.filesDir.listFiles { f ->
            val mimeType = mimeTypeMap.getMimeTypeFromExtension(f.extension)
            val isImage = mimeType != null && mimeType.startsWith(MimeType.IMAGES)
            if (isImage) imagesSizeBytes += f.length()
            isImage
        }

        try {
            if (isYandex) {
                val availableSpace = Network.Yandex.getAvailableSpace()
                if (availableSpace < (file.length() + imagesSizeBytes + 1024 * 1024L)) throw Exception()

                Network.Yandex.createFolder("/homemeds")
                Network.Yandex.createFolder("/homemeds/data")
                Network.Yandex.createFolder("/homemeds/images")
                Network.Yandex.uploadFile("/homemeds/data/${file.name}", file)
            } else {
                // Google Drive upload disabled
            }

            if (!images.isNullOrEmpty()) {
                uploadImages(images)
            }
        } catch (e: IOException) {
            throw Exception(e)
        } finally {
            withContext(NonCancellable) {
                file.delete()
            }
        }
    }

    private suspend fun uploadImages(images: Array<File>) {
        if (isYandex) {
            val remoteData = Network.Yandex.getImagesMetadata() ?: return
            val remoteMap = remoteData.associateBy(FileMetadata::name)
            val uploadList = images.filter { image ->
                val remoteImage = remoteMap[image.name] ?: return@filter true
                val remote = remoteImage.mapper()
                !image.md5().equals(remote.md5, true) && image.lastModified() > remote.modified
            }
            supervisorScope {
                uploadList.map { file ->
                    async(Dispatchers.IO.limitedParallelism(3)) {
                        Network.Yandex.uploadFile("/homemeds/images/${file.name}", file)
                    }
                }.awaitAll()
            }
        } else {
            // Google Drive uploadImages disabled
        }
    }

    private suspend fun downloadImages(images: Array<File>?) {
        if (isYandex) {
            val remoteData = Network.Yandex.getImagesMetadata() ?: return
            val localData = images?.associateBy { it.name.orEmpty() }
            val downloadList = remoteData.filter { image ->
                val localFile = localData?.get(image.name) ?: return@filter true
                val remote = image.mapper()
                !localFile.md5().equals(remote.md5, true) && remote.modified > localFile.lastModified()
            }
            supervisorScope {
                downloadList.map { image ->
                    async(Dispatchers.IO.limitedParallelism(3)) {
                        val name = image.name ?: return@async
                        val file = File(context.filesDir, name)
                        Network.Yandex.downloadFile("/homemeds/images/$name", file)
                        file.setLastModified(image.mapper().modified)
                    }
                }.awaitAll()
            }
        } else {
            // Google Drive downloadImages disabled
        }
    }

    private suspend fun download() {
        val tempFile = File(context.cacheDir, "medicines_temp.json")
        val downloadSuccess = if (isYandex) {
            Network.Yandex.downloadFile("/homemeds/data/medicines.json", tempFile)
        } else {
            // Google Drive download disabled
            false
        }

        if (downloadSuccess) {
            val database = MedicineDatabase.getInstance(context)
            try {
                tempFile.inputStream().use { inputStream ->
                    val backup = Json.decodeFromStream<BackupData<List<Medicine>>>(inputStream)
                    if (backup.version <= database.openHelper.readableDatabase.version) {
                        database.medicineDAO().syncMedicines(backup.data)
                    }
                }
            } finally {
                withContext(NonCancellable) { tempFile.delete() }
            }
        }

        val mimeTypeMap = MimeTypeMap.getSingleton()
        val images = context.filesDir.listFiles { f ->
            val mimeType = mimeTypeMap.getMimeTypeFromExtension(f.extension)
            mimeType != null && mimeType.startsWith(MimeType.IMAGES)
        }
        downloadImages(images)
    }

    private suspend fun serializeData(): File {
        val database = MedicineDatabase.getInstance(context)
        val file = File(context.cacheDir, "medicines.json")
        val medicines = database.medicineDAO().getAll()
        val backupData = BackupData(
            version = database.openHelper.readableDatabase.version,
            data = medicines
        )
        file.outputStream().buffered().use { outputStream ->
            Json.encodeToStream(backupData, outputStream)
        }
        return file
    }
}
