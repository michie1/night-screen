# Review notes

Night Screen is a local utility with no login, network access, ads, analytics,
or paid features.

To test the main function:

1. Open the app and grant Display over other apps.
2. Grant notifications.
3. Press Start dimming.
4. Leave the app. The overlay remains active.
5. Reopen the app to use the floating slider.
6. Use Stop in the ongoing notification to remove the overlay and end the
   foreground service.

The overlay permission is essential because Android has no ordinary app API
for drawing a user-controlled dimming filter over other apps. The foreground
service is essential because dimming is expected to continue after the app
activity closes.

At very dark levels Android may block touches through an overlay. The app
warns about this and keeps an ongoing notification Stop action as a safe exit.
