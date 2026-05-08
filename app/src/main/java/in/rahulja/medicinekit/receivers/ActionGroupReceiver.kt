package `in`.rahulja.medicinekit.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import `in`.rahulja.medicinekit.data.MedicineDatabase
import `in`.rahulja.medicinekit.utils.ACTION_CLOSE_ALL_FULL_SCREEN_INTENTS
import `in`.rahulja.medicinekit.utils.BLANK
import `in`.rahulja.medicinekit.utils.ID
import `in`.rahulja.medicinekit.utils.IS_ENOUGH_IN_STOCK
import `in`.rahulja.medicinekit.utils.TAKEN_ID
import `in`.rahulja.medicinekit.utils.TYPE
import `in`.rahulja.medicinekit.utils.extensions.goAsync

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ActionGroupReceiver : BroadcastReceiver(), KoinComponent {
    private val database: MedicineDatabase by inject()

    override fun onReceive(context: Context, intent: Intent) = goAsync {
        val manager = NotificationManagerCompat.from(context)

        manager.cancel(Int.MAX_VALUE)
        manager.activeNotifications
            .filter { it.packageName == context.packageName }
            .filter { it.notification.extras.containsKey(IS_ENOUGH_IN_STOCK) }
            .forEach { item ->
                val medicineId = item.notification.extras.getLong(ID)
                val takenId = item.notification.extras.getLong(TAKEN_ID)
                val amount = item.notification.extras.getDouble(BLANK)

                manager.cancel(takenId.toInt())
                database.takenDAO().setNotified(takenId)
                if (intent.action == TYPE) {
                    database.takenDAO().setTaken(takenId, true, System.currentTimeMillis())
                    database.medicineDAO().intakeMedicine(medicineId, amount)
                }
            }

        context.sendBroadcast(Intent(ACTION_CLOSE_ALL_FULL_SCREEN_INTENTS).setPackage(context.packageName))
    }
}