@file:OptIn(ExperimentalMaterial3Api::class)
package com.fitifinance.comrade.ui.meals

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fitifinance.comrade.data.entity.MealWindow
import com.fitifinance.comrade.engine.DayPlan
import com.fitifinance.comrade.engine.FoodItem
import com.fitifinance.comrade.ui.theme.HeroStatCard
import com.fitifinance.comrade.viewmodel.WeeklyMealPlanViewModel
import com.fitifinance.comrade.viewmodel.fitiViewModel

private val windowLabels = mapOf(
    MealWindow.BREAKFAST to "Breakfast",
    MealWindow.LUNCH to "Lunch",
    MealWindow.SUPPER to "Supper",
    MealWindow.LATE_NIGHT to "Late Night"
)
private val windowOrder = listOf(MealWindow.BREAKFAST, MealWindow.LUNCH, MealWindow.SUPPER, MealWindow.LATE_NIGHT)

@Composable
fun WeeklyMealPlanScreen(onBack: () -> Unit) {
    val vm: WeeklyMealPlanViewModel = fitiViewModel()
    val state by vm.uiState.collectAsState()
    var selectedDay by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("This Week's Meals") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
                },
                actions = {
                    IconButton(onClick = { vm.regenerate() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Reshuffle week")
                    }
                }
            )
        }
    ) { padding ->
        val plan = state.plan
        if (plan == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(Modifier.padding(padding).padding(16.dp)) {
            HeroStatCard(
                label = "Weekly food estimate",
                valueText = "KSh ${plan.totalCostKes.toInt()}",
                modifier = Modifier.fillMaxWidth(),
                progressFraction = 1f,
                statusText = "~KSh ${plan.averageDailyCostKes.toInt()}/day \u00b7 built from what you love, skips what you hate & your allergies"
            )

            Spacer(Modifier.height(16.dp))
            ScrollableTabRow(selectedTabIndex = selectedDay, edgePadding = 0.dp) {
                plan.days.forEachIndexed { index, day ->
                    Tab(
                        selected = selectedDay == index,
                        onClick = { selectedDay = index },
                        text = { Text(day.dayLabel.take(3)) }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            val day = plan.days.getOrNull(selectedDay)
            if (day != null) {
                DayPlanColumn(day)
            }
        }
    }
}

@Composable
private fun DayPlanColumn(day: DayPlan) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(day.dayLabel, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text("KSh ${day.totalCostKes.toInt()}", style = MaterialTheme.typography.titleMedium)
    }
    Spacer(Modifier.height(8.dp))

    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(windowOrder.filter { day.meals.containsKey(it) }) { window ->
            val item = day.meals.getValue(window)
            MealSlotCard(window, item)
        }
    }
}

@Composable
private fun MealSlotCard(window: MealWindow, item: FoodItem) {
    Card(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(windowLabels.getValue(window), style = MaterialTheme.typography.labelLarge)
                Text(item.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(item.vendorTag, style = MaterialTheme.typography.bodySmall)
            }
            Text("KSh ${item.costKes.toInt()}", style = MaterialTheme.typography.titleMedium)
        }
    }
}
