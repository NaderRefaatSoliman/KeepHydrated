package com.keephydrated.app.presentation.ui.history

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keephydrated.app.R
import com.keephydrated.app.domain.model.AchievementStatus
import com.keephydrated.app.domain.model.DailyHydrationSummary
import com.keephydrated.app.domain.model.HistoryPeriod
import com.keephydrated.app.presentation.ui.components.DrDroppyFloatingMascot
import com.keephydrated.app.presentation.ui.guide.HealthGuideScreen
import com.keephydrated.app.presentation.ui.theme.BluePrimary
import com.keephydrated.app.util.LocalizationUtils
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onNavigateToHome: () -> Unit = {},
    modifier: Modifier = Modifier,
    resetGuideKey: Int = 0
) {
    val uiState by viewModel.uiState.collectAsState()
    val isArabic = LocalLayoutDirection.current == LayoutDirection.Rtl
    var showHealthGuide by remember { mutableStateOf(false) }

    LaunchedEffect(resetGuideKey) {
        showHealthGuide = false
    }

    BackHandler(enabled = true) {
        if (showHealthGuide) {
            showHealthGuide = false
        } else {
            onNavigateToHome()
        }
    }

    if (showHealthGuide) {
        HealthGuideScreen(
            userSettings = uiState.userSettings,
            onBackClick = { showHealthGuide = false }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(stringResource(R.string.hydration_history), fontWeight = FontWeight.Bold)
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateToHome) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.nav_home)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    // Period Selector (Week / Month / Year)
                item {
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            HistoryPeriod.WEEK to stringResource(R.string.period_week),
                            HistoryPeriod.MONTH to stringResource(R.string.period_month),
                            HistoryPeriod.YEAR to stringResource(R.string.period_year)
                        ).forEach { (period, label) ->
                            FilterChip(
                                selected = uiState.selectedPeriod == period,
                                onClick = { viewModel.selectPeriod(period) },
                                label = { Text(label, fontWeight = FontWeight.SemiBold) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Overview Stat Cards
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val formattedAvg = LocalizationUtils.formatNumber(uiState.averageIntakeMl, isArabic)
                        val formattedDays = LocalizationUtils.formatNumber(uiState.daysGoalMetCount, isArabic)

                        StatCard(
                            title = stringResource(R.string.daily_average),
                            value = stringResource(R.string.ml_format, formattedAvg),
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = stringResource(R.string.goals_reached),
                            value = stringResource(R.string.days_count, formattedDays),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Achievement Status Summary Chips (Done / Partial / Zero)
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StatusCountChip(
                                label = stringResource(R.string.status_done),
                                count = LocalizationUtils.formatNumber(uiState.doneCount, isArabic),
                                color = Color(0xFF2E7D32)
                            )
                            StatusCountChip(
                                label = stringResource(R.string.status_partial),
                                count = LocalizationUtils.formatNumber(uiState.partialCount, isArabic),
                                color = Color(0xFFF57C00)
                            )
                            StatusCountChip(
                                label = stringResource(R.string.status_zero),
                                count = LocalizationUtils.formatNumber(uiState.zeroCount, isArabic),
                                color = Color(0xFF757575)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Intake Progress Bar Chart
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            val periodLabel = when (uiState.selectedPeriod) {
                                HistoryPeriod.WEEK -> stringResource(R.string.period_week)
                                HistoryPeriod.MONTH -> stringResource(R.string.period_month)
                                HistoryPeriod.YEAR -> stringResource(R.string.period_year)
                            }
                            Text(
                                text = stringResource(R.string.chart_title, periodLabel),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = BluePrimary
                                )
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            HydrationBarChart(
                                chartData = uiState.chartData,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }

                // Daily Records List Header
                item {
                    Text(
                        text = stringResource(R.string.intake_records),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                val records = if (uiState.selectedPeriod == HistoryPeriod.WEEK) {
                    uiState.periodSummaries
                } else {
                    uiState.periodSummaries.ifEmpty { uiState.dailySummaries }
                }

                if (records.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.no_history),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                } else {
                    items(records, key = { "${it.date}_${it.totalIntakeMl}" }) { summary ->
                        DailySummaryCard(summary = summary)
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 8.dp),
                contentAlignment = if (isArabic) Alignment.BottomEnd else Alignment.BottomStart
            ) {
                DrDroppyFloatingMascot(
                    onClick = { showHealthGuide = true },
                    showTips = false
                )
            }
        }
    }
    }
    }
}

@Composable
fun StatusCountChip(
    label: String,
    count: String,
    color: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
        )
        Text(
            text = count,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                color = color
            )
        )
    }
}

