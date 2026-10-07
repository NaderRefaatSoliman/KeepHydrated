package com.keephydrated.app.presentation.ui.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keephydrated.app.domain.repository.SettingsRepository
import com.keephydrated.app.domain.usecase.AddWaterIntakeUseCase
import com.keephydrated.app.domain.usecase.DeleteWaterIntakeUseCase
import com.keephydrated.app.domain.usecase.GetTodayHydrationUseCase
import com.keephydrated.app.domain.usecase.LogBottleDrinkUseCase
import com.keephydrated.app.domain.usecase.RefillBottleUseCase
import com.keephydrated.app.domain.usecase.UndoLastIntakeUseCase
import com.keephydrated.app.domain.util.HydrationCalculator
import com.keephydrated.app.notification.NotificationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getTodayHydrationUseCase: GetTodayHydrationUseCase,
    private val addWaterIntakeUseCase: AddWaterIntakeUseCase,
    private val deleteWaterIntakeUseCase: DeleteWaterIntakeUseCase,
    private val undoLastIntakeUseCase: UndoLastIntakeUseCase,
    private val settingsRepository: SettingsRepository,
    private val logBottleDrinkUseCase: LogBottleDrinkUseCase,
    private val refillBottleUseCase: RefillBottleUseCase,
    @ApplicationContext private val context: Context? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // Real-time observer monitoring local phone date to guarantee midnight reset
    private val dateTickerFlow: Flow<LocalDate> = flow {
        var lastDate = LocalDate.now()
        emit(lastDate)
        while (true) {
            delay(5_000)
            val currentDate = LocalDate.now()
            if (currentDate != lastDate) {
                lastDate = currentDate
                emit(currentDate)
            }
        }
    }

    init {
        observeHydrationAndSettings()
    }

    private fun observeHydrationAndSettings() {
        viewModelScope.launch {
            dateTickerFlow.flatMapLatest { today ->
                combine(
                    getTodayHydrationUseCase(today),
                    settingsRepository.getUserSettings()
                ) { summary, settings ->
                    val bottleStatus = settings.toBottleStatus()
                    val rec = HydrationCalculator.calculate(
                        age = settings.userAge,
                        sex = settings.userSex,
                        weightKg = settings.userWeightKg,
                        heightCm = settings.userHeightCm,
                        activityLevel = settings.userActivityLevel
                    )
                    val safeMaxDailyMl = rec.safeMaxDailyMl
                    val isLimitExceeded = summary.totalIntakeMl >= safeMaxDailyMl

                    HomeUiState(
                        currentIntakeMl = summary.totalIntakeMl,
                        dailyGoalMl = summary.goalMl,
                        progressPercentage = summary.progressPercentage,
                        isGoalAchieved = summary.isGoalAchieved,
                        todayIntakes = summary.intakes,
                        isLoading = false,
                        userMessage = _uiState.value.userMessage,
                        frequentIntakeMl = settings.frequentIntakeMl,
                        bottleStatus = bottleStatus,
                        isBottleMode = settings.bottleModeEnabled,
                        userSettings = settings,
                        safeMaxDailyMl = safeMaxDailyMl,
                        isLimitExceeded = isLimitExceeded
                    )
                }
            }.collectLatest { state ->
                _uiState.value = state
            }
        }
    }

    fun addWater(amountMl: Int) {
        viewModelScope.launch {
            try {
                val previousTotal = _uiState.value.currentIntakeMl
                val goal = _uiState.value.dailyGoalMl
                addWaterIntakeUseCase(amountMl)
                val newTotal = previousTotal + amountMl

                // If in bottle mode, also deduct from bottle
                if (_uiState.value.isBottleMode) {
                    settingsRepository.recordBottleDrink(amountMl)
                }

                if (previousTotal < goal && newTotal >= goal && goal > 0) {
                    context?.let { ctx ->
                        val settings = settingsRepository.getUserSettings().first()
                        NotificationHelper.showCelebrationNotification(
                            ctx,
                            goal,
                            settings.celebrationSound,
                            settings.language
                        )
                    }
                }

                val safeLimit = _uiState.value.safeMaxDailyMl
                if (newTotal >= safeLimit) {
                    val isArabic = _uiState.value.userSettings.language == "ar"
                    val warningMsg = if (isArabic) {
                        "⚠️ تحذير: شرب الماء فوق الحد الأقصى الآمن ($safeLimit مل)! الإجمالي: $newTotal مل."
                    } else {
                        "⚠️ Warning: Safe limit ($safeLimit ml) exceeded! Total: $newTotal ml."
                    }
                    _uiState.update { it.copy(userMessage = warningMsg) }

                    // Push high-priority red alert notification outside the app
                    context?.let { ctx ->
                        NotificationHelper.showToxicityAlertNotification(
                            ctx,
                            newTotal,
                            safeLimit,
                            _uiState.value.userSettings.language
                        )
                    }
                } else {
                    val isArabic = _uiState.value.userSettings.language == "ar"
                    val formattedAmount = LocalizationUtils.formatNumber(amountMl, isArabic)
                    val msg = if (isArabic) "تمت إضافة $formattedAmount مل 💧" else "Added $amountMl ml"
                    _uiState.update { it.copy(userMessage = msg) }
                }
            } catch (e: Exception) {
                val isArabic = _uiState.value.userSettings.language == "ar"
                val errMsg = if (isArabic) "فشل تسجيل الماء" else (e.message ?: "Failed to log water")
                _uiState.update { it.copy(userMessage = errMsg) }
            }
        }
    }

    fun drinkFromBottle(amountMl: Int? = null) {
        val amount = amountMl ?: _uiState.value.frequentIntakeMl
        viewModelScope.launch {
            try {
                logBottleDrinkUseCase(amount)
                val newTotal = _uiState.value.currentIntakeMl + amount
                val safeLimit = _uiState.value.safeMaxDailyMl
                if (newTotal >= safeLimit) {
                    val isArabic = _uiState.value.userSettings.language == "ar"
                    val warningMsg = if (isArabic) {
                        "⚠️ تحذير: شرب الماء فوق الحد الأقصى الآمن ($safeLimit مل)! الإجمالي: $newTotal مل."
                    } else {
                        "⚠️ Warning: Safe limit ($safeLimit ml) exceeded! Total: $newTotal ml."
                    }
                    _uiState.update { it.copy(userMessage = warningMsg) }

                    // Push high-priority red alert notification outside the app
                    context?.let { ctx ->
                        NotificationHelper.showToxicityAlertNotification(
                            ctx,
                            newTotal,
                            safeLimit,
                            _uiState.value.userSettings.language
                        )
                    }
                } else {
                    val isArabic = _uiState.value.userSettings.language == "ar"
                    val formattedAmount = LocalizationUtils.formatNumber(amount, isArabic)
                    val msg = if (isArabic) "تم شرب $formattedAmount مل من الزجاجة! 💧" else "Logged $amount ml from bottle! 💧"
                    _uiState.update { it.copy(userMessage = msg) }
                }
            } catch (e: Exception) {
                val isArabic = _uiState.value.userSettings.language == "ar"
                val errMsg = if (isArabic) "فشل تسجيل الشرب من الزجاجة" else (e.message ?: "Failed to record sip")
                _uiState.update { it.copy(userMessage = errMsg) }
            }
        }
    }

    fun refillBottle() {
        viewModelScope.launch {
            try {
                refillBottleUseCase()
                val isArabic = _uiState.value.userSettings.language == "ar"
                val msg = if (isArabic) "تمت إعادة تعبئة الزجاجة! بداية جديدة 🔄" else "Bottle refilled! Fresh start 🔄"
                _uiState.update { it.copy(userMessage = msg) }
            } catch (e: Exception) {
                val isArabic = _uiState.value.userSettings.language == "ar"
                val errMsg = if (isArabic) "فشل إعادة تعبئة الزجاجة" else (e.message ?: "Failed to refill bottle")
                _uiState.update { it.copy(userMessage = errMsg) }
            }
        }
    }

    fun deleteIntake(id: Long) {
        viewModelScope.launch {
            try {
                deleteWaterIntakeUseCase(id)
            } catch (e: Exception) {
                val isArabic = _uiState.value.userSettings.language == "ar"
                val errMsg = if (isArabic) "فشل حذف التسجيل" else "Failed to delete entry"
                _uiState.update { it.copy(userMessage = errMsg) }
            }
        }
    }

    fun undoLastIntake() {
        viewModelScope.launch {
            val undone = undoLastIntakeUseCase()
            val isArabic = _uiState.value.userSettings.language == "ar"
            if (undone) {
                val msg = if (isArabic) "تم التراجع عن آخر تسجيل" else "Last entry undone"
                _uiState.update { it.copy(userMessage = msg) }
            } else {
                val msg = if (isArabic) "لا يوجد تسجيل للتراجع عنه" else "No entry to undo"
                _uiState.update { it.copy(userMessage = msg) }
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }
}
