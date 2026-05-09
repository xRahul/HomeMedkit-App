package `in`.rahulja.medicinekit.models.viewModels

import `in`.rahulja.medicinekit.data.dao.AppDAO
import `in`.rahulja.medicinekit.models.events.SettingsEvent
import `in`.rahulja.medicinekit.receivers.AlarmSetter
import `in`.rahulja.medicinekit.utils.AppPreferences
import `in`.rahulja.medicinekit.utils.enums.Sorting
import `in`.rahulja.medicinekit.utils.enums.Theme
import io.mockk.every
import io.mockk.mockk
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

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val preferences: AppPreferences = mockk(relaxed = true)
    private val dao: AppDAO = mockk(relaxed = true)
    private val alarmManager: AlarmSetter = mockk(relaxed = true)
    
    private lateinit var viewModel: SettingsViewModel

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        every { preferences.startPageFlow } returns flowOf(`in`.rahulja.medicinekit.utils.enums.Page.MEDICINES)
        every { preferences.sortingOrderFlow } returns flowOf(Sorting.IN_NAME)
        every { preferences.checkExpirationFlow } returns flowOf(false)
        every { preferences.theme } returns flowOf(Theme.SYSTEM)
        every { preferences.useAiFlow } returns flowOf(true)
        every { preferences.aiModeFlow } returns flowOf(`in`.rahulja.medicinekit.utils.enums.AiMode.ML_KIT)
        every { preferences.geminiApiKey } returns ""
        every { dao.getKitsFlow() } returns flowOf(emptyList())
        
        viewModel = SettingsViewModel(preferences, dao, alarmManager)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `ToggleDialog events should update state`() = runTest {
        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.state.collect() }
        
        viewModel.onEvent(SettingsEvent.ShowKits)
        assertEquals(true, viewModel.state.value.showKits)

        viewModel.onEvent(SettingsEvent.ShowKits)
        assertEquals(false, viewModel.state.value.showKits)
        
        collectJob.cancel()
    }
}
