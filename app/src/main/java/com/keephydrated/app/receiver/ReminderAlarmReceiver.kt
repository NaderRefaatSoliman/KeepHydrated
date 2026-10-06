package com.keephydrated.app.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.keephydrated.app.KeepHydratedApp
import com.keephydrated.app.domain.model.ReminderMode
import com.keephydrated.app.domain.repository.SettingsRepository
import com.keephydrated.app.notification.NotificationHelper
import com.keephydrated.app.worker.ReminderScheduler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

@AndroidEntryPoint
class ReminderAlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var reminderScheduler: ReminderScheduler

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val action = intent.action
                val settings = settingsRepository.getUserSettings().first()

                if (!settings.remindersEnabled) {
                    return@launch
                }

                if (action == ACTION_BOTTLE_REFILL_ALARM) {
                    if (settings.bottleModeEnabled) {
                        NotificationHelper.showBottleRefillReminder(context, settings)
                    }
                    return@launch
                }

                val currentHour = LocalTime.now().hour
                var shouldNotify = true

                if (settings.bottleModeEnabled) {
                    val bottleStatus = settings.toBottleStatus()
                    if (bottleStatus.needsRefill) {
                        NotificationHelper.showBottleRefillReminder(context, settings)
                        shouldNotify = false
                    }
                } else {
                    when (settings.reminderMode) {
                        ReminderMode.CUSTOM_ROUTINE -> {
                            if (currentHour !in settings.customReminderHours) {
                                shouldNotify = false
                            }
                        }
                        ReminderMode.INTERVAL -> {
                            val isQuietHour = if (settings.startHour <= settings.endHour) {
                                currentHour < settings.startHour || currentHour >= settings.endHour
                            } else {
                                currentHour in settings.endHour until settings.startHour
                            }
                            if (isQuietHour) {
                                shouldNotify = false
                            }
                        }
                    }
                }

                if (shouldNotify) {
                    val notification = NotificationHelper.buildReminderNotification(context, settings)
                    val notificationManager =
                        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.notify(KeepHydratedApp.REMINDER_NOTIFICATION_ID, notification)
                }

                // Schedule next alarm
                reminderScheduler.scheduleNextAlarm(settings)
            } catch (_: Exception) {
                // Graceful fallback
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_REMINDER_ALARM = "com.keephydrated.app.action.REMINDER_ALARM"
        const val ACTION_BOTTLE_REFILL_ALARM = "com.keephydrated.app.action.BOTTLE_REFILL_ALARM"
    }
}
