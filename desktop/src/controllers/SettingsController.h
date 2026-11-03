#pragma once

#include <QObject>
#include <QString>
#include <QVariantList>
#include <QVariantMap>

class SettingsController : public QObject {
    Q_OBJECT

    // Profile Settings
    Q_PROPERTY(QString adminName READ adminName WRITE setAdminName NOTIFY profileChanged)
    Q_PROPERTY(QString adminRole READ adminRole WRITE setAdminRole NOTIFY profileChanged)
    Q_PROPERTY(QString adminEmail READ adminEmail WRITE setAdminEmail NOTIFY profileChanged)
    Q_PROPERTY(QString adminPhone READ adminPhone WRITE setAdminPhone NOTIFY profileChanged)
    Q_PROPERTY(QString jurisdiction READ jurisdiction WRITE setJurisdiction NOTIFY profileChanged)

    // SLA Config
    Q_PROPERTY(int slaCriticalWaterHours READ slaCriticalWaterHours WRITE setSlaCriticalWaterHours NOTIFY slaChanged)
    Q_PROPERTY(int slaPotholeHours READ slaPotholeHours WRITE setSlaPotholeHours NOTIFY slaChanged)
    Q_PROPERTY(int slaStreetlightHours READ slaStreetlightHours WRITE setSlaStreetlightHours NOTIFY slaChanged)
    Q_PROPERTY(int slaGarbageHours READ slaGarbageHours WRITE setSlaGarbageHours NOTIFY slaChanged)
    Q_PROPERTY(int slaDrainHours READ slaDrainHours WRITE setSlaDrainHours NOTIFY slaChanged)
    Q_PROPERTY(bool autoEscalateAeeEnabled READ autoEscalateAeeEnabled WRITE setAutoEscalateAeeEnabled NOTIFY slaChanged)
    Q_PROPERTY(int escalateThresholdHours READ escalateThresholdHours WRITE setEscalateThresholdHours NOTIFY slaChanged)
    Q_PROPERTY(bool smsAlertsEnabled READ smsAlertsEnabled WRITE setSmsAlertsEnabled NOTIFY slaChanged)
    Q_PROPERTY(bool whatsAppAlertsEnabled READ whatsAppAlertsEnabled WRITE setWhatsAppAlertsEnabled NOTIFY slaChanged)
    Q_PROPERTY(bool emailAlertsEnabled READ emailAlertsEnabled WRITE setEmailAlertsEnabled NOTIFY slaChanged)

    // AI Auto-Triage & Routing
    Q_PROPERTY(int aiConfidenceThreshold READ aiConfidenceThreshold WRITE setAiConfidenceThreshold NOTIFY aiRoutingChanged)
    Q_PROPERTY(bool autoGeofenceEnabled READ autoGeofenceEnabled WRITE setAutoGeofenceEnabled NOTIFY aiRoutingChanged)
    Q_PROPERTY(bool duplicateDetectionEnabled READ duplicateDetectionEnabled WRITE setDuplicateDetectionEnabled NOTIFY aiRoutingChanged)
    Q_PROPERTY(int duplicateSimilarityThreshold READ duplicateSimilarityThreshold WRITE setDuplicateSimilarityThreshold NOTIFY aiRoutingChanged)

    // Privacy & Security
    Q_PROPERTY(bool maskCitizenPhone READ maskCitizenPhone WRITE setMaskCitizenPhone NOTIFY privacyChanged)
    Q_PROPERTY(bool maskCitizenAddress READ maskCitizenAddress WRITE setMaskCitizenAddress NOTIFY privacyChanged)
    Q_PROPERTY(bool autoFeedbackNotification READ autoFeedbackNotification WRITE setAutoFeedbackNotification NOTIFY privacyChanged)
    Q_PROPERTY(int sessionTimeoutMinutes READ sessionTimeoutMinutes WRITE setSessionTimeoutMinutes NOTIFY privacyChanged)

    // Server & Cloud Sync
    Q_PROPERTY(QString kgisServerStatus READ kgisServerStatus NOTIFY serverStatusChanged)
    Q_PROPERTY(QString lastSyncedTime READ lastSyncedTime NOTIFY serverStatusChanged)
    Q_PROPERTY(QString databaseStorageUsed READ databaseStorageUsed NOTIFY serverStatusChanged)
    Q_PROPERTY(QVariantList auditLogs READ auditLogs NOTIFY auditLogsChanged)

public:
    explicit SettingsController(QObject *parent = nullptr);

    // Profile Getters & Setters
    QString adminName() const { return m_adminName; }
    void setAdminName(const QString &val);
    QString adminRole() const { return m_adminRole; }
    void setAdminRole(const QString &val);
    QString adminEmail() const { return m_adminEmail; }
    void setAdminEmail(const QString &val);
    QString adminPhone() const { return m_adminPhone; }
    void setAdminPhone(const QString &val);
    QString jurisdiction() const { return m_jurisdiction; }
    void setJurisdiction(const QString &val);

