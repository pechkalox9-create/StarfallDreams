#include <QApplication>
#include <QByteArray>
#include <QComboBox>
#include <QDir>
#include <QFile>
#include <QFileDialog>
#include <QFileInfo>
#include <QHBoxLayout>
#include <QHeaderView>
#include <QLabel>
#include <QLineEdit>
#include <QMainWindow>
#include <QMessageBox>
#include <QPlainTextEdit>
#include <QProcess>
#include <QProcessEnvironment>
#include <QRegularExpression>
#include <QPushButton>
#include <QStandardPaths>
#include <QTableWidget>
#include <QTableWidgetItem>
#include <QTimer>
#include <QTime>
#include <QAbstractItemView>
#include <QVBoxLayout>
#include <QtEndian>

#include <memory>

namespace {
QString architectureOf(const QString &path)
{
    QFile file(path);
    if (!file.open(QIODevice::ReadOnly))
        return QStringLiteral("Unreadable");

    const QByteArray data = file.read(4096);
    if (data.size() < 64 || data.at(0) != 'M' || data.at(1) != 'Z')
        return QStringLiteral("Not a PE file");

    const auto *bytes = reinterpret_cast<const uchar *>(data.constData());
    const quint32 peOffset = qFromLittleEndian<quint32>(bytes + 0x3c);
    if (peOffset > static_cast<quint32>(data.size() - 6))
        return QStringLiteral("Invalid PE header");

    const qsizetype offset = static_cast<qsizetype>(peOffset);
    if (data.mid(offset, 4) != QByteArray("PE\0\0", 4))
        return QStringLiteral("Invalid PE signature");

    const quint16 machine = qFromLittleEndian<quint16>(bytes + offset + 4);
    switch (machine) {
    case 0x014c: return QStringLiteral("x86");
    case 0x8664: return QStringLiteral("x86-64");
    case 0xaa64: return QStringLiteral("ARM64");
    default: return QStringLiteral("Unknown (0x%1)").arg(machine, 4, 16, QLatin1Char('0'));
    }
}

QString processCommandLine(const QString &pid)
{
    QFile cmdline(QStringLiteral("/proc/%1/cmdline").arg(pid));
    if (cmdline.open(QIODevice::ReadOnly)) {
        QByteArray raw = cmdline.readAll();
        raw.replace('\0', ' ');
        const QString value = QString::fromLocal8Bit(raw).trimmed();
        if (!value.isEmpty())
            return value;
    }

    QFile comm(QStringLiteral("/proc/%1/comm").arg(pid));
    if (comm.open(QIODevice::ReadOnly))
        return QString::fromLocal8Bit(comm.readAll()).trimmed();
    return {};
}

class MainWindow final : public QMainWindow
{
public:
    MainWindow()
    {
        setWindowTitle(QStringLiteral("WineBridge"));
        resize(1040, 720);

        auto *central = new QWidget(this);
        auto *root = new QVBoxLayout(central);
        root->setContentsMargins(24, 22, 24, 22);
        root->setSpacing(16);

        auto *heading = new QLabel(QStringLiteral("WINEBRIDGE"));
        heading->setObjectName("heading");
        auto *subtitle = new QLabel(QStringLiteral("Native DLL launch manager for Wine"));
        subtitle->setObjectName("subtitle");
        root->addWidget(heading);
        root->addWidget(subtitle);

        auto *filesBox = new QWidget;
        auto *filesLayout = new QVBoxLayout(filesBox);
        filesLayout->setContentsMargins(0, 0, 0, 0);
        filesLayout->setSpacing(10);

        auto *exeLabel = new QLabel(QStringLiteral("WINDOWS APPLICATION"));
        exeLabel->setObjectName("sectionLabel");
        auto *exeRow = new QHBoxLayout;
        exePath_ = new QLineEdit;
        exePath_->setPlaceholderText("Select an application (.exe)");
        auto *exeBrowse = new QPushButton("Browse");
        exeRow->addWidget(exePath_, 1);
        exeRow->addWidget(exeBrowse);

        auto *dllLabel = new QLabel(QStringLiteral("NATIVE DLL"));
        dllLabel->setObjectName("sectionLabel");
        auto *dllRow = new QHBoxLayout;
        dllPath_ = new QLineEdit;
        dllPath_->setPlaceholderText("Select a native library (.dll)");
        auto *dllBrowse = new QPushButton("Browse");
        dllRow->addWidget(dllPath_, 1);
        dllRow->addWidget(dllBrowse);

        auto *archRow = new QHBoxLayout;
        exeArch_ = new QLabel("EXE: —");
        dllArch_ = new QLabel("DLL: —");
        archRow->addWidget(exeArch_);
        archRow->addSpacing(18);
        archRow->addWidget(dllArch_);
        archRow->addStretch();

        filesLayout->addWidget(exeLabel);
        filesLayout->addLayout(exeRow);
        filesLayout->addWidget(dllLabel);
        filesLayout->addLayout(dllRow);
        filesLayout->addLayout(archRow);
        root->addWidget(filesBox);

        auto *actionRow = new QHBoxLayout;
        winePath_ = new QLineEdit;
        const QString wine = QStandardPaths::findExecutable("wine");
        const QString wine64 = QStandardPaths::findExecutable("wine64");
        winePath_->setText(!wine.isEmpty() ? wine : wine64);
        winePath_->setPlaceholderText("Wine executable (wine / wine64)");
        auto *launch = new QPushButton("Launch with Wine");
        launch->setObjectName("primary");
        actionRow->addWidget(new QLabel("WINE"));
        actionRow->addWidget(winePath_, 1);
        actionRow->addWidget(launch);
        root->addLayout(actionRow);

        auto *pluginRow = new QHBoxLayout;
        pluginDirectory_ = new QLineEdit;
        pluginDirectory_->setPlaceholderText("Choose the game's supported plugin / mods folder");
        auto *pluginBrowse = new QPushButton("Browse");
        auto *stageDll = new QPushButton("Stage DLL for selected process");
        stageDll->setObjectName("primary");
        pluginRow->addWidget(new QLabel("PLUGIN FOLDER"));
        pluginRow->addWidget(pluginDirectory_, 1);
        pluginRow->addWidget(pluginBrowse);
        pluginRow->addWidget(stageDll);
        root->addLayout(pluginRow);

        auto *panels = new QHBoxLayout;
        auto *processPanel = new QVBoxLayout;
        auto *processTitleRow = new QHBoxLayout;
        auto *processTitle = new QLabel("RUNNING PROCESSES");
        processTitle->setObjectName("sectionLabel");
        auto *refresh = new QPushButton("Refresh");
        processTitleRow->addWidget(processTitle);
        processTitleRow->addStretch();
        processTitleRow->addWidget(refresh);
        processTable_ = new QTableWidget(0, 2);
        processTable_->setHorizontalHeaderLabels({"PID", "COMMAND"});
        processTable_->horizontalHeader()->setSectionResizeMode(0, QHeaderView::ResizeToContents);
        processTable_->horizontalHeader()->setSectionResizeMode(1, QHeaderView::Stretch);
        processTable_->verticalHeader()->hide();
        processTable_->setSelectionBehavior(QAbstractItemView::SelectRows);
        processTable_->setEditTriggers(QAbstractItemView::NoEditTriggers);
        processPanel->addLayout(processTitleRow);
        processPanel->addWidget(processTable_);
        panels->addLayout(processPanel, 3);

        auto *logPanel = new QVBoxLayout;
        auto *logTitle = new QLabel("ACTIVITY LOG");
        logTitle->setObjectName("sectionLabel");
        log_ = new QPlainTextEdit;
        log_->setReadOnly(true);
        log_->setPlaceholderText("Wine output and application events will appear here.");
        logPanel->addWidget(logTitle);
        logPanel->addWidget(log_);
        panels->addLayout(logPanel, 2);
        root->addLayout(panels, 1);

        auto *footer = new QLabel("Cooperative plugin staging for a selected Wine process  •  No remote-process injection");
        footer->setObjectName("footer");
        root->addWidget(footer);
        setCentralWidget(central);

        connect(exeBrowse, &QPushButton::clicked, this, [this] {
            const QString path = QFileDialog::getOpenFileName(this, "Choose Windows application", {}, "Windows executables (*.exe);;All files (*)");
            if (!path.isEmpty()) {
                exePath_->setText(path);
                updateArchitecture();
            }
        });
        connect(dllBrowse, &QPushButton::clicked, this, [this] {
            const QString path = QFileDialog::getOpenFileName(this, "Choose native DLL", {}, "DLL files (*.dll);;All files (*)");
            if (!path.isEmpty()) {
                dllPath_->setText(path);
                updateArchitecture();
            }
        });
        connect(exePath_, &QLineEdit::editingFinished, this, [this] { updateArchitecture(); });
        connect(dllPath_, &QLineEdit::editingFinished, this, [this] { updateArchitecture(); });
        connect(refresh, &QPushButton::clicked, this, [this] { refreshProcesses(); });
        connect(pluginBrowse, &QPushButton::clicked, this, [this] {
            const QString dir = QFileDialog::getExistingDirectory(this, "Choose supported game plugin folder");
            if (!dir.isEmpty()) pluginDirectory_->setText(dir);
        });
        connect(stageDll, &QPushButton::clicked, this, [this] { stageDllForSelectedProcess(); });
        connect(launch, &QPushButton::clicked, this, [this] { launchWithWine(); });

        auto *timer = new QTimer(this);
        connect(timer, &QTimer::timeout, this, [this] { refreshProcesses(); });
        timer->start(5000);
        refreshProcesses();
        appendLog("WineBridge ready. Select a process, DLL and the game-supported plugin folder.");
    }

private:
    void appendLog(const QString &message)
    {
        log_->appendPlainText(QString("[%1] %2").arg(QTime::currentTime().toString("HH:mm:ss"), message));
    }

