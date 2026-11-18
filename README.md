# Pastagram

Pastagram is an Android messenger starter built with Kotlin and Jetpack Compose.

## Current build
- Chat list with search and sample conversations
- Conversation screen with locally managed demo messages
- Contacts and profile tabs
- Material-style Android UI

> This first version is a UI prototype. Messages are stored only in app memory; accounts, real-time delivery, media uploads, push notifications and server-side persistence are not connected yet.

## Build
Open this project in Android Studio and run the `app` configuration, or run:

```bash
gradle assembleDebug
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`

The GitHub Actions workflow builds the debug APK on every push to `main` and uploads it as an artifact.
