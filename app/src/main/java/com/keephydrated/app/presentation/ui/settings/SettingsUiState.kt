package com.keephydrated.app.presentation.ui.settings

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
    val isLoading: Boolean = true,
    val userMessage: String? = null
)
