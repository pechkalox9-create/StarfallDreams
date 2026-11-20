#include <QApplication>
#include <QFileDialog>
#include <QFileInfo>
#include <QLabel>
#include <QMainWindow>
#include <QPushButton>
#include <QProcess>
#include <QTextEdit>
#include <QVBoxLayout>
#include <QHBoxLayout>
#include <QWidget>
#include <QDateTime>

class MainWindow final : public QMainWindow {
    Q_OBJECT
public:
    MainWindow() {
        setWindowTitle("StarfallDreams — DLL Test Console");
        resize(760, 520);
        auto *root = new QWidget;
        auto *layout = new QVBoxLayout(root);
        layout->setContentsMargins(24, 24, 24, 24);
        layout->setSpacing(14);
        auto *heading = new QLabel("STARFALL DREAMS");
        heading->setStyleSheet("font-size:22px;font-weight:700;letter-spacing:3px;color:#e8eaf6;");
        auto *sub = new QLabel("Wine-compatible DLL test environment");
        sub->setStyleSheet("color:#8d95ad;font-size:12px;");
        layout->addWidget(heading);
        layout->addWidget(sub);
        auto *section = new QLabel("TEST MODULE");
        section->setStyleSheet("color:#9ca7c5;font-weight:600;margin-top:16px;");
        layout->addWidget(section);
        auto *row = new QHBoxLayout;
        dllPath = new QLabel("No DLL selected");
        dllPath->setStyleSheet("background:#171b2a;border:1px solid #303852;border-radius:7px;padding:12px;color:#b8c1d9;");
        auto *browse = new QPushButton("Browse DLL");
        connect(browse, &QPushButton::clicked, this, &MainWindow::chooseDll);
        row->addWidget(dllPath, 1);
        row->addWidget(browse);
        layout->addLayout(row);
        auto *launch = new QPushButton("Launch isolated test host");
        launch->setMinimumHeight(42);
        launch->setStyleSheet("QPushButton{background:#6758d8;color:white;border:0;border-radius:7px;font-weight:600;} QPushButton:hover{background:#786be6;}");
        connect(launch, &QPushButton::clicked, this, &MainWindow::launchHost);
        layout->addWidget(launch);
        auto *note = new QLabel("The DLL is loaded only by the companion test host. This tool does not inject into unrelated processes.");
        note->setWordWrap(true);
        note->setStyleSheet("color:#d5b879;background:#292516;border:1px solid #5a4924;border-radius:6px;padding:10px;");
        layout->addWidget(note);
        layout->addWidget(new QLabel("ACTIVITY LOG"));
        log = new QTextEdit;
        log->setReadOnly(true);
        layout->addWidget(log, 1);
        setCentralWidget(root);
        setStyleSheet("QMainWindow,QWidget{background:#10131e;color:#e8eaf6;font-family:'Segoe UI',sans-serif;} QPushButton{background:#252b40;border:1px solid #3a4564;border-radius:7px;padding:10px 16px;color:#e8eaf6;} QPushButton:hover{background:#303852;} QTextEdit{background:#0b0e16;border:1px solid #303852;border-radius:7px;color:#a9e6c2;padding:8px;}");
        appendLog("Application ready.");
    }
private slots:
    void chooseDll() {
        const auto path = QFileDialog::getOpenFileName(this, "Select DLL", {}, "Windows libraries (*.dll);;All files (*)");
        if (path.isEmpty()) return;
        dll = path;
        dllPath->setText(QFileInfo(path).fileName());
        dllPath->setToolTip(path);
        appendLog("Selected: " + path);
    }
    void launchHost() {
        if (dll.isEmpty()) { appendLog("ERROR: select a DLL first."); return; }
#ifdef Q_OS_WIN
        const QString host = QCoreApplication::applicationDirPath() + "/DllTestHost.exe";
#else
        const QString host = QCoreApplication::applicationDirPath() + "/DllTestHost";
#endif
        if (!QFileInfo::exists(host)) { appendLog("ERROR: DllTestHost executable not found: " + host); return; }
        auto *process = new QProcess(this);
        connect(process, &QProcess::readyReadStandardOutput, this, [this, process] { appendLog(QString::fromLocal8Bit(process->readAllStandardOutput()).trimmed()); });
        connect(process, &QProcess::readyReadStandardError, this, [this, process] { appendLog(QString::fromLocal8Bit(process->readAllStandardError()).trimmed()); });
        connect(process, &QProcess::finished, this, [this, process](int code) { appendLog(QString("Test host exited: %1").arg(code)); process->deleteLater(); });
        process->start(host, {dll});
        if (!process->waitForStarted(3000)) appendLog("ERROR: could not start test host.");
        else appendLog("Started isolated test host.");
    }
private:
    void appendLog(const QString &message) { log->append("[" + QDateTime::currentDateTime().toString("HH:mm:ss") + "] " + message.toHtmlEscaped()); }
    QLabel *dllPath{};
    QTextEdit *log{};
    QString dll;
};
int main(int argc, char *argv[]) {
    QApplication app(argc, argv);
    MainWindow window;
    window.show();
    return app.exec();
}
#include "main.moc"
