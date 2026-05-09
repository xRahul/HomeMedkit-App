package `in`.rahulja.medicinekit.receivers

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import `in`.rahulja.medicinekit.MainActivity
import `in`.rahulja.medicinekit.R
import `in`.rahulja.medicinekit.data.MedicineDatabase
import `in`.rahulja.medicinekit.utils.ALARM_ID
import `in`.rahulja.medicinekit.utils.BLANK
import `in`.rahulja.medicinekit.utils.CHANNEL_ID_INTAKES
import `in`.rahulja.medicinekit.utils.DEEP_LINK_BASE_URL
import `in`.rahulja.medicinekit.utils.Formatter
import `in`.rahulja.medicinekit.utils.ID
import `in`.rahulja.medicinekit.utils.IS_ENOUGH_IN_STOCK
import `in`.rahulja.medicinekit.utils.TAKEN_ID
import `in`.rahulja.medicinekit.utils.TYPE
import `in`.rahulja.medicinekit.utils.extensions.goAsync
import `in`.rahulja.medicinekit.utils.extensions.safeNotify

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AlarmReceiver : BroadcastReceiver(), KoinComponent {
    private val database: MedicineDatabase by inject()
    private val alarmSetter: AlarmSetter by inject()

    override fun onReceive(context: Context, intent: Intent) = goAsync {
        val takenId = intent.getLongExtra(ALARM_ID, 0L)

        val taken = database.appDAO().getTakenById(takenId) ?: return@goAsync
        val intake = database.appDAO().getIntakeById(taken.intakeId) ?: return@goAsync
        val medicine = database.appDAO().getMedicineById(intake.medicineId) ?: return@goAsync

        val flag = medicine.prodAmount >= taken.amount

        val action = Intent(context, ActionReceiver::class.java).apply {
            putExtra(TAKEN_ID, takenId)
            putExtra(ID, intake.medicineId)
            putExtra(BLANK, taken.amount)
        }

        val pendingA = createPending(context, action, takenId)
        val pendingB = createPending(context, action.setAction(TYPE), takenId)

        val confirm = createAction(context, pendingB, R.string.intake_text_taken)
        val decline = createAction(context, pendingA, R.string.intake_text_not_taken)

        alarmSetter.setPreAlarm(intake.intakeId)

        if (!taken.notified) {
            with(NotificationManagerCompat.from(context)) {
                safeNotify(
                    context = context,
                    code = takenId.toInt(),
                    notification = commonBuilder(
                        context = context,
                        title = R.string.text_do_intake,
                        text = context.getString(
                            if (flag) R.string.text_intake_time else R.string.text_intake_amount_not_enough,
                            medicine.nameAlias.ifEmpty(medicine::productName),
                            Formatter.decimalFormat(taken.amount),
                            context.getString(medicine.doseType.title),
                            Formatter.decimalFormat(medicine.prodAmount - taken.amount)
                        ),
                        flag = flag,
                        actions = listOf(confirm, decline)
                    ).apply {
                        if (flag) {
                            addAction(confirm)
                            addAction(decline)

                            if (intake.cancellable) setTimeoutAfter(600000L)
                            if (intake.fullScreen) {
                                val uri = DEEP_LINK_BASE_URL.toUri()
                                    .buildUpon()
                                    .appendQueryParameter("takenId", takenId.toString())
                                    .appendQueryParameter("medicineId", intake.medicineId.toString())
                                    .appendQueryParameter("amount", taken.amount.toString())
                                    .build()

                                val intent = Intent(context, MainActivity::class.java).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    data = uri
                                }

                                val pendingIntent = PendingIntent.getActivity(
                                    context,
                                    takenId.toInt(),
                                    intent,
                                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                                )

                                setFullScreenIntent(pendingIntent, true)
                            }
                        }
                    }
                        .setDeleteIntent(pendingA)
                        .setSilent(intake.noSound)
                        .setExtras(
                            Bundle().apply {
                                putLong(TAKEN_ID, takenId)
                                putLong(ID, intake.medicineId)
                                putDouble(BLANK, taken.amount)
                                putBoolean(IS_ENOUGH_IN_STOCK, flag)
                            }
                        )
                        .build()
                )

                if (activeNotifications.size > 1) {
                    val flag = activeNotifications
                        .filter { it.packageName == context.packageName }
                        .filter { it.notification.extras.containsKey(IS_ENOUGH_IN_STOCK) }
                        .all { it.notification.extras.getBoolean(IS_ENOUGH_IN_STOCK) }

                    if (flag) {
                        val action = Intent(context, ActionGroupReceiver::class.java)

                        val pendingA = createPending(context, action)
                        val pendingB = createPending(context, action.setAction(TYPE))

                        val confirm = createAction(context, pendingB, R.string.text_action_intake_all_accept)
                        val decline = createAction(context, pendingA, R.string.text_action_intake_all_decline)

                        safeNotify(
                            context = context,
                            code = Int.MAX_VALUE,
                            notification = commonBuilder(
                                context = context,
                                title = R.string.text_do_intake_all,
                                text = context.getString(R.string.text_intake_sure_to_intake_all),
                                flag = true,
                                actions = listOf(confirm, decline)
                            )
                                .addAction(confirm)
                                .addAction(decline)
                                .setTimeoutAfter(600000L)
                                .build()
                        )
                    } else {
                        cancel(Int.MAX_VALUE)
                    }
                }
            }
        }
    }

    private fun createPending(context: Context, action: Intent, code: Number) = PendingIntent
        .getBroadcast(context, code.toInt(), action, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

    private fun createPending(context: Context, action: Intent) =
        createPending(context, action, Int.MAX_VALUE)

    private fun createAction(context: Context, pending: PendingIntent, icon: Int, title: Int) =
        NotificationCompat.Action.Builder(icon, context.getString(title), pending).build()

    private fun createAction(context: Context, pending: PendingIntent, title: Int) =
        createAction(context, pending, R.drawable.vector_time, title)

    private fun commonBuilder(
        context: Context,
        title: Int,
        text: String,
        flag: Boolean,
        actions: List<NotificationCompat.Action>
    ) = NotificationCompat.Builder(context, CHANNEL_ID_INTAKES)
        .setAutoCancel(false)
        .setCategory(NotificationCompat.CATEGORY_REMINDER)
        .setContentTitle(context.getString(title))
        .setSmallIcon(R.drawable.ic_launcher_notification)
        .setStyle(NotificationCompat.BigTextStyle().bigText(text))
        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        .extend(
            NotificationCompat.WearableExtender().apply {
                setContentIntentAvailableOffline(false)
                if (flag) {
                    addActions(actions)
                }
            }
        )
}