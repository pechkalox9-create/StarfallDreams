#define UNICODE
#define _UNICODE
#include <windows.h>
#include <shlobj.h>
#include <shellapi.h>
#include <commctrl.h>
#include <filesystem>
#include <string>
#include <vector>

namespace fs = std::filesystem;
static HWND g_path, g_status, g_log, g_plugins, g_open;
static fs::path g_game;
static const wchar_t* kClass = L"StarfallDreamsWindow";

static void logLine(const std::wstring& s) {
    int n = GetWindowTextLengthW(g_log);
    SendMessageW(g_log, EM_SETSEL, n, n);
    std::wstring line = s + L"\r\n";
    SendMessageW(g_log, EM_REPLACESEL, FALSE, (LPARAM)line.c_str());
}
static bool hasExe(const fs::path& p) {
    return fs::exists(p / L"StandChillow.exe") || fs::exists(p / L"StandChillow_x64.exe");
}
static std::wstring detectRuntime(const fs::path& root) {
    std::error_code ec;
    for (auto const& e : fs::directory_iterator(root, ec)) {
        if (!e.is_directory(ec)) continue;
        const auto name = e.path().filename().wstring();
        if (name.size() > 5 && name.substr(name.size()-5) == L"_Data") {
            auto data = e.path();
            if (fs::exists(data / L"il2cpp_data")) return L"Unity IL2CPP";
            if (fs::exists(data / L"Managed")) return L"Unity Mono";
        }
    }
    if (fs::exists(root / L"il2cpp_data")) return L"Unity IL2CPP";
    if (fs::exists(root / L"Managed")) return L"Unity Mono";
    return L"Runtime not detected (need game folder screenshot)";
}
static void inspect() {
    if (g_game.empty()) return;
    const auto runtime = detectRuntime(g_game);
    const bool exe = hasExe(g_game);
    SetWindowTextW(g_status, (std::wstring(exe ? L"StandChillow executable found  |  " : L"EXE not found  |  ") + runtime).c_str());
    logLine(L"Game folder: " + g_game.wstring());
    logLine(L"Detected: " + runtime);
    if (!exe) logLine(L"Check that the selected folder contains StandChillow.exe.");
    if (runtime.find(L"not detected") != std::wstring::npos)
        logLine(L"Please select the game's root folder (the folder containing the .exe).");
    else
        logLine(L"Next: use the matching BepInEx build only after confirming the game architecture.");
}
static void chooseFolder(HWND hwnd) {
    BROWSEINFOW bi{}; bi.hwndOwner=hwnd; bi.lpszTitle=L"Select the StandChillow game folder";
    bi.ulFlags=BIF_RETURNONLYFSDIRS|BIF_NEWDIALOGSTYLE;
    PIDLIST_ABSOLUTE pid=SHBrowseForFolderW(&bi);
    if (!pid) return;
    wchar_t buf[MAX_PATH]{};
    if (SHGetPathFromIDListW(pid,buf)) {
        g_game=buf; SetWindowTextW(g_path,g_game.c_str()); inspect();
    }
    CoTaskMemFree(pid);
}
static void openPlugins() {
    if (g_game.empty()) { logLine(L"Select the StandChillow folder first."); return; }
    fs::path p=g_game/L"BepInEx"/L"plugins";
    std::error_code ec; fs::create_directories(p,ec);
    if (ec) { logLine(L"Could not create BepInEx/plugins. Check folder permissions."); return; }
    ShellExecuteW(nullptr,L"open",p.c_str(),nullptr,nullptr,SW_SHOWNORMAL);
    logLine(L"Opened plugin folder. Put only compatible Unity plugin DLLs here.");
}
static void openGame() {
    if (g_game.empty()) { logLine(L"Select the StandChillow folder first."); return; }
    ShellExecuteW(nullptr,L"open",g_game.c_str(),nullptr,nullptr,SW_SHOWNORMAL);
}
static HWND make(HWND parent,const wchar_t* cls,const wchar_t* text,DWORD style,int x,int y,int w,int h,int id) {
    return CreateWindowExW(0,cls,text,WS_CHILD|WS_VISIBLE|style,x,y,w,h,parent,(HMENU)(INT_PTR)id,GetModuleHandleW(nullptr),nullptr);
}
static LRESULT CALLBACK wndProc(HWND hwnd,UINT msg,WPARAM wp,LPARAM lp) {
    switch(msg) {
    case WM_CREATE: {
        HFONT font=(HFONT)GetStockObject(DEFAULT_GUI_FONT);
        make(hwnd,L"STATIC",L"STARFALL DREAMS  /  STANDCHILLOW",0,24,18,600,30,0);
        make(hwnd,L"STATIC",L"Unity mod manager  •  Windows / Wine",0,25,50,600,22,0);
        make(hwnd,L"STATIC",L"GAME FOLDER",0,25,92,150,20,0);
        g_path=make(hwnd,L"EDIT",L"",WS_BORDER|ES_AUTOHSCROLL,25,116,480,30,101);
        make(hwnd,L"BUTTON",L"Choose folder",BS_PUSHBUTTON,515,116,135,30,102);
        g_status=make(hwnd,L"STATIC",L"Select the StandChillow installation folder to inspect it.",0,25,160,630,28,103);
        g_plugins=make(hwnd,L"BUTTON",L"Open / create BepInEx plugins",BS_PUSHBUTTON,25,205,240,36,104);
        g_open=make(hwnd,L"BUTTON",L"Open game folder",BS_PUSHBUTTON,280,205,180,36,105);
        make(hwnd,L"STATIC",L"ACTIVITY LOG",0,25,265,200,20,0);
        g_log=make(hwnd,L"EDIT",L"Ready. Select the StandChillow game root folder.\r\n",WS_BORDER|ES_MULTILINE|ES_AUTOVSCROLL|ES_READONLY|WS_VSCROLL,25,290,625,180,106);
        for(HWND c=GetWindow(hwnd,GW_CHILD);c;c=GetWindow(c,GW_HWNDNEXT)) SendMessageW(c,WM_SETFONT,(WPARAM)font,TRUE);
        return 0;
    }
    case WM_COMMAND:
        switch(LOWORD(wp)) { case 102: chooseFolder(hwnd); break; case 104: openPlugins(); break; case 105: openGame(); break; }
        return 0;
    case WM_DESTROY: PostQuitMessage(0); return 0;
    }
    return DefWindowProcW(hwnd,msg,wp,lp);
}
int WINAPI wWinMain(HINSTANCE h,HINSTANCE,LPWSTR,int show) {
    CoInitializeEx(nullptr,COINIT_APARTMENTTHREADED);
    INITCOMMONCONTROLSEX ic{sizeof(ic),ICC_STANDARD_CLASSES}; InitCommonControlsEx(&ic);
    WNDCLASSW wc{}; wc.lpfnWndProc=wndProc; wc.hInstance=h; wc.lpszClassName=kClass;
    wc.hCursor=LoadCursor(nullptr,IDC_ARROW); wc.hbrBackground=(HBRUSH)(COLOR_WINDOW+1);
    wc.hIcon=LoadIcon(nullptr,IDI_APPLICATION); RegisterClassW(&wc);
    HWND w=CreateWindowExW(0,kClass,L"StarfallDreams — StandChillow Mod Manager",WS_OVERLAPPED|WS_CAPTION|WS_SYSMENU|WS_MINIMIZEBOX,
        CW_USEDEFAULT,CW_USEDEFAULT,700,550,nullptr,nullptr,h,nullptr);
    if(!w) return 1; ShowWindow(w,show); UpdateWindow(w);
    MSG m; while(GetMessageW(&m,nullptr,0,0)>0){TranslateMessage(&m);DispatchMessageW(&m);}
    CoUninitialize(); return (int)m.wParam;
}
