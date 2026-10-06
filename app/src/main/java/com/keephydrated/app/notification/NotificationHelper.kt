package com.keephydrated.app.notification

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.keephydrated.app.KeepHydratedApp
import com.keephydrated.app.R
import com.keephydrated.app.domain.model.CelebrationSound
import com.keephydrated.app.domain.model.NotificationSound
import com.keephydrated.app.domain.model.UserSettings
import com.keephydrated.app.presentation.MainActivity
import com.keephydrated.app.receiver.WaterIntakeNotificationReceiver
import com.keephydrated.app.util.LocalizationUtils

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

    /**
     * Creates a horizontal glass tube / cylinder filled with water dynamically based on target percentage,
     * supporting localized Arabic-Indic numerals and typography.
     */
    fun createWaterTubeBitmap(
        currentMl: Int,
        goalMl: Int,
        percent: Int,
        isArabic: Boolean = false
    ): Bitmap {
        val width = 720
        val height = 180
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 1. Dark midnight water background card
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF0D1B2A.toInt()
        }
        val bgRect = RectF(0f, 0f, width.toFloat(), height.toFloat())
        canvas.drawRoundRect(bgRect, 22f, 22f, bgPaint)

        // 2. Tube dimensions & container
        val tubeLeft = 36f
        val tubeTop = 44f
        val tubeRight = width - 36f
        val tubeBottom = height - 44f
        val tubeHeight = tubeBottom - tubeTop
        val tubeCornerRadius = tubeHeight / 2f
        val tubeRect = RectF(tubeLeft, tubeTop, tubeRight, tubeBottom)

        // 3. Draw glass tube empty interior
        val tubeBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF1B3A5B.toInt()
        }
        canvas.drawRoundRect(tubeRect, tubeCornerRadius, tubeCornerRadius, tubeBgPaint)

        // 4. Fill Tube with Water gradient according to loaded percentage
        val fillFraction = (percent / 100f).coerceIn(0f, 1f)
        if (fillFraction > 0f) {
            val fillWidth = (tubeRight - tubeLeft) * fillFraction
            val waterRect = RectF(tubeLeft, tubeTop, tubeLeft + fillWidth, tubeBottom)

            canvas.save()
            val clipPath = Path().apply {
                addRoundRect(tubeRect, tubeCornerRadius, tubeCornerRadius, Path.Direction.CW)
            }
            canvas.clipPath(clipPath)

            val waterShader = LinearGradient(
                tubeLeft, tubeTop,
                tubeLeft + fillWidth, tubeBottom,
                intArrayOf(0xFF00E5FF.toInt(), 0xFF03A9F4.toInt(), 0xFF01579B.toInt()),
                null,
                Shader.TileMode.CLAMP
            )
            val waterPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = waterShader
            }
            canvas.drawRect(waterRect, waterPaint)

            // Surface meniscus / edge highlight
            val meniscusPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xCCFFFFFF.toInt()
                strokeWidth = 3.5f
                style = Paint.Style.STROKE
            }
            canvas.drawLine(tubeLeft + fillWidth - 2f, tubeTop + 2f, tubeLeft + fillWidth - 2f, tubeBottom - 2f, meniscusPaint)

            // Rising water bubbles
            val bubblePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0x99FFFFFF.toInt()
            }
            if (fillWidth > 60f) {
                canvas.drawCircle(tubeLeft + fillWidth * 0.28f, tubeTop + tubeHeight * 0.38f, 5f, bubblePaint)
                canvas.drawCircle(tubeLeft + fillWidth * 0.58f, tubeTop + tubeHeight * 0.62f, 6.5f, bubblePaint)
                canvas.drawCircle(tubeLeft + fillWidth * 0.84f, tubeTop + tubeHeight * 0.34f, 4.5f, bubblePaint)
            }

            canvas.restore()
        }

        // 5. Draw Glass Tube Outline & Highlights
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF81D4FA.toInt()
            strokeWidth = 4f
            style = Paint.Style.STROKE
        }
        canvas.drawRoundRect(tubeRect, tubeCornerRadius, tubeCornerRadius, borderPaint)

        // Glass reflection highlight along the top
        val reflectionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0x77FFFFFF.toInt()
            strokeWidth = 2.5f
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
        }
        canvas.drawLine(tubeLeft + tubeCornerRadius, tubeTop + 6f, tubeRight - tubeCornerRadius, tubeTop + 6f, reflectionPaint)

        // 6. Localized Text Overlay
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            setShadowLayer(4f, 1f, 1f, 0xCC000000.toInt())
        }
        val textY = tubeTop + (tubeHeight / 2f) + (textPaint.textSize / 3f)

        val formattedCurrent = LocalizationUtils.formatNumber(currentMl, isArabic)
        val formattedGoal = LocalizationUtils.formatNumber(goalMl, isArabic)
        val formattedPercent = LocalizationUtils.formatNumber(percent, isArabic)
        val unit = if (isArabic) "مل" else "ml"
        val pctSign = if (isArabic) "٪" else "%"
        val text = "$formattedCurrent / $formattedGoal $unit ($formattedPercent$pctSign)"

        canvas.drawText(text, width / 2f, textY, textPaint)

        return bitmap
    }

    fun buildReminderNotification(context: Context, settings: UserSettings): Notification {
        val isArabic = settings.language == "ar"
        val localizedContext = LocalizationUtils.getLocalizedContext(context, settings.language)

        val sound = NotificationSound.fromId(settings.notificationSound)
        val channelId = KeepHydratedApp.getChannelIdForSound(sound.id)
        val soundUri = KeepHydratedApp.getSoundUri(context, sound)
        val cupBitmap = getWaterCupBitmap(context)

        val quickAddAmount = if (settings.bottleModeEnabled) {
            settings.frequentIntakeMl
        } else {
            settings.defaultQuickAddMl
        }
        val formattedQuickAmount = LocalizationUtils.formatNumber(quickAddAmount, isArabic)

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

        val actionTitle = if (isArabic) "+ $formattedQuickAmount مل 💧" else "+ $quickAddAmount ml 💧"
        val quickAddAction = NotificationCompat.Action.Builder(
            R.drawable.ic_notification_water_cup,
            actionTitle,
            quickAddPendingIntent
        ).build()

        val title: String
        val bodyText: String

        if (settings.bottleModeEnabled) {
            val bottleStatus = settings.toBottleStatus()
            val formattedVol = LocalizationUtils.formatNumber(bottleStatus.volumeMl, isArabic)
            val formattedRem = LocalizationUtils.formatNumber(bottleStatus.remainingMl, isArabic)
            title = localizedContext.getString(R.string.bottle_reminder_title)
            bodyText = localizedContext.getString(
                R.string.bottle_reminder_msg,
                formattedVol,
                formattedRem
            )
        } else {
            title = localizedContext.getString(R.string.reminder_title)
            bodyText = localizedContext.getString(R.string.reminder_tap_text, formattedQuickAmount)
        }

        val wearableExtender = NotificationCompat.WearableExtender()
            .addAction(quickAddAction)
            .setContentAction(0)
            .setBridgeTag("keephydrated_water_reminder")

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification_water_cup)
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
        val isArabic = settings.language == "ar"
        val localizedContext = LocalizationUtils.getLocalizedContext(context, settings.language)

        val sound = NotificationSound.fromId(settings.notificationSound)
        val soundUri = KeepHydratedApp.getSoundUri(context, sound)
        val cupBitmap = getWaterCupBitmap(context)

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
            R.drawable.ic_notification_water_cup,
            localizedContext.getString(R.string.bottle_refill_action),
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
            .setSmallIcon(R.drawable.ic_notification_water_cup)
            .setColor(WATER_COLOR)
            .setContentTitle(localizedContext.getString(R.string.bottle_refill_title))
            .setContentText(localizedContext.getString(R.string.bottle_refill_msg))
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
     * Shows a confirmation notification with a horizontal water tube filled according to target percentage.
     */
    fun showIntakeLoggedConfirmation(
        context: Context,
        amountMl: Int,
        newTotalMl: Int,
        goalMl: Int,
        customTitle: String? = null,
        customMessage: String? = null,
        language: String = "en"
    ) {
        val isArabic = language == "ar"
        val localizedContext = LocalizationUtils.getLocalizedContext(context, language)

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

        val formattedAmount = LocalizationUtils.formatNumber(amountMl, isArabic)
        val formattedTotal = LocalizationUtils.formatNumber(newTotalMl, isArabic)
        val formattedGoal = LocalizationUtils.formatNumber(goalMl, isArabic)
        val formattedPercent = LocalizationUtils.formatNumber(percent, isArabic)

        val title = customTitle ?: localizedContext.getString(R.string.logged_confirmation_title, formattedAmount)
        val text = customMessage ?: localizedContext.getString(R.string.logged_confirmation_body, formattedTotal, formattedGoal, formattedPercent)
        val cupBitmap = getWaterCupBitmap(context)
        val tubeBitmap = createWaterTubeBitmap(newTotalMl, goalMl, percent, isArabic)

        val notification = NotificationCompat.Builder(context, KeepHydratedApp.SILENT_ACK_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_water_cup)
            .setColor(WATER_COLOR)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSilent(true)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setProgress(100, percent.coerceIn(0, 100), false)
            .setStyle(
                NotificationCompat.BigPictureStyle()
                    .bigPicture(tubeBitmap)
                    .setSummaryText(text)
            )
            .apply { cupBitmap?.let { setLargeIcon(it) } }
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(KeepHydratedApp.REMINDER_NOTIFICATION_ID, notification)
    }

    /**
     * Pushes a celebratory notification with custom celebration sound when the daily limit/goal is hit.
     */
    fun showCelebrationNotification(
        context: Context,
        goalMl: Int,
        celebrationSoundId: String? = null,
        language: String = "en"
    ) {
        val isArabic = language == "ar"
        val localizedContext = LocalizationUtils.getLocalizedContext(context, language)

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

        val formattedGoal = LocalizationUtils.formatNumber(goalMl, isArabic)

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification_water_cup)
            .setColor(WATER_COLOR)
            .setContentTitle(localizedContext.getString(R.string.celebration_title))
            .setContentText(localizedContext.getString(R.string.celebration_body, formattedGoal))
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
