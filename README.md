# StarfallDreams

Wine-compatible desktop UI for testing DLLs in a dedicated companion process.

## Scope
The application provides a DLL file picker and activity log. The selected DLL is loaded by DllTestHost itself using LoadLibraryW on Windows. It does not inject code into arbitrary or unrelated processes.

## Build
Requires CMake 3.21+, C++17, and Qt 6 Widgets. Build with the Qt MinGW kit:

```powershell
cmake -S . -B build -G "MinGW Makefiles" -DCMAKE_PREFIX_PATH="C:/Qt/6.x.x/mingw_64"
cmake --build build --config Release
```

Deploy Qt runtime dependencies beside StarfallDreams.exe (for example with windeployqt). Keep DllTestHost.exe in the same directory.

Run under Wine:

```bash
wine StarfallDreams.exe
```

Build both executables for the same architecture as the DLL. Wine does not make Windows DLLs compatible with native Linux processes. Only load DLLs you trust.