    void updateArchitecture()
    {
        const QString exe = exePath_->text().trimmed();
        const QString dll = dllPath_->text().trimmed();
        const QString exeArch = QFileInfo::exists(exe) ? architectureOf(exe) : QStringLiteral("—");
        const QString dllArch = QFileInfo::exists(dll) ? architectureOf(dll) : QStringLiteral("—");
        exeArch_->setText("EXE: " + exeArch);
        dllArch_->setText("DLL: " + dllArch);
        const bool knownExe = exeArch == "x86" || exeArch == "x86-64" || exeArch == "ARM64";
        const bool knownDll = dllArch == "x86" || dllArch == "x86-64" || dllArch == "ARM64";
        if (knownExe && knownDll && exeArch != dllArch)
            appendLog(QString("Architecture mismatch: EXE is %1, DLL is %2.").arg(exeArch, dllArch));
    }

    void refreshProcesses()
    {
        QDir proc("/proc");
        const QStringList entries = proc.entryList(QDir::Dirs | QDir::NoDotAndDotDot);
        processTable_->setRowCount(0);
        for (const QString &entry : entries) {
            bool ok = false;
            entry.toLongLong(&ok);
            if (!ok)
                continue;
            const QString command = processCommandLine(entry);
            if (command.isEmpty())
                continue;
            const int row = processTable_->rowCount();
            processTable_->insertRow(row);
            processTable_->setItem(row, 0, new QTableWidgetItem(entry));
            processTable_->setItem(row, 1, new QTableWidgetItem(command));
        }
    }

