package com.fitifinance.comrade.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.fitifinance.comrade.viewmodel.SettingsViewModel
import com.fitifinance.comrade.viewmodel.fitiViewModel

@Composable
fun BudgetSettingsScreen(onBack: () -> Unit) {
    val vm: SettingsViewModel = fitiViewModel()
    val state by vm.uiState.collectAsState()

    var dailyMeal by remember { mutableStateOf("") }
    var monthly by remember { mutableStateOf("") }
    var nightOut by remember { mutableStateOf("") }
    var fareShield by remember { mutableStateOf("") }
    var initialized by remember { mutableStateOf(false) }
    var saved by remember { mutableStateOf(false) }

    LaunchedEffect(state.profile) {
        if (!initialized) {
            dailyMeal = state.profile.dailyMealBudgetKes.toInt().toString()
            monthly = state.profile.monthlyBudgetKes.toInt().toString()
            nightOut = state.profile.nightOutBudgetKes.toInt().toString()
            fareShield = state.profile.fareShieldKes.toInt().toString()
            initialized = true
        }
    }

    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
            Text("Budgets", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(8.dp))
        Text("These caps drive alerts and dashboard progress bars across the app.", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(20.dp))

        BudgetField("Daily meal budget (KES)", dailyMeal) { dailyMeal = it }
        BudgetField("Monthly budget (KES)", monthly) { monthly = it }
        BudgetField("Night-out budget (KES)", nightOut) { nightOut = it }
        BudgetField("Emergency fare shield (KES)", fareShield) { fareShield = it }

        Spacer(Modifier.height(20.dp))
        Button(
            onClick = {
                vm.saveBudgets(
                    dailyMealBudgetKes = dailyMeal.toDoubleOrNull() ?: state.profile.dailyMealBudgetKes,
                    monthlyBudgetKes = monthly.toDoubleOrNull() ?: state.profile.monthlyBudgetKes,
                    nightOutBudgetKes = nightOut.toDoubleOrNull() ?: state.profile.nightOutBudgetKes,
                    fareShieldKes = fareShield.toDoubleOrNull() ?: state.profile.fareShieldKes
                )
                saved = true
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Save Budgets") }

        if (saved) {
            Spacer(Modifier.height(8.dp))
            Text("Saved ✓", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun BudgetField(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { new -> if (new.all { it.isDigit() }) onChange(new) },
        label = { Text(label) },
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
    )
}
