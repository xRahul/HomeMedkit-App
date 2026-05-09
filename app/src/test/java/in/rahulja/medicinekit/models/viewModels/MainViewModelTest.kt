package `in`.rahulja.medicinekit.models.viewModels

import android.net.Uri
import androidx.work.WorkManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.flowOf
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import `in`.rahulja.medicinekit.ui.navigation.Screen
import `in`.rahulja.medicinekit.utils.AppPreferences
import `in`.rahulja.medicinekit.utils.enums.Page

class MainViewModelTest {

    private val workManager: WorkManager = mockk()
    private val preferences: AppPreferences = mockk()
    private lateinit var viewModel: MainViewModel

    @BeforeEach
    fun setup() {
        mockkStatic(Uri::class)
        every { Uri.parse(any()) } answers {
            val uriString = it.invocation.args[0] as String
            val mockUri = mockk<Uri>()
            every { mockUri.toString() } returns uriString
            
            val uriWithoutQuery = uriString.substringBefore("?")
            val parts = uriWithoutQuery.split("://")
            val path = if (parts.size > 1) parts[1] else parts[0]
            val segments = path.split("/").filter { s -> s.isNotEmpty() }
            
            every { mockUri.pathSegments } returns segments
            
            val query = uriString.substringAfter("?", "")
            val queryParamNames = if (query.isEmpty()) emptySet() else {
                query.split("&").map { p -> p.substringBefore("=") }.toSet()
            }
            every { mockUri.queryParameterNames } returns queryParamNames
            every { mockUri.getQueryParameter(any()) } answers {
                val paramName = it.invocation.args[0] as String
                query.split("&")
                    .find { p -> p.substringBefore("=") == paramName }
                    ?.substringAfter("=")
            }
            
            every { mockUri.equals(any()) } answers {
                val other = it.invocation.args[0]
                if (other is Uri) {
                    other.toString() == uriString
                } else false
            }
            mockUri
        }
        
        every { workManager.getWorkInfosForUniqueWorkFlow(any()) } returns flowOf(emptyList())
        every { preferences.isAutoSyncEnabled } returns false
        every { preferences.startPage } returns Page.MEDICINES
        
        viewModel = MainViewModel(workManager, preferences)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `getDeepLink returns start page route when data is null`() {
        val result = viewModel.getDeepLink(null)
        assertEquals(Screen.Medicines, result)
    }

    @Test
    fun `getDeepLink returns start page route when data is unknown`() {
        val mockUri = Uri.parse("https://example.com")
        
        val result = viewModel.getDeepLink(mockUri)
        assertEquals(Screen.Medicines, result)
    }
}
