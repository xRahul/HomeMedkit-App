package ru.application.homemedkit.network

import android.content.Context
// import com.google.android.gms.auth.api.signin.GoogleSignIn
// import com.google.api.client.extensions.android.http.AndroidHttp
// import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
// import com.google.api.client.json.jackson2.JacksonFactory
// import com.google.api.services.drive.Drive
// import com.google.api.services.drive.DriveScopes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
// import java.io.FileOutputStream

object GoogleDriveApi {
    // private var driveService: Drive? = null

    fun init(context: Context) {
        /*
        val account = GoogleSignIn.getLastSignedInAccount(context)
        if (account != null) {
            val credential = GoogleAccountCredential.usingOAuth2(
                context, listOf(DriveScopes.DRIVE_FILE, DriveScopes.DRIVE_APPDATA)
            )
            credential.selectedAccount = account.account
            driveService = Drive.Builder(
                AndroidHttp.newCompatibleTransport(),
                JacksonFactory.getDefaultInstance(),
                credential
            ).setApplicationName("HomeMedKit").build()
        }
        */
    }

    fun isReady(): Boolean = false // driveService != null

    suspend fun getAvailableSpace(): Long = withContext(Dispatchers.IO) {
        /*
        try {
            val about = driveService?.about()?.get()?.setFields("storageQuota")?.execute()
            val limit = about?.storageQuota?.limit ?: 0L
            val usage = about?.storageQuota?.usage ?: 0L
            limit - usage
        } catch (e: Exception) {
            -1L
        }
        */
        -1L
    }

    suspend fun getOrCreateFolderId(folderName: String, parentId: String? = null): String? = withContext(Dispatchers.IO) {
        /*
        try {
            var query = "mimeType='application/vnd.google-apps.folder' and name='$folderName' and trashed=false"
            if (parentId != null) {
                query += " and '$parentId' in parents"
            }
            val result = driveService?.files()?.list()?.setQ(query)?.setSpaces("drive")?.execute()
            val files = result?.files
            if (files != null && files.isNotEmpty()) {
                files.first().id
            } else {
                val fileMetadata = com.google.api.services.drive.model.File()
                fileMetadata.name = folderName
                fileMetadata.mimeType = "application/vnd.google-apps.folder"
                if (parentId != null) {
                    fileMetadata.parents = listOf(parentId)
                }
                val folder = driveService?.files()?.create(fileMetadata)?.setFields("id")?.execute()
                folder?.id
            }
        } catch (e: Exception) {
            null
        }
        */
        null
    }

    suspend fun uploadFile(file: File, folderId: String, mimeType: String = "application/octet-stream"): Boolean = withContext(Dispatchers.IO) {
        /*
        try {
            // Check if file exists
            val query = "name='${file.name}' and '$folderId' in parents and trashed=false"
            val result = driveService?.files()?.list()?.setQ(query)?.setSpaces("drive")?.execute()
            val files = result?.files

            val fileContent = com.google.api.client.http.FileContent(mimeType, file)
            if (files != null && files.isNotEmpty()) {
                val fileId = files.first().id
                val fileMetadata = com.google.api.services.drive.model.File()
                driveService?.files()?.update(fileId, fileMetadata, fileContent)?.execute()
            } else {
                val fileMetadata = com.google.api.services.drive.model.File()
                fileMetadata.name = file.name
                fileMetadata.parents = listOf(folderId)
                driveService?.files()?.create(fileMetadata, fileContent)?.execute()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
        */
        false
    }

    suspend fun downloadFile(fileName: String, folderId: String, destinationFile: File): Boolean = withContext(Dispatchers.IO) {
        /*
        try {
            val query = "name='$fileName' and '$folderId' in parents and trashed=false"
            val result = driveService?.files()?.list()?.setQ(query)?.setSpaces("drive")?.execute()
            val files = result?.files
            if (files != null && files.isNotEmpty()) {
                val fileId = files.first().id
                val outputStream = FileOutputStream(destinationFile)
                driveService?.files()?.get(fileId)?.executeMediaAndDownloadTo(outputStream)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
        */
        false
    }

    suspend fun getFilesMetadata(folderId: String): List<Any>? = withContext(Dispatchers.IO) {
        /*
        try {
            val query = "'$folderId' in parents and trashed=false"
            val result = driveService?.files()?.list()?.setQ(query)?.setSpaces("drive")?.execute()
            result?.files
        } catch (e: Exception) {
            null
        }
        */
        null
    }
}
