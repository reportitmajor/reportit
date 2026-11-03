#include "FirestoreSyncService.h"
#include <QJsonDocument>
#include <QJsonObject>
#include <QJsonArray>
#include <QDateTime>
#include <QUrl>
#include <QUrlQuery>
#include <QNetworkRequest>
#include <QDebug>

const QString FirestoreSyncService::FIRESTORE_BASE_URL = 
    "https://firestore.googleapis.com/v1/projects/reportit-43ba2/databases/(default)/documents/reports";

FirestoreSyncService::FirestoreSyncService(QObject *parent)
    : QObject(parent)
{
    // Auto-poll every 10 seconds for real-time mobile citizen sync
    connect(&m_pollTimer, &QTimer::timeout, this, &FirestoreSyncService::fetchReports);
    m_pollTimer.start(10000);

    // Initial fetch on launch
    QTimer::singleShot(1000, this, &FirestoreSyncService::fetchReports);
}

void FirestoreSyncService::fetchReports()
{
    if (m_isSyncing) return;

    m_isSyncing = true;
    emit syncingChanged();

    QUrl url(FIRESTORE_BASE_URL);
    QNetworkRequest request(url);
    request.setHeader(QNetworkRequest::ContentTypeHeader, "application/json");

    QNetworkReply *reply = m_networkManager.get(request);
    connect(reply, &QNetworkReply::finished, this, [this, reply]() {
        onFetchFinished(reply);
    });
}

void FirestoreSyncService::onFetchFinished(QNetworkReply *reply)
{
    m_isSyncing = false;
    emit syncingChanged();

    reply->deleteLater();

    if (reply->error() != QNetworkReply::NoError) {
        qWarning() << "Firestore Sync Network Error:" << reply->errorString();
        return;
    }

    QByteArray data = reply->readAll();
    QJsonDocument doc = QJsonDocument::fromJson(data);
    if (!doc.isObject()) return;

    QJsonObject root = doc.object();
    QJsonArray documents = root["documents"].toArray();

    QVector<DetailedReportItem> parsedReports;

    auto getStringField = [](const QJsonObject &fields, const QString &key, const QString &def = QString()) -> QString {
        if (!fields.contains(key)) return def;
        return fields[key].toObject()["stringValue"].toString(def);
    };

    auto getDoubleField = [](const QJsonObject &fields, const QString &key, double def = 0.0) -> double {
        if (!fields.contains(key)) return def;
        QJsonObject valObj = fields[key].toObject();
        if (valObj.contains("doubleValue")) return valObj["doubleValue"].toDouble(def);
        if (valObj.contains("integerValue")) return valObj["integerValue"].toString().toDouble();
        return def;
    };

    auto getIntField = [](const QJsonObject &fields, const QString &key, int def = 0) -> int {
        if (!fields.contains(key)) return def;
        QJsonObject valObj = fields[key].toObject();
        if (valObj.contains("integerValue")) return valObj["integerValue"].toString().toInt();
        if (valObj.contains("doubleValue")) return valObj["doubleValue"].toInt(def);
        return def;
    };

    for (const auto &docVal : documents) {
        QJsonObject docObj = docVal.toObject();
        QJsonObject fields = docObj["fields"].toObject();

        QString id = getStringField(fields, "id");
        if (id.isEmpty()) {
            // Extract from doc name
            QString docName = docObj["name"].toString();
            id = docName.section('/', -1);
        }

        DetailedReportItem item;
        item.reportId = id;
        item.issueTitle = getStringField(fields, "title", "Civic Grievance");
        item.category = getStringField(fields, "category", "Infrastructure");
        item.department = getStringField(fields, "department", "BBMP Engineering Wing");
        item.severity = getStringField(fields, "severity", "HIGH");
        item.status = getStringField(fields, "status", "SUBMITTED");
        item.slaHoursLeft = getIntField(fields, "slaRemainingHours", 48);
        item.slaBreached = (item.slaHoursLeft < 0);
        item.slaDeadlineText = QString("%1h Remaining").arg(item.slaHoursLeft);
        item.wardNumber = getIntField(fields, "wardNumber", 150);
        item.wardName = getStringField(fields, "wardName", "Bellandur");
        item.acCode = getStringField(fields, "acCode", "AC-174");
        item.acName = getStringField(fields, "acName", "Mahadevapura");
        item.mlaName = getStringField(fields, "mlaName", "Member of Legislative Assembly");
        item.location = getStringField(fields, "locationAddress", "Bengaluru");
        item.latitude = getDoubleField(fields, "latitude", 12.9260);
        item.longitude = getDoubleField(fields, "longitude", 77.6762);
        item.citizenName = getStringField(fields, "reportedBy", "Citizen Reporter");
        item.citizenPhone = getStringField(fields, "userEmail", "citizen@reportit.app");
        item.description = getStringField(fields, "description", "");
        item.createdDate = getStringField(fields, "submissionDate", "Today");
        item.assignedOfficer = getStringField(fields, "assignedOfficer", "Pending Assignment");
        item.assignedRole = "Zonal Field Officer";
        item.workOrderToken = QString("WO-BLR-%1").arg(qAbs(qHash(id)) % 10000);
        item.resolutionNotes = "";
        
        // Parse creation timestamp
        QString createTimeStr = docObj["createTime"].toString();
        QDateTime createDt = QDateTime::fromString(createTimeStr, Qt::ISODate);
        if (!createDt.isValid()) {
            createDt = QDateTime::fromString(docObj["updateTime"].toString(), Qt::ISODate);
        }
        item.createdAtEpoch = createDt.isValid() ? createDt.toMSecsSinceEpoch() : QDateTime::currentMSecsSinceEpoch();

        // Prioritize Cloudinary / ImgBB CDN URL
        QString imgUrl = getStringField(fields, "imageUrl");
        if (imgUrl.isEmpty()) imgUrl = getStringField(fields, "thumbUrl");
        item.citizenPhotoUrl = imgUrl;

        QString resUrl = getStringField(fields, "resolutionPhotoUrl");
        if (resUrl.isEmpty()) resUrl = getStringField(fields, "resolvedImageUrl");
        item.resolutionPhotoUrl = resUrl;
        item.resolutionNotes = getStringField(fields, "resolutionNotes");

        // Timeline events
        item.timelineEvents.append(QVariantMap{
            {"time", item.createdDate},
            {"action", "Report Submitted via Mobile App"},
            {"actor", item.citizenName},
            {"role", "Citizen Reporter"},
            {"icon", "📱"}
        });

        parsedReports.append(item);
    }

    m_cloudReportsCount = parsedReports.size();
    m_lastSyncTime = QDateTime::currentDateTime().toString("hh:mm:ss AP");
    emit cloudReportsCountChanged();
    emit lastSyncTimeChanged();

    if (!parsedReports.isEmpty()) {
        emit reportsReceived(parsedReports);
    }
}