    // SLA Getters & Setters
    int slaCriticalWaterHours() const { return m_slaCriticalWaterHours; }
    void setSlaCriticalWaterHours(int val);
    int slaPotholeHours() const { return m_slaPotholeHours; }
    void setSlaPotholeHours(int val);
    int slaStreetlightHours() const { return m_slaStreetlightHours; }
    void setSlaStreetlightHours(int val);
    int slaGarbageHours() const { return m_slaGarbageHours; }
    void setSlaGarbageHours(int val);
    int slaDrainHours() const { return m_slaDrainHours; }
    void setSlaDrainHours(int val);
    bool autoEscalateAeeEnabled() const { return m_autoEscalateAeeEnabled; }
    void setAutoEscalateAeeEnabled(bool val);
    int escalateThresholdHours() const { return m_escalateThresholdHours; }
    void setEscalateThresholdHours(int val);
    bool smsAlertsEnabled() const { return m_smsAlertsEnabled; }
    void setSmsAlertsEnabled(bool val);
    bool whatsAppAlertsEnabled() const { return m_whatsAppAlertsEnabled; }
    void setWhatsAppAlertsEnabled(bool val);
    bool emailAlertsEnabled() const { return m_emailAlertsEnabled; }
    void setEmailAlertsEnabled(bool val);

    // AI Getters & Setters
    int aiConfidenceThreshold() const { return m_aiConfidenceThreshold; }
    void setAiConfidenceThreshold(int val);
    bool autoGeofenceEnabled() const { return m_autoGeofenceEnabled; }
    void setAutoGeofenceEnabled(bool val);
    bool duplicateDetectionEnabled() const { return m_duplicateDetectionEnabled; }
    void setDuplicateDetectionEnabled(bool val);
    int duplicateSimilarityThreshold() const { return m_duplicateSimilarityThreshold; }
    void setDuplicateSimilarityThreshold(int val);

    // Privacy Getters & Setters
    bool maskCitizenPhone() const { return m_maskCitizenPhone; }
    void setMaskCitizenPhone(bool val);
    bool maskCitizenAddress() const { return m_maskCitizenAddress; }
    void setMaskCitizenAddress(bool val);
    bool autoFeedbackNotification() const { return m_autoFeedbackNotification; }
    void setAutoFeedbackNotification(bool val);
    int sessionTimeoutMinutes() const { return m_sessionTimeoutMinutes; }
    void setSessionTimeoutMinutes(int val);

    // Server & Audit Getters
    QString kgisServerStatus() const { return m_kgisServerStatus; }
    QString lastSyncedTime() const { return m_lastSyncedTime; }
    QString databaseStorageUsed() const { return m_databaseStorageUsed; }
    QVariantList auditLogs() const { return m_auditLogs; }

    Q_INVOKABLE void saveProfile(const QString &name, const QString &role, const QString &email, const QString &phone, const QString &jurisdiction);
    Q_INVOKABLE void saveSlaHours(int water, int pothole, int light, int garbage, int drain, int escalateHours, bool sms, bool whatsapp, bool email);
    Q_INVOKABLE void saveAiRouting(int confidence, bool geofence, bool duplicate, int similarity);
    Q_INVOKABLE void savePrivacy(bool maskPhone, bool maskAddress, bool autoFeedback, int timeout);
    Q_INVOKABLE QString triggerBackup();
    Q_INVOKABLE void testKgisConnection();

signals:
    void profileChanged();
    void slaChanged();
    void aiRoutingChanged();
    void privacyChanged();
    void serverStatusChanged();
    void auditLogsChanged();

private:
    QString m_adminName;
    QString m_adminRole;
    QString m_adminEmail;
    QString m_adminPhone;
    QString m_jurisdiction;

    int m_slaCriticalWaterHours;
    int m_slaPotholeHours;
    int m_slaStreetlightHours;
    int m_slaGarbageHours;
    int m_slaDrainHours;
    bool m_autoEscalateAeeEnabled;
    int m_escalateThresholdHours;
    bool m_smsAlertsEnabled;
    bool m_whatsAppAlertsEnabled;
    bool m_emailAlertsEnabled;

    int m_aiConfidenceThreshold;
    bool m_autoGeofenceEnabled;
    bool m_duplicateDetectionEnabled;
    int m_duplicateSimilarityThreshold;

    bool m_maskCitizenPhone;
    bool m_maskCitizenAddress;
    bool m_autoFeedbackNotification;
    int m_sessionTimeoutMinutes;

    QString m_kgisServerStatus;
    QString m_lastSyncedTime;
    QString m_databaseStorageUsed;
    QVariantList m_auditLogs;

    void addAuditLog(const QString &action);
};
