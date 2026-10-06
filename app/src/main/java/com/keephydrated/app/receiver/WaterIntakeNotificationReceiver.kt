package com.keephydrated.app.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.keephydrated.app.KeepHydratedApp
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
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val action = intent.action
                val settings = settingsRepository.getUserSettings().first()

                if (action == ACTION_REFILL_BOTTLE) {
                    // Refill the bottle session
                    settingsRepository.refillBottle()

                    // Clear any lingering bottle refill reminder notification
                    val notificationManager =
                        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.cancel(KeepHydratedApp.BOTTLE_NOTIFICATION_ID)

                    // Show confirmation in tray
                    NotificationHelper.showIntakeLoggedConfirmation(
                        context = context,
                        amountMl = 0,
                        newTotalMl = settings.bottleVolumeMl,
                        goalMl = settings.dailyGoalMl,
                        customTitle = "🔄 Bottle Refilled!",
                        customMessage = "Fresh ${settings.bottleVolumeMl} ml ready to drink."
                    )
                } else {
                    // Quick-add intake logging
                    val explicitAmount = intent.getIntExtra(EXTRA_AMOUNT, 0)
                    val amount = if (explicitAmount > 0) explicitAmount else settings.defaultQuickAddMl

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

                        // If bottle tracking mode is active, also register drink from bottle
                        if (settings.bottleModeEnabled) {
                            settingsRepository.recordBottleDrink(amount)
                        }

                        val newTotal = previousTotal + amount

                        // Push celebration notification if daily goal was reached
                        if (previousTotal < goal && newTotal >= goal && goal > 0) {
                            NotificationHelper.showCelebrationNotification(context, goal, settings.celebrationSound)
                        }

                        // Show silent confirmation in notification tray
                        NotificationHelper.showIntakeLoggedConfirmation(
                            context = context,
                            amountMl = amount,
                            newTotalMl = newTotal,
                            goalMl = goal
                        )
                    }
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
        const val ACTION_REFILL_BOTTLE = "com.keephydrated.app.action.REFILL_BOTTLE"
        const val EXTRA_AMOUNT = "extra_water_amount"
    }
}
