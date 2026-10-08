package com.keephydrated.app.presentation.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keephydrated.app.R
import com.keephydrated.app.presentation.ui.components.BottleTrackingCard
import com.keephydrated.app.presentation.ui.components.CircularHydrationProgress
import com.keephydrated.app.presentation.ui.components.DrDroppyFloatingMascot
import com.keephydrated.app.presentation.ui.components.IntakeHistoryItem
import com.keephydrated.app.presentation.ui.components.QuickAddSection
import com.keephydrated.app.presentation.ui.theme.BluePrimary
import com.keephydrated.app.presentation.ui.theme.CyanSecondary
import com.keephydrated.app.util.LocalizationUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier,
    resetGuideKey: Int = 0
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showCustomDialog by remember { mutableStateOf(false) }
    var showHealthGuide by remember { mutableStateOf(false) }

    LaunchedEffect(resetGuideKey) {
        showHealthGuide = false
    }

    val isArabic = LocalLayoutDirection.current == LayoutDirection.Rtl

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessage()
        }
    }

    if (showHealthGuide) {
        com.keephydrated.app.presentation.ui.guide.HealthGuideScreen(
            userSettings = uiState.userSettings,
            onBackClick = { showHealthGuide = false }
        )
    } else {
        Scaffold(
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
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        // Complete Profile Reminder Banner (if onboarding was skipped)
                        if (!uiState.userSettings.isOnboardingCompleted) {
                            item {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = CyanSecondary.copy(alpha = 0.15f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AccountCircle,
                                            contentDescription = null,
                                            tint = BluePrimary,
                                            modifier = Modifier.size(28.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = stringResource(R.string.complete_profile_reminder_title),
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.bodyMedium.copy(color = BluePrimary)
                                            )
                                            Text(
                                                text = stringResource(R.string.complete_profile_reminder_desc),
                                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Button(
                                            onClick = { showHealthGuide = true },
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                                        ) {
                                            Text(
                                                text = stringResource(R.string.complete_profile_action),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Circular Hydration Progress Tracker
                        item {
                            CircularHydrationProgress(
                                currentMl = uiState.currentIntakeMl,
                                goalMl = uiState.dailyGoalMl,
                                frequentIntakeMl = uiState.frequentIntakeMl,
                                safeMaxDailyMl = uiState.safeMaxDailyMl,
                                onFrequentIntakeClick = {
                                    viewModel.addWater(uiState.frequentIntakeMl)
                                }
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // Safe Daily Upper Limit Badge
                            val formattedSafeLimit = LocalizationUtils.formatNumber(uiState.safeMaxDailyMl, isArabic)
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ) {
                                Text(
                                    text = stringResource(R.string.safe_daily_limit_badge, formattedSafeLimit),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }

                            // Undo Icon Button Placed Ergonomically Directly Beneath Tracking Circle
                            if (uiState.todayIntakes.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.clickable { viewModel.undoLastIntake() }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Undo,
                                            contentDescription = stringResource(R.string.undo_intake),
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = stringResource(R.string.undo_intake),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                        }

                        // Water Toxicity / Hyponatremia Warning Card (when safe ceiling reached)
                        if (uiState.isLimitExceeded) {
                            item {
                                val formattedCurrent = LocalizationUtils.formatNumber(uiState.currentIntakeMl, isArabic)
                                val formattedSafeLimit = LocalizationUtils.formatNumber(uiState.safeMaxDailyMl, isArabic)
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                                    border = BorderStroke(1.5.dp, Color(0xFFFF9800)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Warning,
                                                contentDescription = null,
                                                tint = Color(0xFFE65100),
                                                modifier = Modifier.size(22.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = stringResource(R.string.safe_daily_limit_reached_title),
                                                style = MaterialTheme.typography.titleSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFBF360C)
                                                )
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = stringResource(
                                                R.string.safe_daily_limit_reached_desc,
                                                formattedCurrent,
                                                formattedSafeLimit
                                            ),
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = Color(0xFF212121),
                                                lineHeight = 18.sp
                                            )
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        TextButton(
                                            onClick = { showHealthGuide = true },
                                            modifier = Modifier.align(Alignment.End)
                                        ) {
                                            Text(
                                                text = stringResource(R.string.guide_tour_title) + " 🩺",
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFE65100)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                            }
                        }

                        if (uiState.isBottleMode) {
                            item {
                                BottleTrackingCard(
                                    bottleStatus = uiState.bottleStatus,
                                    frequentAmountMl = uiState.frequentIntakeMl,
                                    onDrinkSip = { viewModel.drinkFromBottle() },
                                    onRefillBottle = { viewModel.refillBottle() }
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                            }
                        }

                        item {
                            QuickAddSection(
                                onAddWater = { amount -> viewModel.addWater(amount) }
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val count = LocalizationUtils.formatNumber(uiState.todayIntakes.size, isArabic)
                                Text(
                                    text = stringResource(R.string.todays_logs, count),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        if (uiState.todayIntakes.isEmpty()) {
                            item {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    ),
                                    elevation = CardDefaults.cardElevation(2.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = stringResource(R.string.no_water_logged),
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        )
                                    }
                                }
                            }
                        } else {
                            items(
                                items = uiState.todayIntakes,
                                key = { it.id }
                            ) { intake ->
                                IntakeHistoryItem(
                                    intake = intake,
                                    onDelete = { id -> viewModel.deleteIntake(id) }
                                )
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(90.dp))
                        }
                    }

                    // Floating Dr. Droppy Mascot with speech bubble tips
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 8.dp),
                        contentAlignment = if (isArabic) Alignment.BottomEnd else Alignment.BottomStart
                    ) {
                        DrDroppyFloatingMascot(
                            onClick = { showHealthGuide = true },
                            showTips = uiState.userSettings.droppyTipsEnabled
                        )
                    }

                    // Floating Action Button for Custom Add
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = if (isArabic) Alignment.BottomStart else Alignment.BottomEnd
                    ) {
                        FloatingActionButton(
                            onClick = { showCustomDialog = true },
                            containerColor = BluePrimary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ) {
                            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.custom_add))
                        }
                    }
                }
            }
        }
    }

    if (showCustomDialog) {
        CustomWaterDialog(
            onDismiss = { showCustomDialog = false },
            onAdd = { amount ->
                viewModel.addWater(amount)
                showCustomDialog = false
            }
        )
    }
}

@Composable
fun CustomWaterDialog(
    onDismiss: () -> Unit,
    onAdd: (Int) -> Unit
) {
    var textValue by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.custom_add)) },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.dialog_custom_water_desc),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = textValue,
                    onValueChange = {
                        textValue = it
                        isError = false
                    },
                    label = { Text(stringResource(R.string.dialog_amount_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = isError,
                    supportingText = {
                        if (isError) {
                            Text(stringResource(R.string.dialog_invalid_amount))
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = textValue.toIntOrNull()
                    if (amount != null && amount > 0) {
                        onAdd(amount)
                    } else {
                        isError = true
                    }
                }
            ) {
                Text(stringResource(R.string.dialog_add))
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(stringResource(R.string.dialog_cancel))
            }
        }
    )
}
