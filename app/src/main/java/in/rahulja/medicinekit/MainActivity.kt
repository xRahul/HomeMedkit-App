package `in`.rahulja.medicinekit

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import `in`.rahulja.medicinekit.receivers.AlarmSetter
import `in`.rahulja.medicinekit.ui.navigation.Navigation
import `in`.rahulja.medicinekit.ui.theme.AppTheme
import `in`.rahulja.medicinekit.utils.AppLocale
import `in`.rahulja.medicinekit.utils.AppPreferences
import `in`.rahulja.medicinekit.utils.extensions.showToast
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val preferences: AppPreferences by inject()
    private val alarmManager: AlarmSetter by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                Navigation()
            }

            LaunchedEffect(Unit) {
                if (preferences.wasDataImported) {
                    showToast(R.string.text_success)
                    preferences.removeImportedKey()
                    alarmManager.resetAll()
                }
            }
        }
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(AppLocale.wrapContext(newBase))
    }
}