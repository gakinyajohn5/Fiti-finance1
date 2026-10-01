package com.fitifinance.comrade.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.fitifinance.comrade.navigation.MainTabs
import com.fitifinance.comrade.ui.theme.glassColors

/**
 * The four primary destinations, always one tap away. Rendered as a floating
 * glass pill; the selected tab gets the blue-to-cyan accent fill. Colors come
 * from the active theme, so it re-skins with every theme mode.
 *
 * Meals, Night Out and Savings are not in the bar; they open from the dashboard.
 */
data class BottomNavTab(val route: String, val label: String, val icon: ImageVector)

val bottomNavTabs = listOf(
    BottomNavTab(MainTabs.DASHBOARD, "Home", Icons.Filled.Home),
    BottomNavTab(MainTabs.ANALYTICS, "Analytics", Icons.Filled.BarChart),
    BottomNavTab(MainTabs.TRANSFERS, "Transfers", Icons.Filled.SwapHoriz),
    BottomNavTab(MainTabs.PROFILE, "Profile", Icons.Filled.Person)
)

@Composable
fun FitiBottomNavBar(navController: NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val g = glassColors()
    val cs = MaterialTheme.colorScheme
    val barShape = RoundedCornerShape(30.dp)
    val itemShape = RoundedCornerShape(24.dp)

    Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(barShape)
                .background(cs.surface.copy(alpha = 0.92f))
                .background(Brush.verticalGradient(listOf(g.fillTop, g.fillBottom)))
                .border(1.dp, Brush.linearGradient(listOf(g.borderStart, g.borderEnd)), barShape)
                .padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            bottomNavTabs.forEach { tab ->
                val selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true
                val tint = if (selected) cs.onPrimary else g.textMuted
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(itemShape)
                        .then(if (selected) Modifier.background(g.accent) else Modifier)
                        .selectable(
                            selected = selected,
                            role = Role.Tab,
                            onClick = {
                                if (!selected) {
                                    navController.navigate(tab.route) {
                                        // One copy of each tab on the back stack; state is kept.
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                        .padding(vertical = 9.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(tab.icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.height(3.dp))
                    Text(tab.label, style = MaterialTheme.typography.labelMedium, color = tint, maxLines = 1)
                }
            }
        }
    }
}