    void stageDllForSelectedProcess()
    {
        const int row = processTable_->currentRow();
        if (row < 0 || !processTable_->item(row, 0)) {
            QMessageBox::information(this, "Select a process", "Select the running game process first.");
            return;
        }

        const QString pid = processTable_->item(row, 0)->text();
        const QString dll = QFileInfo(dllPath_->text().trimmed()).absoluteFilePath();
        const QString targetDir = QDir::cleanPath(pluginDirectory_->text().trimmed());

        if (!QFileInfo(dll).isFile() || QFileInfo(dll).suffix().compare("dll", Qt::CaseInsensitive) != 0) {
            QMessageBox::warning(this, "Choose a DLL", "Choose a valid .dll file first.");
            return;
        }
        if (targetDir.isEmpty() || !QFileInfo(targetDir).isDir()) {
            QMessageBox::warning(this, "Choose plugin folder",
                                 "Choose an existing plugin/mod folder supported by the game.");
            return;
        }

        const QString dllArch = architectureOf(dll);
        if (dllArch != "x86" && dllArch != "x86-64" && dllArch != "ARM64") {
            QMessageBox::warning(this, "Invalid DLL", "The selected file is not a supported PE DLL.");
            return;
        }

        const QString destination = QDir(targetDir).filePath(QFileInfo(dll).fileName());
        if (QFileInfo(dll).absoluteFilePath() == QFileInfo(destination).absoluteFilePath()) {
            appendLog(QString("DLL is already in the selected plugin folder (PID %1).").arg(pid));
            return;
        }
        if (QFileInfo::exists(destination)) {
            const auto answer = QMessageBox::question(this, "Replace plugin",
                "A file with this name already exists. Replace it?",
                QMessageBox::Yes | QMessageBox::No, QMessageBox::No);
            if (answer != QMessageBox::Yes) return;
            if (!QFile::remove(destination)) {
                QMessageBox::critical(this, "Copy failed", "Could not replace the existing DLL.");
                return;
            }
        }
        if (!QFile::copy(dll, destination)) {
            QMessageBox::critical(this, "Copy failed",
                                  "Could not copy the DLL. Check folder permissions and free space.");
            appendLog("Failed to stage DLL: " + destination);
            return;
        }

        appendLog(QString("Staged %1 for selected process PID %2 at %3.")
                  .arg(QFileInfo(dll).fileName(), pid, destination));
        appendLog("The game must support this plugin folder and load the DLL itself; no live injection was performed.");
        QMessageBox::information(this, "DLL staged",
            "The DLL was copied to the selected plugin folder. The running game must support that folder and load plugins itself. This operation does not inject code into the process.");
    }

