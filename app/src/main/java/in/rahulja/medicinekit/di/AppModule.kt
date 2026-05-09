package `in`.rahulja.medicinekit.di

import androidx.work.WorkManager
import `in`.rahulja.medicinekit.receivers.AlarmSetter
import `in`.rahulja.medicinekit.utils.AppPreferences
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {
    single { AppPreferences.getInstance(androidContext()) }
    single { AlarmSetter.getInstance(androidContext()) }
    single { WorkManager.getInstance(androidContext()) }
}
