package com.keephydrated.app.domain.usecase

import com.keephydrated.app.domain.model.NotificationSound
import com.keephydrated.app.domain.model.ReminderMode
import com.keephydrated.app.domain.repository.SettingsRepository
import javax.inject.Inject

class SaveUserSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    suspend fun updateGoal(goalMl: Int) {
        require(goalMl in 500..10000) { "Daily goal must be between 500ml and 10000ml" }
        settingsRepository.updateDailyGoal(goalMl)
    }

    suspend fun updateReminderInterval(hours: Int) {
        require(hours in 1..12) { "Reminder interval must be between 1 and 12 hours" }
        settingsRepository.updateReminderInterval(hours)
    }

    suspend fun updateReminderIntervalMinutes(minutes: Int) {
        require(minutes in 15..720) { "Reminder interval must be between 15 minutes and 12 hours (720 minutes)" }
        settingsRepository.updateReminderIntervalMinutes(minutes)
    }

    suspend fun updateRemindersEnabled(enabled: Boolean) {
        settingsRepository.updateRemindersEnabled(enabled)
    }

    suspend fun updateActiveHours(startHour: Int, endHour: Int) {
        require(startHour in 0..23 && endHour in 0..23) { "Hours must be between 0 and 23" }
        settingsRepository.updateActiveHours(startHour, endHour)
    }

    suspend fun updateReminderMode(mode: ReminderMode) {
        settingsRepository.updateReminderMode(mode)
    }

    suspend fun updateCustomReminderHours(hours: Set<Int>) {
        require(hours.all { it in 0..23 }) { "All selected hours must be between 0 and 23" }
        settingsRepository.updateCustomReminderHours(hours)
    }

    suspend fun updateNotificationSound(soundId: String) {
        val sound = NotificationSound.fromId(soundId)
        settingsRepository.updateNotificationSound(sound.id)
    }
}
