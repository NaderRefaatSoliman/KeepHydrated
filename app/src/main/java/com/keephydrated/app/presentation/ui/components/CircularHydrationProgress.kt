package com.keephydrated.app.presentation.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keephydrated.app.R
import com.keephydrated.app.presentation.ui.theme.BluePrimary
import com.keephydrated.app.presentation.ui.theme.CyanSecondary
import com.keephydrated.app.util.LocalizationUtils
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CircularHydrationProgress(
    currentMl: Int,
    goalMl: Int,
    frequentIntakeMl: Int = 250,
    onFrequentIntakeClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    size: Dp = 250.dp,
    strokeWidth: Dp = 18.dp
) {
    val progress = if (goalMl > 0) (currentMl.toFloat() / goalMl.toFloat()).coerceIn(0f, 1f) else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "progressAnimation"
    )

    val percentage = if (goalMl > 0) ((currentMl.toFloat() / goalMl.toFloat()) * 100).toInt() else 0
    val isArabic = LocalLayoutDirection.current == LayoutDirection.Rtl

    val formattedPercent = LocalizationUtils.formatNumber(percentage, isArabic)
    val formattedCurrent = LocalizationUtils.formatNumber(currentMl, isArabic)
    val formattedGoal = LocalizationUtils.formatNumber(goalMl, isArabic)
    val formattedFrequent = LocalizationUtils.formatNumber(frequentIntakeMl, isArabic)

    // Animated water wave transition
    val infiniteTransition = rememberInfiniteTransition(label = "waveTransition")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveOffset"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // Inner Water Wave Canvas (Clipped to circular interior)
        Box(
            modifier = Modifier
                .size(size - strokeWidth * 2 - 8.dp)
                .clip(CircleShape)
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val width = this.size.width
                val height = this.size.height

                val waterLevelY = height * (1f - animatedProgress)

                if (animatedProgress > 0f) {
                    val wavePath = Path()
                    wavePath.moveTo(0f, height)
                    wavePath.lineTo(0f, waterLevelY)

                    val waveAmplitude = 10f * (1f - (animatedProgress - 0.5f).let { if (it < 0) -it else it } * 1.5f).coerceAtLeast(0.2f)
                    val waveFrequency = 1.8f

                    var x = 0f
                    while (x <= width) {
                        val relativeX = x / width
                        val y = waterLevelY + waveAmplitude * sin(relativeX * 2 * PI.toFloat() * waveFrequency + waveOffset)
                        wavePath.lineTo(x, y)
                        x += 4f
                    }

                    wavePath.lineTo(width, height)
                    wavePath.close()

                    drawPath(
                        path = wavePath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                CyanSecondary.copy(alpha = 0.35f),
                                BluePrimary.copy(alpha = 0.22f),
                                Color(0xFF0288D1).copy(alpha = 0.15f)
                            ),
                            startY = waterLevelY,
                            endY = height
                        )
                    )
                }
            }
        }

        // Circular Ring Canvas
        Canvas(modifier = Modifier.size(size)) {
            val stroke = strokeWidth.toPx()
            val arcSize = Size(size.toPx() - stroke, size.toPx() - stroke)
            val topLeft = Offset(stroke / 2, stroke / 2)

            // Background Track
            drawArc(
                color = Color(0xFFE0F2FE),
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )

            // Progress Arc
            if (animatedProgress > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        0.0f to CyanSecondary,
                        0.5f to BluePrimary,
                        1.0f to BluePrimary
                    ),
                    startAngle = 135f,
                    sweepAngle = 270f * animatedProgress,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
            }

            // Frequent Drinking Amount Marker on the Track
            if (goalMl > 0 && frequentIntakeMl in 1..goalMl) {
                val frequentFraction = (frequentIntakeMl.toFloat() / goalMl.toFloat()).coerceIn(0f, 1f)
                val markerAngle = 135f + (270f * frequentFraction)
                val angleRad = Math.toRadians(markerAngle.toDouble())

                val radius = (size.toPx() - stroke) / 2
                val centerX = size.toPx() / 2
                val centerY = size.toPx() / 2

                val markerX = (centerX + radius * cos(angleRad)).toFloat()
                val markerY = (centerY + radius * sin(angleRad)).toFloat()

                drawCircle(
                    color = Color.White,
                    radius = stroke * 0.38f,
                    center = Offset(markerX, markerY)
                )
                drawCircle(
                    color = Color(0xFF0288D1),
                    radius = stroke * 0.25f,
                    center = Offset(markerX, markerY)
                )
            }
        }

        // Center Content Overlay
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable(enabled = onFrequentIntakeClick != null) {
                onFrequentIntakeClick?.invoke()
            }
        ) {
            Text(
                text = stringResource(R.string.progress_percentage, formattedPercent),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = BluePrimary,
                    fontSize = 38.sp
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = stringResource(R.string.intake_progress_format, formattedCurrent, formattedGoal),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )

            // Frequent Drinking selected amount badge on the progress circle
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = BluePrimary.copy(alpha = 0.12f),
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Text(
                    text = "💧 ${stringResource(R.string.frequent_sip_badge, formattedFrequent)}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = BluePrimary
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }

            if (currentMl >= goalMl && goalMl > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.goal_achieved_badge),
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}
