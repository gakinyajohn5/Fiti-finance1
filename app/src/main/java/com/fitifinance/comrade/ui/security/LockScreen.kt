package com.fitifinance.comrade.ui.security

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.fitifinance.comrade.security.BiometricAuthenticator
import com.fitifinance.comrade.viewmodel.LockViewModel
import com.fitifinance.comrade.viewmodel.fitiViewModel
import kotlinx.coroutines.launch

/**
 * Full-screen PIN pad shown whenever the app is locked. If biometrics are
 * enabled and available it also fires a fingerprint/face prompt automatically.
 */
@Composable
fun LockScreen(onUnlocked: () -> Unit) {
    val vm: LockViewModel = fitiViewModel()
    val state by vm.securityState.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val activity = context as? FragmentActivity

    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var biometricTried by remember { mutableStateOf(false) }

    LaunchedEffect(state.biometricEnabled, activity) {
        if (!biometricTried && state.biometricEnabled && activity != null &&
            BiometricAuthenticator.isAvailable(activity)
        ) {
            biometricTried = true
            BiometricAuthenticator.prompt(
                activity = activity,
                onSuccess = onUnlocked,
                onError = { /* fall back silently to the PIN pad */ }
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Enter your PIN", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            repeat(4) { index ->
                val filled = index < pin.length
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .background(
                            color = if (filled) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                            shape = CircleShape
                        )
                )
            }
        }

        if (error != null) {
            Spacer(Modifier.height(16.dp))
            Text(error!!, color = Color(0xFFEF4444), style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(Modifier.height(36.dp))

        val rows = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf(if (state.biometricEnabled) "bio" else "", "0", "del")
        )

        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
                row.forEach { key ->
                    PinKey(
                        key = key,
                        onDigit = { digit ->
                            if (pin.length < 4) {
                                pin += digit
                                error = null
                                if (pin.length == 4) {
                                    scope.launch {
                                        if (vm.verifyPin(pin)) {
                                            onUnlocked()
                                        } else {
                                            error = "Wrong PIN, try again"
                                            pin = ""
                                        }
                                    }
                                }
                            }
                        },
                        onDelete = { if (pin.isNotEmpty()) pin = pin.dropLast(1) },
                        onBiometric = {
                            if (activity != null) {
                                BiometricAuthenticator.prompt(
                                    activity = activity,
                                    onSuccess = onUnlocked,
                                    onError = { error = it }
                                )
                            }
                        }
                    )
                }
            }
            Spacer(Modifier.height(18.dp))
        }
    }
}

@Composable
private fun PinKey(
    key: String,
    onDigit: (String) -> Unit,
    onDelete: () -> Unit,
    onBiometric: () -> Unit
) {
    Box(modifier = Modifier.size(64.dp), contentAlignment = Alignment.Center) {
        when (key) {
            "" -> {} // empty spacer slot
            "del" -> IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Backspace, contentDescription = "Delete")
            }
            "bio" -> IconButton(onClick = onBiometric) {
                Icon(Icons.Filled.Fingerprint, contentDescription = "Use biometric", modifier = Modifier.size(28.dp))
            }
            else -> TextButton(onClick = { onDigit(key) }) {
                Text(key, style = MaterialTheme.typography.headlineMedium)
            }
        }
    }
}
