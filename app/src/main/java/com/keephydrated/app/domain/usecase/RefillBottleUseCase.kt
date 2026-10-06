package com.keephydrated.app.domain.usecase

import com.keephydrated.app.domain.repository.SettingsRepository
import javax.inject.Inject

class RefillBottleUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke() {
        settingsRepository.refillBottle()
    }
}
