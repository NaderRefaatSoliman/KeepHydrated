package com.keephydrated.app.presentation.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keephydrated.app.R
import com.keephydrated.app.domain.model.BottleStatus
import com.keephydrated.app.presentation.ui.theme.BluePrimary
import com.keephydrated.app.presentation.ui.theme.CyanSecondary
import com.keephydrated.app.util.LocalizationUtils

@Composable
fun BottleTrackingCard(
    bottleStatus: BottleStatus,
    frequentAmountMl: Int,
    onDrinkSip: () -> Unit,
    onRefillBottle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = bottleStatus.progressFraction
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "bottleProgress"
    )

    val isArabic = LocalLayoutDirection.current == LayoutDirection.Rtl
    val formattedRemaining = LocalizationUtils.formatNumber(bottleStatus.remainingMl, isArabic)
    val formattedVolume = LocalizationUtils.formatNumber(bottleStatus.volumeMl, isArabic)
    val formattedRefills = LocalizationUtils.formatNumber(bottleStatus.refillCount, isArabic)
    val formattedTimeLeft = LocalizationUtils.formatNumber(bottleStatus.remainingMinutes, isArabic)
    val formattedSip = LocalizationUtils.formatNumber(frequentAmountMl, isArabic)

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(3.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Title & Refill Count Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocalDrink,
                        contentDescription = null,
                        tint = BluePrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.bottle_status_active),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = BluePrimary
                        )
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = CyanSecondary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = stringResource(R.string.bottle_refilled_today, formattedRefills),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = BluePrimary
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Body: Ergonomic Sports Water Bottle Illustration & Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // High-Fidelity Sports Water Bottle Graphic
                Box(
                    modifier = Modifier
                        .size(width = 62.dp, height = 110.dp)
                        .padding(end = 12.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Canvas(modifier = Modifier.matchParentSize()) {
                        val w = size.width
                        val h = size.height

                        // 1. Carry Loop on Cap
                        drawArc(
                            color = Color(0xFF0277BD),
                            startAngle = 180f,
                            sweepAngle = 180f,
                            useCenter = false,
                            topLeft = Offset(w * 0.42f, 0f),
                            size = Size(w * 0.28f, h * 0.10f),
                            style = Stroke(width = 2.5f, cap = StrokeCap.Round)
                        )

                        // 2. Flip-Top Sports Nozzle Cap
                        drawRoundRect(
                            color = Color(0xFF01579B),
                            topLeft = Offset(w * 0.32f, h * 0.06f),
                            size = Size(w * 0.36f, h * 0.08f),
                            cornerRadius = CornerRadius(4f, 4f)
                        )

                        // 3. Spout Ring Collar
                        drawRoundRect(
                            color = Color(0xFF0288D1),
                            topLeft = Offset(w * 0.28f, h * 0.13f),
                            size = Size(w * 0.44f, h * 0.04f),
                            cornerRadius = CornerRadius(3f, 3f)
                        )

                        // 4. Ergonomic Contoured Bottle Body Path (Glass/Tritan Body)
                        val bottleBodyTop = h * 0.17f
                        val bottleBodyBottom = h - 2f
                        val bodyHeight = bottleBodyBottom - bottleBodyTop
                        val leftX = w * 0.08f
                        val rightX = w * 0.92f

                        // Outer Bottle Silhouette (Frosted Translucent Container)
                        val bodyPath = Path().apply {
                            moveTo(w * 0.28f, bottleBodyTop)
                            // Shoulder to upper body
                            cubicTo(w * 0.15f, bottleBodyTop + bodyHeight * 0.05f, leftX, bottleBodyTop + bodyHeight * 0.12f, leftX, bottleBodyTop + bodyHeight * 0.25f)
                            // Waist contour indentation (ergonomic grip)
                            cubicTo(leftX, bottleBodyTop + bodyHeight * 0.45f, leftX + 4f, bottleBodyTop + bodyHeight * 0.52f, leftX + 4f, bottleBodyTop + bodyHeight * 0.60f)
                            cubicTo(leftX + 4f, bottleBodyTop + bodyHeight * 0.68f, leftX, bottleBodyTop + bodyHeight * 0.75f, leftX, bottleBodyTop + bodyHeight * 0.88f)
                            // Rounded bottom-left corner
                            quadraticTo(leftX, bottleBodyBottom, leftX + 12f, bottleBodyBottom)
                            // Base bottom line
                            lineTo(rightX - 12f, bottleBodyBottom)
                            // Rounded bottom-right corner
                            quadraticTo(rightX, bottleBodyBottom, rightX, bottleBodyBottom - 12f)
                            // Right side ergonomic grip
                            cubicTo(rightX, bottleBodyTop + bodyHeight * 0.75f, rightX - 4f, bottleBodyTop + bodyHeight * 0.68f, rightX - 4f, bottleBodyTop + bodyHeight * 0.60f)
                            cubicTo(rightX - 4f, bottleBodyTop + bodyHeight * 0.52f, rightX, bottleBodyTop + bodyHeight * 0.45f, rightX, bottleBodyTop + bodyHeight * 0.25f)
                            // Shoulder to neck
                            cubicTo(rightX, bottleBodyTop + bodyHeight * 0.12f, w * 0.85f, bottleBodyTop + bodyHeight * 0.05f, w * 0.72f, bottleBodyTop)
                            close()
                        }

                        // Draw Body Background (Frosted translucent blue)
                        drawPath(
                            path = bodyPath,
                            color = Color(0xFFE1F5FE).copy(alpha = 0.85f)
                        )

                        // 5. Liquid Fill (clipped inside bottle contour)
                        val remainingFraction = (1f - animatedProgress).coerceIn(0f, 1f)
                        if (remainingFraction > 0.02f) {
                            val liquidLevelY = bottleBodyBottom - (bodyHeight * remainingFraction)
                            val liquidColor = if (bottleStatus.needsRefill) Color(0xFFFFA726) else Color(0xFF00B0FF)

                            val liquidPath = Path().apply {
                                moveTo(leftX + 2f, bottleBodyBottom - 4f)
                                lineTo(leftX + 2f, liquidLevelY)
                                // Wave Meniscus
                                quadraticTo(w * 0.5f, liquidLevelY - 2.5f, rightX - 2f, liquidLevelY)
                                lineTo(rightX - 2f, bottleBodyBottom - 4f)
                                quadraticTo(rightX - 2f, bottleBodyBottom, rightX - 10f, bottleBodyBottom)
                                lineTo(leftX + 10f, bottleBodyBottom)
                                quadraticTo(leftX + 2f, bottleBodyBottom, leftX + 2f, bottleBodyBottom - 4f)
                                close()
                            }

                            drawPath(path = liquidPath, color = liquidColor)

                            // Wave Surface Highlight Line
                            drawLine(
                                color = Color.White.copy(alpha = 0.8f),
                                start = Offset(leftX + 4f, liquidLevelY),
                                end = Offset(rightX - 4f, liquidLevelY),
                                strokeWidth = 1.8f,
                                cap = StrokeCap.Round
                            )

                            // Small Bubbles
                            drawCircle(
                                color = Color.White.copy(alpha = 0.7f),
                                radius = 2.2f,
                                center = Offset(w * 0.45f, (liquidLevelY + bottleBodyBottom) * 0.5f)
                            )
                            drawCircle(
                                color = Color.White.copy(alpha = 0.6f),
                                radius = 1.6f,
                                center = Offset(w * 0.60f, (liquidLevelY + bottleBodyBottom) * 0.65f)
                            )
                        }

                        // 6. Bottle Glass Border
                        drawPath(
                            path = bodyPath,
                            color = Color(0xFF81D4FA),
                            style = Stroke(width = 1.6f)
                        )

                        // 7. Volume Measurement Graduation Ticks on Left Side
                        val tickLeft = leftX + 3f
                        listOf(0.25f, 0.50f, 0.75f).forEach { fraction ->
                            val tickY = bottleBodyBottom - (bodyHeight * fraction)
                            val tickLen = if (fraction == 0.50f) 8f else 5f
                            drawLine(
                                color = Color(0xFF0288D1).copy(alpha = 0.65f),
                                start = Offset(tickLeft, tickY),
                                end = Offset(tickLeft + tickLen, tickY),
                                strokeWidth = 1.2f,
                                cap = StrokeCap.Round
                            )
                        }

                        // 8. Specular Reflection Highlight (Right Glare Streak)
                        drawLine(
                            color = Color.White.copy(alpha = 0.65f),
                            start = Offset(rightX - 5f, bottleBodyTop + bodyHeight * 0.15f),
                            end = Offset(rightX - 5f, bottleBodyBottom - bodyHeight * 0.20f),
                            strokeWidth = 1.8f,
                            cap = StrokeCap.Round
                        )
                    }
                }

                // Stats Column
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(
                            R.string.bottle_remaining,
                            formattedRemaining,
                            formattedVolume
                        ),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    if (bottleStatus.needsRefill) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFE65100),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (bottleStatus.isBottleEmpty) {
                                    stringResource(R.string.bottle_empty)
                                } else {
                                    stringResource(R.string.bottle_time_up)
                                },
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFFE65100),
                                    fontWeight = FontWeight.Bold
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    } else {
                        Text(
                            text = stringResource(R.string.bottle_time_left, formattedTimeLeft),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = if (bottleStatus.needsRefill) Color(0xFFE65100) else BluePrimary,
                        trackColor = Color(0xFFE0F2FE),
                        strokeCap = StrokeCap.Round
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons: Drink Sip & Refill Bottle (Fully scalable with maxLines & softWrap)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onDrinkSip,
                    enabled = bottleStatus.remainingMl > 0,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BluePrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                ) {
                    Text(
                        text = stringResource(R.string.bottle_drink_sip, formattedSip),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                OutlinedButton(
                    onClick = onRefillBottle,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.bottle_refill_action),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
