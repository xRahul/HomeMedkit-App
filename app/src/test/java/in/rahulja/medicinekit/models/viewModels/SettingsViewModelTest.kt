package `in`.rahulja.medicinekit.models.viewModels

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
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import `in`.rahulja.medicinekit.data.dao.KitDAO
import `in`.rahulja.medicinekit.models.events.SettingsEvent
import `in`.rahulja.medicinekit.utils.Preferences

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val preferences: Preferences = mockk()
    private val kitDAO: KitDAO = mockk()
    private lateinit var viewModel: SettingsViewModel

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        every { preferences.startPageFlow } returns flowOf()
        every { preferences.sortingOrderFlow } returns flowOf()
        every { preferences.checkExpirationFlow } returns flowOf()
        every { preferences.theme } returns flowOf()
        every { preferences.useAiFlow } returns flowOf()
        every { preferences.aiModeFlow } returns flowOf()
        every { preferences.geminiApiKey } returns ""
        every { kitDAO.getFlow() } returns flowOf(emptyList())
        
        viewModel = SettingsViewModel(preferences, kitDAO)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `ShowClearing toggles state correctly`() = runTest {
        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.state.collect() }
        
        viewModel.onEvent(SettingsEvent.ShowClearing)
        advanceUntilIdle()
        assertTrue(viewModel.state.value.showClearing)
        
        viewModel.onEvent(SettingsEvent.ShowClearing)
        advanceUntilIdle()
        assertEquals(false, viewModel.state.value.showClearing)
        
        collectJob.cancel()
    }

    @Test
    fun `ShowKits toggles state correctly`() = runTest {
        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.state.collect() }
        
        viewModel.onEvent(SettingsEvent.ShowKits)
        advanceUntilIdle()
        assertTrue(viewModel.state.value.showKits)
        
        collectJob.cancel()
    }
}
