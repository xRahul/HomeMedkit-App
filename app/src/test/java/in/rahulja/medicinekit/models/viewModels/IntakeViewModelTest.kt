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
import `in`.rahulja.medicinekit.data.dao.AppDAO
import `in`.rahulja.medicinekit.models.events.IntakeEvent
import `in`.rahulja.medicinekit.receivers.AlarmSetter
import `in`.rahulja.medicinekit.utils.AppPreferences
import `in`.rahulja.medicinekit.utils.enums.Interval
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class IntakeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val dao: AppDAO = mockk(relaxed = true)
    private val preferences: AppPreferences = mockk(relaxed = true)
    private val alarmManager: AlarmSetter = mockk(relaxed = true)
    
    private lateinit var viewModel: IntakeViewModel

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        every { preferences.isFirstLaunch } returns false
        
        // Mock android.icu.text.DecimalFormat and NumberFormat
        mockkStatic(android.icu.text.DecimalFormat::class)
        mockkStatic(android.icu.text.NumberFormat::class)
        
        viewModel = IntakeViewModel(1L, 1L, dao, preferences, alarmManager)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `initial state should be loaded from dao`() = runTest {
        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.state.collect() }
        
        // Given
        coEvery { dao.getIntakeById(1L) } returns null
        coEvery { dao.getMedicineById(1L) } returns null

        // When
        viewModel.loadData()
        advanceUntilIdle()

        // Then
        // Verification logic here
        collectJob.cancel()
    }

    @Test
    fun `SetInterval event should update state`() = runTest {
        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.state.collect() }
        
        // When
        viewModel.onEvent(IntakeEvent.SetInterval(Interval.DAILY))
        advanceUntilIdle()
        
        assertEquals("1", viewModel.state.value.interval)
        assertEquals(Interval.DAILY, viewModel.state.value.intervalType)
        
        collectJob.cancel()
    }
}
