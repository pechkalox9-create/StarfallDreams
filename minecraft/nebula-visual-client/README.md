# Nebula Visual Client

A client-side visual HUD and settings menu for Minecraft Java 1.21.11 + Fabric.

## Features
- Minimal Nebula-themed settings screen.
- Open the menu with **Right Shift** or **F8**.
- Toggleable HUD watermark.
- Toggleable FPS counter.
- Toggleable player coordinates.
- Toggleable local clock.
- Client-side only: no server installation is required.

This project uses an original interface and original implementation. It does not bundle copied assets or proprietary client code, and it does not include gameplay cheats.

## Build
Use Java 21 and run:

```sh
gradle --no-daemon clean build
```

The remapped mod JAR is created in `build/libs/`.

## Install
Install Fabric Loader and Fabric API for Minecraft 1.21.11, then place the JAR in the instance's `mods` folder.
