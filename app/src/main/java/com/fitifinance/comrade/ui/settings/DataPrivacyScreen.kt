package com.fitifinance.comrade.ui.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.fitifinance.comrade.viewmodel.SettingsViewModel
import com.fitifinance.comrade.viewmodel.fitiViewModel
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun DataPrivacyScreen(onBack: () -> Unit) {
    val vm: SettingsViewModel = fitiViewModel()
    val state by vm.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val smsGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) ==
        PackageManager.PERMISSION_GRANTED

    var showClearConfirm by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
            Text("SMS & Data", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(16.dp))

        Text("SMS Auto-Parsing", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(6.dp))
        Card(Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(if (smsGranted) "Permission granted" else "Permission not granted", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "Needed to auto-parse M-PESA Paybill/Till/P2P messages",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                TextButton(onClick = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                }) { Text("App Settings") }
            }
        }

        Spacer(Modifier.height(24.dp))
        Text("Recipient Rules", style = MaterialTheme.typography.titleMedium)
        Text("\"Always categorize as…\" shortcuts saved from the transaction prompt", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(6.dp))
        if (state.recipients.isEmpty()) {
            Text("No saved rules yet", style = MaterialTheme.typography.bodyMedium)
        } else {
            state.recipients.forEach { recipient ->
                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(recipient.fullName, style = MaterialTheme.typography.bodyMedium)
                            Text(recipient.defaultCategory.name, style = MaterialTheme.typography.bodySmall)
                        }
                        IconButton(onClick = { vm.deleteRecipient(recipient.fullName) }) {
                            Icon(Icons.Filled.Close, contentDescription = "Remove rule")
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Text("Your Data", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(6.dp))
        OutlinedButton(
            onClick = {
                scope.launch {
                    val csv = vm.buildTransactionsCsv()
                    val exportsDir = File(context.cacheDir, "exports").apply { mkdirs() }
                    val file = File(exportsDir, "fiti_transactions.csv")
                    file.writeText(csv)
                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/csv"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Export transactions"))
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Export transactions (CSV)") }

        Spacer(Modifier.height(10.dp))
        OutlinedButton(
            onClick = { showClearConfirm = true },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth()
        ) { Text("Clear all data") }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Clear all data?") },
            text = { Text("This permanently deletes all transactions, meal logs, drink logs, savings jars, and recipient rules on this device. This can't be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    vm.clearAllData { showClearConfirm = false }
                }) { Text("Delete everything", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { showClearConfirm = false }) { Text("Cancel") } }
        )
    }
}
