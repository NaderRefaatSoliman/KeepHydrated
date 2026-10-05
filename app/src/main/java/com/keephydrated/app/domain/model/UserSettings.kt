package com.keephydrated.app.domain.model

enum class ReminderMode {
    INTERVAL,
    CUSTOM_ROUTINE
}

enum class NotificationSound(
    val id: String,
    val displayName: String,
    val description: String,
    val resName: String
) {
    WATER_DROP("water_drop", "Water Droplet", "Crisp, refreshing single water drop", "water_drop"),
    WATER_POUR("water_pour", "Pouring Water", "Fresh stream pouring into a glass", "water_pour"),
    GENTLE_STREAM("gentle_stream", "Gentle Stream", "Peaceful flowing brook", "gentle_stream"),
    WATER_BUBBLE("water_bubble", "Water Bubbles", "Playful rising water bubbles", "water_bubble"),
    OCEAN_WAVE("ocean_wave", "Ocean Wave", "Soothing coastal wave surge", "ocean_wave"),
    SYSTEM_DEFAULT("system_default", "System Default", "Standard device alert tone", "");

    companion object {
        fun fromId(id: String?): NotificationSound {
            return entries.find { it.id == id } ?: WATER_DROP
        }
    }
}

data class UserSettings(
    val dailyGoalMl: Int = 2000,
    val reminderIntervalHours: Int = 2,
    val reminderIntervalMinutes: Int = if (reminderIntervalHours > 0) reminderIntervalHours * 60 else 120,
    val remindersEnabled: Boolean = true,
    val startHour: Int = 8,  // 8:00 AM (Waking time)
    val endHour: Int = 22,    // 10:00 PM (Bedtime / sleep time)
    val reminderMode: ReminderMode = ReminderMode.INTERVAL,
    val customReminderHours: Set<Int> = setOf(9, 12, 15, 18, 21),
    val notificationSound: String = NotificationSound.WATER_DROP.id,
    val defaultQuickAddMl: Int = 250,
    val quickAddOnNotificationClick: Boolean = false
)
