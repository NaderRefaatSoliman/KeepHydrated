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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keephydrated.app.R
import com.keephydrated.app.domain.model.BottleStatus
import com.keephydrated.app.presentation.ui.theme.BluePrimary
import com.keephydrated.app.presentation.ui.theme.CyanSecondary

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
                        text = stringResource(R.string.bottle_refilled_today, bottleStatus.refillCount),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = BluePrimary
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Body: Bottle Illustration & Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Stylized Water Bottle Graphic
                Box(
                    modifier = Modifier
                        .size(width = 54.dp, height = 90.dp)
                        .padding(end = 12.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Canvas(modifier = Modifier.matchParentSize()) {
                        val w = size.width
                        val h = size.height

                        // Bottle Cap
                        drawRoundRect(
                            color = Color(0xFF0288D1),
                            topLeft = Offset(w * 0.35f, 0f),
                            size = Size(w * 0.3f, h * 0.1f),
                            cornerRadius = CornerRadius(4f, 4f)
                        )
                        // Bottle Neck
                        drawRect(
                            color = Color(0xFFB3E5FC),
                            topLeft = Offset(w * 0.38f, h * 0.1f),
                            size = Size(w * 0.24f, h * 0.08f)
                        )
                        // Bottle Glass Outline
                        drawRoundRect(
                            color = Color(0xFFE1F5FE),
                            topLeft = Offset(0f, h * 0.18f),
                            size = Size(w, h * 0.82f),
                            cornerRadius = CornerRadius(12f, 12f)
                        )

                        // Remaining Liquid (Fill from bottom)
                        val remainingFraction = 1f - animatedProgress
                        if (remainingFraction > 0f) {
                            val liquidHeight = h * 0.82f * remainingFraction
                            val liquidTopY = h - liquidHeight
                            drawRoundRect(
                                color = Color(0xFF03A9F4),
                                topLeft = Offset(2f, liquidTopY),
                                size = Size(w - 4f, liquidHeight - 2f),
                                cornerRadius = CornerRadius(10f, 10f)
                            )
                        }
                    }
                }

                // Stats Column
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(
                            R.string.bottle_remaining,
                            bottleStatus.remainingMl,
                            bottleStatus.volumeMl
                        ),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
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
                                )
                            )
                        }
                    } else {
                        Text(
                            text = stringResource(R.string.bottle_time_left, bottleStatus.remainingMinutes),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
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

            // Action Buttons: Drink Sip & Refill Bottle
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
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = stringResource(R.string.bottle_drink_sip, frequentAmountMl),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = onRefillBottle,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
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
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
