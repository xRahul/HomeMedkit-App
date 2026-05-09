package `in`.rahulja.medicinekit.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import `in`.rahulja.medicinekit.R
import `in`.rahulja.medicinekit.data.MedicineDatabase
import `in`.rahulja.medicinekit.utils.CHANNEL_ID_EXP
import `in`.rahulja.medicinekit.utils.extensions.goAsync
import `in`.rahulja.medicinekit.utils.extensions.safeNotify

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ExpirationReceiver : BroadcastReceiver(), KoinComponent {
    private val database: MedicineDatabase by inject()
    private val alarmSetter: AlarmSetter by inject()

    override fun onReceive(context: Context, intent: Intent) = goAsync {
        val inMonth = System.currentTimeMillis() + 30 * 86400000L // millis in day

        database.appDAO().getExpiredSoon(inMonth).forEach {
            NotificationManagerCompat.from(context).safeNotify(
                context = context,
                code = it.id.toInt(),
                notification = NotificationCompat.Builder(context, CHANNEL_ID_EXP)
                    .setAutoCancel(true)
                    .setCategory(NotificationCompat.CATEGORY_REMINDER)
                    .setContentText(context.getString(R.string.text_expire_soon, it.nameAlias.ifEmpty(it::productName)))
                    .setContentTitle(context.getString(R.string.text_attention))
                    .setSmallIcon(R.drawable.ic_launcher_notification)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .build()
            )
        }

        alarmSetter.checkExpiration(true)
    }
}

