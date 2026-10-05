package com.keephydrated.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentResolver
import android.content.Context
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.keephydrated.app.domain.model.CelebrationSound
import com.keephydrated.app.domain.model.NotificationSound
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class KeepHydratedApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels(this)
    }

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "hydration_reminders_channel"
        const val SILENT_ACK_CHANNEL_ID = "hydration_silent_ack_channel"
        const val CELEBRATION_CHANNEL_ID = "hydration_celebration_channel"
        const val CHANNEL_PREFIX = "hydration_channel_"
        const val CELEBRATION_CHANNEL_PREFIX = "hydration_celebration_"

        const val REMINDER_NOTIFICATION_ID = 1001
        const val CELEBRATION_NOTIFICATION_ID = 1002

        fun getChannelIdForSound(soundId: String?): String {
            if (soundId.isNullOrBlank() || soundId == NotificationSound.SYSTEM_DEFAULT.id) {
                return NOTIFICATION_CHANNEL_ID
            }
            return "${CHANNEL_PREFIX}${soundId}"
        }

        fun getCelebrationChannelIdForSound(soundId: String?): String {
            if (soundId.isNullOrBlank() || soundId == CelebrationSound.SYSTEM_DEFAULT.id) {
                return CELEBRATION_CHANNEL_ID
            }
            return "${CELEBRATION_CHANNEL_PREFIX}${soundId}"
        }

        fun getSoundUri(context: Context, sound: NotificationSound): Uri? {
            if (sound == NotificationSound.SYSTEM_DEFAULT || sound.resName.isBlank()) {
                return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            }
            val resId = context.resources.getIdentifier(sound.resName, "raw", context.packageName)
            return if (resId != 0) {
                Uri.parse("${ContentResolver.SCHEME_ANDROID_RESOURCE}://${context.packageName}/$resId")
            } else {
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            }
        }

        fun getCelebrationSoundUri(context: Context, sound: CelebrationSound): Uri? {
            if (sound == CelebrationSound.SYSTEM_DEFAULT || sound.resName.isBlank()) {
                return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            }
            val resId = context.resources.getIdentifier(sound.resName, "raw", context.packageName)
            return if (resId != 0) {
                Uri.parse("${ContentResolver.SCHEME_ANDROID_RESOURCE}://${context.packageName}/$resId")
            } else {
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            }
        }

        fun createNotificationChannels(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build()

                // Default legacy channel
                val defaultChannel = NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    context.getString(R.string.channel_name),
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = context.getString(R.string.channel_description)
                    enableVibration(true)
                }
                notificationManager.createNotificationChannel(defaultChannel)

                // Dedicated silent acknowledgment channel (for logged intake confirmations)
                val silentAckChannel = NotificationChannel(
                    SILENT_ACK_CHANNEL_ID,
                    "Intake Confirmations",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Silent confirmations when water intake is logged"
                    enableVibration(false)
                    setSound(null, null)
                }
                notificationManager.createNotificationChannel(silentAckChannel)

                // Dedicated reminder channels per custom water sound
                NotificationSound.entries.forEach { sound ->
                    val channelId = getChannelIdForSound(sound.id)
                    if (channelId != NOTIFICATION_CHANNEL_ID) {
                        val channelName = "${context.getString(R.string.channel_name)} - ${sound.displayName}"
                        val channel = NotificationChannel(
                            channelId,
                            channelName,
                            NotificationManager.IMPORTANCE_HIGH
                        ).apply {
                            description = "${context.getString(R.string.channel_description)} (${sound.displayName})"
                            enableVibration(true)
                            val soundUri = getSoundUri(context, sound)
                            if (soundUri != null) {
                                setSound(soundUri, audioAttributes)
                            }
                        }
                        notificationManager.createNotificationChannel(channel)
                    }
                }

                // Default celebration channel
                val defaultCelebrationChannel = NotificationChannel(
                    CELEBRATION_CHANNEL_ID,
                    "Goal Celebrations",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Celebrations when daily water intake goal is reached"
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 250, 150, 250, 150, 400)
                    val resId = context.resources.getIdentifier("celebration", "raw", context.packageName)
                    if (resId != 0) {
                        val celebrationSoundUri =
                            Uri.parse("${ContentResolver.SCHEME_ANDROID_RESOURCE}://${context.packageName}/$resId")
                        setSound(celebrationSoundUri, audioAttributes)
                    }
                }
                notificationManager.createNotificationChannel(defaultCelebrationChannel)

                // Celebration channels per celebration sound
                CelebrationSound.entries.forEach { sound ->
                    val channelId = getCelebrationChannelIdForSound(sound.id)
                    if (channelId != CELEBRATION_CHANNEL_ID) {
                        val channelName = "Goal Celebration - ${sound.displayName}"
                        val channel = NotificationChannel(
                            channelId,
                            channelName,
                            NotificationManager.IMPORTANCE_HIGH
                        ).apply {
                            description = sound.description
                            enableVibration(true)
                            vibrationPattern = longArrayOf(0, 250, 150, 250, 150, 400)
                            val soundUri = getCelebrationSoundUri(context, sound)
                            if (soundUri != null) {
                                setSound(soundUri, audioAttributes)
                            }
                        }
                        notificationManager.createNotificationChannel(channel)
                    }
                }
            }
        }
    }
}
