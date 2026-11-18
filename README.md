# XxKOCMOCxX

A client-side Fabric utility and PvP mod for Minecraft 1.21.11.

## Requirements
- Java 21
- Gradle 8.14+
- Minecraft 1.21.11
- Fabric Loader 0.18.1+
- Fabric API 0.141.1+1.21.11

## Build
```bash
gradle build
```

The remapped mod jar is written to `build/libs/`.

## Controls
- Right Shift: open/close ClickGUI
- Click a module button to toggle it

## Included modules
- Sprint: automatically maintains sprint while moving forward.
- FullBright: raises client gamma while enabled and restores the previous value when disabled.

This project uses Mojang mappings for Minecraft 1.21.11, as recommended by Fabric for this release.
