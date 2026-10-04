package com.keephydrated.app.presentation.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keephydrated.app.presentation.ui.theme.BlueOnPrimaryContainer
import com.keephydrated.app.presentation.ui.theme.BluePrimaryContainer

data class ContainerOption(
    val title: String,
    val amountMl: Int,
    val emoji: String
)

@Composable
fun QuickAddSection(
    onAddWater: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val containers = listOf(
        ContainerOption("Cup", 150, "☕"),
        ContainerOption("Glass", 250, "🥛"),
        ContainerOption("Can", 330, "🥤"),
        ContainerOption("Bottle", 500, "🍶"),
        ContainerOption("Flask", 750, "🫙"),
        ContainerOption("Large", 1000, "🚰")
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Quick Add",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
        )

        // Horizontally scrollable row ensures all cards scale perfectly on any device width
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(horizontal = 2.dp, vertical = 4.dp)
        ) {
            items(containers, key = { it.amountMl }) { container ->
                QuickAddCard(
                    container = container,
                    onClick = { onAddWater(container.amountMl) },
                    modifier = Modifier.width(92.dp)
                )
            }
        }
    }
}

@Composable
fun QuickAddCard(
    container: ContainerOption,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = BluePrimaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 6.dp)
        ) {
            Text(
                text = container.emoji,
                fontSize = 26.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "+${container.amountMl}ml",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = BlueOnPrimaryContainer
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = container.title,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.sp,
                    color = BlueOnPrimaryContainer.copy(alpha = 0.85f)
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
