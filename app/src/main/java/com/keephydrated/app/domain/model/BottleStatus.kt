package com.keephydrated.app.domain.model

data class BottleStatus(
    val isEnabled: Boolean = false,
    val volumeMl: Int = 750,
    val drankMl: Int = 0,
    val targetDurationMinutes: Int = 180,
    val startTimeMillis: Long = 0L,
    val refillCount: Int = 0
) {
    val remainingMl: Int
        get() = (volumeMl - drankMl).coerceAtLeast(0)

    val progressFraction: Float
        get() = if (volumeMl > 0) (drankMl.toFloat() / volumeMl.toFloat()).coerceIn(0f, 1f) else 0f

    val elapsedMinutes: Int
        get() {
            if (startTimeMillis <= 0L) return 0
            val elapsed = (System.currentTimeMillis() - startTimeMillis) / (60 * 1000L)
            return elapsed.toInt().coerceAtLeast(0)
        }

    val remainingMinutes: Int
        get() = (targetDurationMinutes - elapsedMinutes).coerceAtLeast(0)

    val isTimeExpired: Boolean
        get() = startTimeMillis > 0L && elapsedMinutes >= targetDurationMinutes

    val isBottleEmpty: Boolean
        get() = remainingMl == 0 && drankMl > 0

    val needsRefill: Boolean
        get() = isTimeExpired || isBottleEmpty
}
