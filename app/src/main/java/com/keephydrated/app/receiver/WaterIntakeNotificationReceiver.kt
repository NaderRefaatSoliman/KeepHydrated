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
import com.keephydrated.app.util.LocalizationUtils
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
                val isArabic = settings.language == "ar"

                if (action == ACTION_REFILL_BOTTLE) {
                    settingsRepository.refillBottle()

                    val notificationManager =
                        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.cancel(KeepHydratedApp.BOTTLE_NOTIFICATION_ID)

                    val formattedVolume = LocalizationUtils.formatNumber(settings.bottleVolumeMl, isArabic)
                    val title = if (isArabic) "🔄 تمت إعادة تعبئة الزجاجة!" else "🔄 Bottle Refilled!"
                    val msg = if (isArabic) "جاهز لشرب $formattedVolume مل ماء منعش." else "Fresh ${settings.bottleVolumeMl} ml ready to drink."

                    NotificationHelper.showIntakeLoggedConfirmation(
                        context = context,
                        amountMl = 0,
                        newTotalMl = settings.bottleVolumeMl,
                        goalMl = settings.dailyGoalMl,
                        customTitle = title,
                        customMessage = msg,
                        language = settings.language
                    )
                } else {
                    val explicitAmount = intent.getIntExtra(EXTRA_AMOUNT, 0)
                    val amount = if (explicitAmount > 0) explicitAmount else settings.defaultQuickAddMl

                    if (amount > 0) {
                        val today = LocalDate.now()
                        val existingIntakes = hydrationRepository.getIntakesForDate(today).first()
                        val previousTotal = existingIntakes.sumOf { it.amountMl }
                        val goal = settings.dailyGoalMl

                        hydrationRepository.insertIntake(
                            WaterIntake(
                                amountMl = amount,
                                timestamp = LocalDateTime.now()
                            )
                        )

                        if (settings.bottleModeEnabled) {
                            settingsRepository.recordBottleDrink(amount)
                        }

                        val newTotal = previousTotal + amount

                        if (previousTotal < goal && newTotal >= goal && goal > 0) {
                            NotificationHelper.showCelebrationNotification(
                                context = context,
                                goalMl = goal,
                                celebrationSoundId = settings.celebrationSound,
                                language = settings.language
                            )
                        }

                        NotificationHelper.showIntakeLoggedConfirmation(
                            context = context,
                            amountMl = amount,
                            newTotalMl = newTotal,
                            goalMl = goal,
                            language = settings.language
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
