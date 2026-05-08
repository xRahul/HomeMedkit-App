@file:OptIn(ExperimentalMaterial3Api::class)

package `in`.rahulja.medicinekit.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import kotlinx.coroutines.launch
import me.zhanghai.compose.preference.ListPreference
import me.zhanghai.compose.preference.Preference
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import me.zhanghai.compose.preference.SwitchPreference
import me.zhanghai.compose.preference.preference
import me.zhanghai.compose.preference.preferenceCategory
import me.zhanghai.compose.preference.switchPreference
import me.zhanghai.compose.preference.textFieldPreference
import me.zhanghai.compose.preference.twoTargetSwitchPreference
import `in`.rahulja.medicinekit.R
import `in`.rahulja.medicinekit.data.dto.Kit
import `in`.rahulja.medicinekit.dialogs.DraggableItem
import `in`.rahulja.medicinekit.dialogs.dragContainer
import `in`.rahulja.medicinekit.dialogs.rememberDragDropState
import `in`.rahulja.medicinekit.models.events.SettingsEvent
import `in`.rahulja.medicinekit.models.viewModels.SettingsViewModel
import `in`.rahulja.medicinekit.ui.elements.IconButton
import `in`.rahulja.medicinekit.ui.elements.NavigationIcon
import `in`.rahulja.medicinekit.ui.elements.VectorIcon
import `in`.rahulja.medicinekit.ui.navigation.LocalBarVisibility
import `in`.rahulja.medicinekit.ui.screens.permissions.PermissionsScreen
import `in`.rahulja.medicinekit.ui.theme.isDynamicColorAvailable
import `in`.rahulja.medicinekit.utils.ActionHandler
import `in`.rahulja.medicinekit.utils.ActionResult
import `in`.rahulja.medicinekit.utils.KEY_AI_MODE
import `in`.rahulja.medicinekit.utils.KEY_AI_SETTINGS
import `in`.rahulja.medicinekit.utils.KEY_APP_SYSTEM
import `in`.rahulja.medicinekit.utils.KEY_APP_THEME
import `in`.rahulja.medicinekit.utils.KEY_APP_VIEW
import `in`.rahulja.medicinekit.utils.KEY_AUTO_CHANGE_INTAKE_WHEN_TIME_CHANGE
import `in`.rahulja.medicinekit.utils.KEY_AUTO_SYNC_ENABLED
import `in`.rahulja.medicinekit.utils.KEY_BASIC_SETTINGS
import `in`.rahulja.medicinekit.utils.KEY_CHECK_EXP_DATE
import `in`.rahulja.medicinekit.utils.KEY_CLEAR_CACHE
import `in`.rahulja.medicinekit.utils.KEY_CONFIRM_EXIT
import `in`.rahulja.medicinekit.utils.KEY_DOWNLOAD
import `in`.rahulja.medicinekit.utils.KEY_DYNAMIC_COLOR
import `in`.rahulja.medicinekit.utils.KEY_GEMINI_API_KEY
import `in`.rahulja.medicinekit.utils.KEY_IMPORT_EXPORT
import `in`.rahulja.medicinekit.utils.KEY_LANGUAGE
import `in`.rahulja.medicinekit.utils.KEY_START_PAGE
import `in`.rahulja.medicinekit.utils.KEY_USE_AI
import `in`.rahulja.medicinekit.utils.KEY_USE_ALARM_CLOCK
import `in`.rahulja.medicinekit.utils.KEY_USE_VIBRATION_SCAN
import `in`.rahulja.medicinekit.utils.AppLocale
import `in`.rahulja.medicinekit.utils.CLIENT_ID_YANDEX
import `in`.rahulja.medicinekit.utils.DataManager
import `in`.rahulja.medicinekit.utils.KEY_AUTOLAUNCH
import `in`.rahulja.medicinekit.utils.KEY_FIXING
import `in`.rahulja.medicinekit.utils.KEY_KITS
import `in`.rahulja.medicinekit.utils.KEY_PERMISSIONS
import `in`.rahulja.medicinekit.utils.di.AlarmManager
import `in`.rahulja.medicinekit.utils.di.Preferences
import `in`.rahulja.medicinekit.utils.enums.Page
import `in`.rahulja.medicinekit.utils.enums.Sorting
import `in`.rahulja.medicinekit.utils.enums.Theme
import `in`.rahulja.medicinekit.utils.extensions.canScheduleExactAlarms
import `in`.rahulja.medicinekit.utils.extensions.drawHorizontalDivider
import `in`.rahulja.medicinekit.utils.extensions.getLanguageList
import `in`.rahulja.medicinekit.utils.extensions.getLocalizedName
import `in`.rahulja.medicinekit.utils.extensions.openAutoStartSettings
import `in`.rahulja.medicinekit.utils.extensions.restartApplication
import `in`.rahulja.medicinekit.utils.extensions.showToast
import `in`.rahulja.medicinekit.utils.launcherExportDatabase
import `in`.rahulja.medicinekit.utils.launcherExportImages
import `in`.rahulja.medicinekit.utils.launcherImportDatabase
import `in`.rahulja.medicinekit.utils.launcherImportImages
import java.util.Locale

