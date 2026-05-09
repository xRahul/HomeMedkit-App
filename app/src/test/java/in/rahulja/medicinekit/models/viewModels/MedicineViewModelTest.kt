package `in`.rahulja.medicinekit.models.viewModels

import `in`.rahulja.medicinekit.data.dao.KitDAO
import `in`.rahulja.medicinekit.data.dao.MedicineDAO
import `in`.rahulja.medicinekit.models.events.MedicineEvent
import `in`.rahulja.medicinekit.models.states.MedicineDialogState
import `in`.rahulja.medicinekit.utils.AiMedicineParser
import `in`.rahulja.medicinekit.utils.Formatter
import `in`.rahulja.medicinekit.utils.enums.AiMode
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
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

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
        mockkObject(AiMedicineParser)
        
        every { daoK.getFlow() } returns flowOf(emptyList())
        coEvery { dao.getById(any()) } returns null
        
        viewModel = MedicineViewModel(
            id = 0L,
            cis = "",
            duplicate = false,
            openCamera = false,
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

    @Test
    fun `ViewModel initializes with TakePhoto dialog when openCamera is true`() = runTest {
        val vm = MedicineViewModel(0L, "", false, true, dao, daoK)
        val collectJob = launch(UnconfinedTestDispatcher()) { vm.state.collect() }
        advanceUntilIdle()
        assertEquals(MedicineDialogState.TakePhoto, vm.state.value.dialogState)
        collectJob.cancel()
    }

    @Test
    fun `ViewModel initializes with empty images when openCamera is true and id is 0`() = runTest {
        val vm = MedicineViewModel(0L, "", false, true, dao, daoK)
        val collectJob = launch(UnconfinedTestDispatcher()) { vm.state.collect() }
        advanceUntilIdle()
        assertTrue(vm.state.value.images.isEmpty())
        collectJob.cancel()
    }

    @Test
    fun `ProcessImageWithAi with ML_KIT sanitizes extracted text and shows review`() = runTest {
        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.state.collect() }
        val mockContext = mockk<android.content.Context>(relaxed = true)
        val mockUri = mockk<android.net.Uri>(relaxed = true)
        
        coEvery { AiMedicineParser.parseWithMLKit(any(), any()) } returns "Line1\nLine2\n"
        
        viewModel.onEvent(MedicineEvent.ProcessImageWithAi(
            context = mockContext,
            uri = mockUri,
            useAi = true,
            aiMode = AiMode.ML_KIT,
            apiKey = ""
        ))
        
        advanceUntilIdle()
        
        assertEquals(MedicineDialogState.AiReview, viewModel.state.value.dialogState)
        assertEquals("Line1 Line2", viewModel.state.value.aiResult?.name)
        assertEquals("Line1 Line2", viewModel.state.value.aiResult?.comment)
        collectJob.cancel()
    }

    @Test
    fun `ProcessImageWithAi with GEMINI transitions to AiReview state`() = runTest {
        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.state.collect() }
        val mockContext = mockk<android.content.Context>(relaxed = true)
        val mockUri = mockk<android.net.Uri>(relaxed = true)
        val aiResult = `in`.rahulja.medicinekit.utils.AiMedicineResult(
            name = "Gemini Med",
            salts = "Gemini Salts",
            dose = "500mg",
            form = "Tablet"
        )
        
        coEvery { AiMedicineParser.parseWithMLKit(any(), any()) } returns "Raw Text"
        coEvery { AiMedicineParser.parseWithGemini(any(), any(), any(), any(), any()) } returns aiResult
        
        viewModel.onEvent(MedicineEvent.ProcessImageWithAi(
            context = mockContext,
            uri = mockUri,
            useAi = true,
            aiMode = AiMode.GEMINI,
            apiKey = "fake-key"
        ))
        
        advanceUntilIdle()
        
        assertEquals(MedicineDialogState.AiReview, viewModel.state.value.dialogState)
        assertEquals(aiResult, viewModel.state.value.aiResult)
        collectJob.cancel()
    }

    @Test
    fun `ApplyAiResult maps fields and clears aiResult`() = runTest {
        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.state.collect() }
        val aiResult = `in`.rahulja.medicinekit.utils.AiMedicineResult(
            name = "Gemini Med",
            salts = "Gemini Salts",
            dose = "500mg",
            form = "Tablet",
            indications = "Headache",
            recommendations = "Take twice a day",
            storage = "Cool dry place"
        )
        
        // Directly set aiResult in state for testing ApplyAiResult
        viewModel.onEvent(MedicineEvent.ProcessImageWithAi(
            mockk(), mockk(), true, AiMode.GEMINI, "key"
        ))
        coEvery { AiMedicineParser.parseWithMLKit(any(), any()) } returns "test"
        coEvery { AiMedicineParser.parseWithGemini(any(), any(), any(), any(), any()) } returns aiResult
        advanceUntilIdle()
        
        // Now apply it
        viewModel.onEvent(MedicineEvent.ApplyAiResult)
        advanceUntilIdle()
        
        val state = viewModel.state.value
        assertEquals("Gemini Med", state.productName)
        assertEquals("Gemini Salts", state.salts)
        assertEquals("500mg", state.prodDNormName)
        assertEquals("Tablet", state.prodFormNormName)
        assertEquals("Headache", state.phKinetics)
        assertEquals("Take twice a day", state.recommendations)
        assertEquals("Cool dry place", state.storageConditions)
        assertEquals(null, state.aiResult)
        assertEquals(MedicineDialogState.PictureGrid, state.dialogState)
        collectJob.cancel()
    }
}
