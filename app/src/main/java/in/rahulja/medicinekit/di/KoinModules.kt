package `in`.rahulja.medicinekit.di

import androidx.work.WorkManager
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.dsl.worker
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import `in`.rahulja.medicinekit.data.MedicineDatabase
import `in`.rahulja.medicinekit.models.viewModels.*
import `in`.rahulja.medicinekit.receivers.AlarmSetter
import `in`.rahulja.medicinekit.utils.Preferences
import `in`.rahulja.medicinekit.worker.SyncWorker

val appModule = module {
    single { MedicineDatabase.getInstance(androidContext()) }
    single { Preferences.getInstance(androidContext()) }
    single { AlarmSetter.getInstance(androidContext()) }
    single { WorkManager.getInstance(androidContext()) }

    // DAOs
    single { get<MedicineDatabase>().medicineDAO() }
    single { get<MedicineDatabase>().intakeDAO() }
    single { get<MedicineDatabase>().alarmDAO() }
    single { get<MedicineDatabase>().intakeDayDAO() }
    single { get<MedicineDatabase>().kitDAO() }
    single { get<MedicineDatabase>().takenDAO() }

    // ViewModels
    viewModelOf(::MainViewModel)
    viewModel { (code: String?) -> AuthViewModel(code, get(), get()) }
    viewModel { (id: Long, cis: String, duplicate: Boolean, openCamera: Boolean) ->
        MedicineViewModel(id, cis, duplicate, openCamera, get(), get())
    }
    viewModel { (intakeId: Long, medicineId: Long) ->
        IntakeViewModel(intakeId, medicineId, get(), get(), get(), get(), get(), get())
    }
    viewModelOf(::IntakesViewModel)
    viewModelOf(::MedicinesViewModel)
    viewModelOf(::ScannerViewModel)
    viewModelOf(::SettingsViewModel)

    worker { SyncWorker(get(), get(), get()) }
}
