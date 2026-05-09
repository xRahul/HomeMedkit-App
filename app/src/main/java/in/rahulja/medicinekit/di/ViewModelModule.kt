package `in`.rahulja.medicinekit.di

import `in`.rahulja.medicinekit.models.viewModels.*
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val viewModelModule = module {
    viewModelOf(::MainViewModel)
    viewModel { (code: String?) -> AuthViewModel(code, get(), get()) }
    viewModel { (id: Long, cis: String, duplicate: Boolean, openCamera: Boolean) ->
        MedicineViewModel(id, cis, duplicate, openCamera, get(), get(), get(), get())
    }
    viewModel { (intakeId: Long, medicineId: Long) ->
        IntakeViewModel(intakeId, medicineId, get(), get(), get())
    }
    viewModelOf(::IntakesViewModel)
    viewModelOf(::MedicinesViewModel)
    viewModelOf(::ScannerViewModel)
    viewModelOf(::SettingsViewModel)
}
