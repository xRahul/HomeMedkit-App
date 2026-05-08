@file:OptIn(ExperimentalMaterial3Api::class)

package `in`.rahulja.medicinekit.ui.screens

import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import `in`.rahulja.medicinekit.R
import `in`.rahulja.medicinekit.R.string.text_exit_app
import `in`.rahulja.medicinekit.R.string.text_no
import `in`.rahulja.medicinekit.R.string.text_yes
import `in`.rahulja.medicinekit.data.model.MedicineList
import `in`.rahulja.medicinekit.models.viewModels.MedicinesViewModel
import `in`.rahulja.medicinekit.ui.elements.BoxWithEmptyListText
import `in`.rahulja.medicinekit.ui.elements.DialogKits
import `in`.rahulja.medicinekit.ui.elements.IconButton
import `in`.rahulja.medicinekit.ui.elements.MedicineImage
import `in`.rahulja.medicinekit.ui.elements.ScaffoldSearchBar
import `in`.rahulja.medicinekit.ui.elements.TextDate
import `in`.rahulja.medicinekit.ui.elements.VectorIcon
import `in`.rahulja.medicinekit.ui.navigation.Screen
import `in`.rahulja.medicinekit.utils.di.Preferences
import `in`.rahulja.medicinekit.utils.enums.MedicineListView
import `in`.rahulja.medicinekit.utils.enums.Sorting
import `in`.rahulja.medicinekit.utils.extensions.drawHorizontalDivider

