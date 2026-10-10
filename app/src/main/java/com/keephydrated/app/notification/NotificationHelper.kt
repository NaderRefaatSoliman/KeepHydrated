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
     * Converts vector drawable ic_water_cup into a crisp high-res Bitmap.
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
     * Converts Dr. Droppy mascot vector drawable into a crisp high-res Bitmap for the side largeIcon.
     */
    fun getDroppyBitmap(context: Context): Bitmap? {
        return try {
            val drawable = ContextCompat.getDrawable(context, R.drawable.ic_droppy_guide) ?: return null
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
     * rendered on a clean full white notification card featuring the exact app logo (cup with drop above it).
     */
    fun createWaterTubeBitmap(
        context: Context,
        currentMl: Int,
        goalMl: Int,
        percent: Int,
        isArabic: Boolean = false
    ): Bitmap {
        val width = 720
        val height = 190
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 1. Pure Full White Notification Box
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
        }
        val bgRect = RectF(0f, 0f, width.toFloat(), height.toFloat())
        canvas.drawRoundRect(bgRect, 20f, 20f, bgPaint)

        // Subtle Card Outline for contrast on light/dark themes
        val cardBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFE0E0E0.toInt()
            strokeWidth = 2f
            style = Paint.Style.STROKE
        }
        canvas.drawRoundRect(bgRect, 20f, 20f, cardBorderPaint)

        // 2. Draw App Logo (cup of water with a droplet above it)
        val cupDrawable = ContextCompat.getDrawable(context, R.drawable.ic_water_cup)
        if (cupDrawable != null) {
            val logoSize = 42
            val logoY = 10
            val logoX = if (isArabic) (width - 64) else 24
            cupDrawable.setBounds(logoX, logoY, logoX + logoSize, logoY + logoSize)
            cupDrawable.draw(canvas)
        }

        // 3. Header Text: Current Intake vs Goal
        val formattedCurrent = LocalizationUtils.formatNumber(currentMl, isArabic)
        val formattedGoal = LocalizationUtils.formatNumber(goalMl, isArabic)
        val formattedPercent = LocalizationUtils.formatNumber(percent, isArabic)
        val unit = if (isArabic) "مل" else "ml"
        val pctSign = if (isArabic) "٪" else "%"

        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF0277BD.toInt() // Deep water blue
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val headerText = "$formattedCurrent / $formattedGoal $unit ($formattedPercent$pctSign)"
        canvas.drawText(headerText, width / 2f, 42f, headerPaint)

        // 4. Horizontal Tube Container
        val tubeLeft = 36f
        val tubeTop = 68f
        val tubeRight = width - 36f
        val tubeBottom = 138f
        val tubeHeight = tubeBottom - tubeTop
        val tubeCornerRadius = tubeHeight / 2f
        val tubeRect = RectF(tubeLeft, tubeTop, tubeRight, tubeBottom)

        // Draw glass tube background track (soft pastel water tone)
        val tubeTrackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFE0F2FE.toInt()
        }
        canvas.drawRoundRect(tubeRect, tubeCornerRadius, tubeCornerRadius, tubeTrackPaint)

        // 4. Water-Blue Horizontal Filling
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
                intArrayOf(0xFF29B6F6.toInt(), 0xFF0288D1.toInt(), 0xFF01579B.toInt()),
                null,
                Shader.TileMode.CLAMP
            )
            val waterPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = waterShader
            }
            canvas.drawRect(waterRect, waterPaint)

            // Meniscus / wave edge line
            val meniscusPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xEEFFFFFF.toInt()
                strokeWidth = 3.5f
                style = Paint.Style.STROKE
            }
            canvas.drawLine(tubeLeft + fillWidth - 2f, tubeTop + 2f, tubeLeft + fillWidth - 2f, tubeBottom - 2f, meniscusPaint)

            // Rising water bubbles
            val bubblePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xAAFFFFFF.toInt()
            }
            if (fillWidth > 50f) {
                canvas.drawCircle(tubeLeft + fillWidth * 0.3f, tubeTop + tubeHeight * 0.4f, 5f, bubblePaint)
                canvas.drawCircle(tubeLeft + fillWidth * 0.6f, tubeTop + tubeHeight * 0.65f, 6.5f, bubblePaint)
                canvas.drawCircle(tubeLeft + fillWidth * 0.85f, tubeTop + tubeHeight * 0.35f, 4.5f, bubblePaint)
            }

            canvas.restore()
        }

        // 5. Glass Tube Border & Reflection highlights
        val tubeBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF0288D1.toInt()
            strokeWidth = 3f
            style = Paint.Style.STROKE
        }
        canvas.drawRoundRect(tubeRect, tubeCornerRadius, tubeCornerRadius, tubeBorderPaint)

        // Top edge shimmer reflection
        val reflectionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0x88FFFFFF.toInt()
            strokeWidth = 2.5f
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
        }
        canvas.drawLine(tubeLeft + tubeCornerRadius, tubeTop + 6f, tubeRight - tubeCornerRadius, tubeTop + 6f, reflectionPaint)

        // 6. Subtext / Progress status
        val subtextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF546E7A.toInt()
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
        }
        val statusText = if (percent >= 100) {
            if (isArabic) "🎉 اكتمل الهدف اليومي للترطيب!" else "🎉 Daily Hydration Goal Completed!"
        } else {
            if (isArabic) "متبقي $formattedPercent$pctSign نحو تحقيق الهدف اليومي" else "$formattedPercent$pctSign of daily hydration target reached"
        }
        canvas.drawText(statusText, width / 2f, 168f, subtextPaint)

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
        val unit = if (isArabic) "مل" else "ml"

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

        val actionTitle = if (isArabic) "+ $formattedQuickAmount $unit 💧" else "+ $quickAddAmount ml 💧"
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

        val droppyBitmap = getDroppyBitmap(context)
        (droppyBitmap ?: cupBitmap)?.let { builder.setLargeIcon(it) }

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
        val droppyBitmap = getDroppyBitmap(context)
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

        (droppyBitmap ?: cupBitmap)?.let { builder.setLargeIcon(it) }

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
        val droppyBitmap = getDroppyBitmap(context)
        val cupBitmap = getWaterCupBitmap(context)
        val tubeBitmap = createWaterTubeBitmap(context, newTotalMl, goalMl, percent, isArabic)

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
            .apply { (droppyBitmap ?: cupBitmap)?.let { setLargeIcon(it) } }
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
        val droppyBitmap = getDroppyBitmap(context)
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

        (droppyBitmap ?: cupBitmap)?.let { builder.setLargeIcon(it) }

        if (soundUri != null) {
            builder.setSound(soundUri)
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(KeepHydratedApp.CELEBRATION_NOTIFICATION_ID, builder.build())
    }

    /**
     * Pushes a high-priority red alert notification outside the app warning of water toxicity
     * and hyponatremia when intake approaches or exceeds the physiological safe maximum limit.
     */
    fun showToxicityAlertNotification(
        context: Context,
        currentMl: Int,
        maxLimitMl: Int,
        language: String
    ) {
        val isArabic = language == "ar"
        val formattedCurrent = LocalizationUtils.formatNumber(currentMl, isArabic)
        val formattedMax = LocalizationUtils.formatNumber(maxLimitMl, isArabic)
        val unit = if (isArabic) "مل" else "ml"

        val title = if (isArabic) "⚠️ تجاوزت الحد الأقصى الآمن للماء!" else "⚠️ Safe Daily Water Limit Exceeded!"
        val text = if (isArabic) {
            "إجمالي شربك الآن $formattedCurrent $unit متجاوزاً الحد الأقصى ($formattedMax $unit). خطر التسمم المائي! توقف عن الشرب وتجنب أكثر من ٨٠٠ مل في الساعة."
        } else {
            "Total intake is $formattedCurrent $unit, exceeding safe ceiling ($formattedMax $unit). Hyponatremia risk! Stop drinking and pace yourself."
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            3003,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val droppyBitmap = getDroppyBitmap(context)
        val cupBitmap = getWaterCupBitmap(context)

        val builder = NotificationCompat.Builder(context, KeepHydratedApp.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_water_cup)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setColor(0xFFD32F2F.toInt()) // Red Warning Style
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 400, 200, 400))

        (droppyBitmap ?: cupBitmap)?.let { builder.setLargeIcon(it) }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(3003, builder.build())
    }
}
