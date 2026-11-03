#pragma once

#include <QObject>
#include <QString>
#include <QVariantList>
#include <QVariantMap>
#include "../models/WardBoundaryModel.h"
#include "../models/IncidentMarkerModel.h"

class MapController : public QObject {
    Q_OBJECT

    Q_PROPERTY(QString selectedConstituency READ selectedConstituency WRITE selectConstituency NOTIFY constituencyChanged)
    Q_PROPERTY(int selectedWardNumber READ selectedWardNumber NOTIFY wardSelectionChanged)
    Q_PROPERTY(QString selectedWardName READ selectedWardName NOTIFY wardSelectionChanged)
    Q_PROPERTY(QString selectedReportId READ selectedReportId NOTIFY incidentSelectionChanged)

    Q_PROPERTY(QVariantMap selectedWardData READ selectedWardData NOTIFY wardSelectionChanged)
    Q_PROPERTY(QVariantMap selectedIncidentData READ selectedIncidentData NOTIFY incidentSelectionChanged)

    Q_PROPERTY(QString statusFilter READ statusFilter WRITE setStatusFilter NOTIFY filterChanged)
    Q_PROPERTY(QString categoryFilter READ categoryFilter WRITE setCategoryFilter NOTIFY filterChanged)

    Q_PROPERTY(bool showHeatmap READ showHeatmap WRITE setShowHeatmap NOTIFY layerToggled)
    Q_PROPERTY(bool showPins READ showPins WRITE setShowPins NOTIFY layerToggled)
    Q_PROPERTY(bool showBorders READ showBorders WRITE setShowBorders NOTIFY layerToggled)

    Q_PROPERTY(qreal centerLat READ centerLat NOTIFY viewCenterChanged)
    Q_PROPERTY(qreal centerLng READ centerLng NOTIFY viewCenterChanged)
    Q_PROPERTY(qreal zoom READ zoom NOTIFY viewCenterChanged)

    Q_PROPERTY(qreal boundsMinLng READ boundsMinLng NOTIFY boundsChanged)
    Q_PROPERTY(qreal boundsMinLat READ boundsMinLat NOTIFY boundsChanged)
    Q_PROPERTY(qreal boundsMaxLng READ boundsMaxLng NOTIFY boundsChanged)
    Q_PROPERTY(qreal boundsMaxLat READ boundsMaxLat NOTIFY boundsChanged)

    Q_PROPERTY(QVariantList constituencyList READ constituencyList CONSTANT)
    Q_PROPERTY(QVariantList wardListForCurrentAC READ wardListForCurrentAC NOTIFY constituencyChanged)

    Q_PROPERTY(WardBoundaryModel* wardModel READ wardModel CONSTANT)
    Q_PROPERTY(IncidentMarkerModel* incidentModel READ incidentModel CONSTANT)

public:
    explicit MapController(QObject *parent = nullptr);

    QString selectedConstituency() const { return m_selectedConstituency; }
    int selectedWardNumber() const { return m_selectedWardNumber; }
    QString selectedWardName() const { return m_selectedWardName; }
    QString selectedReportId() const { return m_selectedReportId; }

    QVariantMap selectedWardData() const { return m_selectedWardData; }
    QVariantMap selectedIncidentData() const { return m_selectedIncidentData; }

    QString statusFilter() const { return m_statusFilter; }
    QString categoryFilter() const { return m_categoryFilter; }

    bool showHeatmap() const { return m_showHeatmap; }
    bool showPins() const { return m_showPins; }
    bool showBorders() const { return m_showBorders; }

    qreal centerLat() const { return m_centerLat; }
    qreal centerLng() const { return m_centerLng; }
    qreal zoom() const { return m_zoom; }

    qreal boundsMinLng() const { return m_boundsMinLng; }
    qreal boundsMinLat() const { return m_boundsMinLat; }
    qreal boundsMaxLng() const { return m_boundsMaxLng; }
    qreal boundsMaxLat() const { return m_boundsMaxLat; }

    QVariantList constituencyList() const { return m_constituencies; }
    QVariantList wardListForCurrentAC() const;

    WardBoundaryModel* wardModel() { return &m_wardModel; }
    IncidentMarkerModel* incidentModel() { return &m_incidentModel; }

    void setSyncService(class FirestoreSyncService *service) { m_syncService = service; }

    Q_INVOKABLE void selectConstituency(const QString &acName);
    Q_INVOKABLE void selectWard(int wardNumber);
    Q_INVOKABLE void selectIncident(const QString &reportId);
    Q_INVOKABLE void clearSelection();

    Q_INVOKABLE void setStatusFilter(const QString &status);
    Q_INVOKABLE void setCategoryFilter(const QString &category);

    Q_INVOKABLE void setShowHeatmap(bool show);
    Q_INVOKABLE void setShowPins(bool show);
    Q_INVOKABLE void setShowBorders(bool show);
    Q_INVOKABLE void toggleHeatmap();

    Q_INVOKABLE void setViewCenter(qreal lat, qreal lng, qreal zoomLevel);
    Q_INVOKABLE void resetView();

    Q_INVOKABLE void updateIncidentStatus(const QString &reportId, const QString &newStatus);
    Q_INVOKABLE void assignOfficer(const QString &reportId, const QString &officerName);

public slots:
    void onCloudReportsReceived(const QVector<DetailedReportItem> &reports);

signals:
    void constituencyChanged();
    void wardSelectionChanged();
    void incidentSelectionChanged();
    void filterChanged();
    void layerToggled();
    void viewCenterChanged();
    void boundsChanged();

private:
    QString m_selectedConstituency;
    int m_selectedWardNumber;
    QString m_selectedWardName;
    QString m_selectedReportId;

    QVariantMap m_selectedWardData;
    QVariantMap m_selectedIncidentData;

    QString m_statusFilter;
    QString m_categoryFilter;

    bool m_showHeatmap;
    bool m_showPins;
    bool m_showBorders;

    qreal m_centerLat;
    qreal m_centerLng;
    qreal m_zoom;

    qreal m_boundsMinLng;
    qreal m_boundsMinLat;
    qreal m_boundsMaxLng;
    qreal m_boundsMaxLat;

    QVariantList m_constituencies;

    WardBoundaryModel m_wardModel;
    IncidentMarkerModel m_incidentModel;
    class FirestoreSyncService *m_syncService = nullptr;

    void updateFilters();
    void initConstituencies();
    void updateBounds();
};
