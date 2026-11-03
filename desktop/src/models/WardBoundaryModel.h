#pragma once

#include <QAbstractListModel>
#include <QString>
#include <QVector>
#include <QVariantList>
#include <QVariantMap>

struct WardFeature {
    int wardNumber;
    QString wardName;
    QString acCode;
    QString acName;
    QString mlaName;
    QString gbaZone;
    QString aeeName;
    QString aeeContact;
    QString healthInspector;
    int totalComplaints;
    int criticalCount;
    int inProgressCount;
    int resolvedCount;
    qreal resolutionRate;
    QString color;
    QVariantList polygonCoordinates; // List of {lat, lng} maps
    qreal centroidLat;
    qreal centroidLng;
    qreal minLat;
    qreal maxLat;
    qreal minLng;
    qreal maxLng;
};

class WardBoundaryModel : public QAbstractListModel {
    Q_OBJECT

public:
    enum WardRoles {
        WardNumberRole = Qt::UserRole + 1,
        WardNameRole,
        AcCodeRole,
        AcNameRole,
        MlaNameRole,
        GbaZoneRole,
        AeeNameRole,
        AeeContactRole,
        HealthInspectorRole,
        TotalComplaintsRole,
        CriticalCountRole,
        InProgressCountRole,
        ResolvedCountRole,
        ResolutionRateRole,
        ColorRole,
        CoordinatesRole,
        CentroidLatRole,
        CentroidLngRole,
        MinLatRole,
        MaxLatRole,
        MinLngRole,
        MaxLngRole
    };

    explicit WardBoundaryModel(QObject *parent = nullptr);

    int rowCount(const QModelIndex &parent = QModelIndex()) const override;
    QVariant data(const QModelIndex &index, int role = Qt::DisplayRole) const override;
    QHash<int, QByteArray> roleNames() const override;

    void loadFromGeoJson(const QString &filePath);
    void filterByConstituency(const QString &acCode);
    void resetFilter();

    QVariantMap getWardByNumber(int wardNumber) const;

private:
    QVector<WardFeature> m_allWards;
    QVector<WardFeature> m_displayedWards;
};
