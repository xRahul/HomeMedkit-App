package ru.application.homemedkit.utils.di

import androidx.work.WorkManager
import org.koin.core.context.GlobalContext
import ru.application.homemedkit.data.MedicineDatabase
import ru.application.homemedkit.receivers.AlarmSetter
import ru.application.homemedkit.utils.Preferences

val Database: MedicineDatabase get() = GlobalContext.get().get()

val Preferences: Preferences get() = GlobalContext.get().get()

val AlarmManager: AlarmSetter get() = GlobalContext.get().get()

val WorkManager: WorkManager get() = GlobalContext.get().get()
