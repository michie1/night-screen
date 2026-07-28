# Foreground-service declaration

## Service type

`specialUse`

## Use case

Persistent, user-controlled screen dimming overlay.

## Description

Night Screen starts its foreground service only after the user presses Start
dimming. The service keeps the screen filter active while the user moves to
other apps. This is the app's main function and cannot continue after the
activity closes without an active service.

The service always has an ongoing notification with a Stop action. The user
can also stop it in the app. It uses `START_NOT_STICKY` and does not restart
after the process is killed or the phone is rebooted.

## If start is deferred

The requested dimming does not begin. The user may remain at an unsafe or
uncomfortable screen brightness until the service starts.

## If interrupted

The overlay is removed and the screen returns to its normal brightness. No
user data is lost. The user must start dimming again.

## Review video script

Record one short, unedited video:

1. Open Night Screen.
2. Grant Display over other apps through Android settings.
3. Grant notifications.
4. Press Start dimming.
5. Return to the home screen and show the active filter.
6. Open another app to show that dimming continues.
7. Open Night Screen and move the floating brightness slider.
8. Open Settings and show bright-light auto-stop.
9. Pull down notifications and press Stop.
10. Show that the filter and notification are removed.
