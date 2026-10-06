package com.keephydrated.app.domain.repository

import com.keephydrated.app.domain.model.ReminderMode
import com.keephydrated.app.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getUserSettings(): Flow<UserSettings>
    suspend fun updateDailyGoal(goalMl: Int)
    suspend fun updateReminderInterval(hours: Int)
    suspend fun updateReminderIntervalMinutes(minutes: Int)
    suspend fun updateRemindersEnabled(enabled: Boolean)
    suspend fun updateActiveHours(startHour: Int, endHour: Int)
    suspend fun updateReminderMode(mode: ReminderMode)
    suspend fun updateCustomReminderHours(hours: Set<Int>)
    suspend fun updateNotificationSound(soundId: String)
    suspend fun updateCelebrationSound(soundId: String)
    suspend fun updateDefaultQuickAddMl(amountMl: Int)
    suspend fun updateQuickAddOnNotificationClick(enabled: Boolean)

    // Language & UI Preferences
    suspend fun updateLanguage(language: String)
    suspend fun updateFrequentIntakeMl(amountMl: Int)

    // Bottle Filling Tracking Mode
    suspend fun updateBottleModeEnabled(enabled: Boolean)
    suspend fun updateBottleConfig(volumeMl: Int, durationMinutes: Int)
    suspend fun recordBottleDrink(drankAmountMl: Int)
    suspend fun refillBottle()
}
