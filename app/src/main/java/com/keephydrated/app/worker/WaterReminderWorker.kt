package com.keephydrated.app.worker

import android.app.NotificationManager
import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.keephydrated.app.KeepHydratedApp
import com.keephydrated.app.domain.model.ReminderMode
import com.keephydrated.app.domain.model.UserSettings
import com.keephydrated.app.domain.repository.HydrationRepository
import com.keephydrated.app.domain.repository.SettingsRepository
import com.keephydrated.app.domain.util.HydrationCalculator
import com.keephydrated.app.notification.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalTime

@HiltWorker
class WaterReminderWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val settingsRepository: SettingsRepository,
    private val hydrationRepository: HydrationRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val settings = settingsRepository.getUserSettings().first()

        if (!settings.remindersEnabled) {
            return Result.success()
        }

        // Calculate today's intake and clinical safe limits
        val today = LocalDate.now()
        val todayIntakes = hydrationRepository.getIntakesForDate(today).first()
        val totalIntakeMl = todayIntakes.sumOf { it.amountMl }

        val rec = HydrationCalculator.calculate(
            age = settings.userAge,
            sex = settings.userSex,
            weightKg = settings.userWeightKg,
            heightCm = settings.userHeightCm,
            activityLevel = settings.userActivityLevel
        )
        val safeLimitMl = rec.safeMaxDailyMl

        // SAFETY MANDATE 1: If safe max limit is reached or exceeded, suppress reminders!
        if (totalIntakeMl >= safeLimitMl) {
            return Result.success()
        }

        // SAFETY MANDATE 2: If daily goal is reached and user has not enabled remindAfterGoalReached, suppress reminders!
        if (settings.dailyGoalMl > 0 && totalIntakeMl >= settings.dailyGoalMl && !settings.remindAfterGoalReached) {
            return Result.success()
        }

        if (settings.bottleModeEnabled) {
            val bottleStatus = settings.toBottleStatus()
            if (bottleStatus.needsRefill) {
                NotificationHelper.showBottleRefillReminder(context, settings)
                return Result.success()
            }
        } else {
            val currentHour = LocalTime.now().hour

            when (settings.reminderMode) {
                ReminderMode.CUSTOM_ROUTINE -> {
                    if (currentHour !in settings.customReminderHours) {
                        return Result.success()
                    }
                }
                ReminderMode.INTERVAL -> {
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
