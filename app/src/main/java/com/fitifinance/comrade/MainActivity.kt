package com.fitifinance.comrade
import androidx.compose.runtime.saveable.rememberSaveable

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.fitifinance.comrade.engine.ThemeMode
import com.fitifinance.comrade.navigation.FitiNavGraph
import com.fitifinance.comrade.navigation.Routes
import com.fitifinance.comrade.ui.security.LockScreen
import com.fitifinance.comrade.ui.theme.FitiFinanceTheme
import kotlinx.coroutines.launch

/**
 * Extends FragmentActivity (rather than plain ComponentActivity) because
 * androidx.biometric's BiometricPrompt requires a FragmentActivity host.
 */
class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val app = application as FitiApplication
            val scope = rememberCoroutineScope()

            // ---- App Lock: re-lock after leaving the foreground, honoring the auto-lock delay ----
            val lockEnabled by app.securityPreferences.isLockEnabled.collectAsState(initial = false)
            val appSettings by app.appPreferences.state.collectAsState(
                initial = com.fitifinance.comrade.settings.AppSettingsState()
            )
            var isUnlocked by rememberSaveable { mutableStateOf(false) }
            val lifecycleOwner = LocalLifecycleOwner.current
            DisposableEffect(lifecycleOwner, lockEnabled) {
                var lockJob: kotlinx.coroutines.Job? = null
                val observer = LifecycleEventObserver { _, event ->
                    when (event) {
                        Lifecycle.Event.ON_STOP -> {
                            if (lockEnabled) {
                                val delayMs = appSettings.autoLockDelaySeconds * 1000L
                                lockJob = scope.launch {
                                    if (delayMs > 0) kotlinx.coroutines.delay(delayMs)
                                    isUnlocked = false
                                }
                            }
                        }
                        Lifecycle.Event.ON_START -> lockJob?.cancel()
                        else -> {}
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                    lockJob?.cancel()
                }
            }

            // ---- Runtime permissions: SMS parser + location context engine ----
            val permissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { /* Results observed lazily; features simply stay dormant until granted. */ }

            // Guards against launching the permission dialog more than once per
            // Activity instance. LaunchedEffect(Unit) should already only fire
            // once, but if anything upstream (lock-state changes, recomposition
            // from a structural change) causes this composable to be disposed
            // and recreated, a bare LaunchedEffect(Unit) would fire again and
            // rapidly exhaust Android's internal request-code range, crashing
            // with "Can only use lower 16 bits for requestCode". rememberSaveable
            // makes the guard survive recomposition, so the dialog only ever
            // launches once for the lifetime of this Activity instance.
            var hasRequestedPermissions by rememberSaveable { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                if (hasRequestedPermissions) return@LaunchedEffect
                hasRequestedPermissions = true

                val permissions = mutableListOf(
                    Manifest.permission.RECEIVE_SMS,
                    Manifest.permission.READ_SMS,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissions += Manifest.permission.POST_NOTIFICATIONS
                }
                permissionLauncher.launch(permissions.toTypedArray())
            }

            // Decide start destination based on whether onboarding was already completed.
            var startDestination by remember { mutableStateOf<String?>(null) }
            LaunchedEffect(Unit) {
                val profile = app.repository.getProfile()
                startDestination = if (profile.onboardingComplete) Routes.MAIN else Routes.ONBOARDING
            }

            val themeMode by app.locationContextEngine.currentMode.collectAsState()

            FitiFinanceTheme(themeMode = themeMode) {
                startDestination?.let { destination ->
                    if (lockEnabled && !isUnlocked) {
                        LockScreen(onUnlocked = { isUnlocked = true })
                    } else {
                        FitiNavGraph(startDestination = destination)
                    }
                }
            }
        }
    }
}
