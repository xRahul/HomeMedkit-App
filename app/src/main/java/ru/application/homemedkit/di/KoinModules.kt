package ru.application.homemedkit.di

import androidx.work.WorkManager
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.androidx.workmanager.dsl.worker
import org.koin.dsl.module
import ru.application.homemedkit.data.MedicineDatabase
import ru.application.homemedkit.models.viewModels.*
import ru.application.homemedkit.receivers.AlarmSetter
import ru.application.homemedkit.utils.Preferences
import ru.application.homemedkit.worker.SyncWorker

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
    viewModel { MainViewModel(get(), get()) }
    viewModel { (code: String?) -> AuthViewModel(code, get(), get()) }
    viewModel { (id: Long, cis: String, duplicate: Boolean) ->
        MedicineViewModel(id, cis, duplicate, get(), get())
    }
    viewModel { (intakeId: Long, medicineId: Long) ->
        IntakeViewModel(intakeId, medicineId, get(), get(), get(), get(), get(), get())
    }
    viewModel { IntakesViewModel(get(), get(), get(), get(), get()) }
    viewModel { MedicinesViewModel(get(), get(), get()) }
    viewModel { ScannerViewModel(get()) }
    viewModel { SettingsViewModel(get(), get()) }

    worker { SyncWorker(get(), get(), get()) }
}
