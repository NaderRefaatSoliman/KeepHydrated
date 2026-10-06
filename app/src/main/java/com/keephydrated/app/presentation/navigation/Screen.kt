package com.keephydrated.app.presentation.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.keephydrated.app.R

sealed class Screen(val route: String, @StringRes val titleResId: Int, val icon: ImageVector) {
    object Home : Screen("home", R.string.nav_home, Icons.Default.Opacity)
    object History : Screen("history", R.string.nav_history, Icons.Default.DateRange)
    object Settings : Screen("settings", R.string.nav_settings, Icons.Default.Settings)

    companion object {
        val items = listOf(Home, History, Settings)
    }
}
