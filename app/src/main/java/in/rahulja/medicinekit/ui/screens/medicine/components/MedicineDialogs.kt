@file:OptIn(ExperimentalMaterial3Api::class)

package `in`.rahulja.medicinekit.ui.screens.medicine.components

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickMultipleVisualMedia
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import `in`.rahulja.medicinekit.R
import `in`.rahulja.medicinekit.data.dto.Kit
import `in`.rahulja.medicinekit.dialogs.DraggableItem
import `in`.rahulja.medicinekit.dialogs.MonthYear
import `in`.rahulja.medicinekit.dialogs.DatePicker
import `in`.rahulja.medicinekit.dialogs.dragContainer
import `in`.rahulja.medicinekit.dialogs.rememberDragDropState
import `in`.rahulja.medicinekit.models.events.MedicineEvent
import `in`.rahulja.medicinekit.models.states.MedicineDialogState
import `in`.rahulja.medicinekit.models.states.MedicineState
import `in`.rahulja.medicinekit.ui.elements.BoxLoading
import `in`.rahulja.medicinekit.ui.elements.DialogDelete
import `in`.rahulja.medicinekit.ui.elements.DialogKits
import `in`.rahulja.medicinekit.ui.elements.IconButton
import `in`.rahulja.medicinekit.ui.elements.MedicineImage
import `in`.rahulja.medicinekit.ui.elements.VectorIcon
import `in`.rahulja.medicinekit.ui.screens.CameraPreview
import `in`.rahulja.medicinekit.utils.camera.CameraConfig
import `in`.rahulja.medicinekit.utils.camera.ImageCompressor
import `in`.rahulja.medicinekit.utils.camera.rememberCameraConfig
import `in`.rahulja.medicinekit.utils.enums.DrugType
import `in`.rahulja.medicinekit.utils.enums.ImageEditing
import `in`.rahulja.medicinekit.utils.permissions.rememberPermissionState
import java.io.File

@Composable
fun MedicineDialogs(
    state: MedicineState,
    kits: List<Kit>,
    scope: CoroutineScope,
    onBack: () -> Unit,
    filesDir: File,
    event: (MedicineEvent) -> Unit,
    onCompress: (List<Uri>, suspend (Uri) -> String?) -> Unit,
    onDelete: (File) -> Unit
) {
    val context = LocalContext.current

    when (val dialog = state.dialogState) {
        MedicineDialogState.Loading -> BoxLoading(Modifier.zIndex(10f), state.loadingMessage)

        MedicineDialogState.DataLoss -> DialogDataLoss(
            onDismiss = { event(MedicineEvent.ToggleDialog(MedicineDialogState.DataLoss)) },
            onBack = onBack
        )

        MedicineDialogState.Kits -> DialogKits(
            kits = kits,
            isChecked = { it in state.kits },
            onPick = { event(MedicineEvent.PickKit(it)) },
            onDismiss = { event(MedicineEvent.ToggleDialog(MedicineDialogState.Kits)) },
            onClear = { event(MedicineEvent.ClearKit) }
        )

        MedicineDialogState.Icons -> IconPicker(
            isEnabled = { it.value !in state.images },
            onDismiss = { event(MedicineEvent.ToggleDialog(MedicineDialogState.Icons)) },
            onPick = { event(MedicineEvent.SetIcon(it)) }
        )

        MedicineDialogState.PictureGrid -> DialogPictureGrid(state.images, state.imageEditing, event)
        MedicineDialogState.PictureChoose -> DialogPictureChoose(state.images.size, event) {
            val imageCompressor = ImageCompressor(context)
            onCompress(it, imageCompressor::compressImage)
        }

        is MedicineDialogState.FullImage -> DialogFullImage(
            images = state.images,
            initialPage = MedicineDialogState.getPage(state.dialogState),
            onDismiss = { event(MedicineEvent.ToggleDialog(MedicineDialogState.FullImage(-1))) },
            onShow = { event(MedicineEvent.ToggleDialog(MedicineDialogState.FullImage(it)))}
        )

        MedicineDialogState.TakePhoto -> CameraPhotoPreview(scope, event)

        MedicineDialogState.Date -> MonthYear(
            cancel = { event(MedicineEvent.ToggleDialog(MedicineDialogState.Date)) },
            confirm = { month, year -> event(MedicineEvent.SetExpDate(month, year)) }
        )

        MedicineDialogState.PackageDate -> DatePicker(
            onDismiss = { event(MedicineEvent.ToggleDialog(MedicineDialogState.PackageDate)) },
            onSelect = { event(MedicineEvent.SetPackageDate(it)) },
            onClear = { event(MedicineEvent.ClearPackageDate) }
        )

        MedicineDialogState.Delete -> DialogDelete(
            text = R.string.text_confirm_deletion_med,
            onCancel = { event(MedicineEvent.ToggleDialog(MedicineDialogState.Delete)) },
            onConfirm = { onDelete(filesDir) }
        )

        null -> Unit
    }
}

