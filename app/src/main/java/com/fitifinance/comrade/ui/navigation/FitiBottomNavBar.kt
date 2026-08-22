package com.fitifinance.comrade.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.fitifinance.comrade.navigation.MainTabs

/**
 * The four primary destinations, always reachable with one tap. Deliberately
 * theme-aware rather than hardcoded to a single brand palette: the selected
 * icon/label pick up MaterialTheme.colorScheme.primary, so this bar re-skins
 * itself automatically across Campus (navy), Bar (AMOLED emerald), and
 * Kibanda (warm amber) modes — the same way every other screen already does.
 */
data class BottomNavTab(val route: String, val label: String, val icon: ImageVector)

val bottomNavTabs = listOf(
    BottomNavTab(MainTabs.DASHBOARD, "Home", Icons.Filled.Home),
    BottomNavTab(MainTabs.MEALS, "Meals", Icons.Filled.RestaurantMenu),
    BottomNavTab(MainTabs.NIGHT_OUT, "Night Out", Icons.Filled.LocalBar),
    BottomNavTab(MainTabs.SAVINGS, "Savings", Icons.Filled.Savings)
)

@Composable
fun FitiBottomNavBar(navController: NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        bottomNavTabs.forEach { tab ->
            val selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (!selected) {
                        navController.navigate(tab.route) {
                            // Tapping a tab always returns to its root, and only one
                            // copy of each tab lives on the back stack at a time.
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = { Icon(tab.icon, contentDescription = tab.label) },
                label = { Text(tab.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            )
        }
    }
}
