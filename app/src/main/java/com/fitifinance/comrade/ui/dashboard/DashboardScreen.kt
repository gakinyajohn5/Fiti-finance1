package com.fitifinance.comrade.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fitifinance.comrade.engine.ThemeMode
import com.fitifinance.comrade.ui.theme.HeroStatCard
import com.fitifinance.comrade.viewmodel.DashboardViewModel
import com.fitifinance.comrade.viewmodel.fitiViewModel

@Composable
fun DashboardScreen(
    onOpenMeals: () -> Unit,
    onOpenNightOut: () -> Unit,
    onOpenSavings: () -> Unit,
    onOpenTransactions: () -> Unit,
    onOpenSpendingChart: () -> Unit,
    onOpenSecuritySettings: () -> Unit
) {
    val vm: DashboardViewModel = fitiViewModel()
    val state by vm.uiState.collectAsState()
    val theme by vm.themeMode.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Habari, Comrade 👋", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    "Mode: ${theme.name.lowercase().replaceFirstChar { it.uppercase() }}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            IconButton(onClick = onOpenSecuritySettings) {
                Icon(Icons.Filled.Lock, contentDescription = "App Lock settings")
            }
        }

        Spacer(Modifier.height(12.dp))
        ThemeModeSwitcher(current = theme, onSelect = vm::previewThemeMode)

        Spacer(Modifier.height(20.dp))
        HeroStatCard(
            label = "Today's meal spend",
            valueText = "KSh ${state.todayMealSpendKes.toInt()}",
            modifier = Modifier.fillMaxWidth(),
            statusText = state.remainingLabel()
        )

        Spacer(Modifier.height(20.dp))
        Text("Quick Access", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(10.dp))

        DashboardTile(Icons.Filled.Restaurant, "Meal Planner", "Time-aware suggestions for right now", onOpenMeals)
        if (state.profile.partyDashboardEnabled) {
            DashboardTile(Icons.Filled.LocalBar, "Night-Out & Party", "Track drinks, split bills, fare shield", onOpenNightOut)
        }
        DashboardTile(Icons.Filled.Savings, "Savings Jars", "${state.activeJarCount} active goal(s)", onOpenSavings)
        DashboardTile(
            Icons.Filled.Receipt, "Transactions",
            if (state.pendingPromptCount > 0) "${state.pendingPromptCount} need categorizing" else "All caught up",
            onOpenTransactions
        )
        DashboardTile(
            Icons.Filled.BarChart, "Spending Insights",
            "Daily and weekly spend charts",
            onOpenSpendingChart
        )
    }
}

private fun com.fitifinance.comrade.viewmodel.DashboardState.remainingLabel(): String =
    "KSh ${monthSpendKes.toInt()} spent this month"

@Composable
private fun ThemeModeSwitcher(current: ThemeMode, onSelect: (ThemeMode) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(
            ThemeMode.CAMPUS to "Campus",
            ThemeMode.BAR to "Bar",
            ThemeMode.KIBANDA to "Kibanda"
        ).forEach { (mode, label) ->
            FilterChip(selected = current == mode, onClick = { onSelect(mode) }, label = { Text(label) })
        }
    }
}

@Composable
private fun DashboardTile(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                        shape = MaterialTheme.shapes.small
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Icon(
                Icons.Filled.ChevronRight, contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }
    }
}
