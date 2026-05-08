@file:OptIn(ExperimentalMaterial3Api::class)

package ru.application.homemedkit.ui.elements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarColors
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.application.homemedkit.R
import ru.application.homemedkit.utils.BLANK
import ru.application.homemedkit.utils.extensions.collectLatestChanged

@Composable
fun ScaffoldSearchBar(
    search: String,
    onSearch: (String) -> Unit,
    onClear: () -> Unit = { onSearch(BLANK) },
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable (RowScope.() -> Unit)? = null,
    menuRow: @Composable (() -> Unit)? = null,
    floatingActionButton: (@Composable BoxScope.() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val textFieldState = rememberTextFieldState(search)

    LaunchedEffect(textFieldState) {
        snapshotFlow { textFieldState.text.toString() }.collectLatestChanged(onSearch)
    }

    LaunchedEffect(search) {
        if (search.isEmpty()) {
            textFieldState.clearText()
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            InteractiveAppBarWithSearch(
                navigationIcon = navigationIcon,
                actions = actions,
                inputField = {
                    SearchBarDefaults.InputField(
                        state = textFieldState,
                        onSearch = onSearch,
                        expanded = false,
                        onExpandedChange = {},
                        leadingIcon = { VectorIcon(R.drawable.vector_search) },
                        placeholder = { Text(stringResource(R.string.text_enter_product_name)) },
                        trailingIcon = {
                            if (search.isNotEmpty()) {
                                IconButton(onClear) {
                                    VectorIcon(R.drawable.vector_clear)
                                }
                            }
                        }
                    )
                }
            )

            menuRow?.invoke()

            Box(
                content = { content() },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }

        floatingActionButton?.invoke(this)
    }
}

@Composable
private fun InteractiveAppBarWithSearch(
    inputField: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable (RowScope.() -> Unit)? = null,
    shape: Shape = SearchBarDefaults.inputFieldShape,
    windowInsets: WindowInsets = SearchBarDefaults.windowInsets,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(windowInsets)
    ) {
        Row(
            modifier = Modifier.padding(4.dp, 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            navigationIcon?.let {
                Box(Modifier.padding(start = 4.dp, end = 8.dp)) {
                    it()
                }
            }

            Box(Modifier.weight(1f)) {
                InteractiveSearchBar(
                    inputField = inputField,
                    shape = shape,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .widthIn(360.dp, 720.dp)
                        .fillMaxWidth()
                )
            }

            actions?.let {
                Box(Modifier.padding(start = 8.dp, end = 4.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                        content = it,
                    )
                }
            }
        }
    }
}

@Composable
private fun InteractiveSearchBar(
    inputField: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = SearchBarDefaults.inputFieldShape,
    colors: SearchBarColors = SearchBarDefaults.colors(),
    tonalElevation: Dp = SearchBarDefaults.TonalElevation,
    shadowElevation: Dp = SearchBarDefaults.ShadowElevation,
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = colors.containerColor,
        contentColor = contentColorFor(colors.containerColor),
        tonalElevation = tonalElevation,
        shadowElevation = shadowElevation,
        content = inputField,
    )
}