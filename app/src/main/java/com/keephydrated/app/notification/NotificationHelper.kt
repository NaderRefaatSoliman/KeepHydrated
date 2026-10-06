package com.keephydrated.app.notification

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.keephydrated.app.KeepHydratedApp
import com.keephydrated.app.R
import com.keephydrated.app.domain.model.CelebrationSound
import com.keephydrated.app.domain.model.NotificationSound
import com.keephydrated.app.domain.model.UserSettings
import com.keephydrated.app.presentation.MainActivity
import com.keephydrated.app.receiver.WaterIntakeNotificationReceiver

object NotificationHelper {

    private const val WATER_COLOR = 0xFF0288D1.toInt()

    /**
     * Converts vector drawable ic_water_cup into a crisp high-res Bitmap for setLargeIcon.
     */
    fun getWaterCupBitmap(context: Context): Bitmap? {
        return try {
            val drawable = ContextCompat.getDrawable(context, R.drawable.ic_water_cup) ?: return null
            val size = 192
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        } catch (_: Exception) {
            null
        }
    }

    fun buildReminderNotification(context: Context, settings: UserSettings): Notification {
        val sound = NotificationSound.fromId(settings.notificationSound)
        val channelId = KeepHydratedApp.getChannelIdForSound(sound.id)
        val soundUri = KeepHydratedApp.getSoundUri(context, sound)
        val cupBitmap = getWaterCupBitmap(context)

        // Action 1: Predefined quick add amount
        val quickAddAmount = if (settings.bottleModeEnabled) {
            settings.frequentIntakeMl
        } else {
            settings.defaultQuickAddMl
        }

        val quickAddIntent = Intent(context, WaterIntakeNotificationReceiver::class.java).apply {
            action = WaterIntakeNotificationReceiver.ACTION_QUICK_ADD
            putExtra(WaterIntakeNotificationReceiver.EXTRA_AMOUNT, quickAddAmount)
        }
        val contentPendingIntent = PendingIntent.getBroadcast(
            context,
            100,
            quickAddIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val quickAddActionIntent = Intent(context, WaterIntakeNotificationReceiver::class.java).apply {
            action = WaterIntakeNotificationReceiver.ACTION_QUICK_ADD
            putExtra(WaterIntakeNotificationReceiver.EXTRA_AMOUNT, quickAddAmount)
        }
        val quickAddPendingIntent = PendingIntent.getBroadcast(
            context,
            101,
            quickAddActionIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val quickAddAction = NotificationCompat.Action.Builder(
            R.drawable.ic_water_cup,
            "+ $quickAddAmount ml 💧",
            quickAddPendingIntent
        ).build()

        // Notification title & body (Customized for Bottle Mode if active)
        val title: String
        val bodyText: String

        if (settings.bottleModeEnabled) {
            val bottleStatus = settings.toBottleStatus()
            title = context.getString(R.string.bottle_reminder_title)
            bodyText = context.getString(
                R.string.bottle_reminder_msg,
                bottleStatus.volumeMl,
                bottleStatus.remainingMl
            )
        } else {
            title = context.getString(R.string.reminder_title)
            bodyText = context.getString(R.string.reminder_tap_text, quickAddAmount)
        }

        // WearableExtender for Smartwatches
        val wearableExtender = NotificationCompat.WearableExtender()
            .addAction(quickAddAction)
            .setContentAction(0)
            .setBridgeTag("keephydrated_water_reminder")

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_water_cup)
            .setColor(WATER_COLOR)
            .setContentTitle(title)
            .setContentText(bodyText)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setVibrate(longArrayOf(0, 350, 200, 350))
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .addAction(quickAddAction)
            .extend(wearableExtender)

        cupBitmap?.let { builder.setLargeIcon(it) }

        if (soundUri != null) {
            builder.setSound(soundUri)
        }

        return builder.build()
    }

    /**
     * Dedicated reminder when the bottle drinking duration has elapsed and the bottle must be refilled.
     */
    fun showBottleRefillReminder(context: Context, settings: UserSettings) {
        val sound = NotificationSound.fromId(settings.notificationSound)
        val soundUri = KeepHydratedApp.getSoundUri(context, sound)
        val cupBitmap = getWaterCupBitmap(context)

        // Refill Action Intent
        val refillIntent = Intent(context, WaterIntakeNotificationReceiver::class.java).apply {
            action = WaterIntakeNotificationReceiver.ACTION_REFILL_BOTTLE
        }
        val refillPendingIntent = PendingIntent.getBroadcast(
            context,
            102,
            refillIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val refillAction = NotificationCompat.Action.Builder(
            R.drawable.ic_water_cup,
            context.getString(R.string.bottle_refill_action),
            refillPendingIntent
        ).build()

        val mainActivityIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            103,
            mainActivityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val wearableExtender = NotificationCompat.WearableExtender()
            .addAction(refillAction)
            .setBridgeTag("keephydrated_bottle_refill")

        val builder = NotificationCompat.Builder(context, KeepHydratedApp.BOTTLE_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_water_cup)
            .setColor(WATER_COLOR)
            .setContentTitle(context.getString(R.string.bottle_refill_title))
            .setContentText(context.getString(R.string.bottle_refill_msg))
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setVibrate(longArrayOf(0, 400, 200, 400, 200, 500))
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .addAction(refillAction)
            .extend(wearableExtender)

        cupBitmap?.let { builder.setLargeIcon(it) }

        if (soundUri != null) {
            builder.setSound(soundUri)
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(KeepHydratedApp.BOTTLE_NOTIFICATION_ID, builder.build())
    }

    /**
     * Shows a completely silent notification confirming the logged intake or bottle refill.
     */
    fun showIntakeLoggedConfirmation(
        context: Context,
        amountMl: Int,
        newTotalMl: Int,
        goalMl: Int,
        customTitle: String? = null,
        customMessage: String? = null
    ) {
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

        val title = customTitle ?: context.getString(R.string.logged_confirmation_title, amountMl)
        val text = customMessage ?: context.getString(R.string.logged_confirmation_body, newTotalMl, goalMl, percent)
        val cupBitmap = getWaterCupBitmap(context)

        val notification = NotificationCompat.Builder(context, KeepHydratedApp.SILENT_ACK_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_water_cup)
            .setColor(WATER_COLOR)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSilent(true)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .apply { cupBitmap?.let { setLargeIcon(it) } }
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
        val cupBitmap = getWaterCupBitmap(context)

        val wearableExtender = NotificationCompat.WearableExtender()
            .setBridgeTag("keephydrated_goal_celebration")

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_water_cup)
            .setColor(WATER_COLOR)
            .setContentTitle(context.getString(R.string.celebration_title))
            .setContentText(context.getString(R.string.celebration_body, goalMl))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 300, 150, 300, 150, 450))
            .extend(wearableExtender)

        cupBitmap?.let { builder.setLargeIcon(it) }

        if (soundUri != null) {
            builder.setSound(soundUri)
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(KeepHydratedApp.CELEBRATION_NOTIFICATION_ID, builder.build())
    }
}