@Composable
fun HydrationBarChart(
    chartData: List<ChartBarData>,
    modifier: Modifier = Modifier
) {
    if (chartData.isEmpty()) return

    val maxAmount = chartData.maxOfOrNull { it.amountMl }?.coerceAtLeast(1) ?: 1
    val maxGoal = chartData.maxOfOrNull { it.goalMl }?.coerceAtLeast(1) ?: 1
    val chartCeiling = maxOf(maxAmount, maxGoal).toFloat()

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val bottomPadding = 30f
        val topPadding = 20f
        val usableHeight = h - bottomPadding - topPadding

        val barCount = chartData.size
        val barSpacing = w / (barCount * 4f)
        val barWidth = (w - (barSpacing * (barCount + 1))) / barCount

        // Draw Goal Baseline line
        val goalFraction = (maxGoal / chartCeiling).coerceIn(0f, 1f)
        val goalY = h - bottomPadding - (usableHeight * goalFraction)
        drawLine(
            color = Color(0xFFB0BEC5),
            start = Offset(0f, goalY),
            end = Offset(w, goalY),
            strokeWidth = 2f,
            cap = StrokeCap.Round
        )

        chartData.forEachIndexed { index, data ->
            val fraction = (data.amountMl / chartCeiling).coerceIn(0f, 1f)
            val barHeight = usableHeight * fraction
            val left = barSpacing + index * (barWidth + barSpacing)
            val top = h - bottomPadding - barHeight

            val barColor = when (data.status) {
                AchievementStatus.DONE -> Color(0xFF2E7D32)
                AchievementStatus.PARTIAL -> Color(0xFF0288D1)
                AchievementStatus.ZERO -> Color(0xFFE0E0E0)
            }

            drawRoundRect(
                color = barColor,
                topLeft = Offset(left, top),
                size = Size(barWidth, barHeight.coerceAtLeast(4f)),
                cornerRadius = CornerRadius(6f, 6f)
            )
        }
    }

    // Bar Labels below chart
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        chartData.forEach { data ->
            Text(
                text = data.label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = BluePrimary
                )
            )
        }
    }
}

@Composable
fun DailySummaryCard(
    summary: DailyHydrationSummary,
    modifier: Modifier = Modifier
) {
    val isArabic = LocalLayoutDirection.current == LayoutDirection.Rtl
    val locale = if (isArabic) Locale("ar") else Locale.ENGLISH
    val dateFormatter = DateTimeFormatter.ofPattern("EEEE، d MMMM yyyy", locale)

    val (statusLabel, statusColor, statusIcon) = when (summary.status) {
        AchievementStatus.DONE -> Triple(
            stringResource(R.string.status_done),
            Color(0xFF2E7D32),
            Icons.Default.CheckCircle
        )
        AchievementStatus.PARTIAL -> Triple(
            stringResource(R.string.status_partial),
            Color(0xFFF57C00),
            Icons.Default.HourglassBottom
        )
        AchievementStatus.ZERO -> Triple(
            stringResource(R.string.status_zero),
            Color(0xFF757575),
            Icons.Default.RemoveCircleOutline
        )
    }

    val formattedTotal = LocalizationUtils.formatNumber(summary.totalIntakeMl, isArabic)
    val formattedGoal = LocalizationUtils.formatNumber(summary.goalMl, isArabic)
    val formattedPercent = LocalizationUtils.formatNumber((summary.progressPercentage * 100).toInt(), isArabic)

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = summary.date.format(dateFormatter),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = statusColor.copy(alpha = 0.12f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = statusLabel,
                            tint = statusColor,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = statusLabel,
                            color = statusColor,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.intake_progress_format, formattedTotal, formattedGoal),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = BluePrimary
                    )
                )
                Text(
                    text = stringResource(R.string.progress_percentage, formattedPercent),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { summary.progressPercentage.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = statusColor,
                trackColor = Color(0xFFE0F2FE),
                strokeCap = StrokeCap.Round
            )
        }
    }
}
