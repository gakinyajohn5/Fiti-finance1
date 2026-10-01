package com.fitifinance.comrade.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fitifinance.comrade.data.entity.LivingSituation
import com.fitifinance.comrade.data.entity.MealStyle
import com.fitifinance.comrade.data.entity.PartyFrequency
import com.fitifinance.comrade.data.entity.PartyStatus
import com.fitifinance.comrade.data.entity.RoommateStatus
import com.fitifinance.comrade.viewmodel.SettingsViewModel
import com.fitifinance.comrade.viewmodel.fitiViewModel

@Composable
fun ProfileSettingsScreen(onBack: () -> Unit) {
    val vm: SettingsViewModel = fitiViewModel()
    val state by vm.uiState.collectAsState()
    val profile = state.profile
    var saved by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
            Text("Profile", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(8.dp))
        Text("These answers drive which dashboards and suggestions you see.", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(20.dp))

        SectionLabel("Your name")
        // Local state so typing never fights the database round-trip; it is only
        // seeded from the saved profile while still empty (the profile loads async).
        var name by remember { mutableStateOf("") }
        LaunchedEffect(profile.name) { if (name.isBlank()) name = profile.name }
        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it.take(40)
                saved = false
                vm.saveProfileBasics(profile.copy(name = name))
            },
            label = { Text("First name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        SectionLabel("Living situation")
        ChipGroup(
            options = LivingSituation.entries.toList(),
            selected = profile.livingSituation,
            labelFor = { it.readable() },
            onSelect = { saved = false; vm.saveProfileBasics(profile.copy(livingSituation = it)) }
        )

        SectionLabel("Roommates")
        ChipGroup(
            options = RoommateStatus.entries.toList(),
            selected = profile.roommateStatus,
            labelFor = { it.readable() },
            onSelect = { saved = false; vm.saveProfileBasics(profile.copy(roommateStatus = it)) }
        )

        SectionLabel("Meal style")
        ChipGroup(
            options = MealStyle.entries.toList(),
            selected = profile.mealStyle,
            labelFor = { it.readable() },
            onSelect = { saved = false; vm.saveProfileBasics(profile.copy(mealStyle = it)) }
        )

        SectionLabel("Party status")
        ChipGroup(
            options = PartyStatus.entries.toList(),
            selected = profile.partyStatus,
            labelFor = { it.readable() },
            onSelect = { saved = false; vm.saveProfileBasics(profile.copy(partyStatus = it)) }
        )

        if (profile.partyStatus != PartyStatus.NEVER) {
            SectionLabel("Party frequency")
            ChipGroup(
                options = PartyFrequency.entries.filter { it != PartyFrequency.NOT_APPLICABLE },
                selected = profile.partyFrequency,
                labelFor = { it.readable() },
                onSelect = { saved = false; vm.saveProfileBasics(profile.copy(partyFrequency = it)) }
            )
        }

        Spacer(Modifier.height(12.dp))
        Text("Changes save automatically.", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun SectionLabel(text: String) {
    Spacer(Modifier.height(16.dp))
    Text(text, style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun <T> ChipGroup(options: List<T>, selected: T, labelFor: (T) -> String, onSelect: (T) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { option ->
            FilterChip(selected = option == selected, onClick = { onSelect(option) }, label = { Text(labelFor(option)) })
        }
    }
}

private fun LivingSituation.readable() = when (this) {
    LivingSituation.IN_CAMPUS_NO_COOKING -> "In-campus, no cooking"
    LivingSituation.IN_CAMPUS_COOKING_ALLOWED -> "In-campus, cooking OK"
    LivingSituation.OFF_CAMPUS_BEDSITTER -> "Off-campus bedsitter"
}

private fun RoommateStatus.readable() = when (this) {
    RoommateStatus.SOLO -> "Solo"
    RoommateStatus.WITH_ROOMMATES -> "With roommates"
}

private fun MealStyle.readable() = when (this) {
    MealStyle.CHEF_COMRADE -> "Chef Comrade"
    MealStyle.KIBANDA_LOYALIST -> "Kibanda loyalist"
}

private fun PartyStatus.readable() = when (this) {
    PartyStatus.REGULARLY -> "Regularly"
    PartyStatus.OCCASIONALLY -> "Occasionally"
    PartyStatus.NEVER -> "Never"
}

private fun PartyFrequency.readable() = when (this) {
    PartyFrequency.WEEKLY -> "Weekly"
    PartyFrequency.BI_WEEKLY -> "Bi-weekly"
    PartyFrequency.OCCASIONAL -> "Occasional"
    PartyFrequency.NOT_APPLICABLE -> "N/A"
}
