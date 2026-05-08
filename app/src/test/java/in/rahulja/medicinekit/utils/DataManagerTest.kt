package `in`.rahulja.medicinekit.utils

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import `in`.rahulja.medicinekit.data.MedicineDatabase
import `in`.rahulja.medicinekit.data.dao.MedicineDAO
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DataManagerTest {

    private val context = mockk<Context>(relaxed = true)
    private val uri = mockk<Uri>(relaxed = true)
    private val medicineDAO = mockk<MedicineDAO>(relaxed = true)

    @BeforeEach
    fun setUp() {
        mockkObject(MedicineDatabase)
        mockkStatic(MimeTypeMap::class)
        val mimeTypeMap = mockk<MimeTypeMap>()
        every { MimeTypeMap.getSingleton() } returns mimeTypeMap
        
        val database = mockk<MedicineDatabase>(relaxed = true)
        every { MedicineDatabase.getInstance(any()) } returns database
        every { database.medicineDAO() } returns medicineDAO
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `importAll returns false on null input stream`() = runTest {
        every { context.contentResolver.openInputStream(any()) } returns null
        val result = DataManager.importAll(context, uri)
        assertFalse(result)
    }

    @Test
    fun `clearCache returns true on success`() = runTest {
        coEvery { medicineDAO.getAllImageNames() } returns emptyList()
        val result = DataManager.clearCache(context)
        assertTrue(result)
    }
}
