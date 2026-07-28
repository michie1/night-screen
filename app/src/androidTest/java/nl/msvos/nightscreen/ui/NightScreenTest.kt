package nl.msvos.nightscreen.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import nl.msvos.nightscreen.MainUiState
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
                dimPercent = 70,
                overlayPermissionGranted = true,
                notificationPermissionGranted = true,
            ),
        )

        composeRule.onNodeWithText("70%").assertIsDisplayed()
        composeRule.onNodeWithText("Start dimming").assertIsDisplayed()
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
    }

    private fun setScreen(state: MainUiState) {
        composeRule.setContent {
            NightScreenTheme {
                NightScreen(
                    state = state,
                    notificationPermissionDenied = false,
                    onDimChanged = {},
                    onStart = {},
                    onStop = {},
                    onRequestOverlayPermission = {},
                    onRequestNotificationPermission = {},
                    onOpenNotificationSettings = {},
                )
            }
        }
    }
}
