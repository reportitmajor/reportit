#include <QGuiApplication>
#include <QQmlApplicationEngine>
#include <QQmlContext>
#include <QQuickStyle>
#include <QFont>
#include <QFile>
#include <QTextStream>
#include <QDateTime>
#include <QIcon>
#include <iostream>
#include "controllers/DashboardController.h"
#include "controllers/NavigationController.h"
#include "controllers/MapController.h"
#include "controllers/ReportManagementController.h"
#include "controllers/AnalyticsController.h"
#include "controllers/SettingsController.h"

void customLogMessageHandler(QtMsgType type, const QMessageLogContext &context, const QString &msg)
{
    Q_UNUSED(context);
    QString logLine = QString("[%1] %2\n").arg(QDateTime::currentDateTime().toString("yyyy-MM-dd hh:mm:ss.zzz"), msg);
    QFile logFile("qml_debug_log.txt");
    if (logFile.open(QIODevice::WriteOnly | QIODevice::Append | QIODevice::Text)) {
        QTextStream stream(&logFile);
        stream << logLine;
    }
    std::cerr << msg.toStdString() << std::endl;
}

int main(int argc, char *argv[])
{
    qInstallMessageHandler(customLogMessageHandler);

    QGuiApplication app(argc, argv);

    app.setApplicationName("ReportIt Admin");
    app.setOrganizationName("CityOversight");
    app.setApplicationDisplayName("ReportIt Admin - City Oversight");
    app.setWindowIcon(QIcon(":/qt/qml/ReportIt/Admin/resources/images/logo.png"));

    QFont defaultFont("Segoe UI", 10);
    defaultFont.setStyleHint(QFont::SansSerif);
    app.setFont(defaultFont);

    QQuickStyle::setStyle("Basic");

    QQmlApplicationEngine engine;

    DashboardController dashboardController;
    NavigationController navigationController;
    MapController mapController;
    ReportManagementController reportController;
    AnalyticsController analyticsController;
    SettingsController settingsController;

    // Connect mapController to live bidirectional Firestore sync
    mapController.setSyncService(reportController.syncService());

    engine.rootContext()->setContextProperty("dashboardController", &dashboardController);
    engine.rootContext()->setContextProperty("navigationController", &navigationController);
    engine.rootContext()->setContextProperty("mapController", &mapController);
    engine.rootContext()->setContextProperty("reportController", &reportController);
    engine.rootContext()->setContextProperty("analyticsController", &analyticsController);
    engine.rootContext()->setContextProperty("settingsController", &settingsController);
    engine.rootContext()->setContextProperty("reportModel", dashboardController.reportModel());
    engine.rootContext()->setContextProperty("categoryModel", dashboardController.categoryModel());
    engine.rootContext()->setContextProperty("wardModel", mapController.wardModel());
    engine.rootContext()->setContextProperty("incidentModel", mapController.incidentModel());
    engine.rootContext()->setContextProperty("detailedReportModel", reportController.reportModel());

    QObject::connect(reportController.syncService(), &FirestoreSyncService::reportsReceived,
                     &dashboardController, &DashboardController::onCloudReportsReceived);
    QObject::connect(reportController.syncService(), &FirestoreSyncService::reportsReceived,
                     &mapController, &MapController::onCloudReportsReceived);

    const QUrl url(QStringLiteral("qrc:/qt/qml/ReportIt/Admin/qml/Main.qml"));
    QObject::connect(
        &engine,
        &QQmlApplicationEngine::objectCreated,
        &app,
        [url](QObject *obj, const QUrl &objUrl) {
            if (!obj && url == objUrl) {
                qCritical() << "Failed to create root QML object for URL:" << objUrl;
                QCoreApplication::exit(-1);
            } else if (obj && url == objUrl) {
                qInfo() << "Root QML object created successfully:" << obj;
            }
        },
        Qt::QueuedConnection);

    QObject::connect(&app, &QGuiApplication::aboutToQuit, []() {
        qInfo() << "QGuiApplication::aboutToQuit triggered!";
    });

    qInfo() << "Loading QML engine URL:" << url;
    engine.load(url);

    if (engine.rootObjects().isEmpty()) {
        qCritical() << "Engine rootObjects is empty after load!";
    }

    qInfo() << "Starting app.exec()...";
    int result = app.exec();
    qInfo() << "app.exec() returned:" << result;
    return result;
}
