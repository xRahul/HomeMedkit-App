package `in`.rahulja.medicinekit.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import `in`.rahulja.medicinekit.R
import `in`.rahulja.medicinekit.data.MedicineDatabase
import `in`.rahulja.medicinekit.data.dto.IntakeTaken
import `in`.rahulja.medicinekit.utils.ALARM_ID
import `in`.rahulja.medicinekit.utils.CHANNEL_ID_PRE
import `in`.rahulja.medicinekit.utils.Formatter
import `in`.rahulja.medicinekit.utils.extensions.goAsync
import `in`.rahulja.medicinekit.utils.extensions.safeNotify

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class PreAlarmReceiver : BroadcastReceiver(), KoinComponent {
    private val database: MedicineDatabase by inject()
    private val alarmSetter: AlarmSetter by inject()

    override fun onReceive(context: Context, intent: Intent) = goAsync {
        val alarmId = intent.getLongExtra(ALARM_ID, 0L)

        val alarm = database.appDAO().getAlarmById(alarmId) ?: return@goAsync
        val intake = database.appDAO().getIntakeById(alarm.intakeId) ?: return@goAsync
        val medicine = database.appDAO().getMedicineById(intake.medicineId) ?: return@goAsync
        val image = database.appDAO().getMedicineImage(medicine.id).orEmpty()

        database.appDAO().deleteAlarm(alarm)

        val takenId = database.appDAO().insertTaken(
            taken = IntakeTaken(
                medicineId = medicine.id,
                intakeId = alarm.intakeId,
                alarmId = alarmId,
                productName = medicine.nameAlias.ifEmpty(medicine::productName),
                formName = medicine.prodFormNormName,
                amount = alarm.amount,
                doseType = medicine.doseType,
                image = image,
                trigger = alarm.trigger
            )
        )

        alarmSetter.setAlarm(takenId, alarm.trigger)

        if (alarm.preAlarm) {
            NotificationManagerCompat.from(context).safeNotify(
                context = context,
                code = alarmId.toInt(),
                notification = NotificationCompat.Builder(context, CHANNEL_ID_PRE)
                    .setCategory(NotificationCompat.CATEGORY_REMINDER)
                    .setContentTitle(context.getString(R.string.text_intake_prealarm_title))
                    .setSilent(true)
                    .setSmallIcon(R.drawable.ic_launcher_notification)
                    .setStyle(
                        NotificationCompat.BigTextStyle().bigText(
                            context.getString(
                                R.string.text_intake_prealarm_text,
                                medicine.nameAlias.ifEmpty(medicine::productName),
                                Formatter.decimalFormat(alarm.amount),
                                context.getString(medicine.doseType.title),
                                Formatter.timeFormat(alarm.trigger)
                            )
                        )
                    )
                    .setTimeoutAfter(1800000L)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .build()
            )
        }
    }
}