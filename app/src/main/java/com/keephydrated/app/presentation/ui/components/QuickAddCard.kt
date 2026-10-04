package com.keephydrated.app.presentation.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keephydrated.app.presentation.ui.theme.BluePrimaryContainer
import com.keephydrated.app.presentation.ui.theme.BlueOnPrimaryContainer

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
        ContainerOption("Flask", 750, "🫙")
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Quick Add",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            containers.take(3).forEach { container ->
                QuickAddCard(
                    container = container,
                    onClick = { onAddWater(container.amountMl) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            containers.drop(3).forEach { container ->
                QuickAddCard(
                    container = container,
                    onClick = { onAddWater(container.amountMl) },
                    modifier = Modifier.weight(1f)
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
        modifier = modifier
            .clickable(onClick = onClick)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp)
        ) {
            Text(
                text = container.emoji,
                fontSize = 28.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "+${container.amountMl} ml",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = BlueOnPrimaryContainer
                )
            )
            Text(
                text = container.title,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = BlueOnPrimaryContainer.copy(alpha = 0.8f)
                )
            )
        }
    }
}
