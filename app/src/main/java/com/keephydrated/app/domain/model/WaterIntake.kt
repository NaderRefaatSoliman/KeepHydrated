package com.keephydrated.app.domain.model

import java.time.LocalDateTime

data class WaterIntake(
    val id: Long = 0,
    val amountMl: Int,
    val timestamp: LocalDateTime = LocalDateTime.now()
)
