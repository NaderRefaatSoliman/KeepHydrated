package com.keephydrated.app.domain.usecase

import com.keephydrated.app.domain.model.WaterIntake
import com.keephydrated.app.domain.repository.HydrationRepository
import com.keephydrated.app.domain.repository.SettingsRepository
import java.time.LocalDateTime
import javax.inject.Inject

class LogBottleDrinkUseCase @Inject constructor(
    private val hydrationRepository: HydrationRepository,
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(amountMl: Int) {
        if (amountMl <= 0) return
        // 1. Record intake in daily hydration history
        hydrationRepository.insertIntake(
            WaterIntake(
                amountMl = amountMl,
                timestamp = LocalDateTime.now()
            )
        )
        // 2. Update bottle tracking progress
        settingsRepository.recordBottleDrink(amountMl)
    }
}
