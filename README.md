# Ride Guide For Elders

A simple Android app concept for elderly users who need calm, step-by-step help booking an auto or cab through Rapido, Ola, or Uber.

## What It Does

- Shows very large text and touch targets.
- Lets the user choose Rapido, Ola, or Uber.
- Gives app-specific booking instructions one step at a time.
- Shows simple screenshot-style guide pictures for each step.
- Opens the selected ride app if it is installed.
- Falls back to Play Store if the ride app is missing.
- Lets the user add family emergency numbers.
- Opens the phone dialer for `112` or saved family contacts.

## Elder-Friendly Design Choices

- No login, menus, or small icons inside this guidance app.
- Big buttons and high-contrast text.
- One active step is shown clearly, with the next action emphasized.
- Emergency calls open in the dialer so the user confirms before calling.
- The guide pictures are simplified mock screens, not exact third-party app screenshots, so they stay useful even when ride apps change their UI.

## Open In Android Studio

1. Open this folder in Android Studio.
2. Let Android Studio sync Gradle.
3. Run the `app` configuration on an emulator or Android phone.

The app is intentionally written in plain Java with Android views, so the main design is easy to inspect in `app/src/main/java/com/example/rideguide/MainActivity.java`.
