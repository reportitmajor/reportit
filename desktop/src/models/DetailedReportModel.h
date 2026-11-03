#pragma once

#include <QAbstractListModel>
#include <QString>
#include <QVector>
#include <QVariantList>
#include <QVariantMap>

struct DetailedReportItem {
    QString reportId;
    QString issueTitle;
    QString category;         // "Infrastructure", "Sanitation", "Water Supply", "Public Safety", "Environment", "Electrical"
    QString department;       // "BBMP Engineering Wing", "Solid Waste Management", "BWSSB", "BESCOM", "Traffic Police Liaison", "Forest Wing"
    QString severity;         // "CRITICAL", "HIGH", "MEDIUM", "LOW"
    QString status;           // "PENDING", "IN PROGRESS", "RESOLVED", "REJECTED"
    int slaHoursLeft;         // E.g. 3, -5 (negative if breached)
    bool slaBreached;
    QString slaDeadlineText;  // E.g. "Today, 02:00 PM (4h Critical SLA)"
    int wardNumber;
    QString wardName;
    QString acCode;
    QString acName;
    QString mlaName;
    QString location;
    qreal latitude;
    qreal longitude;
    QString citizenName;
    QString citizenPhone;
    QString description;
    QString createdDate;
    QString assignedOfficer;
    QString assignedRole;
    QString workOrderToken;
    QString resolutionNotes;
    QString citizenPhotoUrl;
    QString resolutionPhotoUrl;
    QVariantList timelineEvents; // List of {timestamp, action, actor, role, status} maps
    qint64 createdAtEpoch = 0;
};

class DetailedReportModel : public QAbstractListModel {
    Q_OBJECT

public:
    enum ReportRoles {
        ReportIdRole = Qt::UserRole + 1,
        IssueTitleRole,
        CategoryRole,
        DepartmentRole,
        SeverityRole,
        StatusRole,
        SlaHoursLeftRole,
        SlaBreachedRole,
        SlaDeadlineTextRole,
        WardNumberRole,
        WardNameRole,
        AcCodeRole,
        AcNameRole,
        MlaNameRole,
        LocationRole,
        LatitudeRole,
        LongitudeRole,
        CitizenNameRole,
        CitizenPhoneRole,
        DescriptionRole,
        CreatedDateRole,
        AssignedOfficerRole,
        AssignedRoleRole,
        WorkOrderTokenRole,
        ResolutionNotesRole,
        CitizenPhotoUrlRole,
        ResolutionPhotoUrlRole,
        TimelineEventsRole
    };

    explicit DetailedReportModel(QObject *parent = nullptr);

    int rowCount(const QModelIndex &parent = QModelIndex()) const override;
    QVariant data(const QModelIndex &index, int role = Qt::DisplayRole) const override;
    QHash<int, QByteArray> roleNames() const override;

    void filter(const QString &searchQuery, const QString &statusTab, const QString &category, const QString &constituency, int wardNumber);
    void sort(const QString &sortKey);

    QVariantMap getReportById(const QString &reportId) const;
    void updateStatus(const QString &reportId, const QString &newStatus);
    void assignWorkOrder(const QString &reportId, const QString &officerName, const QString &role, const QString &workOrderToken, const QString &notes);
    void resolveGrievance(const QString &reportId, const QString &resolutionNotes, const QString &resolutionPhotoUrl = QString());
    void mergeCloudReports(const QVector<DetailedReportItem> &cloudReports);

    int totalCount() const { return m_allReports.size(); }
    int needsActionCount() const;
    int inProgressCount() const;
    int resolvedCount() const;

private:
    QVector<DetailedReportItem> m_allReports;
    QVector<DetailedReportItem> m_displayedReports;
    QString m_currentSortKey;

    void loadMasterCivicGrievances();
    void applyCurrentSort();
};
