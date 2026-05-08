package `in`.rahulja.medicinekit.utils.di

import androidx.work.WorkManager
import org.koin.core.context.GlobalContext
import `in`.rahulja.medicinekit.data.MedicineDatabase
import `in`.rahulja.medicinekit.receivers.AlarmSetter
import `in`.rahulja.medicinekit.utils.Preferences

val Database: MedicineDatabase get() = GlobalContext.get().get()

val Preferences: Preferences get() = GlobalContext.get().get()

val AlarmManager: AlarmSetter get() = GlobalContext.get().get()

val WorkManager: WorkManager get() = GlobalContext.get().get()
