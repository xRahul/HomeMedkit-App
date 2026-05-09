package `in`.rahulja.medicinekit.di

import `in`.rahulja.medicinekit.worker.SyncWorker
import org.koin.androidx.workmanager.dsl.worker
import org.koin.dsl.module

val workerModule = module {
    worker<SyncWorker> { SyncWorker(get(), get(), get()) }
}
