package com.keephydrated.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.keephydrated.app.domain.repository.SettingsRepository
import com.keephydrated.app.worker.ReminderScheduler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootCompletedReceiver : BroadcastReceiver() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var reminderScheduler: ReminderScheduler

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED || action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            val pendingResult = goAsync()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val settings = settingsRepository.getUserSettings().first()
                    if (settings.remindersEnabled) {
                        reminderScheduler.scheduleReminders(
                            intervalMinutes = settings.reminderIntervalMinutes,
                            enabled = true,
                            mode = settings.reminderMode
                        )
                    }
                } catch (_: Exception) {
                    // Graceful fallback
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
