#pragma once

#include <QAbstractListModel>
#include <QString>
#include <QVector>
#include "DetailedReportModel.h"

struct IncidentMarker {
    QString reportId;
    QString issueTitle;
    QString location;
    int wardNumber;
    QString wardName;
    QString acName;
    qreal latitude;
    qreal longitude;
    QString status;      // "CRITICAL", "IN PROGRESS", "RESOLVED", "PENDING"
    QString category;    // "Infrastructure", "Sanitation", "Public Safety", "Environment"
    QString date;
    QString citizenName;
    QString citizenContact;
    QString assignedTo;
    QString description;
};

class IncidentMarkerModel : public QAbstractListModel {
    Q_OBJECT

public:
    enum MarkerRoles {
        ReportIdRole = Qt::UserRole + 1,
        IssueTitleRole,
        LocationRole,
        WardNumberRole,
        WardNameRole,
        AcNameRole,
        LatitudeRole,
        LongitudeRole,
        StatusRole,
        CategoryRole,
        DateRole,
        CitizenNameRole,
        CitizenContactRole,
        AssignedToRole,
        DescriptionRole
    };

    explicit IncidentMarkerModel(QObject *parent = nullptr);

    int rowCount(const QModelIndex &parent = QModelIndex()) const override;
    QVariant data(const QModelIndex &index, int role = Qt::DisplayRole) const override;
    QHash<int, QByteArray> roleNames() const override;

    void filter(const QString &constituency, int wardNumber, const QString &status, const QString &category);
    QVariantMap getIncidentById(const QString &reportId) const;
    void updateStatus(const QString &reportId, const QString &newStatus);
    void assignOfficer(const QString &reportId, const QString &officerName);
    void updateFromCloud(const QVector<DetailedReportItem> &reports);

private:
    QVector<IncidentMarker> m_allIncidents;
    QVector<IncidentMarker> m_displayedIncidents;
    void loadSampleIncidents();
};
