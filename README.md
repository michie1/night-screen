# Night Screen

Night Screen is a small Android app that dims the display below Android's
normal minimum brightness. It places one transparent black window over the
screen and keeps ordinary apps beneath it usable.

## Requirements

Android 14 or newer.

The project uses Kotlin, Jetpack Compose, minSdk 34, compileSdk 36, and
targetSdk 36.

## Permissions

### Display over other apps

This access lets Night Screen place its dimming window above other apps. The
app opens Android's own settings page and cannot grant this access itself.

### Notifications

This access is required before dimming can start. It ensures that the ongoing
foreground-service notification and its Stop action stay visible.

## Safety

The app changes `WindowManager.LayoutParams.alpha`, not `View.alpha`.

The brightness scale runs from 2% to 100%:

1. 2% uses a 98% black overlay.
2. 100% adds no dimming and leaves Android's normal brightness unchanged.
3. 0% is not offered.

Below about 20%, the overlay exceeds Android's usual touch-through opacity
limit. Android may then block taps in other apps. Night Screen's own controls
remain usable during preview, and the ongoing notification keeps its Stop
action as a backup exit.

The app creates one overlay window. If adding or updating that window fails,
the foreground service removes the overlay and stops. The service uses
`START_NOT_STICKY`, so dimming does not return after the app process is killed.

## Behavior

Dimming continues after the Night Screen activity is closed while its
foreground service is running.

While Night Screen itself is visible, the overlay is hidden so the controls
and Stop button remain easy to see. The overlay returns when the app moves to
the background.

While dimming is active, changing the brightness slider previews the real
overlay for 10 seconds. Each slider change restarts the 10-second period.
Leaving Night Screen ends the preview and keeps the overlay visible as normal.

Dimming stops when:

1. Stop is pressed in the app.
2. Stop is pressed in the notification.
3. Android terminates or force-stops the process.
4. An overlay update fails.

Dimming does not restart after reboot.

## Bright-light auto-stop

The optional bright-light rule stops dimming after the front light sensor
reports at least 5,000 lux for 10 seconds. Falling below the threshold cancels
the timer.

The light sensor is registered only while dimming is active and this option is
enabled. It uses the sensor's on-change mode, does not poll, and does not wake
the phone. Its battery cost should be tiny.

Secure Android screens can hide application overlays. System bars, lock
screens, screenshots, and display cutouts can also differ by phone maker.

## Build and run

Connect an Android phone with USB debugging enabled, then run:

```text
./gradlew installDebug
adb shell am start -n nl.msvos.nightscreen/.MainActivity
```

Useful overlay logs:

```text
adb logcat -s NightScreen WindowManager InputDispatcher ActivityManager
```

## Privacy

Night Screen:

1. Has no internet permission.
2. Collects no analytics.
3. Stores only the chosen brightness percentage and bright-light switch.
4. Has no accessibility service.
