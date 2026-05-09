@file:OptIn(ExperimentalMaterial3Api::class)

package `in`.rahulja.medicinekit.utils.extensions

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePickerState
import androidx.compose.ui.text.intl.Locale
import `in`.rahulja.medicinekit.R
import `in`.rahulja.medicinekit.R.string.intake_text_not_taken
import `in`.rahulja.medicinekit.data.dto.IntakeTaken
import `in`.rahulja.medicinekit.data.dto.Intake as IntakeDto
import `in`.rahulja.medicinekit.data.model.IntakeUI
import `in`.rahulja.medicinekit.data.model.IntakeAmountTime
import `in`.rahulja.medicinekit.data.model.IntakeFull
import `in`.rahulja.medicinekit.data.model.IntakeList
import `in`.rahulja.medicinekit.data.model.IntakePast
import `in`.rahulja.medicinekit.data.model.IntakeSchedule
import `in`.rahulja.medicinekit.data.model.IntakeTakenFull
import `in`.rahulja.medicinekit.data.model.Schedule
import `in`.rahulja.medicinekit.data.model.ScheduleModel
import `in`.rahulja.medicinekit.data.model.TakenModel
import `in`.rahulja.medicinekit.models.states.IntakeState
import `in`.rahulja.medicinekit.models.states.TakenState
import `in`.rahulja.medicinekit.utils.Formatter
import `in`.rahulja.medicinekit.utils.ResourceText
import `in`.rahulja.medicinekit.utils.enums.IntakeExtra
import `in`.rahulja.medicinekit.utils.enums.Interval
import `in`.rahulja.medicinekit.utils.enums.Period
import `in`.rahulja.medicinekit.utils.enums.SchemaType
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.TextStyle

fun IntakeFull.toState() = IntakeState(
    adding = false,
    editing = false,
    default = true,
    isLoading = false,
    intakeId = intakeId,
    medicineId = medicineId,
    medicine = medicine,
    image = images.firstOrNull().orEmpty(),
    schemaType = schemaType,
    amountStock = medicine.prodAmount.toString(),
    sameAmount = sameAmount,
    doseType = medicine.doseType.title,
    interval = interval.toString(),
    intervalType = Interval.getValue(interval),
    period = period.toString(),
    periodType = Period.getValue(period),
    foodType = foodType,
    pickedTime = pickedTime.map { pickedTime ->
        val localTime = LocalTime.parse(pickedTime.time, Formatter.FORMAT_H_MM)
        val hour = localTime.hour
        val min = localTime.minute

        IntakeAmountTime(
            amount = pickedTime.amount.toString(),
            time = pickedTime.time,
            picker = TimePickerState(hour, min, true)
        )
    },
    pickedDays = pickedDays.sorted(),
    startDate = startDate,
    finalDate = finalDate,
    fullScreen = fullScreen,
    noSound = noSound,
    preAlarm = preAlarm,
    cancellable = cancellable,
    selectedExtras = mutableSetOf<IntakeExtra>().apply {
        if (cancellable) add(IntakeExtra.CANCELLABLE)
        if (fullScreen) add(IntakeExtra.FULLSCREEN)
        if (noSound) add(IntakeExtra.NO_SOUND)
        if (preAlarm) add(IntakeExtra.PREALARM)
    }
)

fun IntakeState.toIntake() = IntakeDto(
    intakeId = intakeId,
    medicineId = medicineId,
    interval = interval.toInt(),
    foodType = foodType,
    period = period.toInt(),
    startDate = startDate,
    finalDate = finalDate,
    schemaType = schemaType,
    sameAmount = sameAmount,
    fullScreen = fullScreen,
    noSound = noSound,
    preAlarm = preAlarm,
    cancellable = cancellable
)

