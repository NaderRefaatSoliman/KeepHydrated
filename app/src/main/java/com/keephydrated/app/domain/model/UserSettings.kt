package com.keephydrated.app.domain.model

data class UserSettings(
    val dailyGoalMl: Int = 2000,
    val reminderIntervalHours: Int = 2,
    val remindersEnabled: Boolean = true,
    val startHour: Int = 8,  // 8:00 AM
    val endHour: Int = 22     // 10:00 PM
)
