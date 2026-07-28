package nl.msvos.nightscreen

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.net.toUri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import nl.msvos.nightscreen.ui.NightScreen
import nl.msvos.nightscreen.ui.ActiveBrightnessPanel
import nl.msvos.nightscreen.ui.NightScreenTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()
    private var notificationPermissionDenied by mutableStateOf(false)
    private var showSettings by mutableStateOf(false)
    private var resetToPanelOnResume = false
    private var keepSettingsOnNextResume = false

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (!resetToPanelOnResume) {
                keepSettingsOnNextResume = false
            }
            notificationPermissionDenied = !granted
            viewModel.refreshPermissions()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            val showActivePanel = state.isRunning && !showSettings

            LaunchedEffect(showActivePanel, state.isRunning) {
                if (showActivePanel) {
                    viewModel.stopLightReading()
                    viewModel.showActivePanel()
                } else {
                    viewModel.startLightReading()
                    if (state.isRunning) {
                        viewModel.showFullSettings()
                    }
                }
            }

            NightScreenTheme {
                if (showActivePanel) {
                    ActiveBrightnessPanel(
                        brightnessTenths = state.brightnessTenths,
                        onBrightnessChanged = viewModel::setBrightnessTenths,
                        onOpenSettings = { showSettings = true },
                        onDismiss = ::finish,
                    )
                } else {
                    NightScreen(
                        state = state,
                        notificationPermissionDenied = notificationPermissionDenied,
                        onBrightnessChanged = viewModel::setBrightnessTenths,
                        onAutoStopChanged = viewModel::setAutoStopInBrightLight,
                        onBrightLightThresholdChanged = viewModel::setBrightLightThresholdLux,
                        onStart = viewModel::startDimming,
                        onStop = viewModel::stopDimming,
                        onRequestOverlayPermission = ::openOverlayPermission,
                        onRequestNotificationPermission = {
                            keepSettingsOnNextResume = true
                            notificationPermissionLauncher.launch(
                                Manifest.permission.POST_NOTIFICATIONS,
                            )
                        },
                        onOpenNotificationSettings = ::openNotificationSettings,
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (
            resetToPanelOnResume &&
            !keepSettingsOnNextResume &&
            viewModel.uiState.value.isRunning
        ) {
            showSettings = false
        }
        resetToPanelOnResume = false
        keepSettingsOnNextResume = false
        viewModel.refreshPermissions()
    }

    override fun onPause() {
        viewModel.stopLightReading()
        super.onPause()
    }

    override fun onStop() {
        if (viewModel.uiState.value.isRunning) {
            resetToPanelOnResume = true
        }
        viewModel.appHidden()
        super.onStop()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (viewModel.uiState.value.isRunning) {
            showSettings = false
        }
    }

    private fun openOverlayPermission() {
        keepSettingsOnNextResume = true
        startActivity(
            Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                "package:$packageName".toUri(),
            ),
        )
    }

    private fun openNotificationSettings() {
        keepSettingsOnNextResume = true
        startActivity(
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
            },
        )
    }
}
