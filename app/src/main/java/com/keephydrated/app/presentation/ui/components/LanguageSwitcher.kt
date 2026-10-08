package com.keephydrated.app.presentation.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keephydrated.app.presentation.ui.theme.BluePrimary

/**
 * Compact dual-icon language switcher (ع / En) for instantaneous language toggle
 * across onboarding and all navigation tabs.
 */
@Composable
fun LanguageSwitcher(
    currentLanguage: String,
    onLanguageChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isArabic = currentLanguage == "ar"

    val arBgColor by animateColorAsState(
        targetValue = if (isArabic) BluePrimary else Color.Transparent,
        animationSpec = tween(durationMillis = 200),
        label = "arBg"
    )
    val arTextColor by animateColorAsState(
        targetValue = if (isArabic) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(durationMillis = 200),
        label = "arText"
    )

    val enBgColor by animateColorAsState(
        targetValue = if (!isArabic) BluePrimary else Color.Transparent,
        animationSpec = tween(durationMillis = 200),
        label = "enBg"
    )
    val enTextColor by animateColorAsState(
        targetValue = if (!isArabic) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(durationMillis = 200),
        label = "enText"
    )

    Surface(
        modifier = modifier
            .border(
                width = 1.dp,
                color = BluePrimary.copy(alpha = 0.35f),
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Arabic option "ع"
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(arBgColor)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        role = Role.RadioButton
                    ) {
                        if (!isArabic) onLanguageChanged("ar")
                    }
                    .defaultMinSize(minWidth = 32.dp, minHeight = 28.dp)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "ع",
                    fontSize = 13.sp,
                    fontWeight = if (isArabic) FontWeight.Bold else FontWeight.Medium,
                    color = arTextColor
                )
            }

            // English option "En"
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(enBgColor)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        role = Role.RadioButton
                    ) {
                        if (isArabic) onLanguageChanged("en")
                    }
                    .defaultMinSize(minWidth = 32.dp, minHeight = 28.dp)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "En",
                    fontSize = 13.sp,
                    fontWeight = if (!isArabic) FontWeight.Bold else FontWeight.Medium,
                    color = enTextColor
                )
            }
        }
    }
}
