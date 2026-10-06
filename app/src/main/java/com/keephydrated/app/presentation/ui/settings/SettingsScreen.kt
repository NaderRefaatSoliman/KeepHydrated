package com.keephydrated.app.presentation.ui.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.material.icons.filled.Watch
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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.keephydrated.app.R
import com.keephydrated.app.domain.model.CelebrationSound
import com.keephydrated.app.domain.model.NotificationSound
import com.keephydrated.app.domain.model.ReminderMode
import com.keephydrated.app.presentation.ui.theme.BluePrimary
import com.keephydrated.app.util.BatteryOptimizationHelper

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var showGoalDialog by remember { mutableStateOf(false) }
    var showCustomIntervalDialog by remember { mutableStateOf(false) }
    var showWakingDayDialog by remember { mutableStateOf(false) }
    var showQuickAddAmountDialog by remember { mutableStateOf(false) }
    var showBottleConfigDialog by remember { mutableStateOf(false) }

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.settings),
                        fontWeight = FontWeight.Bold
                    )
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
            ) {
                // ==========================================
                // 1. HYDRATION TARGET & QUICK ADD
                // ==========================================
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    CategoryHeader(
                        title = stringResource(R.string.category_goal),
                        icon = Icons.Default.TrackChanges
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Daily Target
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
                                    Text(
                                        text = "${uiState.dailyGoalMl} ml",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = BluePrimary
                                        )
                                    )
                                }
                                OutlinedButton(onClick = { showGoalDialog = true }) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(stringResource(R.string.dialog_add))
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                            // Frequent Drinking Size
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
                                    FilterChip(
                                        selected = uiState.frequentIntakeMl == amount,
                                        onClick = { viewModel.updateFrequentIntakeMl(amount) },
                                        label = { Text("${amount}ml", fontSize = 12.sp) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                            // Notification 1-tap logging
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
                                    Text(
                                        text = "Tapping notification body directly logs ${uiState.defaultQuickAddMl} ml",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }
                                Switch(
                                    checked = uiState.quickAddOnNotificationClick,
                                    onCheckedChange = { viewModel.updateQuickAddOnNotificationClick(it) }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }

                // ==========================================
                // 2. BOTTLE TRACKING MODE
                // ==========================================
                item {
                    CategoryHeader(
                        title = stringResource(R.string.category_bottle),
                        icon = Icons.Default.LocalDrink
                    )
                    Spacer(modifier = Modifier.height(8.dp))

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
                                    onCheckedChange = { viewModel.updateBottleModeEnabled(it) }
                                )
                            }

                            if (uiState.bottleModeEnabled) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                                // Bottle Volume Selection
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
                                        FilterChip(
                                            selected = uiState.bottleVolumeMl == vol,
                                            onClick = { viewModel.updateBottleConfig(vol, uiState.bottleTargetDurationMinutes) },
                                            label = { Text("${vol}ml", fontSize = 11.sp) },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Target Duration Selection
                                Text(
                                    text = stringResource(R.string.bottle_target_duration),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf(60 to "1h", 120 to "2h", 180 to "3h", 240 to "4h").forEach { (mins, label) ->
                                        FilterChip(
                                            selected = uiState.bottleTargetDurationMinutes == mins,
                                            onClick = { viewModel.updateBottleConfig(uiState.bottleVolumeMl, mins) },
                                            label = { Text(label, fontSize = 11.sp) },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }

                // ==========================================
                // 3. REMINDERS & SCHEDULE
                // ==========================================
                item {
                    CategoryHeader(
                        title = stringResource(R.string.category_reminders),
                        icon = Icons.Default.Schedule
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Toggle reminders
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
                                    Text(
                                        text = "Exact real-time alarms waking through Doze mode",
                                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    )
                                }
                                Switch(
                                    checked = uiState.remindersEnabled,
                                    onCheckedChange = { enabled ->
                                        if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                            val hasPermission = ContextCompat.checkSelfPermission(
                                                context,
                                                Manifest.permission.POST_NOTIFICATIONS
                                            ) == PackageManager.PERMISSION_GRANTED
                                            if (!hasPermission) {
                                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                                return@Switch
                                            }
                                        }
                                        viewModel.toggleReminders(enabled)
                                    }
                                )
                            }

                            if (uiState.remindersEnabled) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                                // Mode selection
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
                                        onClick = { viewModel.updateReminderMode(ReminderMode.INTERVAL) },
                                        label = { Text(stringResource(R.string.mode_interval)) },
                                        modifier = Modifier.weight(1f)
                                    )
                                    FilterChip(
                                        selected = uiState.reminderMode == ReminderMode.CUSTOM_ROUTINE,
                                        onClick = { viewModel.updateReminderMode(ReminderMode.CUSTOM_ROUTINE) },
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
                                        listOf(30 to "30m", 45 to "45m", 60 to "1h", 90 to "1.5h", 120 to "2h", 180 to "3h").forEach { (mins, label) ->
                                            FilterChip(
                                                selected = uiState.reminderIntervalMinutes == mins,
                                                onClick = { viewModel.updateReminderIntervalMinutes(mins) },
                                                label = { Text(label) }
                                            )
                                        }
                                        FilterChip(
                                            selected = uiState.reminderIntervalMinutes !in listOf(30, 45, 60, 90, 120, 180),
                                            onClick = { showCustomIntervalDialog = true },
                                            label = { Text("Custom...") },
                                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                        )
                                    }
                                } else {
                                    // Custom Routine
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = stringResource(R.string.routine_hours, uiState.customReminderHours.size),
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                                        )
                                        OutlinedButton(
                                            onClick = { showWakingDayDialog = true },
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
                                                onClick = { viewModel.toggleCustomReminderHour(hour) },
                                                label = { Text(formatHourToAmPm(hour), fontSize = 11.sp) },
                                                modifier = Modifier.height(30.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Battery Optimization Warning Card
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
                    Spacer(modifier = Modifier.height(20.dp))
                }

                // ==========================================
                // 4. NOTIFICATION & SOUNDS
                // ==========================================
                item {
                    CategoryHeader(
                        title = stringResource(R.string.category_sounds),
                        icon = Icons.Default.Notifications
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Reminder Tone
                            Text(
                                text = stringResource(R.string.reminder_tone),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            NotificationSound.entries.forEach { sound ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.updateNotificationSound(sound.id) }
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = uiState.notificationSound == sound.id,
                                        onClick = { viewModel.updateNotificationSound(sound.id) }
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = sound.displayName, fontWeight = FontWeight.SemiBold)
                                        Text(
                                            text = sound.description,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        )
                                    }
                                    IconButton(onClick = { viewModel.previewSound(context, sound) }) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = "Play sound")
                                    }
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                            // Test Notification Button
                            Button(
                                onClick = { viewModel.sendTestNotification(context) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                            ) {
                                Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(stringResource(R.string.test_alert))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }

                // ==========================================
                // 5. LANGUAGE / اللغة
                // ==========================================
                item {
                    CategoryHeader(
                        title = stringResource(R.string.category_language),
                        icon = Icons.Default.Language
                    )
                    Spacer(modifier = Modifier.height(8.dp))

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
                                    onClick = { viewModel.updateLanguage("en") },
                                    label = { Text(stringResource(R.string.lang_english)) },
                                    modifier = Modifier.weight(1f)
                                )
                                FilterChip(
                                    selected = uiState.language == "ar",
                                    onClick = { viewModel.updateLanguage("ar") },
                                    label = { Text(stringResource(R.string.lang_arabic)) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }

                // ==========================================
                // 6. ABOUT SECTION (Clean - NO change details)
                // ==========================================
                item {
                    CategoryHeader(
                        title = stringResource(R.string.category_about),
                        icon = Icons.Default.Info
                    )
                    Spacer(modifier = Modifier.height(8.dp))

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
                    Spacer(modifier = Modifier.height(36.dp))
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
fun CategoryHeader(
    title: String,
    icon: ImageVector
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = BluePrimary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = BluePrimary
            )
        )
    }
}

private fun formatHourToAmPm(hour: Int): String {
    return when {
        hour == 0 -> "12 AM"
        hour == 12 -> "12 PM"
        hour < 12 -> "$hour AM"
        else -> "${hour - 12} PM"
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
                Text("Enter target daily intake in ml (500 - 10000):")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = textValue,
                    onValueChange = {
                        textValue = it
                        isError = false
                    },
                    isError = isError,
                    label = { Text("Target Goal (ml)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    supportingText = {
                        if (isError) Text("Must be between 500 and 10000 ml")
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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Custom Reminder Interval") },
        text = {
            Column {
                Text(
                    text = "Specify how often you want to be reminded:",
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
                        label = { Text("Minutes") },
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
                        label = { Text("Hours") },
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
                    label = { Text(if (isHoursUnit) "Interval in Hours (1 - 12)" else "Interval in Minutes (15 - 720)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    supportingText = {
                        errorMessage?.let { Text(it) }
                            ?: Text(if (isHoursUnit) "1 to 12 hours" else "15 to 720 minutes (Min 15 min by Android)")
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
                        errorMessage = "Please enter a valid number"
                        return@Button
                    }

                    val totalMinutes = if (isHoursUnit) parsed * 60 else parsed
                    if (totalMinutes < 15) {
                        errorMessage = "Minimum interval is 15 minutes (Android system limit)"
                        return@Button
                    }
                    if (totalMinutes > 720) {
                        errorMessage = "Maximum interval is 12 hours (720 minutes)"
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
                    text = "Define your sleep and awake schedule to automatically generate routine reminder hours:",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Wake-up Time:", fontWeight = FontWeight.SemiBold)
                    Text(formatHourToAmPm(wakeHour), fontWeight = FontWeight.Bold, color = BluePrimary)
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
                            label = { Text(formatHourToAmPm(h), fontSize = 11.sp) },
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
                    Text("Bedtime / Sleep:", fontWeight = FontWeight.SemiBold)
                    Text(formatHourToAmPm(sleepHour), fontWeight = FontWeight.Bold, color = BluePrimary)
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
                            label = { Text(formatHourToAmPm(h), fontSize = 11.sp) },
                            modifier = Modifier.height(30.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Reminder Frequency:",
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(1 to "Every 1h", 2 to "Every 2h", 3 to "Every 3h").forEach { (step, label) ->
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
