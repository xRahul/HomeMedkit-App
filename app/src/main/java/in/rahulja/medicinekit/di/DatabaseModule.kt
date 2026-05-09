package `in`.rahulja.medicinekit.di

import `in`.rahulja.medicinekit.data.MedicineDatabase
import `in`.rahulja.medicinekit.data.repository.MedicineRepository
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val databaseModule = module {
    single { MedicineDatabase.getInstance(androidContext()) }

    single { get<MedicineDatabase>().appDAO() }

    singleOf(::MedicineRepository)
}
