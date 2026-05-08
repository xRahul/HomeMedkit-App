package `in`.rahulja.medicinekit.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import `in`.rahulja.medicinekit.data.MedicineDatabase
import `in`.rahulja.medicinekit.utils.BLANK
import `in`.rahulja.medicinekit.utils.ID
import `in`.rahulja.medicinekit.utils.TAKEN_ID
import `in`.rahulja.medicinekit.utils.TYPE
import `in`.rahulja.medicinekit.utils.extensions.goAsync

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ActionReceiver : BroadcastReceiver(), KoinComponent {
    private val database: MedicineDatabase by inject()

    override fun onReceive(context: Context, intent: Intent) = goAsync {
        val medicineId = intent.getLongExtra(ID, 0L)
        val takenId = intent.getLongExtra(TAKEN_ID, 0L)
        val amount = intent.getDoubleExtra(BLANK, 0.0)

        NotificationManagerCompat.from(context).cancel(takenId.toInt())
        database.takenDAO().setNotified(takenId)
        if (intent.action == TYPE) {
            database.takenDAO().setTaken(takenId, true, System.currentTimeMillis())
            database.medicineDAO().intakeMedicine(medicineId, amount)
        }
    }
}