# StarfallDreams — StandChillow DLL Loader

A Windows GUI utility designed to run under Wine on Linux. It provides a DLL-selection workflow for StandChillow Unity plugins.

## Features
- Select the StandChillow installation folder and detect its executable.
- Detect a Unity Mono or IL2CPP data folder.
- Select a plugin DLL and copy it into `BepInEx/plugins`.
- List and remove staged plugin DLLs.
- Launch StandChillow and check whether its process is running.
- Activity log for all operations.

## Important: how DLL loading works

This utility stages Unity plugin DLLs in BepInEx's plugin directory. A compatible BepInEx installation must already be installed and configured for the game's Unity runtime and architecture. BepInEx then loads compatible plugins when the game starts.

**This is not a remote-process injector:** it does not inject arbitrary DLLs into a running process, modify game memory, or bypass anti-cheat/security controls. A random Windows DLL is not necessarily a Unity plugin; it must be built for the game's runtime and plugin API.

## Build

Requires CMake 3.20+ and Visual Studio 2022 (x64):

```powershell
cmake -S . -B build -A x64
cmake --build build --config Release
```

Output: `build/Release/StarfallDreams.exe`.

## Run under Wine

```bash
wine StarfallDreams.exe
```

Use the same Wine prefix for the utility and StandChillow if you want the game paths and installed components to match.

## Runtime detection

- `*_Data/Managed` usually indicates Unity Mono.
- `*_Data/il2cpp_data` usually indicates Unity IL2CPP.

Install the corresponding BepInEx distribution. Detection is a heuristic and does not establish compatibility by itself.