    void launchWithWine()
    {
        const QString exe = QFileInfo(exePath_->text().trimmed()).absoluteFilePath();
        const QString dll = QFileInfo(dllPath_->text().trimmed()).absoluteFilePath();
        const QString wine = winePath_->text().trimmed();

        if (!QFileInfo(exe).isFile() || QFileInfo(exe).suffix().compare("exe", Qt::CaseInsensitive) != 0) {
            QMessageBox::warning(this, "Missing executable", "Choose a valid Windows .exe file.");
            return;
        }
        if (!QFileInfo(dll).isFile() || QFileInfo(dll).suffix().compare("dll", Qt::CaseInsensitive) != 0) {
            QMessageBox::warning(this, "Missing DLL", "Choose a valid .dll file.");
            return;
        }
        if (wine.isEmpty() || !QFileInfo(wine).isExecutable()) {
            QMessageBox::warning(this, "Wine not found", "Enter the path to an executable Wine binary.");
            return;
        }

        const QString exeArch = architectureOf(exe);
        const QString dllArch = architectureOf(dll);
        if (exeArch != dllArch || (exeArch != "x86" && exeArch != "x86-64" && exeArch != "ARM64")) {
            QMessageBox::warning(this, "Architecture check failed",
                                 QString("The PE architectures must match.\nEXE: %1\nDLL: %2").arg(exeArch, dllArch));
            return;
        }

        const QString module = QFileInfo(dll).completeBaseName();
        if (module.isEmpty() || module.contains(QRegularExpression("[^A-Za-z0-9_.-]"))) {
            QMessageBox::warning(this, "Invalid DLL name", "The DLL module name contains unsupported characters.");
            return;
        }

        if (process_ && process_->state() != QProcess::NotRunning) {
            QMessageBox::information(this, "Already running", "WineBridge is already managing a launched process.");
            return;
        }

        auto *process = new QProcess(this);
        process_ = process;
        QProcessEnvironment env = QProcessEnvironment::systemEnvironment();
        const QString dllDir = QFileInfo(dll).absolutePath();
        const QString oldDllPath = env.value("WINEDLLPATH");
        env.insert("WINEDLLPATH", oldDllPath.isEmpty() ? dllDir : dllDir + QDir::listSeparator() + oldDllPath);
        env.insert("WINEDLLOVERRIDES", module + "=n,b");
        process->setProcessEnvironment(env);
        process->setWorkingDirectory(QFileInfo(exe).absolutePath());

        connect(process, &QProcess::readyReadStandardOutput, this, [this, process] {
            const QString output = QString::fromLocal8Bit(process->readAllStandardOutput()).trimmed();
            if (!output.isEmpty()) appendLog(output);
        });
        connect(process, &QProcess::readyReadStandardError, this, [this, process] {
            const QString output = QString::fromLocal8Bit(process->readAllStandardError()).trimmed();
            if (!output.isEmpty()) appendLog("Wine: " + output);
        });
        connect(process, &QProcess::started, this, [this, module] {
            appendLog("Wine process started with native override: " + module + "=n,b");
        });
        connect(process, qOverload<int, QProcess::ExitStatus>(&QProcess::finished), this,
                [this](int code, QProcess::ExitStatus status) {
            appendLog(QString("Wine process finished (code %1, %2).")
                      .arg(code).arg(status == QProcess::NormalExit ? "normal" : "crashed"));
            refreshProcesses();
        });
        connect(process, &QProcess::errorOccurred, this, [this, process](QProcess::ProcessError error) {
            appendLog("Process error: " + process->errorString() + QString(" (type %1)").arg(error));
        });

        appendLog("Launching " + exe);
        appendLog("DLL search directory: " + dllDir);
        process->start(wine, {exe});
    }