@Composable
fun SettingsScreen(onAuthClick: () -> Unit) {
    val context = LocalContext.current
    val barVisibility = LocalBarVisibility.current

    val underlineColor = MaterialTheme.colorScheme.outlineVariant

    val model = koinViewModel<SettingsViewModel>()
    val state by model.state.collectAsStateWithLifecycle()
    val kits by model.kits.collectAsStateWithLifecycle()

    val startPage by model.startPage.collectAsStateWithLifecycle()
    val sorting by model.sortingType.collectAsStateWithLifecycle()
    val checkExpiration by model.checkExpiration.collectAsStateWithLifecycle()
    val theme by model.theme.collectAsStateWithLifecycle()

    val useAi by model.useAi.collectAsStateWithLifecycle()
    val aiMode by model.aiMode.collectAsStateWithLifecycle()

    LaunchedEffect(state.showKits, state.showPermissions) {
        if (state.showKits || state.showPermissions) barVisibility.hide()
        else barVisibility.show()
    }

    ProvidePreferenceLocals {
        LazyColumn {
            preferenceCategory(
                key = KEY_BASIC_SETTINGS,
                title = { Text(stringResource(R.string.preference_basic_settings)) }
            )

            preference(
                key = KEY_KITS,
                title = { Text(stringResource(R.string.preference_kits_group)) },
                summary = { Text(stringResource(R.string.text_tap_to_view)) },
                onClick = { model.onEvent(SettingsEvent.ShowKits) }
            )

            item {
                ListPreference(
                    value = startPage,
                    onValueChange = Preferences::setStartPage,
                    values = Page.entries,
                    title = { Text(stringResource(R.string.preference_start_page)) },
                    summary = { Text(stringResource(startPage.title)) },
                    valueToText = { AnnotatedString(stringResource(it.title)) }
                )
            }

            item {
                ListPreference(
                    value = sorting,
                    onValueChange = Preferences::setSortingType,
                    values = Sorting.entries,
                    title = { Text(stringResource(R.string.preference_sorting_type)) },
                    summary = { Text(stringResource(sorting.title)) },
                    valueToText = { AnnotatedString(stringResource(it.title)) }
                )
            }

            switchPreference(
                key = KEY_CONFIRM_EXIT,
                defaultValue = true,
                title = { Text(stringResource(R.string.preference_confirm_exit)) },
                summary = { Text(stringResource(if (it) R.string.text_on else R.string.text_off)) }
            )

            switchPreference(
                key = KEY_USE_VIBRATION_SCAN,
                defaultValue = false,
                title = { Text(stringResource(R.string.preference_use_vibration_scan)) },
                summary = { Text(stringResource(if (it) R.string.text_on else R.string.text_off)) }
            )

            switchPreference(
                key = KEY_DOWNLOAD,
                defaultValue = true,
                title = { Text(stringResource(R.string.preference_download_images)) },
                summary = { Text(stringResource(if (it) R.string.text_on else R.string.text_off)) }
            )

            item {
                SwitchPreference(
                    value = checkExpiration,
                    onValueChange = Preferences::setCheckExpDate,
                    title = { Text(stringResource(R.string.preference_check_expiration_date)) },
                    summary = { Text(stringResource(if (checkExpiration) R.string.text_daily_at else R.string.text_off)) }
                )
            }

            switchPreference(
                key = KEY_USE_ALARM_CLOCK,
                defaultValue = false,
                enabled = { context.canScheduleExactAlarms() },
                title = { Text(stringResource(R.string.preference_use_alarm_clock)) },
                summary = {
                    Text(
                        text = stringResource(
                            id = when {
                                context.canScheduleExactAlarms() -> if (it) R.string.text_on else R.string.text_off
                                else -> R.string.text_explain_disabled
                            }
                        )
                    )
                }
            )

            switchPreference(
                key = KEY_AUTO_CHANGE_INTAKE_WHEN_TIME_CHANGE,
                defaultValue = false,
                title = { Text(stringResource(R.string.preference_auto_intake_change_on_time_change)) },
                summary = { Text(stringResource(if (it) R.string.text_on else R.string.text_off)) }
            )

            preferenceCategory(
                key = KEY_AI_SETTINGS,
                title = { Text(stringResource(R.string.preference_ai_settings)) }
            )

            switchPreference(
                key = KEY_USE_AI,
                defaultValue = false,
                title = { Text(stringResource(R.string.preference_use_ai)) },
                summary = { Text(stringResource(if (it) R.string.text_on else R.string.text_off)) }
            )

            if (useAi) {
                preference(
                    key = KEY_AI_MODE,
                    title = { Text(stringResource(R.string.preference_ai_mode)) },
                    summary = {
                        Text(stringResource(aiMode.title))
                    },
                    onClick = {
                        val newMode = if (aiMode == `in`.rahulja.medicinekit.utils.enums.AiMode.ML_KIT) `in`.rahulja.medicinekit.utils.enums.AiMode.GEMINI else `in`.rahulja.medicinekit.utils.enums.AiMode.ML_KIT
                        Preferences.setAiMode(newMode)
                    }
                )

                if (aiMode == `in`.rahulja.medicinekit.utils.enums.AiMode.GEMINI) {
                    textFieldPreference(
                        key = KEY_GEMINI_API_KEY,
                        defaultValue = model.geminiApiKey,
                        title = { Text(stringResource(R.string.preference_gemini_api_key)) },
                        textToValue = { it },
                        summary = { Text(if (it.isBlank()) stringResource(R.string.text_empty) else "••••••••") }
                    )
                }
            }

            preferenceCategory(
                key = KEY_APP_VIEW,
                title = { Text(stringResource(R.string.preference_app_view)) },
                modifier = Modifier.drawHorizontalDivider(
                    color = underlineColor,
                    start = { Offset(0f, 0f) },
                    end = { Offset(size.width, 0f) }
                )
            )

            item {
                if (VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                    val activity = LocalActivity.current as? ComponentActivity ?: return@item
                    val localeList = remember { context.getLanguageList() }
                    val locale = LocalConfiguration.current.locales[0].toLanguageTag()

                    var value by remember { mutableStateOf(Preferences.language ?: locale) }

                    ListPreference(
                        value = value,
                        onValueChange = { value = it; AppLocale.setLocale(activity, it) },
                        values = localeList,
                        title = { Text(stringResource(R.string.preference_language)) },
                        summary = { Text(Locale.forLanguageTag(value).getLocalizedName()) },
                        valueToText = { AnnotatedString(Locale.forLanguageTag(it).getLocalizedName()) }
                    )
                } else Preference(
                    title = { Text(stringResource(R.string.preference_language)) },
                    onClick = {
                        context.startActivity(
                            Intent(Settings.ACTION_APP_LOCALE_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                        )
                    }
                )
            }

            item {
                ListPreference(
                    value = theme,
                    onValueChange = Preferences::setTheme,
                    values = Theme.entries,
                    title = { Text(stringResource(R.string.preference_app_theme)) },
                    summary = { Text(stringResource(theme.title)) },
                    valueToText = { AnnotatedString(stringResource(it.title)) }
                )
            }

            switchPreference(
                key = KEY_DYNAMIC_COLOR,
                defaultValue = false,
                title = { Text(stringResource(R.string.preference_dynamic_color)) },
                enabled = { isDynamicColorAvailable() }
            )

            preferenceCategory(
                key = KEY_APP_SYSTEM,
                title = { Text(stringResource(R.string.preference_system)) },
                modifier = Modifier.drawHorizontalDivider(
                    color = underlineColor,
                    start = { Offset(0f, 0f) },
                    end = { Offset(size.width, 0f) }
                )
            )

            if (CLIENT_ID_YANDEX.isNotBlank()) {
                twoTargetSwitchPreference(
                    key = KEY_AUTO_SYNC_ENABLED,
                    defaultValue = false,
                    onClick = { onAuthClick() },
                    switchEnabled = { Preferences.token != null },
                    title = { Text(stringResource(R.string.text_sync)) },
                    summary = { enabled ->
                        Text(
                            text = stringResource(
                                id = if (enabled) R.string.preference_auto_sync_summary_enabled
                                else R.string.preference_auto_sync_summary_disabled
                            )
                        )
                    }
                )
            }

            preference(
                key = KEY_PERMISSIONS,
                title = { Text(stringResource(R.string.preference_permissions)) },
                summary = { Text(stringResource(R.string.text_tap_to_view)) },
                onClick = { model.onEvent(SettingsEvent.ShowPermissions) }
            )

            preference(
                key = KEY_IMPORT_EXPORT,
                title = { Text(stringResource(R.string.preference_import_export)) },
                onClick = { model.onEvent(SettingsEvent.ShowExport) }
            )

            preference(
                key = KEY_FIXING,
                title = { Text(stringResource(R.string.preference_fixing_notifications)) },
                onClick = { model.onEvent(SettingsEvent.ShowFixing) }
            )

            preference(
                key = KEY_AUTOLAUNCH,
                title = { Text(stringResource(R.string.preference_system_app_autolaunch)) },
                onClick = {
                    val launched = context.openAutoStartSettings()
                    if (!launched) {
                        context.showToast(R.string.text_toast_autostart_not_found)
                    }
                }
            )

            preference(
                key = KEY_CLEAR_CACHE,
                title = { Text(stringResource(R.string.preference_clear_app_cache)) },
                onClick = { model.onEvent(SettingsEvent.ShowClearing) }
            )
        }
    }
    
    KitsManager(
        isVisible = state.showKits,
        kits = kits,
        onSave = model::saveKitsPosition,
        onUpsert = model::upsertKit,
        onDelete = model::deleteKit,
        onBack = { model.onEvent(SettingsEvent.ShowKits) }
    )

    AnimatedVisibility(
        visible = state.showPermissions,
        enter = scaleIn(),
        exit = scaleOut()
    ) {
        BackHandler(state.showPermissions) { model.onEvent(SettingsEvent.ShowPermissions) }
        PermissionsScreen(onBack = { model.onEvent(SettingsEvent.ShowPermissions) })
    }

    when {
        state.showExport -> DialogData(model::onDataAction) { model.onEvent(SettingsEvent.ShowExport) }
        state.showFixing -> DialogFixingNotifications { model.onEvent(SettingsEvent.ShowFixing) }
        state.showClearing -> DialogClearing(model::onDataAction) { model.onEvent(SettingsEvent.ShowClearing) }
    }
}

@Composable
private fun KitsManager(
    isVisible: Boolean,
    kits: List<Kit>,
    onSave: (List<Kit>) -> Unit,
    onUpsert: (Kit) -> Unit,
    onDelete: (Kit) -> Unit,
    onBack: () -> Unit
) {
    var list by remember { mutableStateOf(emptyList<Kit>()) }
    var kit by remember { mutableStateOf(Kit(position = list.size.toLong())) }

    var ordering by rememberSaveable { mutableStateOf(false) }
    var show by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(kits) {
        list = kits
        kit = Kit(position = list.size.toLong())
    }

    val listState = rememberLazyListState()
    val dragState = rememberDragDropState(listState) { fromIndex, toIndex ->
        list = list.toMutableList().apply { add(toIndex, removeAt(fromIndex)) }
    }

    BackHandler(isVisible, onBack)
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInHorizontally { it },
        exit = slideOutHorizontally { it }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.preference_kits_group)) },
                    navigationIcon = { NavigationIcon(onBack) },
                    actions = {
                        if (ordering) {
                            IconButton(
                                content = { VectorIcon(R.drawable.vector_confirm) },
                                onClick = {
                                    onSave(list)
                                    ordering = false
                                }
                            )
                        } else {
                            IconButton(
                                onClick = { ordering = true },
                                content = { VectorIcon(R.drawable.vector_sort) }
                            )
                        }
                    }
                )
            },
            floatingActionButton = {
                if (!ordering) {
                    ExtendedFloatingActionButton(
                        text = { Text(stringResource(R.string.text_add)) },
                        icon = { VectorIcon(R.drawable.vector_add) },
                        onClick = { show = true },
                    )
                }
            }
        ) { values ->
            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(values)
                    .then(if (ordering) Modifier.dragContainer(dragState) else Modifier)
            ) {
                itemsIndexed(
                    items = list,
                    key = { _, item -> item.kitId }
                ) { index, item ->
                    DraggableItem(dragState, index) { isDragging ->
                        val scale by animateFloatAsState(if (isDragging) 1.05f else 1.0f)
                        val elevation by animateDpAsState(if (isDragging) 8.dp else 0.dp)
                        val translationX by animateFloatAsState(if (isDragging) 20f else 0f)
                        val translationY by animateFloatAsState(if (isDragging) -10f else 0f)

                        Box(
                            modifier = Modifier
                                .zIndex(if (isDragging) 1f else 0f)
                                .graphicsLayer {
                                    this.translationX = translationX
                                    this.translationY = translationY
                                    scaleX = scale
                                    scaleY = scale
                                    shadowElevation = elevation.toPx()
                                    shape = RoundedCornerShape(12.dp)
                                    clip = true
                                }
                        ) {
                            ListItem(
                                headlineContent = { Text(item.title) },
                                leadingContent = {
                                    if (!ordering) {
                                        IconButton(
                                            content = { VectorIcon(R.drawable.vector_edit) },
                                            onClick = {
                                                kit = Kit(item.kitId, item.title, item.position)
                                                show = true
                                            }
                                        )
                                    }
                                },
                                trailingContent = {
                                    if (ordering) {
                                        VectorIcon(R.drawable.vector_menu)
                                    } else {
                                        IconButton(
                                            onClick = { onDelete(item) },
                                            content = { VectorIcon(R.drawable.vector_delete) }
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        if (show) {
            AlertDialog(
                title = { Text(stringResource(R.string.text_kit_title)) },
                onDismissRequest = { show = false },
                confirmButton = {
                    TextButton(
                        enabled = kit.title.isNotBlank(),
                        content = { Text(stringResource(R.string.text_save)) },
                        onClick = {
                            onUpsert(kit)
                            show = false
                        }
                    )
                },
                dismissButton = {
                    TextButton(
                        content = { Text(stringResource(R.string.text_cancel)) },
                        onClick = {
                            show = false
                            kit = Kit(position = list.size.toLong())
                        }
                    )
                },
                text = {
                    OutlinedTextField(
                        value = kit.title,
                        onValueChange = { kit = kit.copy(title = it) },
                        keyboardOptions = KeyboardOptions(KeyboardCapitalization.Sentences)
                    )
                }
            )
        }
    }
}

@Composable
private fun DialogData(onAction: ActionHandler, onDismiss: () -> Unit) {
    @Composable
    fun LocalButton(@StringRes text: Int, onClick: () -> Unit) = ListItem(
        headlineContent = { Text(stringResource(text)) },
        modifier = Modifier.clickable(onClick = onClick),
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent,
            headlineColor = MaterialTheme.colorScheme.primary,
            leadingIconColor = MaterialTheme.colorScheme.primary
        )
    )

    val exportImages = launcherExportImages(onAction)
    val importImages = launcherImportImages(onAction)

    val exportDatabase = launcherExportDatabase(onAction)
    val importDatabase = launcherImportDatabase(onAction)

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onDismiss) { Text(stringResource(R.string.text_exit)) } },
        title = {
            Row(Modifier.fillMaxWidth(), Arrangement.Center) {
                Text(stringResource(R.string.preference_import_export))
            }
        },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.text_import_data_description),
                    modifier = Modifier.padding(16.dp, 8.dp)
                )

                HorizontalDivider()

                LocalButton(R.string.text_import_database, importDatabase::launch)
                LocalButton(R.string.text_import_images, importImages::launch)

                HorizontalDivider()

                LocalButton(R.string.text_export_database, exportDatabase::launch)
                LocalButton(R.string.text_export_images, exportImages::launch)
            }
        }
    )
}

