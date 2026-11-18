# BERDA Overlay (Android)

Standalone Android HUD overlay for package `com.heesoyam.berda`.

## Current tools
- Floating control panel
- Optional centered crosshair overlay
- INJECT demo button (does not inject code or access game memory)
- Close control

BERDA is described by its publisher as a multiplayer survival game focused on open-world exploration, resource gathering, base building, raids and team play. This app is an independent overlay utility; it does not read game state, reveal hidden players/loot, automate combat, bypass anti-cheat, or modify the game process.

## Build
Open in Android Studio with JDK 17 and Android SDK 35, or use the GitHub Actions workflow. The workflow builds `app-debug.apk` and uploads it as a build artifact.

## Install
Install the debug APK, grant **Display over other apps**, then tap **Enable overlay**. The crosshair is a generic visual marker, not an aim-assist feature.
