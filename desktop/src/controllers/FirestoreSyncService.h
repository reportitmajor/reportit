#pragma once

#include <QObject>
#include <QString>
#include <QVector>
#include <QNetworkAccessManager>
#include <QNetworkReply>
#include <QTimer>
#include "../models/DetailedReportModel.h"

class FirestoreSyncService : public QObject {
    Q_OBJECT

    Q_PROPERTY(bool isSyncing READ isSyncing NOTIFY syncingChanged)
    Q_PROPERTY(QString lastSyncTime READ lastSyncTime NOTIFY lastSyncTimeChanged)
    Q_PROPERTY(int cloudReportsCount READ cloudReportsCount NOTIFY cloudReportsCountChanged)

public:
    explicit FirestoreSyncService(QObject *parent = nullptr);

    bool isSyncing() const { return m_isSyncing; }
    QString lastSyncTime() const { return m_lastSyncTime; }
    int cloudReportsCount() const { return m_cloudReportsCount; }

    Q_INVOKABLE void fetchReports();
    Q_INVOKABLE void updateStatusOnCloud(const QString &reportId, const QString &newStatus, const QString &assignedOfficer = QString(), const QString &resolutionPhotoUrl = QString(), const QString &resolutionNotes = QString());

signals:
    void syncingChanged();
    void lastSyncTimeChanged();
    void cloudReportsCountChanged();
    void reportsReceived(const QVector<DetailedReportItem> &reports);
    void reportStatusUpdated(const QString &reportId, bool success);

private slots:
    void onFetchFinished(QNetworkReply *reply);
    void onUpdateFinished(QNetworkReply *reply);

private:
    QNetworkAccessManager m_networkManager;
    QTimer m_pollTimer;
    bool m_isSyncing = false;
    QString m_lastSyncTime = "Never";
    int m_cloudReportsCount = 0;

    static const QString FIRESTORE_BASE_URL;
};
