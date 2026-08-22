@file:OptIn(ExperimentalMaterial3Api::class)
package com.fitifinance.comrade.ui.meals

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fitifinance.comrade.engine.FoodItem
import com.fitifinance.comrade.ui.theme.HeroStatCard
import com.fitifinance.comrade.viewmodel.MealsViewModel
import com.fitifinance.comrade.viewmodel.fitiViewModel

@Composable
fun MealPlannerScreen(onBack: () -> Unit, onOpenWeeklyPlan: () -> Unit = {}) {
    val vm: MealsViewModel = fitiViewModel()
    val state by vm.uiState.collectAsState()
    var commandText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Meal Planner — ${state.currentWindowLabel}") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
                },
                actions = {
                    TextButton(onClick = onOpenWeeklyPlan) { Text("This Week") }
                }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {

            val allowance = state.remainingAllowanceKes + state.todaySpendKes
            val spentFraction = if (allowance > 0) (state.todaySpendKes / allowance).toFloat() else 0f
            HeroStatCard(
                label = "Remaining today",
                valueText = "KSh ${state.remainingAllowanceKes.toInt()}",
                modifier = Modifier.fillMaxWidth(),
                progressFraction = 1f - spentFraction,
                statusText = "KSh ${state.todaySpendKes.toInt()} spent today"
            )

            state.lastCommandFeedback?.let { feedback ->
                Spacer(Modifier.height(8.dp))
                AssistChip(onClick = { vm.clearFeedback() }, label = { Text(feedback) })
            }

            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = commandText,
                onValueChange = { commandText = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("\"I ate lunch, minus 100 KES\" or \"swap supper to Rice Beans\"") },
                trailingIcon = {
                    IconButton(onClick = {
                        if (commandText.isNotBlank()) {
                            vm.submitCommand(commandText)
                            commandText = ""
                        }
                    }) { Icon(Icons.Filled.Send, contentDescription = "Submit") }
                }
            )

            Spacer(Modifier.height(16.dp))
            Text("Suggested for right now", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            LazyColumn {
                items(state.suggestions) { food ->
                    FoodCard(food, onLog = { vm.logMeal(food) })
                }
            }
        }
    }
}

@Composable
private fun FoodCard(food: FoodItem, onLog: () -> Unit) {
    Card(
        Modifier.fillMaxWidth().padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(food.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    "KSh ${food.costKes.toInt()}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                food.vendorTag, style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "P ${food.proteinG}g · C ${food.carbsG}g · F ${food.fatG}g",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = onLog,
                modifier = Modifier.align(Alignment.End),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Text("Log this meal")
            }
        }
    }
}
