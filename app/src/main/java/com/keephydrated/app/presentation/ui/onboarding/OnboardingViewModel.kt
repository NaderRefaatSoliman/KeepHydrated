package com.keephydrated.app.presentation.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keephydrated.app.domain.repository.SettingsRepository
import com.keephydrated.app.domain.util.HydrationCalculator
import com.keephydrated.app.domain.util.HydrationRecommendation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingUiState(
    val currentStep: Int = 0,
    val name: String = "",
    val age: String = "28",
    val sex: String = "male",
    val weightKg: String = "70",
    val heightCm: String = "175",
    val activityLevel: String = "moderate",
    val recommendedGoalMl: Int = 2300,
    val safeMaxDailyMl: Int = 4200,
    val customGoalMl: Int = 2300,
    val isCompleted: Boolean = false
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    init {
        loadInitial()
    }

    private fun loadInitial() {
        viewModelScope.launch {
            try {
                val settings = settingsRepository.getUserSettings().first()
                val rec = HydrationCalculator.calculate(
                    age = settings.userAge,
                    sex = settings.userSex,
                    weightKg = settings.userWeightKg,
                    heightCm = settings.userHeightCm,
                    activityLevel = settings.userActivityLevel
                )
                _uiState.update {
                    it.copy(
                        name = settings.userName,
                        age = settings.userAge.toString(),
                        sex = settings.userSex,
                        weightKg = settings.userWeightKg.toInt().toString(),
                        heightCm = settings.userHeightCm.toInt().toString(),
                        activityLevel = settings.userActivityLevel,
                        recommendedGoalMl = rec.recommendedDailyMl,
                        safeMaxDailyMl = rec.safeMaxDailyMl,
                        customGoalMl = rec.recommendedDailyMl,
                        isCompleted = settings.isOnboardingCompleted
                    )
                }
            } catch (_: Exception) {}
        }
    }

    fun updateName(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun updateAge(age: String) {
        _uiState.update { it.copy(age = age) }
        recalculate()
    }

    fun updateSex(sex: String) {
        _uiState.update { it.copy(sex = sex) }
        recalculate()
    }

    fun updateWeight(weight: String) {
        _uiState.update { it.copy(weightKg = weight) }
        recalculate()
    }

    fun updateHeight(height: String) {
        _uiState.update { it.copy(heightCm = height) }
        recalculate()
    }

    fun updateActivity(activity: String) {
        _uiState.update { it.copy(activityLevel = activity) }
        recalculate()
    }

    fun updateCustomGoal(goalMl: Int) {
        _uiState.update { it.copy(customGoalMl = goalMl) }
    }

    private fun recalculate() {
        val age = _uiState.value.age.toIntOrNull() ?: 28
        val weight = _uiState.value.weightKg.toFloatOrNull() ?: 70f
        val height = _uiState.value.heightCm.toFloatOrNull() ?: 175f
        val rec = HydrationCalculator.calculate(
            age = age,
            sex = _uiState.value.sex,
            weightKg = weight,
            heightCm = height,
            activityLevel = _uiState.value.activityLevel
        )
        _uiState.update {
            it.copy(
                recommendedGoalMl = rec.recommendedDailyMl,
                safeMaxDailyMl = rec.safeMaxDailyMl,
                customGoalMl = rec.recommendedDailyMl
            )
        }
    }

    fun nextStep() {
        if (_uiState.value.currentStep < 4) {
            _uiState.update { it.copy(currentStep = it.currentStep + 1) }
        }
    }

    fun prevStep() {
        if (_uiState.value.currentStep > 0) {
            _uiState.update { it.copy(currentStep = it.currentStep - 1) }
        }
    }

    fun completeOnboarding(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val age = _uiState.value.age.toIntOrNull() ?: 28
                val weight = _uiState.value.weightKg.toFloatOrNull() ?: 70f
                val height = _uiState.value.heightCm.toFloatOrNull() ?: 175f
                val goal = _uiState.value.customGoalMl.coerceIn(500, 10000)

                settingsRepository.updateUserProfile(
                    name = _uiState.value.name.trim(),
                    age = age,
                    sex = _uiState.value.sex,
                    weightKg = weight,
                    heightCm = height,
                    activityLevel = _uiState.value.activityLevel,
                    recommendedGoalMl = goal
                )
                settingsRepository.completeOnboarding(true)
                _uiState.update { it.copy(isCompleted = true) }
                onSuccess()
            } catch (_: Exception) {
                onSuccess()
            }
        }
    }

    fun skipOnboarding(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                settingsRepository.skipOnboarding()
                onSuccess()
            } catch (_: Exception) {
                onSuccess()
            }
        }
    }
}
