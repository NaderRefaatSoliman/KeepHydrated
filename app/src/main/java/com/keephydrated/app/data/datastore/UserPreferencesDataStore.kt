package com.keephydrated.app.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.keephydrated.app.domain.model.ReminderMode
import com.keephydrated.app.domain.model.UserSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

@Singleton
class UserPreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val DAILY_GOAL = intPreferencesKey("daily_goal_ml")
        val REMINDER_INTERVAL = intPreferencesKey("reminder_interval_hours")
        val REMINDERS_ENABLED = booleanPreferencesKey("reminders_enabled")
        val START_HOUR = intPreferencesKey("start_hour")
        val END_HOUR = intPreferencesKey("end_hour")
        val REMINDER_MODE = stringPreferencesKey("reminder_mode")
        val CUSTOM_REMINDER_HOURS = stringSetPreferencesKey("custom_reminder_hours")
    }

    val userSettingsFlow: Flow<UserSettings> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val dailyGoal = preferences[PreferencesKeys.DAILY_GOAL] ?: 2000
            val reminderInterval = preferences[PreferencesKeys.REMINDER_INTERVAL] ?: 2
            val remindersEnabled = preferences[PreferencesKeys.REMINDERS_ENABLED] ?: true
            val startHour = preferences[PreferencesKeys.START_HOUR] ?: 8
            val endHour = preferences[PreferencesKeys.END_HOUR] ?: 22
            val modeStr = preferences[PreferencesKeys.REMINDER_MODE] ?: ReminderMode.INTERVAL.name
            val mode = try {
                ReminderMode.valueOf(modeStr)
            } catch (e: Exception) {
                ReminderMode.INTERVAL
            }
            val hoursSet = preferences[PreferencesKeys.CUSTOM_REMINDER_HOURS] ?: setOf("9", "12", "15", "18", "21")
            val customHours = hoursSet.mapNotNull { it.toIntOrNull() }.toSet()

            UserSettings(
                dailyGoalMl = dailyGoal,
                reminderIntervalHours = reminderInterval,
                remindersEnabled = remindersEnabled,
                startHour = startHour,
                endHour = endHour,
                reminderMode = mode,
                customReminderHours = if (customHours.isNotEmpty()) customHours else setOf(9, 12, 15, 18, 21)
            )
        }

    suspend fun updateDailyGoal(goalMl: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DAILY_GOAL] = goalMl
        }
    }

    suspend fun updateReminderInterval(hours: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.REMINDER_INTERVAL] = hours
        }
    }

    suspend fun updateRemindersEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.REMINDERS_ENABLED] = enabled
        }
    }

    suspend fun updateActiveHours(startHour: Int, endHour: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.START_HOUR] = startHour
            preferences[PreferencesKeys.END_HOUR] = endHour
        }
    }

    suspend fun updateReminderMode(mode: ReminderMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.REMINDER_MODE] = mode.name
        }
    }

    suspend fun updateCustomReminderHours(hours: Set<Int>) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CUSTOM_REMINDER_HOURS] = hours.map { it.toString() }.toSet()
        }
    }
}
