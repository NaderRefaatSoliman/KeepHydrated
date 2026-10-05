package com.keephydrated.app.data.repository

import com.keephydrated.app.data.datastore.UserPreferencesDataStore
import com.keephydrated.app.domain.model.ReminderMode
import com.keephydrated.app.domain.model.UserSettings
import com.keephydrated.app.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val preferencesDataStore: UserPreferencesDataStore
) : SettingsRepository {

    override fun getUserSettings(): Flow<UserSettings> {
        return preferencesDataStore.userSettingsFlow
    }

    override suspend fun updateDailyGoal(goalMl: Int) {
        preferencesDataStore.updateDailyGoal(goalMl)
    }

    override suspend fun updateReminderInterval(hours: Int) {
        preferencesDataStore.updateReminderInterval(hours)
    }

    override suspend fun updateReminderIntervalMinutes(minutes: Int) {
        preferencesDataStore.updateReminderIntervalMinutes(minutes)
    }

    override suspend fun updateRemindersEnabled(enabled: Boolean) {
        preferencesDataStore.updateRemindersEnabled(enabled)
    }

    override suspend fun updateActiveHours(startHour: Int, endHour: Int) {
        preferencesDataStore.updateActiveHours(startHour, endHour)
    }

    override suspend fun updateReminderMode(mode: ReminderMode) {
        preferencesDataStore.updateReminderMode(mode)
    }

    override suspend fun updateCustomReminderHours(hours: Set<Int>) {
        preferencesDataStore.updateCustomReminderHours(hours)
    }

    override suspend fun updateNotificationSound(soundId: String) {
        preferencesDataStore.updateNotificationSound(soundId)
    }
}
