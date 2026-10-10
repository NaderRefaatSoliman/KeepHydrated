package com.keephydrated.app.presentation.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keephydrated.app.R
import com.keephydrated.app.presentation.ui.theme.BluePrimary
import com.keephydrated.app.presentation.ui.theme.CyanSecondary
import kotlinx.coroutines.delay

@Composable
fun DrDroppyMascotButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp
) {
    // Increased level of animation: bouncy vertical jump + arm/hand waving motion + breathing
    val infiniteTransition = rememberInfiniteTransition(label = "droppyJumpTransition")

    val verticalJump by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -12f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1400
                0f at 0 using LinearOutSlowInEasing
                -12f at 400 using FastOutSlowInEasing
                -2f at 700 using LinearOutSlowInEasing
                -6f at 900 using FastOutSlowInEasing
                0f at 1200 using FastOutSlowInEasing
                0f at 1400
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "verticalJump"
    )

    // Hand/arm waving angle animation
    val wavingAngle by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 900
                -6f at 0 using FastOutSlowInEasing
                8f at 220 using FastOutSlowInEasing
                -4f at 450 using FastOutSlowInEasing
                7f at 680 using FastOutSlowInEasing
                -6f at 900 using FastOutSlowInEasing
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "wavingAngle"
    )

    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathingScale"
    )

    Box(
        modifier = modifier
            .size(size)
            .scale(breathingScale)
            .offset(y = verticalJump.dp)
            .graphicsLayer {
                rotationZ = wavingAngle
                transformOrigin = TransformOrigin(0.35f, 0.70f)
            }
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_droppy_guide),
            contentDescription = stringResource(R.string.dr_droppy_name),
            modifier = Modifier.size(size)
        )
    }
}

@Composable
fun DrDroppyFloatingMascot(
    onClick: () -> Unit,
    showTips: Boolean,
    modifier: Modifier = Modifier
) {
    val isArabic = LocalLayoutDirection.current == LayoutDirection.Rtl
    var tipVisible by remember { mutableStateOf(false) }
    var currentTip by remember { mutableStateOf("") }

    val tipsList = if (isArabic) {
        listOf(
            "اشرب الماء على دفعات منتظمة! 💧",
            "لا تتجاوز لتر ماء في الساعة الواحدة لحماية كليتيك! 🩺",
            "ابدأ يومك بكوب ماء منعش لتنشيط جسمك! ✨",
            "الماء يحسن تركيزك وطاقتك اليومية! 🧠",
            "تجنب الإفراط المفاجئ في الشرب، الاعتدال سر الصحة! ⚖️",
            "شرب الماء قبل الوجبات بـ ٣٠ دقيقة يساعد على تحسين الهضم! 🥗",
            "لون البول الفاتح الشفاف علامة ممتازة على رطوبة جسمك المثالية! 🎯",
            "اشرب الماء بانتظام أثناء ممارسة الرياضة لتعويض الأملاح المفقودة! 🏃‍♂️",
            "الترطيب الجيد يمنع الصداع النصفي ويزيد نضارة بشرتك! 🌟",
            "الماء البارد ينعش الجسم والماء الفاتر يهدئ المعدة! 🚰",
            "الشعور بالعطش يعني أن جسمك بدأ بالفعل في الجفاف، اشرب الآن! ⏳",
            "تناول الخضروات والفواكه يمنحك ترطيباً طبيعياً غنياً بالألياف! 🍉",
            "كوب ماء قبل النوم يحافظ على نشاط دورتك الدموية أثناء الراحة! 🌙",
            "استبدل المشروبات الغازية بالماء لصحة أفضل لكليتيك وقلبك! ❤️",
            "الترطيب الكافي يحمي المفاصل ويقلل من آلام الظهر والإجهاد! 💪"
        )
    } else {
        listOf(
            "Pace your water throughout the day! 💧",
            "Kidneys excrete max 800-1,000 ml/hr. Sip wisely! 🩺",
            "Start your morning with a fresh glass of water! ✨",
            "Proper hydration boosts energy and focus! 🧠",
            "Avoid chugging water all at once; consistency is key! ⚖️",
            "Drinking water 30 min before meals aids digestion! 🥗",
            "Pale, light-colored urine is a great sign of optimal hydration! 🎯",
            "Replenish fluids regularly during workouts and exercise! 🏃‍♂️",
            "Staying hydrated relieves tension headaches and refreshes skin! 🌟",
            "Room-temp water is soothing, cool water is refreshing! 🚰",
            "Feeling thirsty means mild dehydration has already started—drink up! ⏳",
            "Fresh fruits like watermelon provide natural, electrolyte-rich hydration! 🍉",
            "A glass of water before bed keeps circulation smooth overnight! 🌙",
            "Swap sugary sodas for water to protect your kidneys and heart! ❤️",
            "Healthy hydration cushions joints and reduces physical fatigue! 💪"
        )
    }

    LaunchedEffect(showTips) {
        if (showTips) {
            delay(1200)
            currentTip = tipsList.random()
            tipVisible = true
            delay(7000)
            tipVisible = false
        }
    }

    Row(
        modifier = modifier.padding(16.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isArabic) {
            DrDroppyMascotButton(
                onClick = onClick,
                size = 62.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
            AnimatedVisibility(
                visible = tipVisible,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                DroppySpeechBubble(text = currentTip, isArabic = false)
            }
        } else {
            AnimatedVisibility(
                visible = tipVisible,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                DroppySpeechBubble(text = currentTip, isArabic = true)
            }
            Spacer(modifier = Modifier.width(8.dp))
            DrDroppyMascotButton(
                onClick = onClick,
                size = 62.dp
            )
        }
    }
}

@Composable
fun DroppySpeechBubble(
    text: String,
    isArabic: Boolean
) {
    Surface(
        shape = RoundedCornerShape(
            topStart = 16.dp,
            topEnd = 16.dp,
            bottomStart = if (isArabic) 16.dp else 2.dp,
            bottomEnd = if (isArabic) 2.dp else 16.dp
        ),
        color = Color(0xFFE1F5FE),
        shadowElevation = 4.dp,
        modifier = Modifier
            .widthIn(max = 200.dp)
            .padding(bottom = 8.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Bold,
                color = BluePrimary,
                fontSize = 11.sp,
                lineHeight = 15.sp
            ),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}
