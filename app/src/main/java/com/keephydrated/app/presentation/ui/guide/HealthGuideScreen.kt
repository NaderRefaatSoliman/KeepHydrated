package com.keephydrated.app.presentation.ui.guide

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.keephydrated.app.R
import com.keephydrated.app.domain.model.UserSettings
import com.keephydrated.app.domain.util.HydrationCalculator
import com.keephydrated.app.presentation.ui.theme.BluePrimary
import com.keephydrated.app.presentation.ui.theme.CyanSecondary
import com.keephydrated.app.util.LocalizationUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthGuideScreen(
    userSettings: UserSettings,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isArabic = LocalLayoutDirection.current == LayoutDirection.Rtl

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.guide_tour_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .verticalScroll(rememberScrollState())
        ) {
            HealthGuideContent(userSettings = userSettings, isArabic = isArabic)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun HealthGuideContent(
    userSettings: UserSettings,
    isArabic: Boolean,
    modifier: Modifier = Modifier
) {
    val rec = HydrationCalculator.calculate(
        age = userSettings.userAge,
        sex = userSettings.userSex,
        weightKg = userSettings.userWeightKg,
        heightCm = userSettings.userHeightCm,
        activityLevel = userSettings.userActivityLevel
    )

    val formattedGoal = LocalizationUtils.formatNumber(rec.recommendedDailyMl, isArabic)
    val formattedMax = LocalizationUtils.formatNumber(rec.safeMaxDailyMl, isArabic)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Mascot Speech Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CyanSecondary.copy(alpha = 0.12f)),
            elevation = CardDefaults.cardElevation(2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    modifier = Modifier.size(70.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_droppy_guide),
                            contentDescription = "Droppy",
                            modifier = Modifier.size(60.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    val name = if (userSettings.userName.isNotBlank()) userSettings.userName else if (isArabic) "صديقي" else "Friend"
                    Text(
                        text = stringResource(R.string.dr_droppy_name),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF01579B)
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.droppy_recommendation_msg, name),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF0D47A1)
                        )
                    )
                }
            }
        }

        // Prescription Summary Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.WaterDrop, contentDescription = null, tint = BluePrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.recalculated_goal_label, formattedGoal),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF01579B)
                        )
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                val sexLabel = if (userSettings.userSex == "male") {
                    if (isArabic) "ذكر" else "Male"
                } else {
                    if (isArabic) "أنثى" else "Female"
                }
                val activityLabel = when (userSettings.userActivityLevel) {
                    "sedentary" -> if (isArabic) "خامل" else "Sedentary"
                    "light" -> if (isArabic) "خفيف" else "Light"
                    "moderate" -> if (isArabic) "متوسط" else "Moderate"
                    "intense" -> if (isArabic) "مكثف" else "Intense"
                    else -> userSettings.userActivityLevel
                }
                val weightLabel = LocalizationUtils.formatNumber(userSettings.userWeightKg.toInt(), isArabic)
                val ageLabel = LocalizationUtils.formatNumber(userSettings.userAge, isArabic)
                val wUnit = if (isArabic) "كجم" else "kg"
                val aUnit = if (isArabic) "سنة" else "yrs"
                Text(
                    text = if (isArabic) "حسب المعايير: $weightLabel $wUnit، $ageLabel $aUnit، $sexLabel، النشاط: $activityLabel"
                           else "Based on: $weightLabel $wUnit, $ageLabel $aUnit, $sexLabel, Activity: $activityLabel",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF37474F)
                    )
                )
            }
        }

        // Section 1: Dangers & Effects of Dehydration
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
            elevation = CardDefaults.cardElevation(2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocalHospital, contentDescription = null, tint = Color(0xFFB71C1C))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.guide_dehydration_title),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB71C1C)
                        )
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.dehydration_intro),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF263238),
                        fontWeight = FontWeight.Normal
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = stringResource(R.string.dehydration_point1_title),
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFB71C1C),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = stringResource(R.string.dehydration_point1_desc),
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF212121))
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFCFD8DC))

                Text(
                    text = stringResource(R.string.dehydration_point2_title),
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFB71C1C),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = stringResource(R.string.dehydration_point2_desc),
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF212121))
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFCFD8DC))

                Text(
                    text = stringResource(R.string.dehydration_point3_title),
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFB71C1C),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = stringResource(R.string.dehydration_point3_desc),
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF212121))
                )
            }
        }

        // Section 2: ⚠️ Water Toxicity & Hyponatremia Warning
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
            elevation = CardDefaults.cardElevation(2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFE65100))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.guide_toxicity_title),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFBF360C)
                        )
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.toxicity_subtitle),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD84315)
                    )
                )
                Spacer(modifier = Modifier.height(14.dp))

                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.Speed, contentDescription = null, tint = Color(0xFFE65100), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.toxicity_kidney_limit_title),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFFBF360C))
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(R.string.toxicity_kidney_limit_desc),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF212121),
                                fontWeight = FontWeight.Normal
                            )
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFFFE082))

                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFE65100), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.toxicity_safe_ceiling_title),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFFBF360C))
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(R.string.toxicity_safe_ceiling_desc, formattedMax),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF212121),
                                fontWeight = FontWeight.Normal
                            )
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFFFE082))

                Text(
                    text = stringResource(R.string.toxicity_symptoms_title),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFFBF360C))
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.toxicity_symptoms_desc),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF212121),
                        fontWeight = FontWeight.Normal
                    )
                )
            }
        }

        // Section 3: Global Clinical Standards & WHO References
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
            elevation = CardDefaults.cardElevation(2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF2E7D32))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isArabic) "المراجع الطبية ومعايير منظمة الصحة العالمية (WHO)" else "Clinical References & WHO Guidelines",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B5E20)
                        )
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = if (isArabic) "توصيات منظمة الصحة العالمية (WHO):" else "World Health Organization (WHO) Guidelines:",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = if (isArabic) "تنص إرشادات منظمة الصحة العالمية على أن استهلاك الماء اليومي الأساسي للبالغين يتراوح بين ٢ إلى ٣ لترات في الظروف المعتدلة، مع ضرورة توزيع الشرب تدريجياً على مدار ساعات الاستيقاظ لتجنب إجهاد الكلى."
                           else "WHO guidelines indicate a baseline daily hydration requirement of 2.0 to 3.0 liters under temperate conditions, emphasizing consistent, spaced intake across waking hours to safeguard renal function.",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF212121))
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFA5D6A7))

                Text(
                    text = if (isArabic) "الهيئة الأوروبية لسلامة الأغذية (EFSA) و NASEM الأمريكية:" else "EFSA (Europe) & NASEM (USA) Dietary Standards:",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = if (isArabic) "تحدد المراجع العلمية (EFSA 2010 و NASEM 2005) معايير الترطيب الكافي بـ ٢٫٥ لتر/يوم للذكور و ٢٫٠ لتر/يوم للإناث، مع تعديل الهدف ديناميكياً بحسب النشاط البدني وكتلة الجسم، مع تأكيد ألا يتجاوز الشرب الساعي ٨٠٠ إلى ١٠٠٠ مل."
                           else "Dietary reference values set adequate intake at 2.5 L/day for males and 2.0 L/day for females, adjusted for body mass and physical exertion, confirming maximum hourly renal throughput of 800–1,000 ml/hr.",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF212121))
                )
            }
        }
    }
}
