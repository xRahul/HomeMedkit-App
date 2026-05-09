package `in`.rahulja.medicinekit.di

import `in`.rahulja.medicinekit.domain.usecases.MedicineParserUseCase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val domainModule = module {
    single { MedicineParserUseCase(androidContext()) }
}
