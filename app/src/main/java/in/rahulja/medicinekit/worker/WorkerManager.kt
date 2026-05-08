package `in`.rahulja.medicinekit.worker

import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import `in`.rahulja.medicinekit.utils.SYNC_MODE
import `in`.rahulja.medicinekit.utils.WORK_AUTO_SYNC
import `in`.rahulja.medicinekit.utils.enums.SyncMode

object WorkerManager {
    fun startAutoSyncWork(workManager: WorkManager) = startSyncWork(
        workManager = workManager,
        name = WORK_AUTO_SYNC,
        work = createSyncWork(),
        policy = ExistingWorkPolicy.KEEP
    )

    fun startSyncWork(workManager: WorkManager, name: String, work: OneTimeWorkRequest, policy: ExistingWorkPolicy) {
        workManager.enqueueUniqueWork(
            uniqueWorkName = name,
            existingWorkPolicy = policy,
            request = work
        )
    }

    fun createSyncWork(mode: SyncMode = SyncMode.AUTO) = OneTimeWorkRequestBuilder<SyncWorker>()
        .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
        .setInputData(Data.Builder().putString(SYNC_MODE, mode.name).build())
        .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
        .build()
}