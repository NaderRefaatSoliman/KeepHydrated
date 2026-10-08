package com.keephydrated.app.presentation.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Pool
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keephydrated.app.R
import com.keephydrated.app.presentation.ui.components.LanguageSwitcher
import com.keephydrated.app.presentation.ui.theme.BluePrimary
import com.keephydrated.app.presentation.ui.theme.CyanSecondary
import com.keephydrated.app.util.LocalizationUtils

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
    currentLanguage: String = "en",
    onLanguageChanged: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val isArabic = LocalLayoutDirection.current == LayoutDirection.Rtl

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LanguageSwitcher(
                    currentLanguage = currentLanguage,
                    onLanguageChanged = onLanguageChanged
                )

                TextButton(onClick = { viewModel.skipOnboarding(onFinished) }) {
                    Text(
                        text = stringResource(R.string.skip_for_now),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (uiState.currentStep > 0) {
                    OutlinedButton(
                        onClick = { viewModel.prevStep() },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(stringResource(R.string.btn_back))
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                if (uiState.currentStep < 4) {
                    Button(
                        onClick = { viewModel.nextStep() },
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(stringResource(R.string.btn_next))
                    }
                } else {
                    Button(
                        onClick = { viewModel.completeOnboarding(onFinished) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(stringResource(R.string.btn_finish_onboarding), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Step Progress Indicator Dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                (0..4).forEach { stepIndex ->
                    Box(
                        modifier = Modifier
                            .size(if (uiState.currentStep == stepIndex) 10.dp else 7.dp)
                            .clip(CircleShape)
                            .background(
                                if (uiState.currentStep == stepIndex) BluePrimary
                                else if (uiState.currentStep > stepIndex) CyanSecondary
                                else Color.LightGray
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Droppy Mascot Banner
            Surface(
                shape = CircleShape,
                color = CyanSecondary.copy(alpha = 0.15f),
                modifier = Modifier.size(110.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_droppy_guide),
                        contentDescription = "Droppy Mascot",
                        modifier = Modifier.size(90.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedContent(
                targetState = uiState.currentStep,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "onboardingStepAnimation"
            ) { step ->
                when (step) {
                    0 -> StepWelcome()
                    1 -> StepNameAndBio(uiState, viewModel)
                    2 -> StepMetrics(uiState, viewModel)
                    3 -> StepActivity(uiState, viewModel)
                    4 -> StepRecommendation(uiState, isArabic, viewModel)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun StepWelcome() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = stringResource(R.string.onboarding_welcome_title),
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = BluePrimary
            ),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = stringResource(R.string.onboarding_welcome_subtitle),
            style = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun StepNameAndBio(
    uiState: OnboardingUiState,
    viewModel: OnboardingViewModel
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.step_profile_title),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.step_profile_desc),
            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = uiState.name,
            onValueChange = { viewModel.updateName(it) },
            label = { Text(stringResource(R.string.label_name)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = uiState.age,
            onValueChange = { viewModel.updateAge(it) },
            label = { Text(stringResource(R.string.label_age)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = stringResource(R.string.label_sex),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FilterChip(
                selected = uiState.sex == "male",
                onClick = { viewModel.updateSex("male") },
                label = { Text("👨 ${stringResource(R.string.sex_male)}") },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = uiState.sex == "female",
                onClick = { viewModel.updateSex("female") },
                label = { Text("👩 ${stringResource(R.string.sex_female)}") },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun StepMetrics(
    uiState: OnboardingUiState,
    viewModel: OnboardingViewModel
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.step_metrics_title),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = uiState.weightKg,
            onValueChange = { viewModel.updateWeight(it) },
            label = { Text(stringResource(R.string.label_weight)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = uiState.heightCm,
            onValueChange = { viewModel.updateHeight(it) },
            label = { Text(stringResource(R.string.label_height)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun StepActivity(
    uiState: OnboardingUiState,
    viewModel: OnboardingViewModel
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.label_activity),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(14.dp))

        val options = listOf(
            "sedentary" to stringResource(R.string.activity_sedentary),
            "light" to stringResource(R.string.activity_light),
            "moderate" to stringResource(R.string.activity_moderate),
            "intense" to stringResource(R.string.activity_intense)
        )

        options.forEach { (key, label) ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (uiState.activityLevel == key) BluePrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
                    .clickable { viewModel.updateActivity(key) }
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val icon = when (key) {
                        "sedentary" -> Icons.Default.Hotel
                        "light" -> Icons.Default.DirectionsRun
                        "moderate" -> Icons.Default.FitnessCenter
                        else -> Icons.Default.Pool
                    }
                    Icon(icon, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (uiState.activityLevel == key) FontWeight.Bold else FontWeight.Normal
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun StepRecommendation(
    uiState: OnboardingUiState,
    isArabic: Boolean,
    viewModel: OnboardingViewModel
) {
    val name = if (uiState.name.isNotBlank()) uiState.name else if (isArabic) "صديقي" else "Friend"
    val formattedGoal = LocalizationUtils.formatNumber(uiState.customGoalMl, isArabic)
    val formattedMax = LocalizationUtils.formatNumber(uiState.safeMaxDailyMl, isArabic)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.step_recommendation_title),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.droppy_recommendation_msg, name),
            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Target Card
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(3.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$formattedGoal ml",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = BluePrimary,
                        fontSize = 38.sp
                    )
                )
                Text(
                    text = stringResource(R.string.recalculated_goal_label, formattedGoal),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Adjustment Slider
                Slider(
                    value = uiState.customGoalMl.toFloat(),
                    onValueChange = { viewModel.updateCustomGoal(it.toInt()) },
                    valueRange = 1000f..5000f,
                    steps = 39,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Toxicity Warning Box
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFFF3E0),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFE65100),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.toxicity_safe_ceiling_desc, formattedMax),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFFE65100),
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            }
        }
    }
}