@Composable
private fun DialogFixingNotifications(onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    fun onFix() {
        scope.launch {
            launch { AlarmManager.resetAll() }.join()
            context.restartApplication()
        }
    }

    AlertDialog(
        onDismissRequest = onBack,
        dismissButton = { TextButton(onBack) { Text(stringResource(R.string.text_cancel)) } },
        confirmButton = { TextButton(::onFix) { Text(stringResource(R.string.text_confirm)) } },
        title = { Text(stringResource(R.string.text_attention)) },
        text = {
            Text(
                text = stringResource(R.string.text_fix_notifications),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    )
}

@Composable
private fun DialogClearing(actionHandler: ActionHandler, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val onClear = {
        actionHandler.handle(
            actionResult = ActionResult(
                onAction = { DataManager.clearCache(context) },
                onResult = { isSuccess ->
                    if (isSuccess == true) {
                        context.showToast(R.string.text_success)
                    }
                    onDismiss()
                }
            )
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        dismissButton = { TextButton(onDismiss) { Text(stringResource(R.string.text_cancel)) } },
        confirmButton = { TextButton(onClear) { Text(stringResource(R.string.text_confirm)) } },
        title = { Text(stringResource(R.string.text_attention)) },
        text = {
            Text(
                text = stringResource(R.string.text_clear_app_cache_description),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    )
}
