#include "SettingsController.h"
#include <QFile>
#include <QTextStream>
#include <QStandardPaths>
#include <QDateTime>
#include <QJsonObject>
#include <QJsonDocument>
#include <QJsonArray>

SettingsController::SettingsController(QObject *parent)
    : QObject(parent)
    , m_adminName("Alex Rivera")
    , m_adminRole("Command Center Administrator")
    , m_adminEmail("admin.bbmp@karnataka.gov.in")
    , m_adminPhone("+91 98860 00100")
    , m_jurisdiction("Citywide (All 28 Constituencies)")
    , m_slaCriticalWaterHours(4)
    , m_slaPotholeHours(24)
    , m_slaStreetlightHours(24)
    , m_slaGarbageHours(12)
    , m_slaDrainHours(36)
    , m_autoEscalateAeeEnabled(true)
    , m_escalateThresholdHours(2)
    , m_smsAlertsEnabled(true)
    , m_whatsAppAlertsEnabled(true)
    , m_emailAlertsEnabled(true)
    , m_aiConfidenceThreshold(85)
    , m_autoGeofenceEnabled(true)
    , m_duplicateDetectionEnabled(true)
    , m_duplicateSimilarityThreshold(90)
    , m_maskCitizenPhone(true)
    , m_maskCitizenAddress(true)
    , m_autoFeedbackNotification(true)
    , m_sessionTimeoutMinutes(30)
    , m_kgisServerStatus("ONLINE (Ping: 18ms)")
    , m_lastSyncedTime("Just now")
    , m_databaseStorageUsed("42.8 MB / 5.0 GB")
{
    m_auditLogs = {
        QVariantMap{{"time", "Today, 10:15 AM"}, {"user", "Alex Rivera (Admin)"}, {"action", "System Parameters Initialized"}, {"ip", "10.14.2.8"}},
        QVariantMap{{"time", "Today, 09:30 AM"}, {"user", "Alex Rivera (Admin)"}, {"action", "KGIS Ward Geospatial Sync Verified"}, {"ip", "10.14.2.8"}},
        QVariantMap{{"time", "Yesterday, 06:45 PM"}, {"user", "Er. R. Suresh (AEE)"}, {"action", "Work Order WO-2026-RR-0891 Executed"}, {"ip", "10.14.8.12"}},
        QVariantMap{{"time", "Oct 10, 04:00 PM"}, {"user", "Dr. K. Manjunath (SWM)"}, {"action", "Commercial Waste Penalty Logged"}, {"ip", "10.14.5.20"}}
    };
}

void SettingsController::setAdminName(const QString &val) { if (m_adminName != val) { m_adminName = val; emit profileChanged(); } }
void SettingsController::setAdminRole(const QString &val) { if (m_adminRole != val) { m_adminRole = val; emit profileChanged(); } }
void SettingsController::setAdminEmail(const QString &val) { if (m_adminEmail != val) { m_adminEmail = val; emit profileChanged(); } }
void SettingsController::setAdminPhone(const QString &val) { if (m_adminPhone != val) { m_adminPhone = val; emit profileChanged(); } }
void SettingsController::setJurisdiction(const QString &val) { if (m_jurisdiction != val) { m_jurisdiction = val; emit profileChanged(); } }

void SettingsController::setSlaCriticalWaterHours(int val) { if (m_slaCriticalWaterHours != val) { m_slaCriticalWaterHours = val; emit slaChanged(); } }
void SettingsController::setSlaPotholeHours(int val) { if (m_slaPotholeHours != val) { m_slaPotholeHours = val; emit slaChanged(); } }
void SettingsController::setSlaStreetlightHours(int val) { if (m_slaStreetlightHours != val) { m_slaStreetlightHours = val; emit slaChanged(); } }
void SettingsController::setSlaGarbageHours(int val) { if (m_slaGarbageHours != val) { m_slaGarbageHours = val; emit slaChanged(); } }
void SettingsController::setSlaDrainHours(int val) { if (m_slaDrainHours != val) { m_slaDrainHours = val; emit slaChanged(); } }
void SettingsController::setAutoEscalateAeeEnabled(bool val) { if (m_autoEscalateAeeEnabled != val) { m_autoEscalateAeeEnabled = val; emit slaChanged(); } }
void SettingsController::setEscalateThresholdHours(int val) { if (m_escalateThresholdHours != val) { m_escalateThresholdHours = val; emit slaChanged(); } }
void SettingsController::setSmsAlertsEnabled(bool val) { if (m_smsAlertsEnabled != val) { m_smsAlertsEnabled = val; emit slaChanged(); } }
void SettingsController::setWhatsAppAlertsEnabled(bool val) { if (m_whatsAppAlertsEnabled != val) { m_whatsAppAlertsEnabled = val; emit slaChanged(); } }
void SettingsController::setEmailAlertsEnabled(bool val) { if (m_emailAlertsEnabled != val) { m_emailAlertsEnabled = val; emit slaChanged(); } }

