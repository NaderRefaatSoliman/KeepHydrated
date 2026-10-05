package com.keephydrated.app.worker

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.keephydrated.app.KeepHydratedApp
import com.keephydrated.app.R
import com.keephydrated.app.domain.model.NotificationSound
import com.keephydrated.app.domain.model.ReminderMode
import com.keephydrated.app.domain.repository.SettingsRepository
import com.keephydrated.app.presentation.MainActivity
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

        showReminderNotification(settings.notificationSound)
        return Result.success()
    }

    private fun showReminderNotification(soundId: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val sound = NotificationSound.fromId(soundId)
        val channelId = KeepHydratedApp.getChannelIdForSound(sound.id)
        val soundUri = KeepHydratedApp.getSoundUri(context, sound)

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(context.getString(R.string.reminder_title))
            .setContentText(context.getString(R.string.reminder_message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .apply {
                if (soundUri != null) {
                    setSound(soundUri)
                }
            }
            .build()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        const val NOTIFICATION_ID = 1001
    }
}