@Composable
fun DialogPictureChoose(
    imageCount: Int,
    event: (MedicineEvent) -> Unit,
    onPicked: (List<Uri>) -> Unit
) {
    val permissionState = rememberPermissionState(Manifest.permission.CAMERA)

    val maxItems = 5 - imageCount

    val contract = if (maxItems > 1) PickMultipleVisualMedia(maxItems)
    else PickVisualMedia()

    val picker = rememberLauncherForActivityResult(contract) { result ->
        when (result) {
            is List<*> -> {
                if (result.isEmpty() || result.size > maxItems) {
                    return@rememberLauncherForActivityResult
                }

                @Suppress("UNCHECKED_CAST")
                onPicked(result as List<Uri>)
            }

            is Uri? -> result?.let { uri ->
                onPicked(listOf(uri))
            }
        }
    }

    @Composable
    fun LocalButton(@StringRes text: Int, @DrawableRes icon: Int, onClick: () -> Unit) = ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        leadingContent = { VectorIcon(icon, Modifier.size(24.dp)) },
        headlineContent = {
            Text(
                text = stringResource(text),
                style = MaterialTheme.typography.labelLarge
            )
        },
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent,
            headlineColor = MaterialTheme.colorScheme.primary,
            leadingIconColor = MaterialTheme.colorScheme.primary
        )
    )

    AlertDialog(
        onDismissRequest = { event(MedicineEvent.ToggleDialog(MedicineDialogState.PictureChoose)) },
        dismissButton = {},
        confirmButton = {},
        title = { Text(stringResource(R.string.text_set_image)) },
        text = {
            Column {
                LocalButton(R.string.text_take_picture, R.drawable.vector_add_photo) {
                    if (permissionState.isGranted) event(MedicineEvent.ToggleDialog(MedicineDialogState.TakePhoto))
                    else if (permissionState.showRationale) permissionState.openSettings()
                    else permissionState.launchRequest()
                }
                LocalButton(R.string.text_choose_from_gallery, R.drawable.vector_add_from_gallery) {
                    picker.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
                }
                LocalButton(R.string.text_pick_icon, R.drawable.vector_medicine) {
                    event(MedicineEvent.ToggleDialog(MedicineDialogState.Icons))
                }
            }
        }
    )
}

