package com.keephydrated.app.presentation.ui.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.keephydrated.app.R
import com.keephydrated.app.domain.model.NotificationSound
import com.keephydrated.app.domain.model.ReminderMode
import com.keephydrated.app.presentation.ui.theme.BluePrimary
import com.keephydrated.app.presentation.ui.theme.CyanSecondary
import com.keephydrated.app.util.BatteryOptimizationHelper
import com.keephydrated.app.util.LocalizationUtils

enum class SettingsCategory(
    @StringRes val titleResId: Int,
    @StringRes val descResId: Int,
    val icon: ImageVector
) {
    PROFILE(R.string.category_profile, R.string.desc_category_profile, Icons.Default.Tune),
    GUIDE(R.string.category_guide, R.string.desc_category_guide, Icons.Default.Info),
    GOAL(R.string.category_goal, R.string.desc_category_goal, Icons.Default.TrackChanges),
    BOTTLE(R.string.category_bottle, R.string.desc_category_bottle, Icons.Default.LocalDrink),
    REMINDERS(R.string.category_reminders, R.string.desc_category_reminders, Icons.Default.Schedule),
    SOUNDS(R.string.category_sounds, R.string.desc_category_sounds, Icons.Default.Notifications),
    LANGUAGE(R.string.category_language, R.string.desc_category_language, Icons.Default.Language),
    ABOUT(R.string.category_about, R.string.desc_category_about, Icons.Default.Info)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val isArabic = LocalLayoutDirection.current == LayoutDirection.Rtl

    var selectedCategory by remember { mutableStateOf<SettingsCategory?>(null) }

    var showGoalDialog by remember { mutableStateOf(false) }
    var showCustomIntervalDialog by remember { mutableStateOf(false) }
    var showWakingDayDialog by remember { mutableStateOf(false) }

    var isBatteryOptimized by remember {
        mutableStateOf(!BatteryOptimizationHelper.isIgnoringBatteryOptimizations(context))
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isBatteryOptimized = !BatteryOptimizationHelper.isIgnoringBatteryOptimizations(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.toggleReminders(true)
        }
    }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessage()
        }
    }

    // Handle back button when inside a category detail screen
    BackHandler(enabled = selectedCategory != null) {
        selectedCategory = null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (selectedCategory != null) {
                            stringResource(selectedCategory!!.titleResId)
                        } else {
                            stringResource(R.string.settings)
                        },
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    if (selectedCategory != null) {
                        IconButton(onClick = { selectedCategory = null }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.back)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            if (selectedCategory == null) {
                // ==========================================
                // MAIN SETTINGS: LIST OF MAIN CATEGORIES
                // ==========================================
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(SettingsCategory.entries.toTypedArray()) { category ->
                        SettingsCategoryItem(
                            category = category,
                            summary = getCategorySummary(category, uiState, isArabic),
                            onClick = { selectedCategory = category }
                        )
                    }
                }
            } else {
                // ==========================================
                // DETAIL SCREEN FOR SELECTED CATEGORY
                // ==========================================
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    when (selectedCategory!!) {
                        SettingsCategory.PROFILE -> {
                            item {
                                ProfileSettingsDetail(
                                    uiState = uiState,
                                    isArabic = isArabic,
                                    onSaveProfile = { name, age, sex, weight, height, activity, goal ->
                                        viewModel.updateUserProfile(name, age, sex, weight, height, activity, goal)
                                        selectedCategory = null
                                    }
                                )
                            }
                        }
                        SettingsCategory.GUIDE -> {
                            item {
                                com.keephydrated.app.presentation.ui.guide.HealthGuideScreen(
                                    userSettings = uiState.toUserSettings(),
                                    onBackClick = { selectedCategory = null }
                                )
                            }
                        }
                        SettingsCategory.GOAL -> {
                            item {
                                GoalSettingsDetail(
                                    uiState = uiState,
                                    isArabic = isArabic,
                                    onEditGoalClick = { showGoalDialog = true },
                                    onFrequentAmountSelect = { viewModel.updateFrequentIntakeMl(it) },
                                    onQuickAddToggle = { viewModel.updateQuickAddOnNotificationClick(it) }
                                )
                            }
                        }
                        SettingsCategory.BOTTLE -> {
                            item {
                                BottleSettingsDetail(
                                    uiState = uiState,
                                    isArabic = isArabic,
                                    onToggleBottleMode = { viewModel.updateBottleModeEnabled(it) },
                                    onBottleConfigChange = { vol, dur -> viewModel.updateBottleConfig(vol, dur) }
                                )
                            }
                        }
                        SettingsCategory.REMINDERS -> {
                            item {
                                RemindersSettingsDetail(
                                    uiState = uiState,
                                    isArabic = isArabic,
                                    isBatteryOptimized = isBatteryOptimized,
                                    context = context,
                                    onToggleReminders = { enabled ->
                                        if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                            val hasPermission = ContextCompat.checkSelfPermission(
                                                context,
                                                Manifest.permission.POST_NOTIFICATIONS
                                            ) == PackageManager.PERMISSION_GRANTED
                                            if (!hasPermission) {
                                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                                return@RemindersSettingsDetail
                                            }
                                        }
                                        viewModel.toggleReminders(enabled)
                                    },
                                    onModeChange = { viewModel.updateReminderMode(it) },
                                    onIntervalChange = { viewModel.updateReminderIntervalMinutes(it) },
                                    onCustomIntervalClick = { showCustomIntervalDialog = true },
                                    onToggleHour = { viewModel.toggleCustomReminderHour(it) },
                                    onWakingDayPresetClick = { showWakingDayDialog = true }
                                )
                            }
                        }
                        SettingsCategory.SOUNDS -> {
                            item {
                                SoundsSettingsDetail(
                                    uiState = uiState,
                                    isArabic = isArabic,
                                    context = context,
                                    onSelectSound = { viewModel.updateNotificationSound(it) },
                                    onPreviewSound = { viewModel.previewSound(context, it) },
                                    onSendTestAlert = { viewModel.sendTestNotification(context) }
                                )
                            }
                        }
                        SettingsCategory.LANGUAGE -> {
                            item {
                                LanguageSettingsDetail(
                                    uiState = uiState,
                                    onSelectLanguage = { viewModel.updateLanguage(it) }
                                )
                            }
                        }
                        SettingsCategory.ABOUT -> {
                            item {
                                AboutSettingsDetail()
                            }
                        }
                    }
                }
            }
        }
    }

    if (showGoalDialog) {
        DailyGoalDialog(
            currentGoal = uiState.dailyGoalMl,
            onDismiss = { showGoalDialog = false },
            onConfirm = { newGoal ->
                viewModel.updateDailyGoal(newGoal)
                showGoalDialog = false
            }
        )
    }

    if (showCustomIntervalDialog) {
        CustomIntervalDialog(
            currentMinutes = uiState.reminderIntervalMinutes,
            onDismiss = { showCustomIntervalDialog = false },
            onConfirm = { minutes ->
                viewModel.updateReminderIntervalMinutes(minutes)
                showCustomIntervalDialog = false
            }
        )
    }

    if (showWakingDayDialog) {
        WakingDayScheduleDialog(
            currentStartHour = uiState.startHour,
            currentEndHour = uiState.endHour,
            onDismiss = { showWakingDayDialog = false },
            onConfirm = { wakeHour, sleepHour, stepHours ->
                viewModel.applyWakingDayPreset(wakeHour, sleepHour, stepHours)
                showWakingDayDialog = false
            }
        )
    }
}

