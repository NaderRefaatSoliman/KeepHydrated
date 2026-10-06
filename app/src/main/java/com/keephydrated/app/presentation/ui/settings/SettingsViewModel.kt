package com.keephydrated.app.presentation.ui.settings

import android.app.NotificationManager
import android.content.Context
import android.media.MediaPlayer
import android.media.RingtoneManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keephydrated.app.domain.model.CelebrationSound
import com.keephydrated.app.domain.model.NotificationSound
import com.keephydrated.app.domain.model.ReminderMode
import com.keephydrated.app.domain.model.UserSettings
import com.keephydrated.app.domain.repository.SettingsRepository
import com.keephydrated.app.notification.NotificationHelper
import com.keephydrated.app.util.LocaleHelper
import com.keephydrated.app.worker.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val reminderScheduler: ReminderScheduler,
    @ApplicationContext private val context: Context? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private var previewPlayer: MediaPlayer? = null

    init {
        observeSettings()
    }

    private fun observeSettings() {
        viewModelScope.launch {
            settingsRepository.getUserSettings().collectLatest { settings ->
                _uiState.update {
                    it.copy(
                        dailyGoalMl = settings.dailyGoalMl,
                        reminderIntervalHours = settings.reminderIntervalHours,
                        reminderIntervalMinutes = settings.reminderIntervalMinutes,
                        remindersEnabled = settings.remindersEnabled,
                        startHour = settings.startHour,
                        endHour = settings.endHour,
                        reminderMode = settings.reminderMode,
                        customReminderHours = settings.customReminderHours,
                        notificationSound = settings.notificationSound,
                        celebrationSound = settings.celebrationSound,
                        defaultQuickAddMl = settings.defaultQuickAddMl,
                        quickAddOnNotificationClick = settings.quickAddOnNotificationClick,
                        language = settings.language,
                        frequentIntakeMl = settings.frequentIntakeMl,
                        bottleModeEnabled = settings.bottleModeEnabled,
                        bottleVolumeMl = settings.bottleVolumeMl,
                        bottleTargetDurationMinutes = settings.bottleTargetDurationMinutes,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun updateLanguage(languageCode: String) {
        viewModelScope.launch {
            try {
                settingsRepository.updateLanguage(languageCode)
                context?.let { ctx ->
                    LocaleHelper.setLocale(ctx, languageCode)
                }
                _uiState.update { it.copy(language = languageCode) }
            } catch (e: Exception) {
                _uiState.update { it.copy(userMessage = "Failed to update language") }
            }
        }
    }

    fun updateDailyGoal(goalMl: Int) {
        viewModelScope.launch {
            try {
                settingsRepository.updateDailyGoal(goalMl)
                _uiState.update { it.copy(userMessage = "Goal updated to $goalMl ml") }
            } catch (e: Exception) {
                _uiState.update { it.copy(userMessage = e.message ?: "Failed to update goal") }
            }
        }
    }

    fun updateFrequentIntakeMl(amountMl: Int) {
        viewModelScope.launch {
            try {
                settingsRepository.updateFrequentIntakeMl(amountMl)
                _uiState.update { it.copy(frequentIntakeMl = amountMl) }
            } catch (e: Exception) {
                _uiState.update { it.copy(userMessage = "Failed to update frequent amount") }
            }
        }
    }

    fun updateBottleModeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            try {
                settingsRepository.updateBottleModeEnabled(enabled)
                val msg = if (enabled) "Bottle Tracking Mode enabled" else "Bottle Tracking Mode disabled"
                _uiState.update { it.copy(bottleModeEnabled = enabled, userMessage = msg) }
            } catch (e: Exception) {
                _uiState.update { it.copy(userMessage = "Failed to toggle bottle mode") }
            }
        }
    }

    fun updateBottleConfig(volumeMl: Int, durationMinutes: Int) {
        viewModelScope.launch {
            try {
                settingsRepository.updateBottleConfig(volumeMl, durationMinutes)
                _uiState.update {
                    it.copy(
                        bottleVolumeMl = volumeMl,
                        bottleTargetDurationMinutes = durationMinutes,
                        userMessage = "Bottle configured: $volumeMl ml ($durationMinutes mins)"
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(userMessage = "Failed to update bottle settings") }
            }
        }
    }

    fun updateReminderInterval(hours: Int) {
        viewModelScope.launch {
            try {
                settingsRepository.updateReminderInterval(hours)
                reminderScheduler.scheduleReminders(
                    intervalMinutes = hours * 60,
                    enabled = _uiState.value.remindersEnabled,
                    mode = _uiState.value.reminderMode
                )
                val label = if (hours == 1) "1 hour" else "$hours hours"
                _uiState.update { it.copy(userMessage = "Reminder interval updated to $label") }
            } catch (e: Exception) {
                _uiState.update { it.copy(userMessage = e.message ?: "Failed to update interval") }
            }
        }
    }

    fun updateReminderIntervalMinutes(minutes: Int) {
        viewModelScope.launch {
            try {
                settingsRepository.updateReminderIntervalMinutes(minutes)
                reminderScheduler.scheduleReminders(
                    intervalMinutes = minutes,
                    enabled = _uiState.value.remindersEnabled,
                    mode = _uiState.value.reminderMode
                )
                val label = if (minutes % 60 == 0) {
                    val h = minutes / 60
                    if (h == 1) "1 hour" else "$h hours"
                } else {
                    "$minutes minutes"
                }
                _uiState.update { it.copy(userMessage = "Reminder interval updated to $label") }
            } catch (e: Exception) {
                _uiState.update { it.copy(userMessage = e.message ?: "Failed to update interval") }
            }
        }
    }

    fun toggleReminders(enabled: Boolean) {
        viewModelScope.launch {
            try {
                settingsRepository.updateRemindersEnabled(enabled)
                reminderScheduler.scheduleReminders(
                    intervalMinutes = _uiState.value.reminderIntervalMinutes,
                    enabled = enabled,
                    mode = _uiState.value.reminderMode
                )
                val msg = if (enabled) "Reminders enabled" else "Reminders disabled"
                _uiState.update { it.copy(userMessage = msg) }
            } catch (e: Exception) {
                _uiState.update { it.copy(userMessage = "Failed to update reminders") }
            }
        }
    }

    fun updateReminderMode(mode: ReminderMode) {
        viewModelScope.launch {
            try {
                settingsRepository.updateReminderMode(mode)
                reminderScheduler.scheduleReminders(
                    intervalMinutes = _uiState.value.reminderIntervalMinutes,
                    enabled = _uiState.value.remindersEnabled,
                    mode = mode
                )
                val msg = if (mode == ReminderMode.CUSTOM_ROUTINE) "Custom routine enabled" else "Interval mode enabled"
                _uiState.update { it.copy(userMessage = msg) }
            } catch (e: Exception) {
                _uiState.update { it.copy(userMessage = "Failed to change reminder mode") }
            }
        }
    }

    fun setCustomReminderHours(hours: Set<Int>) {
        if (hours.isEmpty()) return
        viewModelScope.launch {
            try {
                settingsRepository.updateCustomReminderHours(hours)
                _uiState.update { it.copy(userMessage = "Preset routine applied (${hours.size} reminders)") }
            } catch (e: Exception) {
                _uiState.update { it.copy(userMessage = e.message ?: "Failed to set routine") }
            }
        }
    }

    fun toggleCustomReminderHour(hour: Int) {
        viewModelScope.launch {
            try {
                val current = _uiState.value.customReminderHours.toMutableSet()
                if (current.contains(hour)) {
                    if (current.size > 1) {
                        current.remove(hour)
                    } else {
                        _uiState.update { it.copy(userMessage = "At least one reminder hour is required") }
                        return@launch
                    }
                } else {
                    current.add(hour)
                }
                settingsRepository.updateCustomReminderHours(current)
            } catch (e: Exception) {
                _uiState.update { it.copy(userMessage = e.message ?: "Failed to update reminder hours") }
            }
        }
    }

    fun updateActiveHours(startHour: Int, endHour: Int) {
        viewModelScope.launch {
            try {
                settingsRepository.updateActiveHours(startHour, endHour)
                _uiState.update { it.copy(userMessage = "Active hours updated") }
            } catch (e: Exception) {
                _uiState.update { it.copy(userMessage = e.message ?: "Failed to update active hours") }
            }
        }
    }

    fun applyWakingDayPreset(wakeHour: Int, sleepHour: Int, stepHours: Int = 2) {
        viewModelScope.launch {
            try {
                settingsRepository.updateActiveHours(wakeHour, sleepHour)
                val step = stepHours.coerceIn(1, 4)
                val generatedHours = mutableSetOf<Int>()

                if (wakeHour <= sleepHour) {
                    var h = wakeHour
                    while (h <= sleepHour) {
                        generatedHours.add(h)
                        h += step
                    }
                } else {
                    var h = wakeHour
                    while (h < 24) {
                        generatedHours.add(h)
                        h += step
                    }
                    var m = h % 24
                    while (m <= sleepHour) {
                        generatedHours.add(m)
                        m += step
                    }
                }

                if (generatedHours.isEmpty()) {
                    generatedHours.add(wakeHour)
                }

                settingsRepository.updateCustomReminderHours(generatedHours)
                _uiState.update {
                    it.copy(userMessage = "Awake schedule applied: ${generatedHours.size} reminders set")
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(userMessage = e.message ?: "Failed to apply awake preset") }
            }
        }
    }

    fun updateNotificationSound(soundId: String) {
        viewModelScope.launch {
            try {
                val sound = NotificationSound.fromId(soundId)
                settingsRepository.updateNotificationSound(sound.id)
                _uiState.update { it.copy(userMessage = "Notification sound: ${sound.displayName}") }
            } catch (e: Exception) {
                _uiState.update { it.copy(userMessage = e.message ?: "Failed to update notification sound") }
            }
        }
    }

    fun updateCelebrationSound(soundId: String) {
        viewModelScope.launch {
            try {
                val sound = CelebrationSound.fromId(soundId)
                settingsRepository.updateCelebrationSound(sound.id)
                _uiState.update { it.copy(userMessage = "Celebration tone: ${sound.displayName}") }
            } catch (e: Exception) {
                _uiState.update { it.copy(userMessage = e.message ?: "Failed to update celebration tone") }
            }
        }
    }

    fun updateDefaultQuickAddMl(amountMl: Int) {
        viewModelScope.launch {
            try {
                settingsRepository.updateDefaultQuickAddMl(amountMl)
                _uiState.update { it.copy(userMessage = "Quick-add amount set to $amountMl ml") }
            } catch (e: Exception) {
                _uiState.update { it.copy(userMessage = e.message ?: "Failed to update quick-add amount") }
            }
        }
    }

    fun updateQuickAddOnNotificationClick(enabled: Boolean) {
        viewModelScope.launch {
            try {
                settingsRepository.updateQuickAddOnNotificationClick(enabled)
                val msg = if (enabled) "One-tap log on notification click enabled" else "One-tap log on notification click disabled"
                _uiState.update { it.copy(userMessage = msg) }
            } catch (e: Exception) {
                _uiState.update { it.copy(userMessage = "Failed to update notification click setting") }
            }
        }
    }

    fun previewSound(context: Context, sound: NotificationSound) {
        try {
            previewPlayer?.release()
            previewPlayer = null

            if (sound == NotificationSound.SYSTEM_DEFAULT || sound.resName.isBlank()) {
                val defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val ringtone = RingtoneManager.getRingtone(context, defaultUri)
                ringtone?.play()
            } else {
                val resId = context.resources.getIdentifier(sound.resName, "raw", context.packageName)
                if (resId != 0) {
                    previewPlayer = MediaPlayer.create(context, resId)?.apply {
                        setOnCompletionListener {
                            it.release()
                            previewPlayer = null
                        }
                        start()
                    }
                }
            }
        } catch (_: Exception) {
            // Graceful fallback
        }
    }

    fun previewCelebrationSound(context: Context, sound: CelebrationSound) {
        try {
            previewPlayer?.release()
            previewPlayer = null

            if (sound == CelebrationSound.SYSTEM_DEFAULT || sound.resName.isBlank()) {
                val defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val ringtone = RingtoneManager.getRingtone(context, defaultUri)
                ringtone?.play()
            } else {
                val resId = context.resources.getIdentifier(sound.resName, "raw", context.packageName)
                if (resId != 0) {
                    previewPlayer = MediaPlayer.create(context, resId)?.apply {
                        setOnCompletionListener {
                            it.release()
                            previewPlayer = null
                        }
                        start()
                    }
                }
            }
        } catch (_: Exception) {
            // Graceful fallback
        }
    }

    fun sendTestNotification(context: Context) {
        try {
            val sound = NotificationSound.fromId(_uiState.value.notificationSound)
            val testSettings = UserSettings(
                dailyGoalMl = _uiState.value.dailyGoalMl,
                reminderIntervalHours = _uiState.value.reminderIntervalHours,
                reminderIntervalMinutes = _uiState.value.reminderIntervalMinutes,
                remindersEnabled = _uiState.value.remindersEnabled,
                startHour = _uiState.value.startHour,
                endHour = _uiState.value.endHour,
                reminderMode = _uiState.value.reminderMode,
                customReminderHours = _uiState.value.customReminderHours,
                notificationSound = sound.id,
                celebrationSound = _uiState.value.celebrationSound,
                defaultQuickAddMl = _uiState.value.defaultQuickAddMl,
                quickAddOnNotificationClick = _uiState.value.quickAddOnNotificationClick,
                language = _uiState.value.language,
                frequentIntakeMl = _uiState.value.frequentIntakeMl,
                bottleModeEnabled = _uiState.value.bottleModeEnabled,
                bottleVolumeMl = _uiState.value.bottleVolumeMl,
                bottleTargetDurationMinutes = _uiState.value.bottleTargetDurationMinutes
            )

            val notification = NotificationHelper.buildReminderNotification(context, testSettings)
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(TEST_NOTIFICATION_ID, notification)

            previewSound(context, sound)
            _uiState.update { it.copy(userMessage = "Test notification sent (${sound.displayName})") }
        } catch (e: Exception) {
            _uiState.update { it.copy(userMessage = e.message ?: "Failed to send test notification") }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }

    override fun onCleared() {
        super.onCleared()
        previewPlayer?.release()
        previewPlayer = null
    }

    companion object {
        const val TEST_NOTIFICATION_ID = 2002
    }
}
