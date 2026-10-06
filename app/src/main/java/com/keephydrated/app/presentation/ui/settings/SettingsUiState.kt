package com.keephydrated.app.presentation.ui.settings

import com.keephydrated.app.domain.model.CelebrationSound
import com.keephydrated.app.domain.model.NotificationSound
import com.keephydrated.app.domain.model.ReminderMode

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
    val isLoading: Boolean = true,
    val userMessage: String? = null
)
