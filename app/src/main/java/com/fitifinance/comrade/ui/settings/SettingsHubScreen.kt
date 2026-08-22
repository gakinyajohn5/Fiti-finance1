package com.fitifinance.comrade.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class SettingsCategory(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val onClick: () -> Unit
)

@Composable
fun SettingsHubScreen(
    onBack: () -> Unit,
    onOpenAppLock: () -> Unit,
    onOpenBudgets: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenDataPrivacy: () -> Unit,
    onOpenAppearance: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenAbout: () -> Unit
) {
    val categories = listOf(
        SettingsCategory(Icons.Filled.Lock, "App Lock & Privacy", "PIN, biometric, hide amounts", onOpenAppLock),
        SettingsCategory(Icons.Filled.AccountBalanceWallet, "Budgets", "Meal, monthly, night-out, fare shield", onOpenBudgets),
        SettingsCategory(Icons.Filled.Notifications, "Notifications", "Damage control, budget alerts, prompts", onOpenNotifications),
        SettingsCategory(Icons.Filled.Storage, "SMS & Data", "Permissions, recipient rules, export, reset", onOpenDataPrivacy),
        SettingsCategory(Icons.Filled.Palette, "Appearance", "Campus, Bar, Kibanda mode", onOpenAppearance),
        SettingsCategory(Icons.Filled.Person, "Profile", "Comrade persona details", onOpenProfile),
        SettingsCategory(Icons.Filled.Info, "About", "Version and feedback", onOpenAbout)
    )

    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
            Text("Settings", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(12.dp))

        categories.forEach { category ->
            SettingsCategoryTile(category)
        }
    }
}

@Composable
private fun SettingsCategoryTile(category: SettingsCategory) {
    Card(
        onClick = category.onClick,
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
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f), shape = MaterialTheme.shapes.small),
                contentAlignment = Alignment.Center
            ) {
                Icon(category.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(category.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    category.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
        }
    }
}
