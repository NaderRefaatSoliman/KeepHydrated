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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
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
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.toggleReminders(true)
        }
    }

    val testNotificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.sendTestNotification(context)
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
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
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
                // Section 1: Hydration Goal
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Hydration Goal",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = BluePrimary
                        )
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
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.TrackChanges, contentDescription = null, tint = BluePrimary)
                                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                                    Text(
                                        text = "Daily Target",
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${uiState.dailyGoalMl} ml",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = BluePrimary
                                        )
                                    )
                                    IconButton(onClick = { showGoalDialog = true }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit Goal")
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Quick Select:",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(1500, 2000, 2500, 3000).forEach { goal ->
                                    FilterChip(
                                        selected = uiState.dailyGoalMl == goal,
                                        onClick = { viewModel.updateDailyGoal(goal) },
                                        label = { Text("${goal}ml") }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Reminders & Alerts",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = BluePrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Battery Optimization Check & Prompt
                    if (isBatteryOptimized) {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.BatteryAlert,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Battery Optimization Active",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Android may delay or silence drinking reminders and smartwatch alerts when the screen is off. Disable battery optimization to guarantee real-time reminders.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = {
                                        BatteryOptimizationHelper.requestIgnoreBatteryOptimization(context)
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error,
                                        contentColor = MaterialTheme.colorScheme.onError
                                    ),
                                    modifier = Modifier.height(34.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                                ) {
                                    Text("Disable Battery Optimization", fontSize = 12.sp)
                                }
                            }
                        }
                    } else {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF2E7D32),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Background Execution: Unrestricted",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "Battery optimization disabled. Real-time alerts and smartwatch syncing active.",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }
                            }
                        }
                    }

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Master reminder switch
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Notifications, contentDescription = null, tint = BluePrimary)
                                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                                    Text(
                                        text = "Drink Water Reminders",
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
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

                                // Reminder Mode: Regular Interval vs Custom Routine
                                Text(
                                    text = "Reminder Schedule Mode:",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    FilterChip(
                                        selected = uiState.reminderMode == ReminderMode.INTERVAL,
                                        onClick = { viewModel.updateReminderMode(ReminderMode.INTERVAL) },
                                        label = { Text("Periodic Interval") }
                                    )
                                    FilterChip(
                                        selected = uiState.reminderMode == ReminderMode.CUSTOM_ROUTINE,
                                        onClick = { viewModel.updateReminderMode(ReminderMode.CUSTOM_ROUTINE) },
                                        label = { Text("Daily Routine Hours") }
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                if (uiState.reminderMode == ReminderMode.INTERVAL) {
                                    // Mode 1: Regular Interval with Predefined + Manual User Input
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Alarm, contentDescription = null, tint = BluePrimary)
                                            Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                                            Text(
                                                text = "Remind me every:",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                            )
                                        }
                                        OutlinedButton(
                                            onClick = { showCustomIntervalDialog = true },
                                            modifier = Modifier.height(32.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Custom", fontSize = 12.sp)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))

                                    val currentMinutes = uiState.reminderIntervalMinutes
                                    val isPredefined = currentMinutes in listOf(60, 120, 240)

                                    FlowRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // Predefined options 1h, 2h, 4h
                                        listOf(
                                            1 to "1 Hour",
                                            2 to "2 Hours",
                                            4 to "4 Hours"
                                        ).forEach { (hours, label) ->
                                            val minutes = hours * 60
                                            FilterChip(
                                                selected = currentMinutes == minutes,
                                                onClick = { viewModel.updateReminderIntervalMinutes(minutes) },
                                                label = { Text(label) }
                                            )
                                        }

                                        // Manual custom input chip
                                        FilterChip(
                                            selected = !isPredefined,
                                            onClick = { showCustomIntervalDialog = true },
                                            label = {
                                                Text(
                                                    if (!isPredefined) {
                                                        "Custom (${formatInterval(currentMinutes)})"
                                                    } else {
                                                        "Custom (X min / X hr)"
                                                    }
                                                )
                                            },
                                            leadingIcon = {
                                                Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                                            }
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Active interval: Every ${formatInterval(currentMinutes)}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                } else {
                                    // Mode 2: Custom Reminder Routine (24-hour selection + Waking Day Preset)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Schedule, contentDescription = null, tint = BluePrimary)
                                            Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                                            Text(
                                                text = "Routine Hours (${uiState.customReminderHours.size}/24 set):",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Select any of the 24 hours or apply schedule presets:",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))

                                    Text(
                                        text = "Quick Presets:",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))

                                    FlowRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // Waking Day Schedule Preset Button
                                        Button(
                                            onClick = { showWakingDayDialog = true },
                                            modifier = Modifier.height(34.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                                        ) {
                                            Icon(Icons.Default.WbSunny, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("🌅 Waking Day Schedule...", fontSize = 12.sp)
                                        }

                                        OutlinedButton(
                                            onClick = { viewModel.setCustomReminderHours(setOf(9, 11, 13, 15, 17)) },
                                            modifier = Modifier.height(34.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                                        ) {
                                            Text("💼 Work Day", fontSize = 12.sp)
                                        }

                                        OutlinedButton(
                                            onClick = { viewModel.setCustomReminderHours(setOf(8, 10, 12, 14, 16, 18, 20, 22)) },
                                            modifier = Modifier.height(34.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                                        ) {
                                            Text("☀️ All Day", fontSize = 12.sp)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))
                                    Text(
                                        text = "24-Hour Day Schedule:",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Full 24 Hours (0 to 23) available for selection
                                    val all24Hours = (0..23).toList()

                                    FlowRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        all24Hours.forEach { hour ->
                                            val isSelected = uiState.customReminderHours.contains(hour)
                                            val timeLabel = formatHourToAmPm(hour)

                                            FilterChip(
                                                selected = isSelected,
                                                onClick = { viewModel.toggleCustomReminderHour(hour) },
                                                label = {
                                                    Text(
                                                        text = timeLabel,
                                                        fontSize = 12.sp
                                                    )
                                                },
                                                leadingIcon = if (isSelected) {
                                                    {
                                                        Icon(
                                                            Icons.Default.Check,
                                                            contentDescription = null,
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                    }
                                                } else null
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    // Section 3: Notification, Sounds & Smartwatch Actions
                    Text(
                        text = "Notification & Sounds",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = BluePrimary
                        )
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
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = BluePrimary)
                                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                                    Text(
                                        text = "Reminder Tone",
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                }

                                // Notification Test Option
                                OutlinedButton(
                                    onClick = {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                            val hasPermission = ContextCompat.checkSelfPermission(
                                                context,
                                                Manifest.permission.POST_NOTIFICATIONS
                                            ) == PackageManager.PERMISSION_GRANTED

                                            if (!hasPermission) {
                                                testNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                                return@OutlinedButton
                                            }
                                        }
                                        viewModel.sendTestNotification(context)
                                    },
                                    modifier = Modifier.height(34.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                                ) {
                                    Icon(
                                        Icons.Default.NotificationsActive,
                                        contentDescription = "Test Notification",
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Test Alert", fontSize = 12.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Choose a refreshing water-inspired sound or test it now:",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            // List of notification sounds suitable for water and drinking
                            NotificationSound.entries.forEach { sound ->
                                val isSelected = uiState.notificationSound == sound.id

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.updateNotificationSound(sound.id) }
                                        .padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = { viewModel.updateNotificationSound(sound.id) }
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column {
                                            Text(
                                                text = sound.displayName,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                                )
                                            )
                                            Text(
                                                text = sound.description,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            )
                                        }
                                    }

                                    // Preview sound button
                                    IconButton(
                                        onClick = { viewModel.previewSound(context, sound) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.PlayArrow,
                                            contentDescription = "Preview ${sound.displayName}",
                                            tint = BluePrimary
                                        )
                                    }
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                            // Goal Celebration Sound Selector
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Celebration, contentDescription = null, tint = BluePrimary)
                                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                                    Text(
                                        text = "Goal Celebration Tone",
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Triumphant sound played when you reach your daily hydration target:",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            // List of celebration sounds
                            CelebrationSound.entries.forEach { sound ->
                                val isSelected = uiState.celebrationSound == sound.id

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.updateCelebrationSound(sound.id) }
                                        .padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = { viewModel.updateCelebrationSound(sound.id) }
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column {
                                            Text(
                                                text = sound.displayName,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                                )
                                            )
                                            Text(
                                                text = sound.description,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = { viewModel.previewCelebrationSound(context, sound) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.PlayArrow,
                                            contentDescription = "Preview ${sound.displayName}",
                                            tint = BluePrimary
                                        )
                                    }
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                            // Smartwatch & Notification Quick Log Settings
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Watch, contentDescription = null, tint = BluePrimary)
                                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                                    Text(
                                        text = "Smartwatch & Shade Quick-Add",
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Predefined amount logged when clicking the notification or quick action on your phone or smartwatch:",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            val predefinedAmounts = listOf(150, 200, 250, 300, 350, 500)
                            val isCustomAmount = uiState.defaultQuickAddMl !in predefinedAmounts

                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                predefinedAmounts.forEach { amount ->
                                    FilterChip(
                                        selected = uiState.defaultQuickAddMl == amount,
                                        onClick = { viewModel.updateDefaultQuickAddMl(amount) },
                                        label = { Text("${amount} ml") }
                                    )
                                }

                                FilterChip(
                                    selected = isCustomAmount,
                                    onClick = { showQuickAddAmountDialog = true },
                                    label = {
                                        Text(if (isCustomAmount) "${uiState.defaultQuickAddMl} ml" else "Custom...")
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "One-Tap Add on Notification Click",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                                    )
                                    Text(
                                        text = "Tapping notification body immediately logs ${uiState.defaultQuickAddMl} ml in the background silently",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Switch(
                                    checked = uiState.quickAddOnNotificationClick,
                                    onCheckedChange = { viewModel.updateQuickAddOnNotificationClick(it) }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "About",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = BluePrimary
                        )
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
                                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                                Text(
                                    text = "KeepHydrated App",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Version 1.5.0 (Exact Real-Time Alarms, Smartwatch Bridging & Battery Optimization)",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
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

    if (showQuickAddAmountDialog) {
        QuickAddAmountDialog(
            currentAmount = uiState.defaultQuickAddMl,
            onDismiss = { showQuickAddAmountDialog = false },
            onConfirm = { amount ->
                viewModel.updateDefaultQuickAddMl(amount)
                showQuickAddAmountDialog = false
            }
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

private fun formatInterval(minutes: Int): String {
    return if (minutes % 60 == 0) {
        val hours = minutes / 60
        if (hours == 1) "1 hour" else "$hours hours"
    } else {
        "$minutes minutes"
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
        title = { Text("Update Daily Goal") },
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
                Text("Save")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun QuickAddAmountDialog(
    currentAmount: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var textValue by remember { mutableStateOf(currentAmount.toString()) }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Predefined Quick-Add Amount") },
        text = {
            Column {
                Text("Enter amount in ml (50 - 2000 ml) for one-tap notification & smartwatch logging:")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = textValue,
                    onValueChange = {
                        textValue = it
                        isError = false
                    },
                    isError = isError,
                    label = { Text("Amount (ml)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    supportingText = {
                        if (isError) Text("Must be between 50 and 2000 ml")
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = textValue.toIntOrNull()
                    if (amount != null && amount in 50..2000) {
                        onConfirm(amount)
                    } else {
                        isError = true
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
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

                // Unit selection: Minutes vs Hours
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

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Quick Presets:",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                )
                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(15 to "15m", 30 to "30m", 45 to "45m", 90 to "90m", 180 to "3h", 300 to "5h").forEach { (mins, label) ->
                        OutlinedButton(
                            onClick = {
                                if (mins % 60 == 0 && mins >= 60) {
                                    isHoursUnit = true
                                    inputValue = (mins / 60).toString()
                                } else {
                                    isHoursUnit = false
                                    inputValue = mins.toString()
                                }
                                errorMessage = null
                            },
                            modifier = Modifier.height(30.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Text(label, fontSize = 11.sp)
                        }
                    }
                }
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
                Text("Set Interval")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
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
                Text("Waking Day Schedule")
            }
        },
        text = {
            Column {
                Text(
                    text = "Define your sleep and awake schedule to automatically generate routine reminder hours:",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
                Spacer(modifier = Modifier.height(14.dp))

                // Wake-up Time Selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.WbSunny, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Wake-up Time:", fontWeight = FontWeight.SemiBold)
                    }
                    Text(
                        formatHourToAmPm(wakeHour),
                        fontWeight = FontWeight.Bold,
                        color = BluePrimary
                    )
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

                // Sleeping / Bedtime Selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Bedtime, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Bedtime / Sleep:", fontWeight = FontWeight.SemiBold)
                    }
                    Text(
                        formatHourToAmPm(sleepHour),
                        fontWeight = FontWeight.Bold,
                        color = BluePrimary
                    )
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

                // Reminder Frequency during day
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

                Spacer(modifier = Modifier.height(12.dp))

                // Live calculation preview
                val previewHours = mutableListOf<Int>()
                if (wakeHour <= sleepHour) {
                    var h = wakeHour
                    while (h <= sleepHour) {
                        previewHours.add(h)
                        h += stepHours
                    }
                } else {
                    var h = wakeHour
                    while (h < 24) {
                        previewHours.add(h)
                        h += stepHours
                    }
                    var m = h % 24
                    while (m <= sleepHour) {
                        previewHours.add(m)
                        m += stepHours
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = "Schedule Preview (${previewHours.size} reminders):",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = previewHours.joinToString(", ") { formatHourToAmPm(it) },
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(wakeHour, sleepHour, stepHours) }
            ) {
                Text("Apply to Routine")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