@Composable
fun MedicinesScreen(model: MedicinesViewModel = koinViewModel(), onNavigate: (Screen) -> Unit) {
    val activity = LocalActivity.current as? ComponentActivity

    val state by model.state.collectAsStateWithLifecycle()
    val medicines by model.medicines.collectAsStateWithLifecycle()
    val grouped by model.grouped.collectAsStateWithLifecycle()
    val kits by model.kits.collectAsStateWithLifecycle()

    val pagerState = rememberPagerState(pageCount = MedicineListView.entries::size)

    val listStates = MedicineListView.entries.map { rememberLazyListState() }
    val listState = remember(state.listView.ordinal) { listStates[state.listView.ordinal] }

    val currentParams = remember(state.search, state.sorting, state.kits) {
        Triple(state.search, state.sorting, state.kits)
    }
    val oldParams = remember { mutableStateOf(currentParams) }

    LaunchedEffect(medicines) {
        if (oldParams.value != currentParams) {
            if (medicines.isNotEmpty()) {
                listState.scrollToItem(0)
            }

            oldParams.value = currentParams
        }
    }

    LaunchedEffect(state.listView) {
        pagerState.animateScrollToPage(state.listView.ordinal)
    }

    val onItemClick = remember {
        { id: Long -> onNavigate(Screen.Medicine(id)) }
    }

    val color = MaterialTheme.colorScheme.outlineVariant

    BackHandler { model.showExit(true) }
    ScaffoldSearchBar(
        search = state.search,
        onSearch = model::onSearch,
        actions = {
            IconButton(model::showSorting) { VectorIcon(R.drawable.vector_sort) }
            DropdownMenu(
                expanded = state.showSorting,
                onDismissRequest = model::showSorting
            ) {
                Text(
                    text = stringResource(R.string.preference_sorting_type),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                Sorting.entries.fastForEach { entry ->
                    DropdownMenuItem(
                        onClick = { model.setSorting(entry) },
                        text = { Text(stringResource(entry.title)) }
                    )
                }

                HorizontalDivider()

                Text(
                    text = stringResource(R.string.text_list_view),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                MedicineListView.entries.fastForEach { entry ->
                    DropdownMenuItem(
                        onClick = { model.pickView(entry) },
                        text = { Text(stringResource(entry.title)) }
                    )
                }
            }

            IconButton(model::toggleFilter) {
                BadgedBox(
                    badge = { if (state.kits.isNotEmpty()) Badge() },
                    content = { VectorIcon(R.drawable.vector_filter) }
                )
            }
        },
        floatingActionButton = {
            var expanded by remember { mutableStateOf(false) }
            Column(horizontalAlignment = Alignment.End) {
                if (expanded) {
                    FloatingActionButton(
                        onClick = { 
                            onNavigate(Screen.Scanner)
                            expanded = false
                        },
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        VectorIcon(R.drawable.vector_scanner)
                    }
                    FloatingActionButton(
                        onClick = { 
                            onNavigate(Screen.Medicine())
                            expanded = false
                        },
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        VectorIcon(R.drawable.vector_edit)
                    }
                }
                FloatingActionButton(
                    onClick = { expanded = !expanded }
                ) {
                    val rotation by animateFloatAsState(if (expanded) 45f else 0f)
                    VectorIcon(
                        icon = R.drawable.vector_add,
                        modifier = Modifier.rotate(rotation)
                    )
                }
            }
        }
    ) {
        HorizontalPager(pagerState, userScrollEnabled = false) { page ->
            when (MedicineListView.entries[page]) {
                MedicineListView.LIST -> if (medicines.isNotEmpty()) {
                    LazyColumn(Modifier.fillMaxSize(), listStates[0]) {
                        items(medicines, MedicineList::id) { medicine ->
                            MedicineItem(
                                medicine = medicine,
                                onClick = onItemClick,
                                modifier = Modifier
                                    .animateItem()
                                    .drawHorizontalDivider(color)
                            )
                        }
                    }
                } else {
                    BoxWithEmptyListText(R.string.text_no_data_found)
                }

                MedicineListView.GROUPS -> if (grouped.isNotEmpty()) {
                    LazyColumn(Modifier.fillMaxSize(), listStates[1]) {
                        grouped.fastForEach { group ->
                            item(group.kit.id) {
                                TextDate(group.kit.title.asString())
                            }

                            itemsIndexed(
                                items = group.medicines,
                                key = { _, item -> Pair(group.kit.id, item.id) }
                            ) { _, medicine ->
                                SegmentedMedicineItem(
                                    medicine = medicine,
                                    onClick = onItemClick,
                                    modifier = Modifier.animateItem()
                                )
                            }
                        }
                    }
                } else {
                    BoxWithEmptyListText(R.string.text_no_data_group_found)
                }
            }
        }
    }

    when {
        state.showFilter -> DialogKits(
            kits = kits,
            isChecked = { it in state.kits },
            onPick = model::pickFilter,
            onDismiss = model::toggleFilter,
            onClear = model::clearFilter,
            itemFilterEmpty = { ItemFilterEmptyMedicines(state.hideEmpty, model::hideEmpty) }
        )

        state.showExit -> if (!Preferences.confirmExit) activity?.finishAndRemoveTask()
        else activity?.let { DialogExit(model::showExit, it::finishAndRemoveTask) }
    }
}

@Composable
private fun MedicineItem(medicine: MedicineList, modifier: Modifier, onClick: (Long) -> Unit) =
    ListItem(
        modifier = modifier.clickable { onClick(medicine.id) },
        leadingContent = { MedicineImage(medicine.image, Modifier.size(56.dp)) },
        overlineContent = { Text(medicine.formName) },
        headlineContent = {
            Text(
                text = medicine.title,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
        },
        supportingContent = {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Text(medicine.expDateS)
                Text(medicine.prodAmountDoseType.asString())
            }
        },
        colors = ListItemDefaults.colors(
            containerColor = containerColor(
                inStock = medicine.inStock,
                isExpired = medicine.isExpired
            )
        )
    )

@Composable
private fun SegmentedMedicineItem(
    medicine: MedicineList,
    modifier: Modifier,
    onClick: (Long) -> Unit
) = ListItem(
    modifier = modifier.padding(vertical = 4.dp).clickable { onClick(medicine.id) },
    leadingContent = { MedicineImage(medicine.image, Modifier.size(56.dp)) },
    headlineContent = {
        Text(
            text = medicine.title,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
    },
    overlineContent = {
        Text(
            text = medicine.formName,
            style = MaterialTheme.typography.labelMedium
        )
    },
    supportingContent = {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
            Text(medicine.expDateS)
            Text(medicine.prodAmountDoseType.asString())
        }
    },
    colors = ListItemDefaults.colors(
        containerColor = containerColor(
            inStock = medicine.inStock,
            isExpired = medicine.isExpired
        )
    )
)

@Composable
private fun ItemFilterEmptyMedicines(isChecked: Boolean, onClick: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .toggleable(
                role = Role.Checkbox,
                value = isChecked,
                onValueChange = onClick
            )
    ) {
        Checkbox(isChecked, null)
        Text(
            text = stringResource(R.string.text_hide_empty),
            modifier = Modifier.padding(start = 16.dp),
            style = MaterialTheme.typography.bodyLarge
        )
    }

    HorizontalDivider()
}

@Composable
private fun DialogExit(onDismiss: () -> Unit, onExit: () -> Unit) =
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onExit) { Text(stringResource(text_yes)) } },
        dismissButton = { TextButton(onDismiss) { Text(stringResource(text_no)) } },
        text = {
            Text(
                text = stringResource(text_exit_app),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    )

@Composable
private fun containerColor(inStock: Boolean, isExpired: Boolean) = when {
    !inStock -> MaterialTheme.colorScheme.surfaceContainer
    isExpired -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f)
    else -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
}
