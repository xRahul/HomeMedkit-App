package `in`.rahulja.medicinekit.ui.elements

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun BoxWithEmptyListText(@StringRes text: Int, modifier: Modifier = Modifier) =
    Box(modifier.fillMaxSize(), Alignment.Center) {
        Text(
            text = stringResource(text),
            textAlign = TextAlign.Center
        )
    }

@Composable
fun BoxLoading(modifier: Modifier = Modifier, message: String? = null) = Box(
    content = {
        androidx.compose.foundation.layout.Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(color = androidx.compose.material3.MaterialTheme.colorScheme.primary)
            if (message != null) {
                Text(
                    text = message,
                    color = Color.White,
                    style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
        }
    },
    contentAlignment = Alignment.Center,
    modifier = modifier
        .fillMaxSize()
        .background(Color.Black.copy(alpha = 0.45f))
)