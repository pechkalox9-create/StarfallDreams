# Starfall Explosions (Fabric)

A standalone Fabric mod module for **Minecraft Java 1.21.11**.

## Features

- Gives the Stellar Controller to a joining player if they do not already have one.
- Right-click the controller to open the orbital console.
- Four visual presets: Nova Pulse, Aurora Veil, Starfall and Void Ripple.
- Cosmetic client-side particles only: no TNT, block changes, entity damage, targeting or world destruction.

## Requirements

- Minecraft Java 1.21.11
- Java 21
- Fabric Loader 0.18.1 or newer
- Fabric API 0.141.0+1.21.11

## Download the JAR

GitHub Actions builds the mod automatically when files in this module change.

1. Open the [Actions build workflow](../../actions/workflows/build-starfall-explosions.yml).
2. Select the latest successful run on the `feature/starfall-explosions-fabric` branch.
3. Download the artifact named `starfall-explosions-fabric-mc-1.21.11`.
4. Open the downloaded ZIP and copy the mod JAR into your Minecraft `mods` folder. Install Fabric Loader and Fabric API for Minecraft 1.21.11 as well.

The workflow currently publishes a downloadable build artifact, not a tagged GitHub Release.

## Build locally

From this directory, run:

```bash
gradle --no-daemon clean build
```

The JAR is written to `build/libs/`.

## License

MIT
