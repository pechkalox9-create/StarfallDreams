#include <iostream>
#ifdef _WIN32
#include <windows.h>
int wmain(int argc, wchar_t **argv) {
    if (argc < 2) { std::wcerr << L"Usage: DllTestHost.exe <module.dll>\n"; return 2; }
    HMODULE module = LoadLibraryW(argv[1]);
    if (!module) { std::wcerr << L"LoadLibraryW failed. Win32 error: " << GetLastError() << L"\n"; return 1; }
    std::wcout << L"DLL loaded into the dedicated test host. Press Enter to unload.\n";
    std::wstring line; std::getline(std::wcin, line);
    FreeLibrary(module);
    std::wcout << L"DLL unloaded.\n";
    return 0;
}
#else
#include <dlfcn.h>
int main(int argc, char **argv) {
    if (argc < 2) { std::cerr << "Usage: DllTestHost <module.so>\n"; return 2; }
    void *module = dlopen(argv[1], RTLD_NOW);
    if (!module) { std::cerr << "dlopen failed: " << dlerror() << "\n"; return 1; }
    std::cout << "Module loaded into the dedicated test host. Press Enter to unload.\n";
    std::cin.get(); dlclose(module); return 0;
}
#endif
