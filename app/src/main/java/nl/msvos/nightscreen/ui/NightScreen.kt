package nl.msvos.nightscreen.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import nl.msvos.nightscreen.MainUiState
import nl.msvos.nightscreen.overlay.BrightnessMapper

@Composable
fun NightScreen(
    state: MainUiState,
    notificationPermissionDenied: Boolean,
    onBrightnessChanged: (Int) -> Unit,
    onAutoStopChanged: (Boolean) -> Unit,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onRequestOverlayPermission: () -> Unit,
    onRequestNotificationPermission: () -> Unit,
    onOpenNotificationSettings: () -> Unit,
) {
    Scaffold { contentPadding ->
        Column(
            modifier = Modifier
                .padding(contentPadding)
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Text(
                text = "Night Screen",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Dim your screen below Android's normal minimum brightness.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (!state.overlayPermissionGranted) {
                PermissionCard(
                    title = "Display over other apps",
                    description = "Required to dim other apps.",
                    buttonLabel = "Allow",
                    onClick = onRequestOverlayPermission,
                )
            }

            if (!state.notificationPermissionGranted) {
                PermissionCard(
                    title = "Notifications",
                    description = "Required for the persistent Stop control.",
                    buttonLabel = if (notificationPermissionDenied) {
                        "Open settings"
                    } else {
                        "Allow"
                    },
                    onClick = if (notificationPermissionDenied) {
                        onOpenNotificationSettings
                    } else {
                        onRequestNotificationPermission
                    },
                )
            }

            if (state.overlayPermissionGranted && state.notificationPermissionGranted) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Brightness",
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = BrightnessMapper.formatPercent(state.brightnessTenths),
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Medium,
                )
                Slider(
                    value = state.brightnessTenths.toFloat(),
                    onValueChange = { onBrightnessChanged(it.roundToInt()) },
                    valueRange = BrightnessMapper.MIN_BRIGHTNESS.toFloat()..
                        BrightnessMapper.MAX_BRIGHTNESS.toFloat(),
                    steps = 998,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics {
                            contentDescription = "Brightness percentage"
                        },
                )
                Text(
                    text = "While dimming, Night Screen uses minimum display brightness " +
                        "plus this filter.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "Below 20%, Android may block taps in other apps. " +
                        "Use notification Stop if needed.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Auto-stop in bright light",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = if (state.lightSensorAvailable) {
                                "Stops after 10 seconds above 5,000 lux."
                            } else {
                                "Light sensor unavailable."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Switch(
                        checked = state.autoStopInBrightLight,
                        onCheckedChange = onAutoStopChanged,
                        enabled = state.lightSensorAvailable,
                    )
                }

                Text(
                    text = if (state.isRunning) {
                        "Dimming active"
                    } else {
                        "Dimming is off"
                    },
                    color = if (state.isRunning) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    style = MaterialTheme.typography.titleMedium,
                )
                Button(
                    onClick = if (state.isRunning) onStop else onStart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text(
                        text = if (state.isRunning) "Stop" else "Start dimming",
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                if (state.isRunning) {
                    Text(
                        text = if (state.isPreviewing) {
                            "Previewing brightness for 10 seconds."
                        } else {
                            "Dimming is paused while this app is open."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun PermissionCard(
    title: String,
    description: String,
    buttonLabel: String,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        shape = RoundedCornerShape(20.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            TextButton(onClick = onClick) {
                Text(buttonLabel)
            }
        }
    }
}
