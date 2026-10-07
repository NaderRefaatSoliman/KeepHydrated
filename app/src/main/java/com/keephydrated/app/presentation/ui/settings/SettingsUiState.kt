package com.keephydrated.app.presentation.ui.settings

import com.keephydrated.app.domain.model.CelebrationSound
import com.keephydrated.app.domain.model.NotificationSound
import com.keephydrated.app.domain.model.ReminderMode
import com.keephydrated.app.domain.model.UserSettings

data class SettingsUiState(
    val dailyGoalMl: Int = 2000,
    val reminderIntervalHours: Int = 2,
    val reminderIntervalMinutes: Int = 120,
    val remindersEnabled: Boolean = true,
    val startHour: Int = 8,
    val endHour: Int = 22,
    val reminderMode: ReminderMode = ReminderMode.INTERVAL,
    val customReminderHours: Set<Int> = setOf(9, 12, 15, 18, 21),
    val notificationSound: String = NotificationSound.WATER_DROP.id,
    val celebrationSound: String = CelebrationSound.CHIME_FANFARE.id,
    val defaultQuickAddMl: Int = 250,
    val quickAddOnNotificationClick: Boolean = true,
    val language: String = "en",
    val frequentIntakeMl: Int = 250,
    val bottleModeEnabled: Boolean = false,
    val bottleVolumeMl: Int = 750,
    val bottleTargetDurationMinutes: Int = 180,
    val userName: String = "",
    val userAge: Int = 28,
    val userSex: String = "male",
    val userWeightKg: Float = 70f,
    val userHeightCm: Float = 175f,
    val userActivityLevel: String = "moderate",
    val isOnboardingCompleted: Boolean = true,
    val droppyTipsEnabled: Boolean = true,
    val remindAfterGoalReached: Boolean = false,
    val isLoading: Boolean = true,
    val userMessage: String? = null
) {
    fun toUserSettings(): UserSettings {
        return UserSettings(
            dailyGoalMl = dailyGoalMl,
            reminderIntervalHours = reminderIntervalHours,
            reminderIntervalMinutes = reminderIntervalMinutes,
            remindersEnabled = remindersEnabled,
            startHour = startHour,
            endHour = endHour,
            reminderMode = reminderMode,
            customReminderHours = customReminderHours,
            notificationSound = notificationSound,
            celebrationSound = celebrationSound,
            defaultQuickAddMl = defaultQuickAddMl,
            quickAddOnNotificationClick = quickAddOnNotificationClick,
            language = language,
            frequentIntakeMl = frequentIntakeMl,
            bottleModeEnabled = bottleModeEnabled,
            bottleVolumeMl = bottleVolumeMl,
            bottleTargetDurationMinutes = bottleTargetDurationMinutes,
            userName = userName,
            userAge = userAge,
            userSex = userSex,
            userWeightKg = userWeightKg,
            userHeightCm = userHeightCm,
            userActivityLevel = userActivityLevel,
            isOnboardingCompleted = isOnboardingCompleted,
            droppyTipsEnabled = droppyTipsEnabled,
            remindAfterGoalReached = remindAfterGoalReached
        )
    }
}
