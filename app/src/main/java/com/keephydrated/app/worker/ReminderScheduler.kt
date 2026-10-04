package com.keephydrated.app.worker

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.keephydrated.app.domain.model.ReminderMode
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun scheduleReminders(
        intervalHours: Int,
        enabled: Boolean,
        mode: ReminderMode = ReminderMode.INTERVAL
    ) {
        val workManager = WorkManager.getInstance(context)

        if (!enabled) {
            workManager.cancelUniqueWork(REMINDER_WORK_NAME)
            return
        }

        val repeatInterval = if (mode == ReminderMode.CUSTOM_ROUTINE) {
            1L // Hourly checks for custom schedule
        } else {
            intervalHours.coerceAtLeast(1).toLong()
        }

        val reminderRequest = PeriodicWorkRequestBuilder<WaterReminderWorker>(
            repeatInterval,
            TimeUnit.HOURS
        ).build()

        workManager.enqueueUniquePeriodicWork(
            REMINDER_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            reminderRequest
        )
    }

    companion object {
        const val REMINDER_WORK_NAME = "hydration_reminder_work"
    }
}
