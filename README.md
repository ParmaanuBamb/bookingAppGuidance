# Ride Guide For Elders

A simple Android app concept for elderly users who need calm, step-by-step help booking an auto or cab through Rapido, Ola, or Uber.

## What It Does

- Shows very large text and touch targets.
- Lets the user choose Rapido, Ola, or Uber.
- Gives one booking instruction at a time.
- Opens the selected ride app if it is installed.
- Falls back to Play Store if the ride app is missing.
- Includes an emergency help button for `112`.

## Open In Android Studio

1. Open this folder in Android Studio.
2. Let Android Studio sync Gradle.
3. Run the `app` configuration on an emulator or Android phone.

The app is intentionally written in plain Java with Android views, so the main design is easy to inspect in `app/src/main/java/com/example/rideguide/MainActivity.java`.
