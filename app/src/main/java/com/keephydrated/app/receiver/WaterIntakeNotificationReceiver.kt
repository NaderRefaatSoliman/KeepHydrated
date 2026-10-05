package com.keephydrated.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.RemoteInput
import com.keephydrated.app.domain.model.WaterIntake
import com.keephydrated.app.domain.repository.HydrationRepository
import com.keephydrated.app.domain.repository.SettingsRepository
import com.keephydrated.app.notification.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

@AndroidEntryPoint
class WaterIntakeNotificationReceiver : BroadcastReceiver() {

    @Inject
    lateinit var hydrationRepository: HydrationRepository

    @Inject
    lateinit var settingsRepository: SettingsRepository

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val settings = settingsRepository.getUserSettings().first()
                val amount = when (action) {
                    ACTION_DROPDOWN_ADD -> {
                        val results = RemoteInput.getResultsFromIntent(intent)
                        val input = results?.getCharSequence(NotificationHelper.KEY_WATER_AMOUNT)?.toString()
                        val parsed = input?.filter { it.isDigit() }?.toIntOrNull()
                        parsed ?: settings.defaultQuickAddMl
                    }
                    ACTION_QUICK_ADD -> {
                        val explicitAmount = intent.getIntExtra(EXTRA_AMOUNT, 0)
                        if (explicitAmount > 0) explicitAmount else settings.defaultQuickAddMl
                    }
                    else -> settings.defaultQuickAddMl
                }

                if (amount > 0) {
                    val today = LocalDate.now()
                    val existingIntakes = hydrationRepository.getIntakesForDate(today).first()
                    val previousTotal = existingIntakes.sumOf { it.amountMl }
                    val goal = settings.dailyGoalMl

                    // Insert the logged intake
                    hydrationRepository.insertIntake(
                        WaterIntake(
                            amountMl = amount,
                            timestamp = LocalDateTime.now()
                        )
                    )

                    val newTotal = previousTotal + amount

                    // Push celebration notification if daily limit/goal was hit
                    if (previousTotal < goal && newTotal >= goal && goal > 0) {
                        NotificationHelper.showCelebrationNotification(context, goal)
                    }

                    // Show confirmation in notification tray
                    NotificationHelper.showIntakeLoggedConfirmation(context, amount, newTotal, goal)
                }
            } catch (_: Exception) {
                // Graceful fallback
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_QUICK_ADD = "com.keephydrated.app.action.QUICK_ADD_WATER"
        const val ACTION_DROPDOWN_ADD = "com.keephydrated.app.action.DROPDOWN_ADD_WATER"
        const val EXTRA_AMOUNT = "extra_water_amount"
    }
}
