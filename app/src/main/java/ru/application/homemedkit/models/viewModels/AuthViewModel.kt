package ru.application.homemedkit.models.viewModels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.application.homemedkit.R
import ru.application.homemedkit.network.AuthStatus
import ru.application.homemedkit.network.Network
import ru.application.homemedkit.network.models.auth.Token
import ru.application.homemedkit.utils.BLANK
import ru.application.homemedkit.utils.Formatter
import ru.application.homemedkit.utils.Preferences
import ru.application.homemedkit.utils.ResourceText
import ru.application.homemedkit.utils.WORK_AUTH_FIRST_TIME
import ru.application.homemedkit.utils.enums.SyncMode
import ru.application.homemedkit.utils.extensions.awaitSyncWorkResult
import ru.application.homemedkit.worker.WorkerManager

class AuthViewModel(
    private val code: String?,
    private val preferences: Preferences,
    private val workManager: WorkManager
) : BaseViewModel<AuthStatus, Unit>() {
    override fun onEvent(event: Unit) = Unit
    override fun initState() = AuthStatus.Loading

    override fun loadData() {
        when {
            preferences.token != null -> {
                if (preferences.authIsYandex) {
                    checkConnection()
                }
            }

            code != null -> getTokenYandex(code)

            else -> updateState { AuthStatus.Nothing }
        }
    }

    private val _snackbarEvent = Channel<ResourceText>()
    val snackbarEvent = _snackbarEvent.receiveAsFlow()

    val lastSync = preferences.lastSyncMillisFlow.map { value ->
        if (value == -1L) {
            ResourceText.StringResource(R.string.text_unknown)
        } else {
            ResourceText.StaticString(
                value = Formatter.dateFormat(value, Formatter.FORMAT_DD_MM_YYYY_HH_MM)
            )
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = ResourceText.StaticString(BLANK)
        )

    var showSyncModeDialog by mutableStateOf(false)
    var showExitDialog by mutableStateOf(false)
    var buttonEnabled by mutableStateOf(true)
        private set

    fun getTokenYandex(code: String) {
        viewModelScope.launch {
            updateState { AuthStatus.Loading }

            val token = Network.Yandex.getToken(code)

            if (token != null) {
                preferences.saveToken(token)
                preferences.setAuthYandex(true)

                updateState { AuthStatus.Success }

                val fileMetadata = Network.Yandex.getFileMetadata("/homemeds/data/medicines.json")
                if (fileMetadata != null) {
                    showSyncModeDialog = true
                } else {
                    syncYandex(SyncMode.AUTO)
                }
            } else {
                updateState { AuthStatus.Error }
            }
        }
    }

    fun syncYandex(mode: SyncMode) {
        buttonEnabled = false
        showSyncModeDialog = false

        val uploadWork = WorkerManager.createSyncWork(mode)
        WorkerManager.startSyncWork(workManager, WORK_AUTH_FIRST_TIME, uploadWork, ExistingWorkPolicy.KEEP)

        viewModelScope.launch {
            workManager.awaitSyncWorkResult { isSuccess ->
                val message = ResourceText.StringResource(
                    resourceId = if (isSuccess) R.string.text_sync_success
                    else R.string.text_sync_error
                )

                _snackbarEvent.send(message)
                buttonEnabled = true
            }
        }
    }

    fun checkConnection() {
        viewModelScope.launch {
            updateState { AuthStatus.Loading }

            val isSuccess = if (preferences.authIsYandex) {
                Network.Yandex.checkConnection()
            } else {
                ru.application.homemedkit.network.GoogleDriveApi.isReady()
            }

            if (isSuccess) {
                updateState { AuthStatus.Success }
            } else {
                updateState { AuthStatus.Error }
            }
        }
    }

    fun loginGoogle(account: android.accounts.Account) {
        preferences.setAuthYandex(false)
        updateState { AuthStatus.Success }
        syncYandex(SyncMode.AUTO)
    }

    fun logoutYandex() {
        viewModelScope.launch {
            updateState { AuthStatus.Loading }

            Network.Yandex.clearToken()

            preferences.saveToken(Token.empty)
            preferences.setAuthYandex(false)
            preferences.setAutoSync(false)
            preferences.updateSyncMillis(-1L)

            updateState { AuthStatus.Nothing }
            showExitDialog = false
        }
    }
}