@Composable
fun IconPicker(isEnabled: (DrugType) -> Boolean, onDismiss: () -> Unit, onPick: (String) -> Unit) = Dialog(onDismiss) {
    ElevatedCard(Modifier.padding(vertical = 64.dp)) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(128.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(DrugType.entries, DrugType::value) { type ->
                ElevatedCard(
                    enabled = isEnabled(type),
                    onClick = { onPick(type.value) },
                    colors = CardDefaults.cardColors(MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    MedicineImage(
                        image = type.icon,
                        modifier = Modifier
                            .size(128.dp)
                            .padding(16.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                    Text(
                        text = stringResource(type.title),
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.SemiBold,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}

@Composable
fun CameraPhotoPreview(scope: CoroutineScope, event: (MedicineEvent) -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val controller = rememberCameraConfig(CameraConfig.UseCases.IMAGE_CAPTURE)

    Box {
        CameraPreview(controller, Modifier.fillMaxSize())

        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            IconButton(
                onClick = { event(MedicineEvent.ToggleDialog(MedicineDialogState.TakePhoto)) },
                content = { VectorIcon(R.drawable.vector_arrow_back, Modifier, Color.White) } )

            IconButton(controller::toggleTorch) {
                VectorIcon(R.drawable.vector_flash, Modifier, Color.White)
            }
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(bottom = 24.dp)
                .align(Alignment.BottomCenter)
                .size(80.dp)
                .clip(CircleShape)
                .background(Color.White, CircleShape)
                .border(4.dp, Color.LightGray, CircleShape)
                .clickable {
                    scope.launch {
                        val image = controller.takePicture {
                            event(MedicineEvent.ToggleDialog(MedicineDialogState.Loading))
                        }

                        event(MedicineEvent.SetImage(image))
                        val useAi = `in`.rahulja.medicinekit.utils.di.Preferences.useAi
                        if (useAi && image != null) {
                            event(
                                MedicineEvent.ProcessImageWithAi(
                                    context = context,
                                    uri = android.net.Uri.fromFile(java.io.File(context.filesDir, image)),
                                    useAi = useAi,
                                    aiMode = `in`.rahulja.medicinekit.utils.di.Preferences.aiMode,
                                    apiKey = `in`.rahulja.medicinekit.utils.di.Preferences.geminiApiKey
                                )
                            )
                        }
                    }
                }
        ) {
            VectorIcon(
                icon = R.drawable.vector_add_photo,
                modifier = Modifier.size(40.dp),
                tint = Color.Black
            )
        }
    }
}

@Composable
fun DialogDataLoss(onDismiss: () -> Unit, onBack: () -> Unit) = AlertDialog(
    onDismissRequest = onDismiss,
    confirmButton = { TextButton(onBack) { Text(stringResource(R.string.text_exit)) } },
    dismissButton = { TextButton(onDismiss) { Text(stringResource(R.string.text_stay)) } },
    text = {
        Text(
            text = stringResource(R.string.text_not_saved_medicine),
            style = MaterialTheme.typography.bodyLarge
        )
    }
)

@Composable
fun DialogFullImage(images: List<String>, initialPage: Int, onDismiss: () -> Unit, onShow: (Int) -> Unit) {
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = images::size)

    LaunchedEffect(pagerState) {
        snapshotFlow(pagerState::currentPage).collectLatest(onShow)
    }

    Dialog(onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 120.dp)
        ) {
            Column(Modifier.fillMaxSize(), Arrangement.Center, Alignment.CenterHorizontally) {
                if (images.isNotEmpty()) {
                    HorizontalPager(pagerState) { page ->
                        Box(Modifier.fillMaxWidth(), Alignment.Center) {
                            MedicineImage(images[page], Modifier.size(240.dp, 340.dp))
                        }
                    }
                } else {
                    MedicineImage(null, Modifier.size(240.dp, 340.dp))
                }

                Spacer(Modifier.height(16.dp))

                PageIndicator(pagerState.pageCount, pagerState.currentPage)
            }
        }
    }
}

@Composable
fun DialogPictureGrid(images: List<String>, imageEditing: ImageEditing, event: (MedicineEvent) -> Unit) {
    val borderColor = MaterialTheme.colorScheme.onSurface

    AlertDialog(
        title = { Text(stringResource(R.string.text_images)) },
        onDismissRequest = { event(MedicineEvent.ToggleDialog(MedicineDialogState.PictureGrid)) },
        confirmButton = {
            TextButton(
                onClick = { event(MedicineEvent.ToggleDialog(MedicineDialogState.PictureGrid)) },
                content = { Text(stringResource(R.string.text_save)) }
            )
        },
        dismissButton = {
            TextButton(
                onClick = { event(MedicineEvent.EditImagesOrder) },
                content = {
                    Text(
                        text = stringResource(
                            id = when (imageEditing) {
                                ImageEditing.ADDING -> R.string.text_edit
                                ImageEditing.REORDERING -> R.string.text_add
                            }
                        )
                    )
                }
            )
        },
        text = {
            when (imageEditing) {
                ImageEditing.ADDING -> LazyVerticalGrid(
                    columns = GridCells.FixedSize(80.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(images) { image ->
                        MedicineImage(
                            image = image,
                            editable = false,
                            modifier = Modifier
                                .size(80.dp, 120.dp)
                                .border(1.dp, borderColor, MaterialTheme.shapes.medium)
                                .padding(4.dp)
                        )
                    }

                    if (images.size < 5) {
                        item {
                            Box(
                                content = { VectorIcon(R.drawable.vector_add) },
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(80.dp, 120.dp)
                                    .drawBehind {
                                        drawRoundRect(
                                            color = borderColor,
                                            cornerRadius = CornerRadius(16.dp.toPx()),
                                            style = Stroke(
                                                width = 4f,
                                                pathEffect = PathEffect.dashPathEffect(
                                                    intervals = floatArrayOf(10f, 10f),
                                                    phase = 0f
                                                )
                                            )
                                        )
                                    }
                                    .clickable {
                                        event(MedicineEvent.ToggleDialog(MedicineDialogState.PictureChoose))
                                    }
                            )
                        }
                    }
                }

                ImageEditing.REORDERING -> {
                    val listState = rememberLazyListState()
                    val dragState = rememberDragDropState(listState) { fromIndex, toIndex ->
                        event(MedicineEvent.OnImageReodering(fromIndex, toIndex))
                    }

                    LazyColumn(
                        modifier = Modifier.dragContainer(dragState),
                        state = listState,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(
                            items = images,
                            key = { _, item -> item }
                        ) { index, item ->
                            DraggableItem(dragState, index) { isDragging ->
                                val scale by animateFloatAsState(if (isDragging) 1.05f else 1.0f)
                                val elevation by animateDpAsState(if (isDragging) 8.dp else 0.dp)
                                val translationX by animateFloatAsState(if (isDragging) 20f else 0f)
                                val translationY by animateFloatAsState(if (isDragging) -10f else 0f)

                                val shape = MaterialTheme.shapes.medium

                                Box(
                                    modifier = Modifier
                                        .zIndex(if (isDragging) 1f else 0f)
                                        .graphicsLayer {
                                            this.translationX = translationX
                                            this.translationY = translationY
                                            scaleX = scale
                                            scaleY = scale
                                            shadowElevation = elevation.toPx()
                                            this.shape = shape
                                            clip = true
                                        }
                                ) {
                                    ListItem(
                                        headlineContent = {
                                            MedicineImage(
                                                image = item,
                                                editable = false,
                                                modifier = Modifier.size(64.dp)
                                            )
                                        },
                                        leadingContent = {
                                            if (images.size > 1) {
                                                IconButton(
                                                    onClick = { event(MedicineEvent.RemoveImage(item)) },
                                                    content = { VectorIcon(R.drawable.vector_delete) }
                                                )
                                            }
                                        },
                                        trailingContent = { VectorIcon(R.drawable.vector_menu) },
                                        colors = ListItemDefaults.colors(
                                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                                        ),
                                        modifier = Modifier.clip(MaterialTheme.shapes.medium)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun PageIndicator(pageCount: Int, currentPage: Int, modifier: Modifier = Modifier) {
    if (pageCount > 1) {
        Row(modifier.fillMaxWidth(), Arrangement.Center) {
            repeat(pageCount) { index ->
                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .clip(CircleShape)
                        .size(12.dp)
                        .background(
                            color = MaterialTheme.colorScheme.onSurface.copy(
                                alpha = if (currentPage == index) 0.3f
                                else 0.7f
                            )
                        )
                )
            }
        }
    }
}
