package com.fitifinance.comrade.ui.security

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.fitifinance.comrade.security.BiometricAuthenticator
import com.fitifinance.comrade.viewmodel.LockViewModel
import com.fitifinance.comrade.viewmodel.SettingsViewModel
import com.fitifinance.comrade.viewmodel.fitiViewModel

@Composable
fun SecuritySettingsScreen(onBack: () -> Unit) {
    val vm: LockViewModel = fitiViewModel()
    val state by vm.securityState.collectAsState()
    val settingsVm: SettingsViewModel = fitiViewModel()
    val settingsState by settingsVm.uiState.collectAsState()
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val biometricHardwareAvailable = activity?.let { BiometricAuthenticator.isAvailable(it) } ?: false

    var showSetPinDialog by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
            Text("App Lock", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(12.dp))
        Text(
            "Protect your spending data with a 4-digit PIN, plus fingerprint or face unlock for quick access.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(Modifier.height(24.dp))
        Card {
            Column(Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("PIN protection", style = MaterialTheme.typography.titleMedium)
                        Text(
                            if (state.hasPinSet) "A PIN is set" else "No PIN set yet",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Switch(
                        checked = state.lockEnabled,
                        onCheckedChange = { enabled ->
                            if (enabled) showSetPinDialog = true else vm.disableLock()
                        }
                    )
                }

                if (state.hasPinSet) {
                    TextButton(onClick = { showSetPinDialog = true }) { Text("Change PIN") }
                }

                Divider(Modifier.padding(vertical = 8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Fingerprint / Face unlock", style = MaterialTheme.typography.titleMedium)
                        Text(
                            when {
                                !state.lockEnabled -> "Turn on PIN protection first"
                                !biometricHardwareAvailable -> "Not available on this device"
                                else -> "Skip the PIN when your biometric is recognized"
                            },
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Switch(
                        checked = state.biometricEnabled,
                        enabled = state.lockEnabled && biometricHardwareAvailable,
                        onCheckedChange = { vm.setBiometricEnabled(it) }
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Hide amounts", style = MaterialTheme.typography.titleMedium)
                        Text("Mask KES figures behind a tap, e.g. over someone's shoulder", style = MaterialTheme.typography.bodySmall)
                    }
                    Switch(
                        checked = settingsState.appSettings.hideAmounts,
                        onCheckedChange = { settingsVm.setHideAmounts(it) }
                    )
                }

                Divider(Modifier.padding(vertical = 8.dp))

                Text("Auto-lock delay", style = MaterialTheme.typography.titleMedium)
                Text(
                    "How long the app stays unlocked after you leave it",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(0 to "Immediately", 30 to "30s", 60 to "1 min", 300 to "5 min").forEach { (seconds, label) ->
                        FilterChip(
                            selected = settingsState.appSettings.autoLockDelaySeconds == seconds,
                            onClick = { settingsVm.setAutoLockDelaySeconds(seconds) },
                            label = { Text(label) }
                        )
                    }
                }
            }
        }
    }

    if (showSetPinDialog) {
        SetPinDialog(
            onDismiss = { showSetPinDialog = false },
            onConfirm = { newPin ->
                vm.setPin(newPin) { showSetPinDialog = false }
            }
        )
    }
}

@Composable
private fun SetPinDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set a 4-digit PIN") },
        text = {
            Column {
                OutlinedTextField(
                    value = pin,
                    onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) pin = it },
                    label = { Text("New PIN") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = confirmPin,
                    onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) confirmPin = it },
                    label = { Text("Confirm PIN") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true
                )
                if (error != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                when {
                    pin.length != 4 -> error = "PIN must be 4 digits"
                    pin != confirmPin -> error = "PINs don't match"
                    else -> onConfirm(pin)
                }
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
