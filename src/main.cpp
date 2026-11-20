#define UNICODE
#define _UNICODE
#include <windows.h>
#include <shlobj.h>
#include <shellapi.h>
#include <commctrl.h>
#include <commdlg.h>
#include <tlhelp32.h>
#include <filesystem>
#include <string>
#include <vector>
#include <algorithm>

namespace fs = std::filesystem;
static HWND g_gamePath, g_dllPath, g_status, g_log, g_plugins, g_process;
static fs::path g_game, g_dll;
static const wchar_t* kClass = L"StarfallDreamsWindow";

static void logLine(const std::wstring& s) {
    int n = GetWindowTextLengthW(g_log);
    SendMessageW(g_log, EM_SETSEL, n, n);
    std::wstring line = s + L"\r\n";
    SendMessageW(g_log, EM_REPLACESEL, FALSE, (LPARAM)line.c_str());
}
static bool gameExe(const fs::path& p, fs::path* found = nullptr) {
    const wchar_t* names[] = {L"StandChillow.exe", L"StandChillow_x64.exe"};
    for (auto name : names) {
        auto candidate = p / name;
        if (fs::exists(candidate)) { if (found) *found = candidate; return true; }
    }
    return false;
}
static std::wstring detectRuntime(const fs::path& root) {
    std::error_code ec;
    for (fs::directory_iterator it(root, ec), end; !ec && it != end; it.increment(ec)) {
        if (!it->is_directory(ec)) continue;
        auto name = it->path().filename().wstring();
        if (name.size() >= 5 && name.substr(name.size()-5) == L"_Data") {
            auto data = it->path();
            if (fs::exists(data / L"il2cpp_data")) return L"Unity IL2CPP";
            if (fs::exists(data / L"Managed")) return L"Unity Mono";
        }
    }
    return L"Unity runtime not detected";
}
static fs::path pluginDir() { return g_game / L"BepInEx" / L"plugins"; }
static void refreshPlugins() {
    SendMessageW(g_plugins, LB_RESETCONTENT, 0, 0);
    if (g_game.empty()) return;
    std::error_code ec;
    auto dir = pluginDir();
    if (!fs::exists(dir, ec)) return;
    for (fs::directory_iterator it(dir, ec), end; !ec && it != end; it.increment(ec)) {
        if (it->is_regular_file(ec) && it->path().extension() == L".dll")
            SendMessageW(g_plugins, LB_ADDSTRING, 0, (LPARAM)it->path().filename().c_str());
    }
}
static void inspectGame() {
    if (g_game.empty()) return;
    fs::path exe;
    bool found = gameExe(g_game, &exe);
    auto runtime = detectRuntime(g_game);
    std::wstring status = found ? L"Game executable found | " : L"Game executable not found | ";
    status += runtime;
    SetWindowTextW(g_status, status.c_str());
    logLine(L"Game folder: " + g_game.wstring());
    logLine(status);
    if (!found) logLine(L"Select the folder containing StandChillow.exe.");
    if (runtime == L"Unity runtime not detected")
        logLine(L"Could not identify Mono/IL2CPP. Check the selected game folder.");
    else
        logLine(L"Use a BepInEx package matching this runtime and the game's architecture.");
    refreshPlugins();
}
static void chooseFolder(HWND hwnd) {
    BROWSEINFOW bi{}; bi.hwndOwner=hwnd;
    bi.lpszTitle=L"Select the StandChillow game folder";
    bi.ulFlags=BIF_RETURNONLYFSDIRS|BIF_NEWDIALOGSTYLE;
    PIDLIST_ABSOLUTE pid=SHBrowseForFolderW(&bi);
    if (!pid) return;
    wchar_t buf[MAX_PATH]{};
    if (SHGetPathFromIDListW(pid,buf)) {
        g_game=buf; SetWindowTextW(g_gamePath,g_game.c_str()); inspectGame();
    }
    CoTaskMemFree(pid);
}
static void chooseDll(HWND hwnd) {
    wchar_t file[MAX_PATH]{};
    OPENFILENAMEW ofn{}; ofn.lStructSize=sizeof(ofn); ofn.hwndOwner=hwnd;
    ofn.lpstrFilter=L"Dynamic-link libraries (*.dll)\0*.dll\0All files (*.*)\0*.*\0";
    ofn.lpstrFile=file; ofn.nMaxFile=MAX_PATH; ofn.Flags=OFN_FILEMUSTEXIST|OFN_PATHMUSTEXIST;
    if (GetOpenFileNameW(&ofn)) {
        g_dll=file; SetWindowTextW(g_dllPath,g_dll.c_str());
        logLine(L"Selected DLL: " + g_dll.filename().wstring());
    }
}
static void installDll() {
    if (g_game.empty() || !gameExe(g_game)) { logLine(L"Select a valid StandChillow folder first."); return; }
    if (g_dll.empty() || !fs::is_regular_file(g_dll) || g_dll.extension()!=L".dll") {
        logLine(L"Select a valid .dll file first."); return;
    }
    auto runtime=detectRuntime(g_game);
    if (runtime==L"Unity runtime not detected") {
        logLine(L"Cannot verify Unity runtime. DLL was not copied."); return;
    }
    std::error_code ec;
    fs::create_directories(pluginDir(),ec);
    if (ec) { logLine(L"Cannot create BepInEx/plugins. Check folder permissions."); return; }
    auto destination=pluginDir()/g_dll.filename();
    if (fs::equivalent(g_dll,destination,ec)) {
        logLine(L"The selected DLL is already in the plugin folder."); return;
    }
    ec.clear();
    fs::copy_file(g_dll,destination,fs::copy_options::overwrite_existing,ec);
    if (ec) { logLine(L"Copy failed: " + ec.message().wstring()); return; }
    logLine(L"Plugin staged: " + destination.wstring());
    logLine(L"Start the game with a compatible BepInEx installation to load it.");
    refreshPlugins();
}
static void removePlugin() {
    int sel=(int)SendMessageW(g_plugins,LB_GETCURSEL,0,0);
    if (sel==LB_ERR) { logLine(L"Select a plugin in the list first."); return; }
    wchar_t name[MAX_PATH]{}; SendMessageW(g_plugins,LB_GETTEXT,sel,(LPARAM)name);
    auto path=pluginDir()/name;
    if (MessageBoxW(nullptr,(std::wstring(L"Remove ") + name + L" from BepInEx/plugins?").c_str(),
                    L"Confirm removal",MB_YESNO|MB_ICONQUESTION)!=IDYES) return;
    std::error_code ec; fs::remove(path,ec);
    if (ec) logLine(L"Could not remove plugin: " + ec.message().wstring());
    else logLine(L"Removed: " + std::wstring(name));
    refreshPlugins();
}
static void launchGame() {
    fs::path exe;
    if (g_game.empty() || !gameExe(g_game,&exe)) { logLine(L"Select a valid StandChillow folder first."); return; }
    SHELLEXECUTEINFOW sei{}; sei.cbSize=sizeof(sei); sei.fMask=SEE_MASK_NOCLOSEPROCESS;
    sei.lpFile=exe.c_str(); sei.lpDirectory=g_game.c_str(); sei.nShow=SW_SHOWNORMAL;
    if (!ShellExecuteExW(&sei)) logLine(L"Could not launch the game. Check the Wine prefix and game files.");
    else { logLine(L"Game launch requested: " + exe.filename().wstring()); if(sei.hProcess) CloseHandle(sei.hProcess); }
}
static void refreshProcess() {
    bool running=false;
    HANDLE snap=CreateToolhelp32Snapshot(TH32CS_SNAPPROCESS,0);
    if (snap!=INVALID_HANDLE_VALUE) {
        PROCESSENTRY32W pe{}; pe.dwSize=sizeof(pe);
        if (Process32FirstW(snap,&pe)) do {
            std::wstring name=pe.szExeFile;
            std::transform(name.begin(),name.end(),name.begin(),::towlower);
            if(name==L"standchillow.exe" || name==L"standchillow_x64.exe") { running=true; break; }
        } while(Process32NextW(snap,&pe));
        CloseHandle(snap);
    }
    SetWindowTextW(g_process,running?L"StandChillow process: running":L"StandChillow process: not detected");
    logLine(running?L"StandChillow process detected.":L"StandChillow process not detected.");
}
static HWND make(HWND parent,const wchar_t* cls,const wchar_t* text,DWORD style,int x,int y,int w,int h,int id) {
    return CreateWindowExW(0,cls,text,WS_CHILD|WS_VISIBLE|style,x,y,w,h,parent,(HMENU)(INT_PTR)id,GetModuleHandleW(nullptr),nullptr);
}
static LRESULT CALLBACK wndProc(HWND hwnd,UINT msg,WPARAM wp,LPARAM lp) {
    switch(msg) {
    case WM_CREATE: {
        HFONT font=(HFONT)GetStockObject(DEFAULT_GUI_FONT);
        make(hwnd,L"STATIC",L"STARFALL DREAMS  /  STANDCHILLOW DLL LOADER",0,22,14,650,24,0);
        make(hwnd,L"STATIC",L"Windows x64 UI | designed to run under Wine",0,22,39,650,20,0);
        make(hwnd,L"STATIC",L"GAME FOLDER",0,22,72,130,18,0);
        g_gamePath=make(hwnd,L"EDIT",L"",WS_BORDER|ES_AUTOHSCROLL,22,94,490,26,101);
        make(hwnd,L"BUTTON",L"Browse...",BS_PUSHBUTTON,522,94,125,26,102);
        g_status=make(hwnd,L"STATIC",L"Choose the StandChillow installation folder.",0,22,128,625,24,103);
        make(hwnd,L"STATIC",L"DLL FILE",0,22,164,100,18,0);
        g_dllPath=make(hwnd,L"EDIT",L"",WS_BORDER|ES_AUTOHSCROLL|ES_READONLY,22,186,490,26,104);
        make(hwnd,L"BUTTON",L"Choose DLL...",BS_PUSHBUTTON,522,186,125,26,105);
        make(hwnd,L"BUTTON",L"Add to plugin folder",BS_PUSHBUTTON,22,225,190,32,106);
        make(hwnd,L"BUTTON",L"Launch StandChillow",BS_PUSHBUTTON,222,225,180,32,107);
        make(hwnd,L"BUTTON",L"Refresh process",BS_PUSHBUTTON,412,225,145,32,108);
        g_process=make(hwnd,L"STATIC",L"StandChillow process: not checked",0,22,267,625,20,109);
        make(hwnd,L"STATIC",L"INSTALLED PLUGIN DLLS  (BepInEx/plugins)",0,22,298,400,18,0);
        g_plugins=make(hwnd,L"LISTBOX",L"",WS_BORDER|WS_VSCROLL|LBS_NOTIFY,22,320,390,90,110);
        make(hwnd,L"BUTTON",L"Remove selected",BS_PUSHBUTTON,422,320,125,30,111);
        make(hwnd,L"STATIC",L"ACTIVITY LOG",0,22,425,200,18,0);
        g_log=make(hwnd,L"EDIT",L"Ready. Select game folder and a compatible plugin DLL.\r\n",WS_BORDER|ES_MULTILINE|ES_AUTOVSCROLL|ES_READONLY|WS_VSCROLL,22,447,625,100,112);
        for(HWND c=GetWindow(hwnd,GW_CHILD);c;c=GetWindow(c,GW_HWNDNEXT)) SendMessageW(c,WM_SETFONT,(WPARAM)font,TRUE);
        return 0;
    }
    case WM_COMMAND:
        switch(LOWORD(wp)) {
            case 102: chooseFolder(hwnd); break;
            case 105: chooseDll(hwnd); break;
            case 106: installDll(); break;
            case 107: launchGame(); break;
            case 108: refreshProcess(); break;
            case 111: removePlugin(); break;
        } return 0;
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
    HWND w=CreateWindowExW(0,kClass,L"StarfallDreams — StandChillow DLL Loader",
        WS_OVERLAPPED|WS_CAPTION|WS_SYSMENU|WS_MINIMIZEBOX,CW_USEDEFAULT,CW_USEDEFAULT,
        690,610,nullptr,nullptr,h,nullptr);
    if(!w) return 1;
    ShowWindow(w,show); UpdateWindow(w);
    MSG m; while(GetMessageW(&m,nullptr,0,0)>0){TranslateMessage(&m);DispatchMessageW(&m);}
    CoUninitialize(); return (int)m.wParam;
}
