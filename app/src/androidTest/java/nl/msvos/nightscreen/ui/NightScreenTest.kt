package nl.msvos.nightscreen.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import nl.msvos.nightscreen.MainUiState
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class NightScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun missingPermissionsShowBothPermissionCards() {
        setScreen(MainUiState())

        composeRule.onNodeWithText("Display over other apps").assertIsDisplayed()
        composeRule.onNodeWithText("Notifications").assertIsDisplayed()
    }

    @Test
    fun readyStateShowsDimControls() {
        setScreen(
            MainUiState(
                brightnessTenths = 700,
                overlayPermissionGranted = true,
                notificationPermissionGranted = true,
            ),
        )

        composeRule.onNodeWithText("70%").assertIsDisplayed()
        composeRule.onNodeWithText("Start dimming").assertIsDisplayed()
        composeRule.onNodeWithText(
            "Below 20%, Android may block taps in other apps. " +
                "Use notification Stop if needed.",
        ).assertIsDisplayed()
        composeRule.onNodeWithText(
            "While dimming, Night Screen uses minimum display brightness plus this filter.",
        ).assertIsDisplayed()
    }

    @Test
    fun extraDarkStateShowsDecimalBrightness() {
        setScreen(
            MainUiState(
                brightnessTenths = 1,
                overlayPermissionGranted = true,
                notificationPermissionGranted = true,
            ),
        )

        composeRule.onNodeWithText("0.1%").assertIsDisplayed()
    }

    @Test
    fun runningStateShowsStopControl() {
        setScreen(
            MainUiState(
                overlayPermissionGranted = true,
                notificationPermissionGranted = true,
                isRunning = true,
            ),
        )

        composeRule.onNodeWithText("Dimming active").assertIsDisplayed()
        composeRule.onNodeWithText("Stop").assertIsDisplayed()
        composeRule.onNodeWithText("Dimming is paused while this app is open.")
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun previewingStateShowsPreviewStatus() {
        setScreen(
            MainUiState(
                overlayPermissionGranted = true,
                notificationPermissionGranted = true,
                isRunning = true,
                isPreviewing = true,
            ),
        )

        composeRule.onNodeWithText("Previewing changes for 10 seconds.")
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.onNodeWithText("Stop").assertIsDisplayed()
    }

    @Test
    fun missingLightSensorExplainsWhyAutoStopIsUnavailable() {
        setScreen(
            MainUiState(
                lightSensorAvailable = false,
                overlayPermissionGranted = true,
                notificationPermissionGranted = true,
            ),
        )

        composeRule.onNodeWithText("Light sensor unavailable.").assertIsDisplayed()
    }

    @Test
    fun lightSensorShowsCurrentLux() {
        setScreen(
            MainUiState(
                currentLux = 310,
                brightLightThresholdLux = 20,
                overlayPermissionGranted = true,
                notificationPermissionGranted = true,
            ),
        )

        composeRule.onNodeWithText(
            "Current light: 310 lux. Stops after 10 seconds above 20 lux.",
        ).assertIsDisplayed()
        composeRule.onNodeWithText("Auto-stop level: 20 lux").assertIsDisplayed()
    }

    @Test
    fun blueLightFilterShowsSavedStrengthAndDisabledSlider() {
        setScreen(
            MainUiState(
                blueLightFilterStrength = 50,
                overlayPermissionGranted = true,
                notificationPermissionGranted = true,
            ),
        )

        composeRule.onNodeWithText("Blue light filter").assertIsDisplayed()
        composeRule.onNodeWithText("Filter strength: 50%").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Blue light filter strength")
            .assertIsNotEnabled()
    }

    @Test
    fun activePanelShowsSliderOffAndSettings() {
        setActivePanel()

        composeRule.onNodeWithContentDescription("Brightness percentage").assertIsDisplayed()
        composeRule.onNodeWithText("Off").assertIsDisplayed()
        composeRule.onNodeWithText("Settings").assertIsDisplayed()
        composeRule.onAllNodesWithText("Night Screen").assertCountEquals(0)
        composeRule.onAllNodesWithText("Stop").assertCountEquals(0)
        composeRule.onAllNodesWithText("Dimming active").assertCountEquals(0)
    }

    @Test
    fun activePanelOutsideAreaDismissesIt() {
        var dismissed = false
        setActivePanel(onDismiss = { dismissed = true })

        composeRule.onNodeWithContentDescription("Dismiss brightness panel")
            .performTouchInput {
                click(Offset(10f, 10f))
            }

        assertTrue(dismissed)
    }

    @Test
    fun activePanelOffUsesOffAction() {
        var turnedOff = false
        setActivePanel(onOff = { turnedOff = true })

        composeRule.onNodeWithText("Off").performTouchInput { click() }

        assertTrue(turnedOff)
    }

    private fun setScreen(state: MainUiState) {
        composeRule.setContent {
            NightScreenTheme {
                NightScreen(
                    state = state,
                    notificationPermissionDenied = false,
                    onBrightnessChanged = {},
                    onBlueLightFilterEnabledChanged = {},
                    onBlueLightFilterStrengthChanged = {},
                    onAutoStopChanged = {},
                    onBrightLightThresholdChanged = {},
                    onStart = {},
                    onStop = {},
                    onRequestOverlayPermission = {},
                    onRequestNotificationPermission = {},
                    onOpenNotificationSettings = {},
                )
            }
        }
    }

    private fun setActivePanel(
        onOff: () -> Unit = {},
        onDismiss: () -> Unit = {},
    ) {
        composeRule.setContent {
            NightScreenTheme {
                ActiveBrightnessPanel(
                    brightnessTenths = 400,
                    onBrightnessChanged = {},
                    onOff = onOff,
                    onOpenSettings = {},
                    onDismiss = onDismiss,
                )
            }
        }
    }
}
