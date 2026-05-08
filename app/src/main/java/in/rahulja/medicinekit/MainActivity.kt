package `in`.rahulja.medicinekit

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import `in`.rahulja.medicinekit.ui.navigation.Navigation
import `in`.rahulja.medicinekit.ui.theme.AppTheme
import `in`.rahulja.medicinekit.utils.AppLocale
import `in`.rahulja.medicinekit.utils.di.AlarmManager
import `in`.rahulja.medicinekit.utils.di.Preferences
import `in`.rahulja.medicinekit.utils.extensions.showToast

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                Navigation()
            }

            LaunchedEffect(Unit) {
                if (Preferences.wasDataImported) {
                    showToast(R.string.text_success)
                    Preferences.removeImportedKey()
                    AlarmManager.resetAll()
                }
            }
        }
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(AppLocale.wrapContext(newBase))
    }
}