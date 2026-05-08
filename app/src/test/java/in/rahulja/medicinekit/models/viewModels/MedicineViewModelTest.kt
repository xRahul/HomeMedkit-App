package `in`.rahulja.medicinekit.models.viewModels

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import `in`.rahulja.medicinekit.data.dao.KitDAO
import `in`.rahulja.medicinekit.data.dao.MedicineDAO
import `in`.rahulja.medicinekit.models.events.MedicineEvent
import `in`.rahulja.medicinekit.utils.Formatter

@OptIn(ExperimentalCoroutinesApi::class)
class MedicineViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val dao: MedicineDAO = mockk()
    private val daoK: KitDAO = mockk()
    private lateinit var viewModel: MedicineViewModel

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        // Mock android.icu.text.DecimalFormat and NumberFormat
        mockkStatic(android.icu.text.DecimalFormat::class)
        mockkStatic(android.icu.text.NumberFormat::class)
        val mockDecimalFormat = mockk<android.icu.text.DecimalFormat>()
        every { android.icu.text.NumberFormat.getInstance(any<java.util.Locale>()) } returns mockDecimalFormat
        every { mockDecimalFormat.setMaximumFractionDigits(any()) } returns Unit
        
        mockkObject(Formatter)
        
        every { daoK.getFlow() } returns flowOf(emptyList())
        coEvery { dao.getById(any()) } returns null
        
        viewModel = MedicineViewModel(
            id = 0L,
            cis = "",
            duplicate = false,
            dao = dao,
            daoK = daoK
        )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `SetProductName updates state correctly`() = runTest {
        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.state.collect() }
        val newName = "New Medicine"
        
        viewModel.onEvent(MedicineEvent.SetProductName(newName))
        advanceUntilIdle()
        
        assertEquals(newName, viewModel.state.value.productName)
        collectJob.cancel()
    }

    @Test
    fun `SetComment updates state correctly`() = runTest {
        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.state.collect() }
        val comment = "Take with food"
        
        viewModel.onEvent(MedicineEvent.SetComment(comment))
        advanceUntilIdle()
        
        assertEquals(comment, viewModel.state.value.comment)
        collectJob.cancel()
    }

    @Test
    fun `SetSalts updates state correctly`() = runTest {
        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.state.collect() }
        val salts = "Paracetamol"
        
        viewModel.onEvent(MedicineEvent.SetSalts(salts))
        advanceUntilIdle()
        
        assertEquals(salts, viewModel.state.value.salts)
        collectJob.cancel()
    }
}
