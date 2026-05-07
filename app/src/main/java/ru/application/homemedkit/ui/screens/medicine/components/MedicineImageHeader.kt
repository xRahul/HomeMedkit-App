package ru.application.homemedkit.ui.screens.medicine.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import ru.application.homemedkit.ui.elements.MedicineImage

@Composable
fun ProductImage(images: List<String>, isDefault: Boolean, onShow: (Int) -> Unit, onDismiss: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = images::size)

    Box(
        modifier = Modifier
            .width(128.dp)
            .fillMaxHeight()
            .border(1.dp, MaterialTheme.colorScheme.onSurface, MaterialTheme.shapes.medium)
            .clickable {
                if (isDefault) onShow(pagerState.currentPage)
                else onDismiss()
            }
    ) {
        if (images.isNotEmpty()) {
            HorizontalPager(pagerState) {
                MedicineImage(
                    image = images[it],
                    editable = !isDefault,
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(8.dp)
                )
            }
        } else {
            MedicineImage(
                image = null,
                editable = !isDefault,
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(8.dp)
            )
        }

        PageIndicator(
            pageCount = pagerState.pageCount,
            currentPage = pagerState.currentPage,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp)
        )
    }
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
