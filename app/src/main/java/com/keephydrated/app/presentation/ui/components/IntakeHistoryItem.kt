package com.keephydrated.app.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.keephydrated.app.R
import com.keephydrated.app.domain.model.WaterIntake
import com.keephydrated.app.presentation.ui.theme.BluePrimary
import com.keephydrated.app.util.LocalizationUtils
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun IntakeHistoryItem(
    intake: WaterIntake,
    onDelete: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val isArabic = LocalLayoutDirection.current == LayoutDirection.Rtl
    val locale = if (isArabic) Locale("ar") else Locale.ENGLISH
    val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a", locale)

    val formattedAmount = LocalizationUtils.formatNumber(intake.amountMl, isArabic)
    val unitMl = stringResource(R.string.unit_ml)

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.WaterDrop,
                    contentDescription = null,
                    tint = BluePrimary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = stringResource(R.string.action_plus_ml, formattedAmount, unitMl),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = intake.timestamp.format(timeFormatter),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }

            IconButton(onClick = { onDelete(intake.id) }) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete entry",
                    tint = Color.Gray
                )
            }
        }
    }
}
