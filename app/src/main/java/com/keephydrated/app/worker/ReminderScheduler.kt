package com.keephydrated.app.worker

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.keephydrated.app.domain.model.ReminderMode
import com.keephydrated.app.domain.model.UserSettings
import com.keephydrated.app.presentation.MainActivity
import com.keephydrated.app.receiver.ReminderAlarmReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun scheduleReminders(
        intervalMinutes: Int,
        enabled: Boolean,
        mode: ReminderMode = ReminderMode.INTERVAL
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val workManager = WorkManager.getInstance(context)

        val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            action = ReminderAlarmReceiver.ACTION_REMINDER_ALARM
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (!enabled) {
            alarmManager.cancel(pendingIntent)
            workManager.cancelUniqueWork(REMINDER_WORK_NAME)
            return
        }

        // 1. Calculate the exact next alarm timestamp
        val triggerAtMillis = calculateNextTriggerMillis(intervalMinutes, mode, emptySet())

        // 2. Schedule with AlarmManager for exact real-time wake-up through Doze
        scheduleExactAlarm(alarmManager, triggerAtMillis, pendingIntent)

        // 3. Resilient WorkManager fallback
        val repeatInterval = if (mode == ReminderMode.CUSTOM_ROUTINE) {
            60L
        } else {
            intervalMinutes.coerceAtLeast(15).toLong()
        }

        val reminderRequest = PeriodicWorkRequestBuilder<WaterReminderWorker>(
            repeatInterval,
            TimeUnit.MINUTES
        ).build()

        workManager.enqueueUniquePeriodicWork(
            REMINDER_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            reminderRequest
        )
    }

    fun scheduleNextAlarm(settings: UserSettings) {
        if (!settings.remindersEnabled) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            action = ReminderAlarmReceiver.ACTION_REMINDER_ALARM
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerAtMillis = calculateNextTriggerMillis(
            settings.reminderIntervalMinutes,
            settings.reminderMode,
            settings.customReminderHours
        )

        scheduleExactAlarm(alarmManager, triggerAtMillis, pendingIntent)
    }

    private fun scheduleExactAlarm(
        alarmManager: AlarmManager,
        triggerAtMillis: Long,
        pendingIntent: PendingIntent
    ) {
        val showIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val showPendingIntent = PendingIntent.getActivity(
            context,
            ALARM_CLOCK_REQUEST_CODE,
            showIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                // setAlarmClock has the highest priority in Android OS and is immune to Doze
                alarmManager.setAlarmClock(
                    AlarmManager.AlarmClockInfo(triggerAtMillis, showPendingIntent),
                    pendingIntent
                )
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        } catch (_: SecurityException) {
            // Fallback if exact alarm permission was revoked by user
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        }
    }

    private fun calculateNextTriggerMillis(
        intervalMinutes: Int,
        mode: ReminderMode,
        customHours: Set<Int>
    ): Long {
        val now = LocalDateTime.now()

        return if (mode == ReminderMode.CUSTOM_ROUTINE && customHours.isNotEmpty()) {
            val currentHour = now.hour
            // Find next hour today that is after the current time
            val upcomingToday = customHours.filter { it > currentHour }.minOrNull()
            if (upcomingToday != null) {
                val target = LocalDateTime.of(LocalDate.now(), LocalTime.of(upcomingToday, 0, 0))
                target.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            } else {
                // Next earliest hour tomorrow
                val firstHourTomorrow = customHours.minOrNull() ?: 8
                val target = LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.of(firstHourTomorrow, 0, 0))
                target.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            }
        } else {
            val safeInterval = intervalMinutes.coerceAtLeast(1)
            System.currentTimeMillis() + safeInterval * 60 * 1000L
        }
    }

    companion object {
        const val REMINDER_WORK_NAME = "hydration_reminder_work"
        const val ALARM_REQUEST_CODE = 3001
        const val ALARM_CLOCK_REQUEST_CODE = 3002
    }
}
