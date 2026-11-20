# StarfallDreams — StandChillow Mod Manager

A small Windows GUI helper for inspecting a StandChillow Unity installation and opening its BepInEx plugin directory. Designed to be usable under Wine as a Windows desktop application.

## What it does
- Selects the StandChillow game root folder.
- Checks for the game executable and detects Unity Mono (`*_Data/Managed`) or IL2CPP (`*_Data/il2cpp_data`).
- Creates/opens `BepInEx/plugins` for placing compatible plugin DLLs.
- Does **not** inject DLLs into arbitrary processes or modify game memory.

## Build
Requires CMake 3.20+ and a C++17 Windows compiler (Visual Studio 2022 recommended):

```powershell
cmake -S . -B build -A x64
cmake --build build --config Release
```

The Windows executable is `build/Release/StarfallDreams.exe`.

## StandChillow compatibility
The exact loader package cannot be selected safely until the installed game folder is inspected:
- `StandChillow_Data/Managed` usually indicates Unity Mono.
- `StandChillow_Data/il2cpp_data` indicates Unity IL2CPP.
- Confirm the game's executable architecture (x86/x64) before installing any loader.

BepInEx has separate runtime/architecture builds. Follow the [official installation guide](https://docs.bepinex.dev/articles/user_guide/installation/). IL2CPP and Mono require different packages; do not mix them.

## Wine
Run the Windows build with `wine StarfallDreams.exe`. The game and loader themselves must also be compatible with the selected Wine prefix.

## Important
This is an installation-folder helper, not a finished game mod or a universal DLL injector. A working StandChillow plugin still needs game-specific APIs and testing in the offline/bot mode.
