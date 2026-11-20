# zxcSOFTINA — Minecraft client work

This directory is reserved for the Fabric client based on the user-provided
`zxcSOFTINA-beta.jar`.

## Inspected artifact

- Minecraft: 1.21.11
- Loader: Fabric 0.18.1
- Java: 21+
- Mod id: `phantomclient`
- Existing UI classes: `PhantomScreen`
- Existing combat module: `KillAura`

## Requested changes

- FreeCam
- ViewModel
- FOV setting
- Review and improve the existing KillAura without adding a duplicate

## Compatibility and UI constraints

Keep the existing menu layout, colors, typography, and interaction style unchanged.
Do not overwrite the unrelated StandChillow loader project in the repository root.

## Build status

No modified JAR is published yet. The uploaded artifact is a compiled JAR with
class files only; it does not include Java sources or Gradle build files. Changes
must be validated against the exact Minecraft/Fabric 1.21.11 mappings and APIs
before a rebuilt JAR can be called ready.
