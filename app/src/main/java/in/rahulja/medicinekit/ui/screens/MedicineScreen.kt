@file:OptIn(ExperimentalMaterial3Api::class)

package `in`.rahulja.medicinekit.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest
import `in`.rahulja.medicinekit.R
import `in`.rahulja.medicinekit.models.events.MedicineAction
import `in`.rahulja.medicinekit.models.events.MedicineEvent
import `in`.rahulja.medicinekit.models.states.MedicineDialogState
import `in`.rahulja.medicinekit.models.viewModels.MedicineViewModel
import `in`.rahulja.medicinekit.ui.elements.CustomSnackbar
import `in`.rahulja.medicinekit.ui.elements.IconButton
import `in`.rahulja.medicinekit.ui.elements.NavigationIcon
import `in`.rahulja.medicinekit.ui.elements.TopBarActions
import `in`.rahulja.medicinekit.ui.elements.VectorIcon
import `in`.rahulja.medicinekit.ui.screens.medicine.components.*
import `in`.rahulja.medicinekit.utils.AppPreferences
import `in`.rahulja.medicinekit.utils.enums.AiMode
import `in`.rahulja.medicinekit.utils.extensions.medicine
import org.koin.compose.koinInject
import java.io.File

@Composable
fun MedicineScreen(model: MedicineViewModel, onBack: () -> Unit, onGoToIntake: (Long) -> Unit) {
    val resources = LocalResources.current
    val context = LocalContext.current
    val preferences: AppPreferences = koinInject()
    val filesDir = context.filesDir

    val scope = rememberCoroutineScope()

    val state by model.state.collectAsStateWithLifecycle()
    val kits by model.kits.collectAsStateWithLifecycle()

    val snackbarHost = remember(::SnackbarHostState)

    val imageReload = remember(state) {
        if (state.technical.scanned) {
            { model.fetchImages(filesDir) }
        } else null
    }

    val duplicate = remember(state) {
        if (state.technical.verified) {
            null
        } else {
            { model.onEvent(MedicineEvent.MakeDuplicate) }
        }
    }

    LaunchedEffect(model.action) {
        model.action.collectLatest { result ->
            when (result) {
                MedicineAction.OnDelete -> onBack()

                is MedicineAction.ShowSnackbar -> {
                    snackbarHost.showSnackbar(
                        visuals = CustomSnackbar(
                            message = resources.getString(result.message),
                            isError = result != MedicineAction.ShowSnackbar.OnMakeDuplicate
                        )
                    )
                }
            }
        }
    }

    BackHandler {
        if (state.dialogState == MedicineDialogState.TakePhoto) {
            model.onEvent(MedicineEvent.ToggleDialog(MedicineDialogState.TakePhoto))
        } else if (!state.default) {
            model.onEvent(MedicineEvent.ToggleDialog(MedicineDialogState.DataLoss))
        } else {
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    NavigationIcon {
                        if (state.default) onBack()
                        else model.onEvent(MedicineEvent.ToggleDialog(MedicineDialogState.DataLoss))
                    }
                },
                actions = {
                    val useAi = preferences.useAi
                    val aiMode = preferences.aiMode
                    if (useAi && aiMode == AiMode.GEMINI && state.images.isNotEmpty() && state.adding) {
                        IconButton(onClick = {
                            model.onEvent(MedicineEvent.ProcessImageWithAi(
                                Uri.fromFile(File(context.filesDir, state.images.first())),
                                true,
                                AiMode.GEMINI,
                                preferences.geminiApiKey
                            ))
                        }) {
                            VectorIcon(R.drawable.vector_refresh, tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    TopBarActions(
                        isDefault = state.default,
                        setModifiable = model::setEditing,
                        onSave = if (state.adding) model::add else model::update,
                        onShowDialog = { model.onEvent(MedicineEvent.ToggleDialog(MedicineDialogState.Delete)) },
                        onReloadImages = imageReload,
                        onDuplicate = duplicate,
                        onNavigate = { onGoToIntake(state.id) }
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHost) { data ->
                val visuals = data.visuals as CustomSnackbar

                Snackbar(
                    snackbarData = data,
                    containerColor = visuals.containerColor,
                    contentColor = visuals.contentColor,
                )
            }
        },
        floatingActionButton = {
            if (state.technical.scanned && !state.technical.verified) {
                ExtendedFloatingActionButton(
                    text = { Text(stringResource(R.string.text_update)) },
                    icon = { VectorIcon(R.drawable.vector_refresh) },
                    onClick = { model.fetch(filesDir) }
                )
            }
        }
    ) { values ->
        Crossfade(state.isLoading, label = "MedicineLoading") { isLoading ->
            if (isLoading) {
                Box(Modifier.fillMaxSize())
            } else {
                LazyColumn(
                    modifier = Modifier.imePadding(),
                    contentPadding = values.medicine(),
                    verticalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    item {
                        MedicineHeaderSection(state, model::onEvent)
                    }
                    item {
                        MedicineGeneralInfoSection(state, model::onEvent)
                    }
                    item {
                        MedicineAdditionalInfoSection(state, model::onEvent)
                    }
                    item {
                        MedicineKitsSection(state, model::onEvent)
                    }
                }
            }
        }
    }

    MedicineDialogs(
        state = state,
        kits = kits,
        scope = scope,
        onBack = onBack,
        filesDir = filesDir,
        event = model::onEvent,
        onCompress = model::compressImages,
        onDelete = { model.delete(it) }
    )
}