void FirestoreSyncService::updateStatusOnCloud(const QString &reportId, const QString &newStatus, const QString &assignedOfficer, const QString &resolutionPhotoUrl, const QString &resolutionNotes)
{
    QString query = "updateMask.fieldPaths=status";
    if (!assignedOfficer.isEmpty()) query += "&updateMask.fieldPaths=assignedOfficer";
    if (!resolutionPhotoUrl.isEmpty()) query += "&updateMask.fieldPaths=resolutionPhotoUrl";
    if (!resolutionNotes.isEmpty()) query += "&updateMask.fieldPaths=resolutionNotes";
    query += "&updateMask.fieldPaths=resolvedAt";

    QUrl url(QString("%1/%2?%3").arg(FIRESTORE_BASE_URL, reportId, query));
    QNetworkRequest request(url);
    request.setHeader(QNetworkRequest::ContentTypeHeader, "application/json");

    QJsonObject fieldsObj;
    QJsonObject statusObj;
    statusObj["stringValue"] = newStatus;
    fieldsObj["status"] = statusObj;

    if (!assignedOfficer.isEmpty()) {
        QJsonObject officerObj;
        officerObj["stringValue"] = assignedOfficer;
        fieldsObj["assignedOfficer"] = officerObj;
    }

    if (!resolutionPhotoUrl.isEmpty()) {
        QJsonObject photoObj;
        photoObj["stringValue"] = resolutionPhotoUrl;
        fieldsObj["resolutionPhotoUrl"] = photoObj;
    }

    if (!resolutionNotes.isEmpty()) {
        QJsonObject notesObj;
        notesObj["stringValue"] = resolutionNotes;
        fieldsObj["resolutionNotes"] = notesObj;
    }

    QJsonObject resolvedAtObj;
    resolvedAtObj["stringValue"] = QDateTime::currentDateTime().toString(Qt::ISODate);
    fieldsObj["resolvedAt"] = resolvedAtObj;

    QJsonObject rootObj;
    rootObj["fields"] = fieldsObj;

    QByteArray body = QJsonDocument(rootObj).toJson();

    QNetworkReply *reply = m_networkManager.sendCustomRequest(request, "PATCH", body);
    connect(reply, &QNetworkReply::finished, this, [this, reply, reportId]() {
        onUpdateFinished(reply);
        emit reportStatusUpdated(reportId, reply->error() == QNetworkReply::NoError);
    });
}

void FirestoreSyncService::onUpdateFinished(QNetworkReply *reply)
{
    reply->deleteLater();
    if (reply->error() == QNetworkReply::NoError) {
        qDebug() << "Successfully synchronized report update to Cloud Firestore.";
        QTimer::singleShot(400, this, &FirestoreSyncService::fetchReports);
    } else {
        qWarning() << "Failed to push update to Cloud Firestore:" << reply->errorString();
    }
}
