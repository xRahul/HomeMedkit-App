package `in`.rahulja.medicinekit.ui.elements

import androidx.annotation.DrawableRes
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import `in`.rahulja.medicinekit.R

@Composable
fun NavigationIcon(onNavigate: () -> Unit) = IconButton(onNavigate) {
    Icon(
        imageVector = ImageVector.vectorResource(R.drawable.vector_arrow_back),
        contentDescription = stringResource(R.string.cd_navigate_up),
        tint = LocalContentColor.current
    )
}

@Composable
fun VectorIcon(
    @DrawableRes icon: Int,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current
) = Icon(
    imageVector = ImageVector.vectorResource(icon),
    contentDescription = null,
    modifier = modifier,
    tint = tint
)
