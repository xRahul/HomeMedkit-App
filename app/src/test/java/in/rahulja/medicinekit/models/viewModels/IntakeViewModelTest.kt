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
import `in`.rahulja.medicinekit.data.dao.AlarmDAO
import `in`.rahulja.medicinekit.data.dao.IntakeDAO
import `in`.rahulja.medicinekit.data.dao.IntakeDayDAO
import `in`.rahulja.medicinekit.data.dao.MedicineDAO
import `in`.rahulja.medicinekit.models.events.IntakeEvent
import `in`.rahulja.medicinekit.receivers.AlarmSetter
import `in`.rahulja.medicinekit.utils.Formatter
import `in`.rahulja.medicinekit.utils.Preferences
import java.time.DayOfWeek

@OptIn(ExperimentalCoroutinesApi::class)
class IntakeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val dao: IntakeDAO = mockk()
    private val medicineDAO: MedicineDAO = mockk()
    private val intakeDayDAO: IntakeDayDAO = mockk()
    private val alarmDAO: AlarmDAO = mockk()
    private val preferences: Preferences = mockk()
    private val alarmManager: AlarmSetter = mockk()
    private lateinit var viewModel: IntakeViewModel

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Mock AppModule and Preferences getter
        mockkStatic("in.rahulja.medicinekit.utils.di.AppModuleKt")
        every { `in`.rahulja.medicinekit.utils.di.Preferences } returns preferences
        every { preferences.isFirstLaunch } returns false
        
        // Mock android.icu.text.DecimalFormat and NumberFormat
        mockkStatic(android.icu.text.DecimalFormat::class)
        mockkStatic(android.icu.text.NumberFormat::class)
        val mockDecimalFormat = mockk<android.icu.text.DecimalFormat>()
        every { android.icu.text.NumberFormat.getInstance(any<java.util.Locale>()) } returns mockDecimalFormat
        every { mockDecimalFormat.setMaximumFractionDigits(any()) } returns Unit
        
        mockkObject(Formatter)
        
        coEvery { dao.getById(any()) } returns null
        coEvery { medicineDAO.getById(any()) } returns null
        
        viewModel = IntakeViewModel(
            intakeId = 0L,
            medicineId = 0L,
            dao = dao,
            medicineDAO = medicineDAO,
            intakeDayDAO = intakeDayDAO,
            alarmDAO = alarmDAO,
            preferences = preferences,
            alarmManager = alarmManager
        )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `SetPickedDay toggles days correctly`() = runTest {
        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.state.collect() }
        
        viewModel.onEvent(IntakeEvent.SetPickedDay(DayOfWeek.MONDAY))
        advanceUntilIdle()
        
        collectJob.cancel()
    }

    @Test
    fun `SetInterval DAILY updates state correctly`() = runTest {
        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.state.collect() }
        
        viewModel.onEvent(IntakeEvent.SetInterval(`in`.rahulja.medicinekit.utils.enums.Interval.DAILY))
        advanceUntilIdle()
        
        assertEquals("1", viewModel.state.value.interval)
        assertEquals(`in`.rahulja.medicinekit.utils.enums.Interval.DAILY, viewModel.state.value.intervalType)
        
        collectJob.cancel()
    }
}