fun IntakeList.toIntake(): IntakeUI {
    val timeText = if (time.size == 1) time.first()
    else time.map { LocalTime.parse(it, Formatter.FORMAT_H_MM) }
        .sorted()
        .joinToString()

    val daysText = when (schemaType) {
        SchemaType.INDEFINITELY, SchemaType.PERSONAL -> when (interval) {
            1 -> ResourceText.StringResource(R.string.text_every_day)
            7 -> ResourceText.StringResource(R.string.text_every_week)
            else -> ResourceText.PluralStringResource(R.plurals.intake_interval_in_day, interval, interval)
        }

        SchemaType.BY_DAYS -> with(days.sorted()) {
            when {
                size == DayOfWeek.entries.size -> ResourceText.StringResource(R.string.text_every_day)
                this == DayOfWeek.entries.weekdays -> ResourceText.StringResource(R.string.text_weekdays)
                this == DayOfWeek.entries.weekends -> ResourceText.StringResource(R.string.text_weekend)
                else -> ResourceText.StaticString(
                    value = joinToString { day ->
                        day.getDisplayName(TextStyle.SHORT, Locale.current.platformLocale)
                    }
                )
            }
        }
    }

    return IntakeUI(
        intakeId = intakeId,
        title = nameAlias.ifEmpty(::productName),
        image = image.firstOrNull().orEmpty(),
        time = timeText,
        days = daysText,
        interval = ResourceText.PluralStringResource(R.plurals.intake_times_a_day, time.size, time.size),
        active = LocalDate.parse(finalDate, Formatter.FORMAT_DD_MM_YYYY) >= LocalDate.now()
    )
}

fun IntakeTaken.toTakenModel() = TakenModel(
    id = takenId,
    alarmId = alarmId,
    title = productName,
    image = image,
    time = Formatter.timeFormat(trigger),
    taken = taken,
    doseAmount = ResourceText.StringResource(
        R.string.intake_text_quantity,
        formName.run {
            if (isNotEmpty()) Formatter.formFormat(this)
            else ResourceText.StringResource(R.string.text_amount)
        },
        Formatter.decimalFormat(amount),
        ResourceText.StringResource(doseType.title)
    )
)

fun Map.Entry<Long, List<IntakeTaken>>.toIntakePast(currentYear: Int) = IntakePast(
    epochDay = key,
    date = LocalDate.ofEpochDay(key).run {
        format(if (currentYear == year) Formatter.FORMAT_D_MMMM_E else Formatter.FORMAT_LONG)
    },
    intakes = value.map(IntakeTaken::toTakenModel)
)

fun Schedule.toScheduleModel() = ScheduleModel(
    id = alarmId,
    alarmId = alarmId,
    title = nameAlias.ifEmpty(::productName),
    image = image,
    time = Formatter.timeFormat(trigger),
    doseAmount = ResourceText.StringResource(
        R.string.intake_text_quantity,
        prodFormNormName.run {
            if (isNotEmpty()) Formatter.formFormat(this)
            else ResourceText.StringResource(R.string.text_amount)
        },
        Formatter.decimalFormat(amount),
        ResourceText.StringResource(doseType.title)
    )
)

fun Map.Entry<Long, List<Schedule>>.toIntakeSchedule(currentYear: Int) = IntakeSchedule(
    epochDay = key,
    date = LocalDate.ofEpochDay(key).run {
        format(if (currentYear == year) Formatter.FORMAT_D_MMMM_E else Formatter.FORMAT_LONG)
    },
    intakes = value.map(Schedule::toScheduleModel)
)

fun IntakeTakenFull.toTakenState(): TakenState {
    val triggerZoned = Formatter.getDateTime(trigger)
    val actualZoned = Formatter.getDateTime(inFact)

    return TakenState(
        takenId = takenId,
        alarmId = alarmId,
        medicine = medicine,
        productName = productName,
        amount = amount,
        date = triggerZoned.format(Formatter.FORMAT_LONG),
        scheduled = triggerZoned.format(Formatter.FORMAT_H_MM),
        actual = if (taken) ResourceText.StaticString(actualZoned.format(Formatter.FORMAT_H_MM))
        else ResourceText.StringResource(intake_text_not_taken),
        inFact = inFact,
        pickerState = actualZoned.run { TimePickerState(hour, minute, true) },
        taken = taken,
        selection = if (taken) 1 else 0,
        notified = notified
    )
}

fun TakenState?.orDefault() = this ?: TakenState()
