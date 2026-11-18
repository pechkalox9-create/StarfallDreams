# Pasta Visual

A client-side visual mod for **Minecraft Java 1.21.11 + Fabric**.

## Included in this first release

- Custom Pasta Visual panel on the Minecraft title screen.
- Dedicated in-game ClickGUI opened with **Right Shift**.
- Hit particles around attacked living entities.
- Separate critical-hit particle burst.
- Toggles for the main-menu branding and compact settings panel.
- Settings saved to `.minecraft/config/pasta-visual.properties`.
- Client-side only: no server installation and no gameplay packet modifications.

## Build

Requirements: JDK 21, Gradle, Fabric Loom 1.14.

```bash
gradle build
```

The remapped mod JAR is written to `build/libs/pasta-visual-1.0.0.jar`.

## Install

Put the JAR and Fabric API for Minecraft 1.21.11 in the selected Fabric profile's `mods` folder.

## Controls

- **Right Shift** — open/close Pasta Visual.
- Use the title-screen **PASTA VISUAL** button to open settings from the main menu.

All visual effects are local client cosmetics. Attack timing, damage, movement and server packets remain vanilla.
