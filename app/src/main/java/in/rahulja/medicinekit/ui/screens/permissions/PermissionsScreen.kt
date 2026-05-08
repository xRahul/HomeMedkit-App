package `in`.rahulja.medicinekit.ui.screens.permissions

import android.Manifest
import android.annotation.SuppressLint
import android.os.Build
import android.os.Build.VERSION
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import `in`.rahulja.medicinekit.R
import `in`.rahulja.medicinekit.utils.permissions.PermissionState
import `in`.rahulja.medicinekit.utils.permissions.rememberPermissionState

@Composable
fun PermissionsScreen(onBack: () -> Unit, onFirstExit: () -> Unit = onBack) {
    @Composable
    fun ButtonGrant(permission: PermissionState) = TextButton(
        onClick = permission::launchRequest,
        enabled = !permission.isGranted,
        content = {
            Text(
                text = stringResource(
                    id = if (permission.isGranted) R.string.text_permission_granted
                    else R.string.text_permission_grant
                )
            )
        }
    )

    @Composable
    fun PermissionItem(
        permissionState: PermissionState,
        @StringRes title: Int,
        @StringRes description: Int
    ) = ListItem(
        headlineContent = { Text(stringResource(title)) },
        trailingContent = { ButtonGrant(permissionState) },
        supportingContent = {
            Text(
                text = stringResource(description),
                style = MaterialTheme.typography.bodySmall
            )
        }
    )

    val scheduleExactAlarms = rememberPermissionState(
        if (VERSION.SDK_INT >= Build.VERSION_CODES.S) Manifest.permission.SCHEDULE_EXACT_ALARM
        else "android.permission.SCHEDULE_EXACT_ALARM"
    )
    val postNotifications = rememberPermissionState(
        if (VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.POST_NOTIFICATIONS
        else "android.permission.POST_NOTIFICATIONS"
    )
    val fullScreenIntent = rememberPermissionState(
        if (VERSION.SDK_INT >= Build.VERSION_CODES.Q) Manifest.permission.USE_FULL_SCREEN_INTENT
        else "android.permission.USE_FULL_SCREEN_INTENT"
    )
    val ignoreBattery = rememberPermissionState(Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)

    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(12.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(painterResource(R.drawable.vector_bell), null, Modifier.size(64.dp))
            Text(
                text = stringResource(R.string.text_pay_attention),
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = stringResource(R.string.text_explain_request_permissions),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Column {
            if (VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PermissionItem(
                    permissionState = scheduleExactAlarms,
                    title = R.string.text_permission_title_reminders,
                    description = R.string.text_explain_reminders
                )
            }
            if (VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                PermissionItem(
                    permissionState = postNotifications,
                    title = R.string.text_permission_title_notifications,
                    description = R.string.text_explain_notifications
                )
            }
            if (VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                PermissionItem(
                    permissionState = fullScreenIntent,
                    title = R.string.text_permission_title_full_screen,
                    description = R.string.text_explain_full_screen_intent
                )
            }
            PermissionItem(
                permissionState = ignoreBattery,
                title = R.string.text_permission_title_ignore_battery,
                description = R.string.text_explain_ignore_battery
            )
        }
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            TextButton(onBack) { Text(stringResource(R.string.text_exit)) }
            Button(
                onClick = onFirstExit,
                enabled = scheduleExactAlarms.isGranted && postNotifications.isGranted,
                content = { Text(stringResource(R.string.text_save)) }
            )
        }
    }
}
