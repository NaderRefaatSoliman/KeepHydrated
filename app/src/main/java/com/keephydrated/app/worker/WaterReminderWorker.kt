package com.keephydrated.app.worker

import android.app.NotificationManager
import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.keephydrated.app.KeepHydratedApp
import com.keephydrated.app.domain.model.ReminderMode
import com.keephydrated.app.domain.model.UserSettings
import com.keephydrated.app.domain.repository.SettingsRepository
import com.keephydrated.app.notification.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.LocalTime

@HiltWorker
class WaterReminderWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val settingsRepository: SettingsRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val settings = settingsRepository.getUserSettings().first()

        if (!settings.remindersEnabled) {
            return Result.success()
        }

        val currentHour = LocalTime.now().hour

        when (settings.reminderMode) {
            ReminderMode.CUSTOM_ROUTINE -> {
                // If custom routine is active, only notify during user-selected hours
                if (currentHour !in settings.customReminderHours) {
                    return Result.success()
                }
            }
            ReminderMode.INTERVAL -> {
                // Check active / quiet hours
                val isQuietHour = if (settings.startHour <= settings.endHour) {
                    currentHour < settings.startHour || currentHour >= settings.endHour
                } else {
                    currentHour in settings.endHour until settings.startHour
                }

                if (isQuietHour) {
                    return Result.success()
                }
            }
        }

        showReminderNotification(settings)
        return Result.success()
    }

    private fun showReminderNotification(settings: UserSettings) {
        val notification = NotificationHelper.buildReminderNotification(context, settings)
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        const val NOTIFICATION_ID = KeepHydratedApp.REMINDER_NOTIFICATION_ID
    }
}
