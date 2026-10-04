package com.keephydrated.app.domain.usecase

import com.keephydrated.app.domain.model.DailyHydrationSummary
import com.keephydrated.app.domain.repository.HydrationRepository
import com.keephydrated.app.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetHydrationHistoryUseCase @Inject constructor(
    private val hydrationRepository: HydrationRepository,
    private val settingsRepository: SettingsRepository
) {
    operator fun invoke(): Flow<List<DailyHydrationSummary>> {
        return combine(
            hydrationRepository.getAllIntakes(),
            settingsRepository.getUserSettings()
        ) { allIntakes, settings ->
            allIntakes
                .groupBy { it.timestamp.toLocalDate() }
                .map { (date, intakes) ->
                    DailyHydrationSummary(
                        date = date,
                        totalIntakeMl = intakes.sumOf { it.amountMl },
                        goalMl = settings.dailyGoalMl,
                        intakes = intakes
                    )
                }
                .sortedByDescending { it.date }
        }
    }
}
