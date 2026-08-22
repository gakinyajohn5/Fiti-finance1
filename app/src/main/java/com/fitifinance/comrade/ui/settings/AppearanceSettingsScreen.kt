package com.fitifinance.comrade.ui.settings

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
import com.fitifinance.comrade.engine.ThemeMode
import com.fitifinance.comrade.viewmodel.SettingsViewModel
import com.fitifinance.comrade.viewmodel.fitiViewModel

@Composable
fun AppearanceSettingsScreen(onBack: () -> Unit) {
    val vm: SettingsViewModel = fitiViewModel()
    val state by vm.uiState.collectAsState()
    val settings = state.appSettings

    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
            Text("Appearance", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(12.dp))

        Card(Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Auto (location-based)", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Switches Campus / Bar / Kibanda mode based on where you are",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Switch(checked = settings.themeAutoMode, onCheckedChange = vm::setThemeAutoMode)
            }
        }

        if (!settings.themeAutoMode) {
            Spacer(Modifier.height(16.dp))
            Text("Pick a mode", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            listOf(
                ThemeMode.CAMPUS to ("Campus" to "Forest green and gold, calm daytime look"),
                ThemeMode.BAR to ("Bar" to "AMOLED dark, emerald fintech look"),
                ThemeMode.KIBANDA to ("Kibanda" to "Warm amber and green")
            ).forEach { (mode, labels) ->
                val (title, subtitle) = labels
                Card(
                    onClick = { vm.setManualThemeMode(mode) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(title, style = MaterialTheme.typography.titleMedium)
                            Text(subtitle, style = MaterialTheme.typography.bodySmall)
                        }
                        RadioButton(selected = settings.manualThemeMode == mode, onClick = { vm.setManualThemeMode(mode) })
                    }
                }
            }
        }
    }
}
