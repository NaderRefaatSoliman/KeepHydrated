package com.keephydrated.app.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.keephydrated.app.domain.repository.SettingsRepository
import com.keephydrated.app.presentation.navigation.Screen
import com.keephydrated.app.presentation.ui.history.HistoryScreen
import com.keephydrated.app.presentation.ui.history.HistoryViewModel
import com.keephydrated.app.presentation.ui.home.HomeScreen
import com.keephydrated.app.presentation.ui.home.HomeViewModel
import com.keephydrated.app.presentation.ui.settings.SettingsScreen
import com.keephydrated.app.presentation.ui.settings.SettingsViewModel
import com.keephydrated.app.presentation.ui.theme.BluePrimary
import com.keephydrated.app.presentation.ui.theme.KeepHydratedTheme
import com.keephydrated.app.util.LocaleHelper
import com.keephydrated.app.worker.ReminderScheduler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var reminderScheduler: ReminderScheduler

    @Inject
    lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            try {
                val settings = settingsRepository.getUserSettings().first()
                // Set initial locale
                LocaleHelper.setLocale(this@MainActivity, settings.language)

                if (settings.remindersEnabled) {
                    reminderScheduler.scheduleReminders(
                        intervalMinutes = settings.reminderIntervalMinutes,
                        enabled = true,
                        mode = settings.reminderMode
                    )
                }
            } catch (_: Exception) {
                // Graceful fallback
            }
        }

        setContent {
            val userSettings by settingsRepository.getUserSettings().collectAsState(initial = null)
            val isArabic = userSettings?.language == "ar"
            val layoutDirection = if (isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr

            CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                KeepHydratedTheme {
                    KeepHydratedMain()
                }
            }
        }
    }
}

@Composable
fun KeepHydratedMain() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                Screen.items.forEach { screen ->
                    val selected = currentRoute == screen.route
                    val title = stringResource(screen.titleResId)
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(screen.icon, contentDescription = title) },
                        label = { Text(title) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BluePrimary,
                            selectedTextColor = BluePrimary
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                val viewModel: HomeViewModel = hiltViewModel()
                HomeScreen(viewModel = viewModel)
            }
            composable(Screen.History.route) {
                val viewModel: HistoryViewModel = hiltViewModel()
                HistoryScreen(viewModel = viewModel)
            }
            composable(Screen.Settings.route) {
                val viewModel: SettingsViewModel = hiltViewModel()
                SettingsScreen(viewModel = viewModel)
            }
        }
    }
}
