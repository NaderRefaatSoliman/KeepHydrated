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
    WATER_DROP_ECHO("water_drop_echo", "Water Drop Echo", "Resonant droplet with serene cavern echoes", "water_drop_echo"),
    WATER_FLOW("water_flow", "Natural Water Flow", "Continuous peaceful stream flowing over smooth pebbles", "water_flow"),
    BOTTLE_FILL("bottle_fill", "Bottle Filling", "Rising water pitch filling a bottle", "bottle_fill"),
    GENTLE_STREAM("gentle_stream", "Gentle Stream", "Peaceful flowing brook", "gentle_stream"),
    WATER_BUBBLE("water_bubble", "Water Bubbles", "Playful rising water bubbles", "water_bubble"),
    WATER_RAIN("water_rain", "Rainfall Drops", "Soothing raindrops tapping on fresh water", "water_rain"),
    WATER_POUR("water_pour", "Pouring Water", "Fresh stream pouring into a glass", "water_pour"),
    WATER_SPLASH("water_splash", "Water Splash", "Dynamic refreshing water splash", "water_splash"),
    WATER_SPILL("water_spill", "Spilling Water", "Dynamic splash of spilling water", "water_spill"),
    OCEAN_WAVE("ocean_wave", "Ocean Wave", "Soothing coastal wave surge", "ocean_wave"),
    SYSTEM_DEFAULT("system_default", "System Default", "Standard device alert tone", "");

    fun getLocalizedDisplayName(isArabic: Boolean): String {
        return if (isArabic) {
            when (this) {
                WATER_DROP -> "قطرة ماء"
                WATER_DROP_ECHO -> "صدى قطرة ماء"
                WATER_FLOW -> "تدفق الماء الطبيعي"
                BOTTLE_FILL -> "تعبئة القارورة بالماء"
                GENTLE_STREAM -> "جدول ماء هادئ"
                WATER_BUBBLE -> "فقاعات الماء"
                WATER_RAIN -> "قطرات المطر"
                WATER_POUR -> "صب الماء في الكوب"
                WATER_SPLASH -> "رذاذ الماء المنعش"
                WATER_SPILL -> "تدفق الماء المنسكب"
                OCEAN_WAVE -> "أمواج المحيط"
                SYSTEM_DEFAULT -> "نغمة النظام الافتراضية"
            }
        } else {
            displayName
        }
    }

    fun getLocalizedDescription(isArabic: Boolean): String {
        return if (isArabic) {
            when (this) {
                WATER_DROP -> "صوت نقي وواضح لقطرة ماء منعشة"
                WATER_DROP_ECHO -> "قطرة ماء رنانة مع صدى هادئ وجميل"
                WATER_FLOW -> "خرير ماء رقراق متدفق بين الحصى"
                BOTTLE_FILL -> "صوت تصاعدي طبيعي لملء قارورة بالماء"
                GENTLE_STREAM -> "مجرى مائي لطيف وهادئ في الطبيعة"
                WATER_BUBBLE -> "فقاعات مائية متصاعدة ومرحة"
                WATER_RAIN -> "زخات مطر هادئة تتساقط على الماء"
                WATER_POUR -> "تدفق منعش يصب الماء في الكوب"
                WATER_SPLASH -> "رذاذ ماء متناثر وحيوي"
                WATER_SPILL -> "صوت تدفق ماء منسكب ومتحرك"
                OCEAN_WAVE -> "مد وجزر ساحلي مهدئ للأعصاب"
                SYSTEM_DEFAULT -> "نغمة الإشعارات القياسية للهاتف"
            }
        } else {
            description
        }
    }

    companion object {
        fun fromId(id: String?): NotificationSound {
            return entries.find { it.id == id } ?: WATER_DROP
        }
    }
}

enum class CelebrationSound(
    val id: String,
    val displayName: String,
    val description: String,
    val resName: String
) {
    CHIME_FANFARE("celebration", "Trophy Chime", "Sparkling arpeggio victory chime", "celebration"),
    VICTORY_SPLASH("celebration_splash", "Victory Splash", "Triumphant splash & celebration chord", "celebration_splash"),
    SPARKLING_BUBBLES("celebration_bubbles", "Sparkling Bubbles", "Joyful bubbling cascade & bright bells", "celebration_bubbles"),
    OCEAN_TRIUMPH("celebration_wave", "Ocean Triumph", "Uplifting coastal surge with soaring chimes", "celebration_wave"),
    SYSTEM_DEFAULT("system_default", "System Default", "Standard celebration notification", "");

    fun getLocalizedDisplayName(isArabic: Boolean): String {
        return if (isArabic) {
            when (this) {
                CHIME_FANFARE -> "نغمة كأس النصر"
                VICTORY_SPLASH -> "رذاذ النصر المنعش"
                SPARKLING_BUBBLES -> "فقاعات احتفالية مبهرة"
                OCEAN_TRIUMPH -> "هدير المحيط المنتصر"
                SYSTEM_DEFAULT -> "نغمة النظام الافتراضية"
            }
        } else {
            displayName
        }
    }

    fun getLocalizedDescription(isArabic: Boolean): String {
        return if (isArabic) {
            when (this) {
                CHIME_FANFARE -> "رنين نصر متألق عند إكمال الهدف"
                VICTORY_SPLASH -> "موجة رذاذ مرحة واحتفالية"
                SPARKLING_BUBBLES -> "فقاعات مائية مبهجة مع أجراس"
                OCEAN_TRIUMPH -> "موجة صاعدة ملهمة مع أصوات رنانة"
                SYSTEM_DEFAULT -> "تنبيه الهاتف الافتراضي"
            }
        } else {
            description
        }
    }

    companion object {
        fun fromId(id: String?): CelebrationSound {
            return entries.find { it.id == id } ?: CHIME_FANFARE
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
    val celebrationSound: String = CelebrationSound.CHIME_FANFARE.id,
    val defaultQuickAddMl: Int = 250,
    val quickAddOnNotificationClick: Boolean = true,
    val language: String = "en",
    val frequentIntakeMl: Int = 250,
    val bottleModeEnabled: Boolean = false,
    val bottleVolumeMl: Int = 750,
    val bottleTargetDurationMinutes: Int = 180,
    val bottleStartTimeMillis: Long = 0L,
    val bottleDrankMl: Int = 0,
    val bottleRefillCount: Int = 0,
    // Personalized Profile & Onboarding
    val userName: String = "",
    val userAge: Int = 28,
    val userSex: String = "male", // "male" or "female"
    val userWeightKg: Float = 70f,
    val userHeightCm: Float = 175f,
    val userActivityLevel: String = "moderate", // "sedentary", "light", "moderate", "intense"
    val isOnboardingCompleted: Boolean = false,
    val isOnboardingSkipped: Boolean = false,
    val droppyTipsEnabled: Boolean = true,
    val remindAfterGoalReached: Boolean = false,
    val themeMode: String = "system" // "system", "light", "dark"
) {
    fun toBottleStatus(): BottleStatus {
        return BottleStatus(
            isEnabled = bottleModeEnabled,
            volumeMl = bottleVolumeMl,
            drankMl = bottleDrankMl,
            targetDurationMinutes = bottleTargetDurationMinutes,
            startTimeMillis = bottleStartTimeMillis,
            refillCount = bottleRefillCount
        )
    }
}
