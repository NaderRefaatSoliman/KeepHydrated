# KeepHydrated 💧 - Android Hydration Tracker & Reminder App

KeepHydrated is a modern, production-grade Android application designed to help users track their daily water intake and receive customizable notifications to maintain optimal hydration.

---

## 📱 Features

1. **Hydration Dashboard:**
   - **Dynamic Circular Progress Indicator:** Visualizes current intake vs daily goal with smooth animations, percentages, and milestone celebrations.
   - **Quick Add Containers:** One-tap logging for common volumes (+150ml Cup, +250ml Glass, +330ml Can, +500ml Bottle, +750ml Flask).
   - **Custom Water Logging:** Dialog to log any custom amount in milliliters.
   - **Undo Action:** Easily undo the most recent entry with a single tap.
   - **Today's Logs:** Real-time log list showing timestamps and amounts with delete support.

2. **Hydration History & Analytics:**
   - **Summary Stats:** Average daily intake calculation and total days goal achieved.
   - **Historical Log:** Grouped by date with completion percentage indicators and achievement tags.

3. **Intelligent Reminders & Settings:**
   - **Configurable Daily Target:** Default 2000 ml with quick chips (1500, 2000, 2500, 3000 ml) or custom input.
   - **Periodic Background Reminders:** Background notifications scheduled via Android `WorkManager`.
   - **Interval Selection:** Flexible intervals (1, 2, 3, or 4 hours).
   - **Quiet Hours Protection:** Prevents notifications from disturbing sleep during nighttime hours.
   - **Android 13+ Notification Permissions:** Seamless runtime permission requests for `POST_NOTIFICATIONS`.

---

## 🏗️ Architecture & Technology Stack

Built following **Clean Architecture** principles and **MVVM** (Model-View-ViewModel) with unidirectional data flow:

```
app/src/main/java/com/keephydrated/app/
├── KeepHydratedApp.kt              # Application class & Notification Channel setup
├── di/                             # Dagger-Hilt Dependency Injection modules
│   ├── DatabaseModule.kt           # Room DB & DAO providers
│   ├── DataStoreModule.kt          # Jetpack DataStore provider
│   └── RepositoryModule.kt         # Interface to implementation bindings
├── domain/                         # Pure business logic (framework independent)
│   ├── model/                      # WaterIntake, DailyHydrationSummary, UserSettings
│   ├── repository/                 # HydrationRepository, SettingsRepository interfaces
│   └── usecase/                    # AddWaterIntake, GetTodayHydration, Undo, etc.
├── data/                           # Data persistence & repository implementations
│   ├── local/                      # Room database, DAO, entity, type converters
│   ├── datastore/                  # UserPreferencesDataStore
│   └── repository/                 # HydrationRepositoryImpl, SettingsRepositoryImpl
├── worker/                         # Background processing
│   ├── WaterReminderWorker.kt      # CoroutineWorker for notifications
│   └── ReminderScheduler.kt        # WorkManager PeriodicWorkRequest scheduler
└── presentation/                   # Jetpack Compose UI & MVVM ViewModels
    ├── MainActivity.kt             # Single activity with Bottom Navigation
    ├── navigation/                 # Navigation items & route definitions
    ├── ui/
    │   ├── theme/                  # Material 3 colors, typography, theme
    │   ├── components/             # Circular progress, quick add cards, history items
    │   ├── home/                   # HomeScreen, HomeViewModel, HomeUiState
    │   ├── history/                # HistoryScreen, HistoryViewModel, HistoryUiState
    │   └── settings/               # SettingsScreen, SettingsViewModel, SettingsUiState
```

### Core Libraries
- **Language:** Kotlin 1.9
- **UI Toolkit:** Jetpack Compose + Material Design 3
- **Dependency Injection:** Dagger Hilt
- **Local Database:** Room Database with Coroutines Flow support
- **Settings Store:** Jetpack DataStore Preferences
- **Background Scheduling:** Android WorkManager + Hilt Worker Integration
- **Concurrency:** Kotlin Coroutines & Flow

---

## 🧪 Testing Suite

Thorough unit test coverage across Domain, Data, Worker, and Presentation layers:

- **`AddWaterIntakeUseCaseTest`:** Validates positive input validation, repository calls, and boundary conditions.
- **`GetTodayHydrationUseCaseTest`:** Validates accurate aggregation of total intakes and goal progress calculation via Turbine.
- **`SaveUserSettingsUseCaseTest`:** Verifies bounds checking (e.g. 500ml - 10000ml goal limits) and repository delegation.
- **`HydrationRepositoryImplTest`:** Verifies Room entity to domain mapping and deletion logic.
- **`HomeViewModelTest`:** Verifies UI state emissions and user action handlers.
- **`HistoryViewModelTest`:** Verifies daily averages and streak/goal calculations.
- **`SettingsViewModelTest`:** Verifies setting persistence and WorkManager rescheduling triggers.
- **`QuietHoursLogicTest`:** Validates day/night time calculations and quiet-hour boundary scenarios.

---

## 🚀 Building & Running

1. **In Android Studio:**
   - Open Android Studio (Hedgehog 2023.1.1+ or newer recommended).
   - Select **File -> Open...** and navigate to `C:\WorkSpace\AI\Projects\Android\KeepHydrated`.
   - Allow Gradle to sync the project dependencies.
   - Select an emulator or physical device running Android 8.0 (API 26) or higher.
   - Click **Run** (`Shift + F10`).

2. **Via Gradle CLI:**
   ```bash
   ./gradlew assembleDebug
   ./gradlew test
   ```
