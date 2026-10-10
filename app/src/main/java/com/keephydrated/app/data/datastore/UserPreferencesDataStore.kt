package com.keephydrated.app.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.keephydrated.app.domain.model.CelebrationSound
import com.keephydrated.app.domain.model.NotificationSound
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
        val REMINDER_INTERVAL_MINUTES = intPreferencesKey("reminder_interval_minutes")
        val REMINDERS_ENABLED = booleanPreferencesKey("reminders_enabled")
        val START_HOUR = intPreferencesKey("start_hour")
        val END_HOUR = intPreferencesKey("end_hour")
        val REMINDER_MODE = stringPreferencesKey("reminder_mode")
        val CUSTOM_REMINDER_HOURS = stringSetPreferencesKey("custom_reminder_hours")
        val NOTIFICATION_SOUND = stringPreferencesKey("notification_sound")
        val CELEBRATION_SOUND = stringPreferencesKey("celebration_sound")
        val DEFAULT_QUICK_ADD_ML = intPreferencesKey("default_quick_add_ml")
        val QUICK_ADD_ON_NOTIFICATION_CLICK = booleanPreferencesKey("quick_add_on_notification_click")

        // Enhancements: Language, Frequent Intake, Bottle Tracking Mode
        val LANGUAGE = stringPreferencesKey("app_language")
        val FREQUENT_INTAKE_ML = intPreferencesKey("frequent_intake_ml")
        val BOTTLE_MODE_ENABLED = booleanPreferencesKey("bottle_mode_enabled")
        val BOTTLE_VOLUME_ML = intPreferencesKey("bottle_volume_ml")
        val BOTTLE_TARGET_DURATION_MINUTES = intPreferencesKey("bottle_target_duration_minutes")
        val BOTTLE_START_TIME_MILLIS = longPreferencesKey("bottle_start_time_millis")
        val BOTTLE_DRANK_ML = intPreferencesKey("bottle_drank_ml")
        val BOTTLE_REFILL_COUNT = intPreferencesKey("bottle_refill_count")

        // Personalized Profile & Onboarding
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_AGE = intPreferencesKey("user_age")
        val USER_SEX = stringPreferencesKey("user_sex")
        val USER_WEIGHT_KG = floatPreferencesKey("user_weight_kg")
        val USER_HEIGHT_CM = floatPreferencesKey("user_height_cm")
        val USER_ACTIVITY_LEVEL = stringPreferencesKey("user_activity_level")
        val IS_ONBOARDING_COMPLETED = booleanPreferencesKey("is_onboarding_completed")
        val IS_ONBOARDING_SKIPPED = booleanPreferencesKey("is_onboarding_skipped")
        val DROPPY_TIPS_ENABLED = booleanPreferencesKey("droppy_tips_enabled")
        val REMIND_AFTER_GOAL_REACHED = booleanPreferencesKey("remind_after_goal_reached")
        val THEME_MODE = stringPreferencesKey("app_theme_mode")
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
            val legacyHours = preferences[PreferencesKeys.REMINDER_INTERVAL] ?: 2
            val reminderIntervalMinutes = preferences[PreferencesKeys.REMINDER_INTERVAL_MINUTES]
                ?: (legacyHours * 60)
            val reminderIntervalHours = (reminderIntervalMinutes / 60).coerceAtLeast(1)
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
            val soundId = preferences[PreferencesKeys.NOTIFICATION_SOUND] ?: NotificationSound.WATER_DROP.id
            val celebrationSoundId = preferences[PreferencesKeys.CELEBRATION_SOUND] ?: CelebrationSound.CHIME_FANFARE.id
            val defaultQuickAddMl = preferences[PreferencesKeys.DEFAULT_QUICK_ADD_ML] ?: 250
            val quickAddOnClick = preferences[PreferencesKeys.QUICK_ADD_ON_NOTIFICATION_CLICK] ?: true

            val language = preferences[PreferencesKeys.LANGUAGE] ?: "en"
            val frequentIntakeMl = preferences[PreferencesKeys.FREQUENT_INTAKE_ML] ?: 250
            val bottleModeEnabled = preferences[PreferencesKeys.BOTTLE_MODE_ENABLED] ?: false
            val bottleVolumeMl = preferences[PreferencesKeys.BOTTLE_VOLUME_ML] ?: 750
            val bottleTargetDurationMinutes = preferences[PreferencesKeys.BOTTLE_TARGET_DURATION_MINUTES] ?: 180
            val bottleStartTimeMillis = preferences[PreferencesKeys.BOTTLE_START_TIME_MILLIS] ?: 0L
            val bottleDrankMl = preferences[PreferencesKeys.BOTTLE_DRANK_ML] ?: 0
            val bottleRefillCount = preferences[PreferencesKeys.BOTTLE_REFILL_COUNT] ?: 0

            val userName = preferences[PreferencesKeys.USER_NAME] ?: ""
            val userAge = preferences[PreferencesKeys.USER_AGE] ?: 28
            val userSex = preferences[PreferencesKeys.USER_SEX] ?: "male"
            val userWeightKg = preferences[PreferencesKeys.USER_WEIGHT_KG] ?: 70f
            val userHeightCm = preferences[PreferencesKeys.USER_HEIGHT_CM] ?: 175f
            val userActivityLevel = preferences[PreferencesKeys.USER_ACTIVITY_LEVEL] ?: "moderate"
            val isOnboardingCompleted = preferences[PreferencesKeys.IS_ONBOARDING_COMPLETED] ?: false
            val isOnboardingSkipped = preferences[PreferencesKeys.IS_ONBOARDING_SKIPPED] ?: false
            val droppyTipsEnabled = preferences[PreferencesKeys.DROPPY_TIPS_ENABLED] ?: true
            val remindAfterGoalReached = preferences[PreferencesKeys.REMIND_AFTER_GOAL_REACHED] ?: false
            val themeMode = preferences[PreferencesKeys.THEME_MODE] ?: "system"

            UserSettings(
                dailyGoalMl = dailyGoal,
                reminderIntervalHours = reminderIntervalHours,
                reminderIntervalMinutes = reminderIntervalMinutes,
                remindersEnabled = remindersEnabled,
                startHour = startHour,
                endHour = endHour,
                reminderMode = mode,
                customReminderHours = if (customHours.isNotEmpty()) customHours else setOf(9, 12, 15, 18, 21),
                notificationSound = soundId,
                celebrationSound = celebrationSoundId,
                defaultQuickAddMl = defaultQuickAddMl,
                quickAddOnNotificationClick = quickAddOnClick,
                language = language,
                frequentIntakeMl = frequentIntakeMl,
                bottleModeEnabled = bottleModeEnabled,
                bottleVolumeMl = bottleVolumeMl,
                bottleTargetDurationMinutes = bottleTargetDurationMinutes,
                bottleStartTimeMillis = bottleStartTimeMillis,
                bottleDrankMl = bottleDrankMl,
                bottleRefillCount = bottleRefillCount,
                userName = userName,
                userAge = userAge,
                userSex = userSex,
                userWeightKg = userWeightKg,
                userHeightCm = userHeightCm,
                userActivityLevel = userActivityLevel,
                isOnboardingCompleted = isOnboardingCompleted,
                isOnboardingSkipped = isOnboardingSkipped,
                droppyTipsEnabled = droppyTipsEnabled,
                remindAfterGoalReached = remindAfterGoalReached,
                themeMode = themeMode
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
            preferences[PreferencesKeys.REMINDER_INTERVAL_MINUTES] = hours * 60
        }
    }

    suspend fun updateReminderIntervalMinutes(minutes: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.REMINDER_INTERVAL_MINUTES] = minutes
            preferences[PreferencesKeys.REMINDER_INTERVAL] = (minutes / 60).coerceAtLeast(1)
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

    suspend fun updateNotificationSound(soundId: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFICATION_SOUND] = soundId
        }
    }

    suspend fun updateCelebrationSound(soundId: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CELEBRATION_SOUND] = soundId
        }
    }

    suspend fun updateDefaultQuickAddMl(amountMl: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_QUICK_ADD_ML] = amountMl
        }
    }

    suspend fun updateQuickAddOnNotificationClick(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.QUICK_ADD_ON_NOTIFICATION_CLICK] = enabled
        }
    }

    suspend fun updateLanguage(language: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LANGUAGE] = language
        }
    }

    suspend fun updateFrequentIntakeMl(amountMl: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FREQUENT_INTAKE_ML] = amountMl
        }
    }

    suspend fun updateBottleModeEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.BOTTLE_MODE_ENABLED] = enabled
            if (enabled && (preferences[PreferencesKeys.BOTTLE_START_TIME_MILLIS] ?: 0L) == 0L) {
                preferences[PreferencesKeys.BOTTLE_START_TIME_MILLIS] = System.currentTimeMillis()
                preferences[PreferencesKeys.BOTTLE_DRANK_ML] = 0
            }
        }
    }

    suspend fun updateBottleConfig(volumeMl: Int, durationMinutes: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.BOTTLE_VOLUME_ML] = volumeMl
            preferences[PreferencesKeys.BOTTLE_TARGET_DURATION_MINUTES] = durationMinutes
        }
    }

    suspend fun recordBottleDrink(drankAmountMl: Int) {
        context.dataStore.edit { preferences ->
            val currentDrank = preferences[PreferencesKeys.BOTTLE_DRANK_ML] ?: 0
            val volume = preferences[PreferencesKeys.BOTTLE_VOLUME_ML] ?: 750
            preferences[PreferencesKeys.BOTTLE_DRANK_ML] = (currentDrank + drankAmountMl).coerceAtMost(volume)
            if ((preferences[PreferencesKeys.BOTTLE_START_TIME_MILLIS] ?: 0L) == 0L) {
                preferences[PreferencesKeys.BOTTLE_START_TIME_MILLIS] = System.currentTimeMillis()
            }
        }
    }

    suspend fun refillBottle() {
        context.dataStore.edit { preferences ->
            val refills = preferences[PreferencesKeys.BOTTLE_REFILL_COUNT] ?: 0
            preferences[PreferencesKeys.BOTTLE_REFILL_COUNT] = refills + 1
            preferences[PreferencesKeys.BOTTLE_DRANK_ML] = 0
            preferences[PreferencesKeys.BOTTLE_START_TIME_MILLIS] = System.currentTimeMillis()
        }
    }

    suspend fun updateUserProfile(
        name: String,
        age: Int,
        sex: String,
        weightKg: Float,
        heightCm: Float,
        activityLevel: String,
        recommendedGoalMl: Int? = null
    ) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_NAME] = name
            preferences[PreferencesKeys.USER_AGE] = age
            preferences[PreferencesKeys.USER_SEX] = sex
            preferences[PreferencesKeys.USER_WEIGHT_KG] = weightKg
            preferences[PreferencesKeys.USER_HEIGHT_CM] = heightCm
            preferences[PreferencesKeys.USER_ACTIVITY_LEVEL] = activityLevel
            if (recommendedGoalMl != null && recommendedGoalMl > 0) {
                preferences[PreferencesKeys.DAILY_GOAL] = recommendedGoalMl
            }
            if (name.isNotBlank() && age > 0 && weightKg > 0 && heightCm > 0) {
                preferences[PreferencesKeys.IS_ONBOARDING_COMPLETED] = true
            }
        }
    }

    suspend fun completeOnboarding(completed: Boolean = true) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun skipOnboarding() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_ONBOARDING_SKIPPED] = true
        }
    }

    suspend fun updateDroppyTipsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DROPPY_TIPS_ENABLED] = enabled
        }
    }

    suspend fun updateRemindAfterGoalReached(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.REMIND_AFTER_GOAL_REACHED] = enabled
        }
    }

    suspend fun updateThemeMode(themeMode: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = themeMode
        }
    }
}
