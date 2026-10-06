package com.keephydrated.app.presentation.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.keephydrated.app.R
import com.keephydrated.app.presentation.ui.components.BottleTrackingCard
import com.keephydrated.app.presentation.ui.components.CircularHydrationProgress
import com.keephydrated.app.presentation.ui.components.IntakeHistoryItem
import com.keephydrated.app.presentation.ui.components.QuickAddSection
import com.keephydrated.app.presentation.ui.theme.BluePrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showCustomDialog by remember { mutableStateOf(false) }

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
                        stringResource(R.string.app_name),
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    if (uiState.todayIntakes.isNotEmpty()) {
                        IconButton(onClick = { viewModel.undoLastIntake() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Undo,
                                contentDescription = stringResource(R.string.undo_intake),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCustomDialog = true },
                containerColor = BluePrimary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.custom_add))
            }
        },
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
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    // Water View: Circular progress with wave animation & frequent intake marker
                    CircularHydrationProgress(
                        currentMl = uiState.currentIntakeMl,
                        goalMl = uiState.dailyGoalMl,
                        frequentIntakeMl = uiState.frequentIntakeMl,
                        onFrequentIntakeClick = {
                            viewModel.addWater(uiState.frequentIntakeMl)
                        }
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }

                // If Bottle Tracking Mode is active, show the interactive Bottle Card
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

                // Quick Add Section (150ml, 250ml, 330ml, 500ml)
                item {
                    QuickAddSection(
                        onAddWater = { amount -> viewModel.addWater(amount) }
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }

                // Today's Logs header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.todays_logs, uiState.todayIntakes.size),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (uiState.todayIntakes.isEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.no_water_logged),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }
                } else {
                    items(uiState.todayIntakes, key = { it.id }) { intake ->
                        IntakeHistoryItem(
                            intake = intake,
                            onDelete = { id -> viewModel.deleteIntake(id) }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp)) // Padding for FAB
                }
            }
        }
    }

    if (showCustomDialog) {
        CustomAmountDialog(
            onDismiss = { showCustomDialog = false },
            onConfirm = { amount ->
                viewModel.addWater(amount)
                showCustomDialog = false
            }
        )
    }
}

@Composable
fun CustomAmountDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var textValue by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.custom_add)) },
        text = {
            Column {
                Text("Enter the water amount in milliliters (ml):")
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
                        if (isError) Text("Please enter a valid amount greater than 0")
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = textValue.toIntOrNull()
                    if (amount != null && amount > 0) {
                        onConfirm(amount)
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
