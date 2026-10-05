package com.keephydrated.app.notification

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.keephydrated.app.KeepHydratedApp
import com.keephydrated.app.R
import com.keephydrated.app.domain.model.CelebrationSound
import com.keephydrated.app.domain.model.NotificationSound
import com.keephydrated.app.domain.model.UserSettings
import com.keephydrated.app.presentation.MainActivity
import com.keephydrated.app.receiver.WaterIntakeNotificationReceiver

object NotificationHelper {

    fun buildReminderNotification(context: Context, settings: UserSettings): Notification {
        val sound = NotificationSound.fromId(settings.notificationSound)
        val channelId = KeepHydratedApp.getChannelIdForSound(sound.id)
        val soundUri = KeepHydratedApp.getSoundUri(context, sound)

        // Tapping the notification body directly adds the configured quick-add amount
        val quickAddIntent = Intent(context, WaterIntakeNotificationReceiver::class.java).apply {
            action = WaterIntakeNotificationReceiver.ACTION_QUICK_ADD
            putExtra(WaterIntakeNotificationReceiver.EXTRA_AMOUNT, settings.defaultQuickAddMl)
        }
        val contentPendingIntent = PendingIntent.getBroadcast(
            context,
            100,
            quickAddIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Predefined configured Quick Add button for mobile shade & smartwatch
        val quickAddActionIntent = Intent(context, WaterIntakeNotificationReceiver::class.java).apply {
            action = WaterIntakeNotificationReceiver.ACTION_QUICK_ADD
            putExtra(WaterIntakeNotificationReceiver.EXTRA_AMOUNT, settings.defaultQuickAddMl)
        }
        val quickAddPendingIntent = PendingIntent.getBroadcast(
            context,
            101,
            quickAddActionIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val quickAddAction = NotificationCompat.Action.Builder(
            android.R.drawable.ic_input_add,
            "+ ${settings.defaultQuickAddMl} ml",
            quickAddPendingIntent
        ).build()

        // WearableExtender for Smartwatches
        val wearableExtender = NotificationCompat.WearableExtender()
            .addAction(quickAddAction)

        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(context.getString(R.string.reminder_title))
            .setContentText("Tap to log ${settings.defaultQuickAddMl} ml water intake")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .addAction(quickAddAction)
            .extend(wearableExtender)
            .apply {
                if (soundUri != null) {
                    setSound(soundUri)
                }
            }
            .build()
    }

    /**
     * Shows a completely silent notification confirming the logged intake amount.
     */
    fun showIntakeLoggedConfirmation(context: Context, amountMl: Int, newTotalMl: Int, goalMl: Int) {
        val percent = if (goalMl > 0) ((newTotalMl.toFloat() / goalMl) * 100).toInt() else 0
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, KeepHydratedApp.SILENT_ACK_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("💧 $amountMl ml Logged")
            .setContentText("Today: $newTotalMl / $goalMl ml ($percent%)")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setSilent(true)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(KeepHydratedApp.REMINDER_NOTIFICATION_ID, notification)
    }

    /**
     * Pushes a celebratory notification with custom celebration sound when the daily limit/goal is hit.
     */
    fun showCelebrationNotification(context: Context, goalMl: Int, celebrationSoundId: String? = null) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val sound = CelebrationSound.fromId(celebrationSoundId)
        val channelId = KeepHydratedApp.getCelebrationChannelIdForSound(sound.id)
        val soundUri = KeepHydratedApp.getCelebrationSoundUri(context, sound)

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("🎉 Daily Goal Achieved!")
            .setContentText("Congratulations! You've reached your daily hydration goal of $goalMl ml! 🏆💧")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 250, 150, 250, 150, 400))
            .apply {
                if (soundUri != null) {
                    setSound(soundUri)
                }
            }
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(KeepHydratedApp.CELEBRATION_NOTIFICATION_ID, notification)
    }
}
