@file:OptIn(ExperimentalMaterial3Api::class)
package com.fitifinance.comrade.ui.nightout

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fitifinance.comrade.engine.DrinkItem
import com.fitifinance.comrade.ui.theme.HeroStatCard
import com.fitifinance.comrade.viewmodel.NightOutViewModel
import com.fitifinance.comrade.viewmodel.fitiViewModel

@Composable
fun NightOutScreen(onBack: () -> Unit) {
    val vm: NightOutViewModel = fitiViewModel()
    val state by vm.uiState.collectAsState()
    var selectedDrink by remember { mutableStateOf<DrinkItem?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Night-Out & Party") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
                }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {

            HeroStatCard(
                label = "Spendable tonight",
                valueText = "KSh ${state.spendableBalanceKes.toInt()}",
                modifier = Modifier.fillMaxWidth(),
                statusText = "KSh ${state.pendingReceivablesKes.toInt()} owed back to you",
                trailing = {
                    if (state.fareShieldBreached) {
                        Icon(Icons.Filled.Warning, contentDescription = "Fare shield breached", tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
            if (state.fareShieldBreached) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Fare shield breached — KSh ${state.profile.fareShieldKes.toInt()} ride-home reserve is at risk.",
                    color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(Modifier.height(16.dp))
            Text("Drinks Menu", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            LazyColumn {
                items(state.drinks) { drink ->
                    DrinkCard(drink) { selectedDrink = drink }
                }
            }
        }
    }

    selectedDrink?.let { drink ->
        LogDrinkSheet(
            drink = drink,
            onDismiss = { selectedDrink = null },
            onFullPay = { total -> vm.logFullPayment(drink, total); selectedDrink = null },
            onSplit = { total, count -> vm.logSplitBill(drink, total, count); selectedDrink = null }
        )
    }
}

@Composable
private fun DrinkCard(drink: DrinkItem, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(drink.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    drink.servingSize, style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Text(
                "~KSh ${drink.typicalPriceKes.toInt()}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LogDrinkSheet(
    drink: DrinkItem,
    onDismiss: () -> Unit,
    onFullPay: (Double) -> Unit,
    onSplit: (Double, Int) -> Unit
) {
    var totalBill by remember { mutableStateOf(drink.typicalPriceKes.toInt().toString()) }
    var comradeCount by remember { mutableStateOf("1") }
    var isSplitMode by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(24.dp)) {
            Text(drink.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = totalBill,
                onValueChange = { totalBill = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Total bill (KSh)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Switch(checked = isSplitMode, onCheckedChange = { isSplitMode = it })
                Spacer(Modifier.width(8.dp))
                Text("Split with comrades")
            }

            if (isSplitMode) {
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = comradeCount,
                    onValueChange = { comradeCount = it.filter { c -> c.isDigit() } },
                    label = { Text("Number of comrades (incl. you)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    val total = totalBill.toDoubleOrNull() ?: return@Button
                    if (isSplitMode) {
                        val count = comradeCount.toIntOrNull()?.coerceAtLeast(1) ?: 1
                        onSplit(total, count)
                    } else {
                        onFullPay(total)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Text(if (isSplitMode) "Split & Log" else "Log Full Payment")
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}
