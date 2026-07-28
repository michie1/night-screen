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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import nl.msvos.nightscreen.ui.NightScreen
import nl.msvos.nightscreen.ui.NightScreenTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()
    private var notificationPermissionDenied by mutableStateOf(false)

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            notificationPermissionDenied = !granted
            viewModel.refreshPermissions()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()

            NightScreenTheme {
                NightScreen(
                    state = state,
                    notificationPermissionDenied = notificationPermissionDenied,
                    onDimChanged = viewModel::setDimPercent,
                    onStart = viewModel::startDimming,
                    onStop = viewModel::stopDimming,
                    onRequestOverlayPermission = ::openOverlayPermission,
                    onRequestNotificationPermission = {
                        notificationPermissionLauncher.launch(
                            Manifest.permission.POST_NOTIFICATIONS,
                        )
                    },
                    onOpenNotificationSettings = ::openNotificationSettings,
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshPermissions()
    }

    private fun openOverlayPermission() {
        startActivity(
            Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                "package:$packageName".toUri(),
            ),
        )
    }

    private fun openNotificationSettings() {
        startActivity(
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
            },
        )
    }
}
