package com.fitifinance.comrade.ui.settings
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fitifinance.comrade.viewmodel.SettingsViewModel
import com.fitifinance.comrade.viewmodel.fitiViewModel

@Composable
fun NotificationSettingsScreen(onBack: () -> Unit) {
    val vm: SettingsViewModel = fitiViewModel()
    val state by vm.uiState.collectAsState(initial = com.fitifinance.comrade.viewmodel.SettingsScreenState())

    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
            Text("Notifications", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(12.dp))

        NotificationToggle(
            title = "Morning-After Damage Control",
            subtitle = "9:00 AM summary after a night-out: total spent, owed back, adjusted food cap",
            checked = state.appSettings.notifyDamageControl,
            onCheckedChange = vm::setNotifyDamageControl
        )
        NotificationToggle(
            title = "Pending categorization reminders",
            subtitle = "Nudge when a P2P transfer still needs a category",
            checked = state.appSettings.notifyPendingCategorization,
            onCheckedChange = vm::setNotifyPendingCategorization
        )
        NotificationToggle(
            title = "Budget threshold alerts",
            subtitle = "Warn when you're close to a monthly or category budget cap",
            checked = state.appSettings.notifyBudgetThreshold,
            onCheckedChange = vm::setNotifyBudgetThreshold
        )
    }
}

@Composable
private fun NotificationToggle(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Card(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall)
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}