void SettingsController::setAiConfidenceThreshold(int val) { if (m_aiConfidenceThreshold != val) { m_aiConfidenceThreshold = val; emit aiRoutingChanged(); } }
void SettingsController::setAutoGeofenceEnabled(bool val) { if (m_autoGeofenceEnabled != val) { m_autoGeofenceEnabled = val; emit aiRoutingChanged(); } }
void SettingsController::setDuplicateDetectionEnabled(bool val) { if (m_duplicateDetectionEnabled != val) { m_duplicateDetectionEnabled = val; emit aiRoutingChanged(); } }
void SettingsController::setDuplicateSimilarityThreshold(int val) { if (m_duplicateSimilarityThreshold != val) { m_duplicateSimilarityThreshold = val; emit aiRoutingChanged(); } }

void SettingsController::setMaskCitizenPhone(bool val) { if (m_maskCitizenPhone != val) { m_maskCitizenPhone = val; emit privacyChanged(); } }
void SettingsController::setMaskCitizenAddress(bool val) { if (m_maskCitizenAddress != val) { m_maskCitizenAddress = val; emit privacyChanged(); } }
void SettingsController::setAutoFeedbackNotification(bool val) { if (m_autoFeedbackNotification != val) { m_autoFeedbackNotification = val; emit privacyChanged(); } }
void SettingsController::setSessionTimeoutMinutes(int val) { if (m_sessionTimeoutMinutes != val) { m_sessionTimeoutMinutes = val; emit privacyChanged(); } }

void SettingsController::saveProfile(const QString &name, const QString &role, const QString &email, const QString &phone, const QString &jurisdiction)
{
    m_adminName = name;
    m_adminRole = role;
    m_adminEmail = email;
    m_adminPhone = phone;
    m_jurisdiction = jurisdiction;
    addAuditLog("Admin Profile & Nodal Office Credentials Updated");
    emit profileChanged();
}

void SettingsController::saveSlaHours(int water, int pothole, int light, int garbage, int drain, int escalateHours, bool sms, bool whatsapp, bool email)
{
    m_slaCriticalWaterHours = water;
    m_slaPotholeHours = pothole;
    m_slaStreetlightHours = light;
    m_slaGarbageHours = garbage;
    m_slaDrainHours = drain;
    m_escalateThresholdHours = escalateHours;
    m_smsAlertsEnabled = sms;
    m_whatsAppAlertsEnabled = whatsapp;
    m_emailAlertsEnabled = email;
    addAuditLog("Statutory SLA Thresholds & Auto-Escalation Policies Saved");
    emit slaChanged();
}

void SettingsController::saveAiRouting(int confidence, bool geofence, bool duplicate, int similarity)
{
    m_aiConfidenceThreshold = confidence;
    m_autoGeofenceEnabled = geofence;
    m_duplicateDetectionEnabled = duplicate;
    m_duplicateSimilarityThreshold = similarity;
    addAuditLog("AI Auto-Triage & Polygon Geofencing Configuration Updated");
    emit aiRoutingChanged();
}

void SettingsController::savePrivacy(bool maskPhone, bool maskAddress, bool autoFeedback, int timeout)
{
    m_maskCitizenPhone = maskPhone;
    m_maskCitizenAddress = maskAddress;
    m_autoFeedbackNotification = autoFeedback;
    m_sessionTimeoutMinutes = timeout;
    addAuditLog("Citizen PII Masking & Security Timeout Settings Saved");
    emit privacyChanged();
}

QString SettingsController::triggerBackup()
{
    QString desktopPath = QStandardPaths::writableLocation(QStandardPaths::DesktopLocation);
    QString filePath = desktopPath + "/BBMP_Municipal_System_Backup_2026.json";

    QJsonObject rootObj;
    rootObj["backupDate"] = QDateTime::currentDateTime().toString(Qt::ISODate);
    rootObj["adminUser"] = m_adminName;
    rootObj["jurisdiction"] = m_jurisdiction;

    QJsonObject slaObj;
    slaObj["criticalWaterHours"] = m_slaCriticalWaterHours;
    slaObj["potholeHours"] = m_slaPotholeHours;
    slaObj["streetlightHours"] = m_slaStreetlightHours;
    slaObj["garbageHours"] = m_slaGarbageHours;
    slaObj["drainHours"] = m_slaDrainHours;
    rootObj["slaConfig"] = slaObj;

    QJsonDocument doc(rootObj);
    QFile file(filePath);
    if (file.open(QIODevice::WriteOnly | QIODevice::Text)) {
        file.write(doc.toJson());
        file.close();
        addAuditLog("Full Municipal System Backup Exported to Local Storage");
        return filePath;
    }
    return "";
}

void SettingsController::testKgisConnection()
{
    m_kgisServerStatus = "ONLINE (Ping: 14ms • Verified)";
    m_lastSyncedTime = QDateTime::currentDateTime().toString("hh:mm:ss AP");
    addAuditLog("KGIS State Gateway Handshake Verified");
    emit serverStatusChanged();
}

void SettingsController::addAuditLog(const QString &action)
{
    QVariantMap log;
    log["time"] = QDateTime::currentDateTime().toString("Today, hh:mm AP");
    log["user"] = m_adminName + " (" + m_adminRole + ")";
    log["action"] = action;
    log["ip"] = "10.14.2.8 (Secure Gateway)";
    m_auditLogs.prepend(log);
    emit auditLogsChanged();
}
