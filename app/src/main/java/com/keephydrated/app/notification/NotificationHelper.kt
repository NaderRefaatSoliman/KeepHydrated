package com.keephydrated.app.notification

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput
import com.keephydrated.app.KeepHydratedApp
import com.keephydrated.app.R
import com.keephydrated.app.domain.model.NotificationSound
import com.keephydrated.app.domain.model.UserSettings
import com.keephydrated.app.presentation.MainActivity
import com.keephydrated.app.receiver.WaterIntakeNotificationReceiver

object NotificationHelper {

    const val KEY_WATER_AMOUNT = "key_water_amount"

    fun buildReminderNotification(context: Context, settings: UserSettings): Notification {
        val sound = NotificationSound.fromId(settings.notificationSound)
        val channelId = KeepHydratedApp.getChannelIdForSound(sound.id)
        val soundUri = KeepHydratedApp.getSoundUri(context, sound)

        // Content intent (tap notification body)
        val contentPendingIntent = if (settings.quickAddOnNotificationClick) {
            val quickAddIntent = Intent(context, WaterIntakeNotificationReceiver::class.java).apply {
                action = WaterIntakeNotificationReceiver.ACTION_QUICK_ADD
                putExtra(WaterIntakeNotificationReceiver.EXTRA_AMOUNT, settings.defaultQuickAddMl)
            }
            PendingIntent.getBroadcast(
                context,
                100,
                quickAddIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            PendingIntent.getActivity(
                context,
                0,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        // Action 1: Pre-defined Quick Add button (for mobile shade & smartwatch)
        val quickAddIntent = Intent(context, WaterIntakeNotificationReceiver::class.java).apply {
            action = WaterIntakeNotificationReceiver.ACTION_QUICK_ADD
            putExtra(WaterIntakeNotificationReceiver.EXTRA_AMOUNT, settings.defaultQuickAddMl)
        }
        val quickAddPendingIntent = PendingIntent.getBroadcast(
            context,
            101,
            quickAddIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val quickAddAction = NotificationCompat.Action.Builder(
            android.R.drawable.ic_input_add,
            "+ ${settings.defaultQuickAddMl} ml",
            quickAddPendingIntent
        ).build()

        // Action 2: Dropdown / Amount Selection Action (RemoteInput with choices for watch & phone)
        val remoteInput = RemoteInput.Builder(KEY_WATER_AMOUNT)
            .setLabel("Select or enter ml")
            .setChoices(arrayOf("150 ml", "200 ml", "250 ml", "350 ml", "500 ml"))
            .build()

        val dropdownIntent = Intent(context, WaterIntakeNotificationReceiver::class.java).apply {
            action = WaterIntakeNotificationReceiver.ACTION_DROPDOWN_ADD
        }
        val dropdownPendingIntent = PendingIntent.getBroadcast(
            context,
            102,
            dropdownIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
        val dropdownAction = NotificationCompat.Action.Builder(
            android.R.drawable.ic_menu_edit,
            "💧 Choose Amount",
            dropdownPendingIntent
        )
            .addRemoteInput(remoteInput)
            .build()

        // WearableExtender for Smartwatches
        val wearableExtender = NotificationCompat.WearableExtender()
            .addAction(quickAddAction)
            .addAction(dropdownAction)

        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(context.getString(R.string.reminder_title))
            .setContentText(context.getString(R.string.reminder_message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .addAction(quickAddAction)
            .addAction(dropdownAction)
            .extend(wearableExtender)
            .apply {
                if (soundUri != null) {
                    setSound(soundUri)
                }
            }
            .build()
    }

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

        val notification = NotificationCompat.Builder(context, KeepHydratedApp.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("💧 $amountMl ml Logged!")
            .setContentText("Today's Total: $newTotalMl / $goalMl ml ($percent%)")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(KeepHydratedApp.REMINDER_NOTIFICATION_ID, notification)
    }

    fun showCelebrationNotification(context: Context, goalMl: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val celebrationResId = context.resources.getIdentifier("celebration", "raw", context.packageName)
        val celebrationSoundUri = if (celebrationResId != 0) {
            Uri.parse("${ContentResolver.SCHEME_ANDROID_RESOURCE}://${context.packageName}/$celebrationResId")
        } else null

        val notification = NotificationCompat.Builder(context, KeepHydratedApp.CELEBRATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("🎉 Daily Goal Achieved!")
            .setContentText("Congratulations! You've reached your daily hydration goal of $goalMl ml! 🏆💧")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 250, 150, 250, 150, 400))
            .apply {
                if (celebrationSoundUri != null) {
                    setSound(celebrationSoundUri)
                }
            }
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(KeepHydratedApp.CELEBRATION_NOTIFICATION_ID, notification)
    }
}
