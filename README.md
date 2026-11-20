# WineBridge

A small Qt 6 desktop utility for launching Windows applications under Wine with a selected **native DLL override**.

WineBridge is a cooperative launch-time loader/configuration tool. It does not attach to, write into, or inject code into an already-running process.

## Features

- Select a Windows executable and a native `.dll`.
- Inspect PE machine architecture (x86, x86-64, ARM64) before launch.
- Launch the selected program under Wine with `WINEDLLOVERRIDES=<module>=n,b`.
- Add the DLL directory to `WINEDLLPATH` for Wine's DLL search.
- View a live snapshot of Linux processes (PID and command).
- See Wine stdout/stderr and launch/exit status in the log.
- Dark Qt Widgets interface.

## Requirements

- Linux
- Qt 6.5+ (Widgets)
- CMake 3.21+
- A working Wine installation (`wine` or `wine64` on PATH)

## Build

```bash
cmake -S . -B build -DCMAKE_BUILD_TYPE=Release
cmake --build build --parallel
./build/winebridge
```

## Usage

1. Select the Windows `.exe` to run.
2. Select the native `.dll` you want Wine to load.
3. WineBridge checks both PE headers and warns if their architectures differ.
4. Click **Launch with Wine**. The app starts a new Wine process with the selected DLL override.
5. Use **Refresh** in the process panel to update the Linux process snapshot.

The DLL should be a trusted library intended for the selected application. Wine resolves native DLLs using its normal DLL search rules; WineBridge adds the DLL's parent directory to `WINEDLLPATH`. The override uses `n,b` (native first, builtin fallback).

Close any existing instance of the target application before launching if you want to test a different DLL override. Overrides are applied at process startup, not retroactively to an existing process.

## Safety and scope

WineBridge only launches a new process under the current user's Wine environment. It intentionally does not provide arbitrary process injection, remote-thread creation, memory writing, stealth, anti-cheat bypass, or privilege escalation. Use it only with software and DLLs you are authorized to test.

## License

MIT. See [LICENSE](LICENSE).
