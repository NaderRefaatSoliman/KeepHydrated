package com.keephydrated.app.presentation.ui.settings

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keephydrated.app.KeepHydratedApp
import com.keephydrated.app.domain.model.NotificationSound
import com.keephydrated.app.domain.model.ReminderMode
import com.keephydrated.app.domain.usecase.GetUserSettingsUseCase
import com.keephydrated.app.domain.usecase.SaveUserSettingsUseCase
import com.keephydrated.app.presentation.MainActivity
import com.keephydrated.app.worker.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getUserSettingsUseCase: GetUserSettingsUseCase,
    private val saveUserSettingsUseCase: SaveUserSettingsUseCase,
    private val reminderScheduler: ReminderScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private var previewPlayer: MediaPlayer? = null

    init {
        observeSettings()
    }

    private fun observeSettings() {
        viewModelScope.launch {
            getUserSettingsUseCase().collectLatest { settings ->
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
                        isLoading = false
                    )
                }
            }
        }
    }

    fun updateDailyGoal(goalMl: Int) {
        viewModelScope.launch {
            try {
                saveUserSettingsUseCase.updateGoal(goalMl)
                _uiState.update { it.copy(userMessage = "Goal updated to $goalMl ml") }
            } catch (e: Exception) {
                _uiState.update { it.copy(userMessage = e.message ?: "Failed to update goal") }
            }
        }
    }

    fun updateReminderInterval(hours: Int) {
        viewModelScope.launch {
            try {
                saveUserSettingsUseCase.updateReminderInterval(hours)
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
                saveUserSettingsUseCase.updateReminderIntervalMinutes(minutes)
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
                saveUserSettingsUseCase.updateRemindersEnabled(enabled)
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
                saveUserSettingsUseCase.updateReminderMode(mode)
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
                saveUserSettingsUseCase.updateCustomReminderHours(hours)
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
                saveUserSettingsUseCase.updateCustomReminderHours(current)
            } catch (e: Exception) {
                _uiState.update { it.copy(userMessage = e.message ?: "Failed to update reminder hours") }
            }
        }
    }

    fun updateActiveHours(startHour: Int, endHour: Int) {
        viewModelScope.launch {
            try {
                saveUserSettingsUseCase.updateActiveHours(startHour, endHour)
                _uiState.update { it.copy(userMessage = "Active hours updated") }
            } catch (e: Exception) {
                _uiState.update { it.copy(userMessage = e.message ?: "Failed to update active hours") }
            }
        }
    }

    fun applyWakingDayPreset(wakeHour: Int, sleepHour: Int, stepHours: Int = 2) {
        viewModelScope.launch {
            try {
                saveUserSettingsUseCase.updateActiveHours(wakeHour, sleepHour)
                val step = stepHours.coerceIn(1, 4)
                val generatedHours = mutableSetOf<Int>()

                if (wakeHour <= sleepHour) {
                    var h = wakeHour
                    while (h <= sleepHour) {
                        generatedHours.add(h)
                        h += step
                    }
                } else {
                    // Overnight awake schedule (e.g., night shift: 20:00 to 06:00)
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

                saveUserSettingsUseCase.updateCustomReminderHours(generatedHours)
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
                saveUserSettingsUseCase.updateNotificationSound(sound.id)
                _uiState.update { it.copy(userMessage = "Notification sound: ${sound.displayName}") }
            } catch (e: Exception) {
                _uiState.update { it.copy(userMessage = e.message ?: "Failed to update notification sound") }
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

    fun sendTestNotification(context: Context) {
        try {
            val sound = NotificationSound.fromId(_uiState.value.notificationSound)
            val channelId = KeepHydratedApp.getChannelIdForSound(sound.id)
            val soundUri = KeepHydratedApp.getSoundUri(context, sound)

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("KeepHydrated Test")
                .setContentText("💧 Sip time! Sound: ${sound.displayName}")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .apply {
                    if (soundUri != null) {
                        setSound(soundUri)
                    }
                }
                .build()

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
