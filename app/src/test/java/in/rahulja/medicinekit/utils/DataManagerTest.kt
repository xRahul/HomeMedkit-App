package `in`.rahulja.medicinekit.utils

import android.content.Context
import android.webkit.MimeTypeMap
import `in`.rahulja.medicinekit.data.MedicineDatabase
import `in`.rahulja.medicinekit.data.dao.AppDAO
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

class DataManagerTest {

    private val context: Context = mockk(relaxed = true)
    private val database: MedicineDatabase = mockk(relaxed = true)
    private val dao: AppDAO = mockk(relaxed = true)
    private val filesDir = File("temp_files")
    private val mimeTypeMap: MimeTypeMap = mockk(relaxed = true)

    @BeforeEach
    fun setup() {
        mockkObject(MedicineDatabase)
        every { MedicineDatabase.getInstance(any()) } returns database
        every { database.appDAO() } returns dao
        every { context.filesDir } returns filesDir
        if (!filesDir.exists()) filesDir.mkdirs()

        mockkStatic(MimeTypeMap::class)
        every { MimeTypeMap.getSingleton() } returns mimeTypeMap
        every { mimeTypeMap.getMimeTypeFromExtension(any()) } returns "image/jpeg"
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
        filesDir.deleteRecursively()
    }

    @Test
    fun `clearCache should delete unreferenced images`() = runTest {
        // Given
        val referencedImage = File(filesDir, "referenced.jpg").apply { createNewFile() }
        val unreferencedImage = File(filesDir, "unreferenced.jpg").apply { createNewFile() }
        
        coEvery { dao.getAllImageNames() } returns listOf("referenced.jpg")
        
        // When
        DataManager.clearCache(context)
        
        // Then
        assertTrue(referencedImage.exists())
        assertTrue(!unreferencedImage.exists())
    }
}