    QLineEdit *exePath_ = nullptr;
    QLineEdit *dllPath_ = nullptr;
    QLineEdit *winePath_ = nullptr;
    QLineEdit *pluginDirectory_ = nullptr;
    QLabel *exeArch_ = nullptr;
    QLabel *dllArch_ = nullptr;
    QTableWidget *processTable_ = nullptr;
    QPlainTextEdit *log_ = nullptr;
    QProcess *process_ = nullptr;
};
} // namespace

int main(int argc, char *argv[])
{
    QApplication app(argc, argv);
    app.setApplicationName("WineBridge");
    app.setStyleSheet(R"(
        QWidget { background: #11151d; color: #e6eaf2; font-family: "Inter", "Noto Sans", sans-serif; font-size: 13px; }
        QLabel#heading { color: #f4f6fb; font-size: 27px; font-weight: 800; letter-spacing: 2px; }
        QLabel#subtitle { color: #8d98aa; font-size: 13px; margin-top: -10px; }
        QLabel#sectionLabel { color: #91a0b7; font-size: 10px; font-weight: 700; letter-spacing: 1.4px; }
        QLabel#footer { color: #69768a; font-size: 11px; padding-top: 5px; }
        QLineEdit, QPlainTextEdit, QTableWidget { background: #191f2b; border: 1px solid #2b3546; border-radius: 8px; padding: 9px; selection-background-color: #375c9b; }
        QLineEdit:focus { border: 1px solid #648fe0; }
        QPushButton { background: #252e3d; border: 1px solid #354258; border-radius: 8px; padding: 9px 15px; font-weight: 600; }
        QPushButton:hover { background: #303d51; }
        QPushButton#primary { background: #386bd0; border: 1px solid #4d7fe0; color: white; padding-left: 20px; padding-right: 20px; }
        QPushButton#primary:hover { background: #477be2; }
        QHeaderView::section { background: #202938; color: #9ba9be; border: none; padding: 8px; font-weight: 700; }
        QTableWidget { gridline-color: #273142; }
        QTableWidget::item { padding: 5px; }
        QScrollBar:vertical { background: #151b25; width: 10px; margin: 0; }
        QScrollBar::handle:vertical { background: #39465b; border-radius: 5px; min-height: 25px; }
    )");

    MainWindow window;
    window.show();
    return app.exec();
}