@Composable
fun SettingsCategoryItem(
    category: SettingsCategory,
    summary: String,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = BluePrimary.copy(alpha = 0.12f),
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = category.icon,
                        contentDescription = null,
                        tint = BluePrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(category.titleResId),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun getCategorySummary(
    category: SettingsCategory,
    uiState: SettingsUiState,
    isArabic: Boolean
): String {
    return when (category) {
        SettingsCategory.PROFILE -> {
            val weight = LocalizationUtils.formatNumber(uiState.userWeightKg.toInt(), isArabic)
            val age = LocalizationUtils.formatNumber(uiState.userAge, isArabic)
            val wUnit = if (isArabic) "كجم" else "kg"
            val aUnit = if (isArabic) "سنة" else "yrs"
            "$weight $wUnit • $age $aUnit"
        }
        SettingsCategory.GUIDE -> {
            if (isArabic) "نصائح الجفاف وأمان التسمم المائي" else "Dehydration & Toxicity Safety"
        }
        SettingsCategory.GOAL -> {
            val goal = LocalizationUtils.formatNumber(uiState.dailyGoalMl, isArabic)
            val frequent = LocalizationUtils.formatNumber(uiState.frequentIntakeMl, isArabic)
            val unit = if (isArabic) "مل" else "ml"
            "$goal $unit • $frequent $unit"
        }
        SettingsCategory.BOTTLE -> {
            if (uiState.bottleModeEnabled) {
                val vol = LocalizationUtils.formatNumber(uiState.bottleVolumeMl, isArabic)
                val hours = LocalizationUtils.formatNumber(uiState.bottleTargetDurationMinutes / 60, isArabic)
                val unit = if (isArabic) "مل" else "ml"
                val hUnit = if (isArabic) "ساعات" else "h"
                "$vol $unit • $hours $hUnit"
            } else {
                if (isArabic) "معطل" else "Disabled"
            }
        }
        SettingsCategory.REMINDERS -> {
            if (uiState.remindersEnabled) {
                if (uiState.reminderMode == ReminderMode.INTERVAL) {
                    val hours = uiState.reminderIntervalMinutes / 60
                    if (hours >= 1) {
                        val h = LocalizationUtils.formatNumber(hours, isArabic)
                        if (isArabic) "كل $h ساعات" else "Every $h hrs"
                    } else {
                        val m = LocalizationUtils.formatNumber(uiState.reminderIntervalMinutes, isArabic)
                        if (isArabic) "كل $m دقيقة" else "Every $m mins"
                    }
                } else {
                    val count = LocalizationUtils.formatNumber(uiState.customReminderHours.size, isArabic)
                    if (isArabic) "جدول روتيني ($count أوقات)" else "Routine ($count set)"
                }
            } else {
                if (isArabic) "معطل" else "Disabled"
            }
        }
        SettingsCategory.SOUNDS -> {
            val sound = NotificationSound.fromId(uiState.notificationSound)
            sound.displayName
        }
        SettingsCategory.LANGUAGE -> {
            if (uiState.language == "ar") "العربية (Arabic)" else "English"
        }
        SettingsCategory.ABOUT -> {
            "KeepHydrated v2.0.0"
        }
    }
}

@Composable
fun ProfileSettingsDetail(
    uiState: SettingsUiState,
    isArabic: Boolean,
    onSaveProfile: (name: String, age: Int, sex: String, weight: Float, height: Float, activity: String, goal: Int) -> Unit
) {
    var name by remember { mutableStateOf(uiState.userName) }
    var ageStr by remember { mutableStateOf(uiState.userAge.toString()) }
    var sex by remember { mutableStateOf(uiState.userSex) }
    var weightStr by remember { mutableStateOf(uiState.userWeightKg.toInt().toString()) }
    var heightStr by remember { mutableStateOf(uiState.userHeightCm.toInt().toString()) }
    var activity by remember { mutableStateOf(uiState.userActivityLevel) }

    val age = ageStr.toIntOrNull() ?: 28
    val weight = weightStr.toFloatOrNull() ?: 70f
    val height = heightStr.toFloatOrNull() ?: 175f

    val rec = com.keephydrated.app.domain.util.HydrationCalculator.calculate(
        age = age,
        sex = sex,
        weightKg = weight,
        heightCm = height,
        activityLevel = activity
    )
    val formattedGoal = LocalizationUtils.formatNumber(rec.recommendedDailyMl, isArabic)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.step_profile_title),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = BluePrimary)
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.label_name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = ageStr,
                    onValueChange = { ageStr = it },
                    label = { Text(stringResource(R.string.label_age)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = weightStr,
                    onValueChange = { weightStr = it },
                    label = { Text(stringResource(R.string.label_weight)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = heightStr,
                onValueChange = { heightStr = it },
                label = { Text(stringResource(R.string.label_height)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            Text(text = stringResource(R.string.label_sex), fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilterChip(
                    selected = sex == "male",
                    onClick = { sex = "male" },
                    label = { Text("👨 ${stringResource(R.string.sex_male)}") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = sex == "female",
                    onClick = { sex = "female" },
                    label = { Text("👩 ${stringResource(R.string.sex_female)}") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(text = stringResource(R.string.label_activity), fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))

            listOf(
                "sedentary" to stringResource(R.string.activity_sedentary),
                "light" to stringResource(R.string.activity_light),
                "moderate" to stringResource(R.string.activity_moderate),
                "intense" to stringResource(R.string.activity_intense)
            ).forEach { (key, label) ->
                FilterChip(
                    selected = activity == key,
                    onClick = { activity = key },
                    label = { Text(label, fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = CyanSecondary.copy(alpha = 0.15f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = stringResource(R.string.recalculated_goal_label, formattedGoal),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = BluePrimary)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = {
                    onSaveProfile(name, age, sex, weight, height, activity, rec.recommendedDailyMl)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
            ) {
                Text(stringResource(R.string.dialog_save), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun GoalSettingsDetail(
    uiState: SettingsUiState,
    isArabic: Boolean,
    onEditGoalClick: () -> Unit,
    onFrequentAmountSelect: (Int) -> Unit,
    onQuickAddToggle: (Boolean) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.daily_target),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                    )
                    val formattedGoal = LocalizationUtils.formatNumber(uiState.dailyGoalMl, isArabic)
                    Text(
                        text = stringResource(R.string.ml_format, formattedGoal),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = BluePrimary
                        )
                    )
                }
                OutlinedButton(onClick = onEditGoalClick) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.dialog_add))
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            Text(
                text = stringResource(R.string.frequent_drink_amount),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
            )
            Text(
                text = stringResource(R.string.frequent_drink_desc),
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(150, 250, 330, 500).forEach { amount ->
                    val formattedAmount = LocalizationUtils.formatNumber(amount, isArabic)
                    FilterChip(
                        selected = uiState.frequentIntakeMl == amount,
                        onClick = { onFrequentAmountSelect(amount) },
                        label = { Text(stringResource(R.string.ml_format, formattedAmount), fontSize = 12.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.one_tap_notification),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                    )
                    val formattedDefaultQuick = LocalizationUtils.formatNumber(uiState.defaultQuickAddMl, isArabic)
                    Text(
                        text = stringResource(R.string.reminder_tap_text, formattedDefaultQuick),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
                Switch(
                    checked = uiState.quickAddOnNotificationClick,
                    onCheckedChange = onQuickAddToggle
                )
            }
        }
    }
}

@Composable
fun BottleSettingsDetail(
    uiState: SettingsUiState,
    isArabic: Boolean,
    onToggleBottleMode: (Boolean) -> Unit,
    onBottleConfigChange: (vol: Int, dur: Int) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.enable_bottle_mode),
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = stringResource(R.string.bottle_mode_desc),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
                Switch(
                    checked = uiState.bottleModeEnabled,
                    onCheckedChange = onToggleBottleMode
                )
            }

            if (uiState.bottleModeEnabled) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                Text(
                    text = stringResource(R.string.bottle_capacity),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(500, 750, 1000, 1500).forEach { vol ->
                        val formattedVol = LocalizationUtils.formatNumber(vol, isArabic)
                        FilterChip(
                            selected = uiState.bottleVolumeMl == vol,
                            onClick = { onBottleConfigChange(vol, uiState.bottleTargetDurationMinutes) },
                            label = { Text(stringResource(R.string.ml_format, formattedVol), fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = stringResource(R.string.bottle_target_duration),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(60 to 1, 120 to 2, 180 to 3, 240 to 4).forEach { (mins, hours) ->
                        val formattedHours = LocalizationUtils.formatNumber(hours, isArabic)
                        FilterChip(
                            selected = uiState.bottleTargetDurationMinutes == mins,
                            onClick = { onBottleConfigChange(uiState.bottleVolumeMl, mins) },
                            label = { Text(stringResource(R.string.hours_format, formattedHours), fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RemindersSettingsDetail(
    uiState: SettingsUiState,
    isArabic: Boolean,
    isBatteryOptimized: Boolean,
    context: android.content.Context,
    onToggleReminders: (Boolean) -> Unit,
    onModeChange: (ReminderMode) -> Unit,
    onIntervalChange: (Int) -> Unit,
    onCustomIntervalClick: () -> Unit,
    onToggleHour: (Int) -> Unit,
    onWakingDayPresetClick: () -> Unit
) {
    Column {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.enable_reminders),
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                    Switch(
                        checked = uiState.remindersEnabled,
                        onCheckedChange = onToggleReminders
                    )
                }

                if (uiState.remindersEnabled) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    Text(
                        text = stringResource(R.string.reminder_mode),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = uiState.reminderMode == ReminderMode.INTERVAL,
                            onClick = { onModeChange(ReminderMode.INTERVAL) },
                            label = { Text(stringResource(R.string.mode_interval)) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = uiState.reminderMode == ReminderMode.CUSTOM_ROUTINE,
                            onClick = { onModeChange(ReminderMode.CUSTOM_ROUTINE) },
                            label = { Text(stringResource(R.string.mode_routine)) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (uiState.reminderMode == ReminderMode.INTERVAL) {
                        Text(
                            text = stringResource(R.string.remind_every),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                30 to stringResource(R.string.minutes_format, LocalizationUtils.formatNumber(30, isArabic)),
                                45 to stringResource(R.string.minutes_format, LocalizationUtils.formatNumber(45, isArabic)),
                                60 to stringResource(R.string.hours_format, LocalizationUtils.formatNumber(1, isArabic)),
                                90 to stringResource(R.string.hours_format, LocalizationUtils.formatNumber(1.5, isArabic)),
                                120 to stringResource(R.string.hours_format, LocalizationUtils.formatNumber(2, isArabic)),
                                180 to stringResource(R.string.hours_format, LocalizationUtils.formatNumber(3, isArabic))
                            ).forEach { (mins, label) ->
                                FilterChip(
                                    selected = uiState.reminderIntervalMinutes == mins,
                                    onClick = { onIntervalChange(mins) },
                                    label = { Text(label) }
                                )
                            }
                            FilterChip(
                                selected = uiState.reminderIntervalMinutes !in listOf(30, 45, 60, 90, 120, 180),
                                onClick = onCustomIntervalClick,
                                label = { Text(stringResource(R.string.custom_chip)) },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val formattedHoursCount = LocalizationUtils.formatNumber(uiState.customReminderHours.size, isArabic)
                            Text(
                                text = stringResource(R.string.routine_hours, formattedHoursCount),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                            )
                            OutlinedButton(
                                onClick = onWakingDayPresetClick,
                                modifier = Modifier.height(32.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(R.string.waking_day_preset), fontSize = 11.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            (6..23).forEach { hour ->
                                val isSelected = uiState.customReminderHours.contains(hour)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onToggleHour(hour) },
                                    label = { Text(LocalizationUtils.formatHour(hour, isArabic), fontSize = 11.sp) },
                                    modifier = Modifier.height(30.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isBatteryOptimized) Color(0xFFFFF3E0) else Color(0xFFE8F5E9)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isBatteryOptimized) Icons.Default.BatteryAlert else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (isBatteryOptimized) Color(0xFFE65100) else Color(0xFF2E7D32)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isBatteryOptimized) stringResource(R.string.battery_optimization_title) else stringResource(R.string.battery_unrestricted_title),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isBatteryOptimized) Color(0xFFE65100) else Color(0xFF2E7D32)
                        )
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isBatteryOptimized) stringResource(R.string.battery_optimization_desc) else stringResource(R.string.battery_unrestricted_desc),
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
                if (isBatteryOptimized) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { BatteryOptimizationHelper.requestIgnoreBatteryOptimization(context) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100))
                    ) {
                        Text(stringResource(R.string.disable_battery_optimization), fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun SoundsSettingsDetail(
    uiState: SettingsUiState,
    isArabic: Boolean,
    context: android.content.Context,
    onSelectSound: (String) -> Unit,
    onPreviewSound: (NotificationSound) -> Unit,
    onSendTestAlert: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.reminder_tone),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
            )
            Spacer(modifier = Modifier.height(6.dp))

            NotificationSound.entries.forEach { sound ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectSound(sound.id) }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = uiState.notificationSound == sound.id,
                        onClick = { onSelectSound(sound.id) }
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = sound.getLocalizedDisplayName(isArabic), fontWeight = FontWeight.SemiBold)
                        Text(
                            text = sound.getLocalizedDescription(isArabic),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                    IconButton(onClick = { onPreviewSound(sound) }) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Play sound")
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            Button(
                onClick = onSendTestAlert,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
            ) {
                Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(stringResource(R.string.test_alert))
            }
        }
    }
}

@Composable
fun LanguageSettingsDetail(
    uiState: SettingsUiState,
    onSelectLanguage: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.app_language),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilterChip(
                    selected = uiState.language == "en",
                    onClick = { onSelectLanguage("en") },
                    label = { Text(stringResource(R.string.lang_english)) },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = uiState.language == "ar",
                    onClick = { onSelectLanguage("ar") },
                    label = { Text(stringResource(R.string.lang_arabic)) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun AboutSettingsDetail() {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = null, tint = BluePrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.app_version),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = BluePrimary
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.app_motto),
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Composable
fun DailyGoalDialog(
    currentGoal: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var textValue by remember { mutableStateOf(currentGoal.toString()) }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.daily_target)) },
        text = {
            Column {
                Text(stringResource(R.string.dialog_daily_goal_desc))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = textValue,
                    onValueChange = {
                        textValue = it
                        isError = false
                    },
                    isError = isError,
                    label = { Text(stringResource(R.string.dialog_daily_goal_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    supportingText = {
                        if (isError) Text(stringResource(R.string.dialog_daily_goal_error))
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = textValue.toIntOrNull()
                    if (amount != null && amount in 500..10000) {
                        onConfirm(amount)
                    } else {
                        isError = true
                    }
                }
            ) {
                Text(stringResource(R.string.dialog_save))
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(stringResource(R.string.dialog_cancel))
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CustomIntervalDialog(
    currentMinutes: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var isHoursUnit by remember { mutableStateOf(currentMinutes >= 60 && currentMinutes % 60 == 0) }
    var inputValue by remember {
        mutableStateOf(
            if (isHoursUnit) (currentMinutes / 60).toString() else currentMinutes.toString()
        )
    }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val minError = stringResource(R.string.dialog_interval_min_error)
    val maxError = stringResource(R.string.dialog_interval_max_error)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dialog_custom_interval_title)) },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.dialog_custom_interval_desc),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = !isHoursUnit,
                        onClick = {
                            if (isHoursUnit) {
                                isHoursUnit = false
                                val num = inputValue.toIntOrNull() ?: 1
                                inputValue = (num * 60).coerceIn(15, 720).toString()
                                errorMessage = null
                            }
                        },
                        label = { Text(stringResource(R.string.unit_minutes)) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = isHoursUnit,
                        onClick = {
                            if (!isHoursUnit) {
                                isHoursUnit = true
                                val num = inputValue.toIntOrNull() ?: 60
                                inputValue = (num / 60).coerceIn(1, 12).toString()
                                errorMessage = null
                            }
                        },
                        label = { Text(stringResource(R.string.unit_hours)) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = inputValue,
                    onValueChange = {
                        inputValue = it
                        errorMessage = null
                    },
                    isError = errorMessage != null,
                    label = { Text(if (isHoursUnit) stringResource(R.string.unit_hours) else stringResource(R.string.unit_minutes)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    supportingText = {
                        errorMessage?.let { Text(it) }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsed = inputValue.toIntOrNull()
                    if (parsed == null) {
                        errorMessage = minError
                        return@Button
                    }

                    val totalMinutes = if (isHoursUnit) parsed * 60 else parsed
                    if (totalMinutes < 15) {
                        errorMessage = minError
                        return@Button
                    }
                    if (totalMinutes > 720) {
                        errorMessage = maxError
                        return@Button
                    }

                    onConfirm(totalMinutes)
                }
            ) {
                Text(stringResource(R.string.dialog_apply))
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(stringResource(R.string.dialog_cancel))
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WakingDayScheduleDialog(
    currentStartHour: Int,
    currentEndHour: Int,
    onDismiss: () -> Unit,
    onConfirm: (wakeHour: Int, sleepHour: Int, stepHours: Int) -> Unit
) {
    var wakeHour by remember { mutableIntStateOf(currentStartHour) }
    var sleepHour by remember { mutableIntStateOf(currentEndHour) }
    var stepHours by remember { mutableIntStateOf(2) }

    val isArabic = LocalLayoutDirection.current == LayoutDirection.Rtl

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.WbSunny, contentDescription = null, tint = BluePrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.waking_day_preset))
            }
        },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.dialog_waking_schedule_desc),
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.label_wake_time), fontWeight = FontWeight.SemiBold)
                    Text(LocalizationUtils.formatHour(wakeHour, isArabic), fontWeight = FontWeight.Bold, color = BluePrimary)
                }
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(5, 6, 7, 8, 9, 10).forEach { h ->
                        FilterChip(
                            selected = wakeHour == h,
                            onClick = { wakeHour = h },
                            label = { Text(LocalizationUtils.formatHour(h, isArabic), fontSize = 11.sp) },
                            modifier = Modifier.height(30.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.label_sleep_time), fontWeight = FontWeight.SemiBold)
                    Text(LocalizationUtils.formatHour(sleepHour, isArabic), fontWeight = FontWeight.Bold, color = BluePrimary)
                }
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(20, 21, 22, 23, 0).forEach { h ->
                        FilterChip(
                            selected = sleepHour == h,
                            onClick = { sleepHour = h },
                            label = { Text(LocalizationUtils.formatHour(h, isArabic), fontSize = 11.sp) },
                            modifier = Modifier.height(30.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = stringResource(R.string.label_reminder_frequency),
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        1 to stringResource(R.string.freq_every_1h),
                        2 to stringResource(R.string.freq_every_2h),
                        3 to stringResource(R.string.freq_every_3h)
                    ).forEach { (step, label) ->
                        FilterChip(
                            selected = stepHours == step,
                            onClick = { stepHours = step },
                            label = { Text(label, fontSize = 12.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(wakeHour, sleepHour, stepHours) }
            ) {
                Text(stringResource(R.string.dialog_apply))
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(stringResource(R.string.dialog_cancel))
            }
        }
    )
}
