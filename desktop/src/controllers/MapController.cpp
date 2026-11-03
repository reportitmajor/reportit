#include "MapController.h"
#include "FirestoreSyncService.h"
#include <QFile>
#include <QJsonDocument>
#include <QJsonObject>
#include <QJsonArray>
#include <QDebug>

MapController::MapController(QObject *parent)
    : QObject(parent)
    , m_selectedConstituency("All Constituencies")
    , m_selectedWardNumber(0)
    , m_selectedWardName("All Wards")
    , m_selectedReportId("")
    , m_statusFilter("All")
    , m_categoryFilter("All")
    , m_showHeatmap(false)
    , m_showPins(true)
    , m_showBorders(true)
    , m_centerLat(12.9716)
    , m_centerLng(77.5946)
    , m_zoom(12.0)
    , m_syncService(nullptr)
{
    initConstituencies();
    m_wardModel.filterByConstituency(m_selectedConstituency);
    updateFilters();
    updateBounds();
}

void MapController::initConstituencies()
{
    QFile file(":/qt/qml/ReportIt/Admin/resources/gis/bengaluru_all_wards.geojson");
    if (!file.exists()) {
        file.setFileName("c:/Users/Karthik S/AndroidStudioProjects/reportit/desktop/resources/gis/bengaluru_all_wards.geojson");
    }

    if (file.open(QIODevice::ReadOnly)) {
        QJsonDocument doc = QJsonDocument::fromJson(file.readAll());
        file.close();
        if (doc.isObject()) {
            QJsonArray constArr = doc.object()["constituencies"].toArray();
            m_constituencies.clear();
            for (const auto &cVal : constArr) {
                QJsonObject cObj = cVal.toObject();
                QVariantMap m;
                m["code"] = cObj["code"].toString();
                m["name"] = cObj["name"].toString();
                m["zone"] = cObj["zone"].toString();
                m["mla"] = cObj["mla"].toString();
                QJsonArray center = cObj["center"].toArray();
                if (center.size() >= 2) {
                    m["lng"] = center[0].toDouble();
                    m["lat"] = center[1].toDouble();
                } else {
                    m["lng"] = 77.5946;
                    m["lat"] = 12.9716;
                }
                m["zoom"] = cObj["zoom"].toDouble(13.0);
                m_constituencies.append(m);
            }
            if (!m_constituencies.isEmpty()) return;
        }
    }

    // Fallback if file not read yet
    m_constituencies = {
        QVariantMap{{"code", "ALL"}, {"name", "All Bengaluru (Master View)"}, {"zone", "Greater Bengaluru Authority"}, {"lat", 12.9716}, {"lng", 77.5946}, {"zoom", 11.5}},
        QVariantMap{{"code", "AC-154"}, {"name", "Rajarajeshwari Nagar"}, {"zone", "South & West Corporation"}, {"lat", 12.9450}, {"lng", 77.5180}, {"zoom", 13.0}},
        QVariantMap{{"code", "AC-174"}, {"name", "Mahadevapura"}, {"zone", "East Corporation"}, {"lat", 12.9800}, {"lng", 77.7100}, {"zoom", 12.8}},
        QVariantMap{{"code", "AC-175"}, {"name", "Bommanahalli"}, {"zone", "South-East Corporation"}, {"lat", 12.9000}, {"lng", 77.6200}, {"zoom", 13.0}}
    };
}

QVariantList MapController::wardListForCurrentAC() const
{
    QVariantList list;
    QVariantMap allWardsOpt;
    allWardsOpt["wardNumber"] = 0;
    allWardsOpt["wardName"] = "All Wards in " + m_selectedConstituency;
    list.append(allWardsOpt);

    for (int i = 0; i < m_wardModel.rowCount(); ++i) {
        QModelIndex idx = m_wardModel.index(i);
        QVariantMap wMap;
        wMap["wardNumber"] = m_wardModel.data(idx, WardBoundaryModel::WardNumberRole).toInt();
        wMap["wardName"] = QString("Ward %1 - %2")
                               .arg(wMap["wardNumber"].toString())
                               .arg(m_wardModel.data(idx, WardBoundaryModel::WardNameRole).toString());
        list.append(wMap);
    }
    return list;
}

void MapController::selectConstituency(const QString &acName)
{
    m_selectedConstituency = acName;
    m_selectedWardNumber = 0;
    m_selectedWardName = "";

    // Set center coordinates for the selected constituency
    for (const auto &val : m_constituencies) {
        QVariantMap m = val.toMap();
        if (m["name"].toString().compare(acName, Qt::CaseInsensitive) == 0 ||
            m["code"].toString().compare(acName, Qt::CaseInsensitive) == 0) {
            m_centerLat = m["lat"].toDouble();
            m_centerLng = m["lng"].toDouble();
            m_zoom = m["zoom"].toDouble();
            emit viewCenterChanged();
            break;
        }
    }

    m_wardModel.filterByConstituency(acName);
    updateFilters();
    updateBounds();

    emit constituencyChanged();
    emit wardSelectionChanged();
}

void MapController::selectWard(int wardNumber)
{
    m_selectedWardNumber = wardNumber;
    if (wardNumber > 0) {
        m_selectedWardData = m_wardModel.getWardByNumber(wardNumber);
        m_selectedWardName = m_selectedWardData["wardName"].toString();
    } else {
        m_selectedWardData.clear();
        m_selectedWardName = "All Wards";
    }

    updateFilters();
    updateBounds();
    emit wardSelectionChanged();
}

void MapController::updateBounds()
{
    qreal minLng = 999.0, minLat = 999.0, maxLng = -999.0, maxLat = -999.0;
    int count = m_wardModel.rowCount();
    for (int i = 0; i < count; ++i) {
        QModelIndex idx = m_wardModel.index(i);
        qreal wMinLng = m_wardModel.data(idx, WardBoundaryModel::MinLngRole).toDouble();
        qreal wMinLat = m_wardModel.data(idx, WardBoundaryModel::MinLatRole).toDouble();
        qreal wMaxLng = m_wardModel.data(idx, WardBoundaryModel::MaxLngRole).toDouble();
        qreal wMaxLat = m_wardModel.data(idx, WardBoundaryModel::MaxLatRole).toDouble();
        if (wMinLng < minLng) minLng = wMinLng;
        if (wMinLat < minLat) minLat = wMinLat;
        if (wMaxLng > maxLng) maxLng = wMaxLng;
        if (wMaxLat > maxLat) maxLat = wMaxLat;
    }

    if (minLng < maxLng && minLat < maxLat) {
        m_boundsMinLng = minLng;
        m_boundsMinLat = minLat;
        m_boundsMaxLng = maxLng;
        m_boundsMaxLat = maxLat;
        m_centerLng = (minLng + maxLng) / 2.0;
        m_centerLat = (minLat + maxLat) / 2.0;
        emit boundsChanged();
        emit viewCenterChanged();
    }
}

void MapController::selectIncident(const QString &reportId)
{
    m_selectedReportId = reportId;
    m_selectedIncidentData = m_incidentModel.getIncidentById(reportId);
    emit incidentSelectionChanged();
}

void MapController::clearSelection()
{
    m_selectedReportId = "";
    m_selectedIncidentData.clear();
    emit incidentSelectionChanged();
}

void MapController::setStatusFilter(const QString &status)
{
    if (m_statusFilter != status) {
        m_statusFilter = status;
        updateFilters();
        emit filterChanged();
    }
}

void MapController::setCategoryFilter(const QString &category)
{
    if (m_categoryFilter != category) {
        m_categoryFilter = category;
        updateFilters();
        emit filterChanged();
    }
}

void MapController::updateFilters()
{
    m_incidentModel.filter(m_selectedConstituency, m_selectedWardNumber, m_statusFilter, m_categoryFilter);
}

void MapController::setShowHeatmap(bool show)
{
    if (m_showHeatmap != show) {
        m_showHeatmap = show;
        emit layerToggled();
    }
}

void MapController::setShowPins(bool show)
{
    if (m_showPins != show) {
        m_showPins = show;
        emit layerToggled();
    }
}

void MapController::setShowBorders(bool show)
{
    if (m_showBorders != show) {
        m_showBorders = show;
        emit layerToggled();
    }
}

void MapController::toggleHeatmap()
{
    setShowHeatmap(!m_showHeatmap);
}

void MapController::setViewCenter(qreal lat, qreal lng, qreal zoomLevel)
{
    m_centerLat = lat;
    m_centerLng = lng;
    m_zoom = zoomLevel;
    emit viewCenterChanged();
}

void MapController::resetView()
{
    selectConstituency(m_selectedConstituency);
}

void MapController::updateIncidentStatus(const QString &reportId, const QString &newStatus)
{
    m_incidentModel.updateStatus(reportId, newStatus);
    if (m_selectedReportId == reportId) {
        m_selectedIncidentData["status"] = newStatus;
        emit incidentSelectionChanged();
    }
    if (m_syncService) {
        m_syncService->updateStatusOnCloud(reportId, newStatus);
    }
}

void MapController::assignOfficer(const QString &reportId, const QString &officerName)
{
    m_incidentModel.assignOfficer(reportId, officerName);
    if (m_selectedReportId == reportId) {
        m_selectedIncidentData["assignedTo"] = officerName;
        emit incidentSelectionChanged();
    }
    if (m_syncService) {
        m_syncService->updateStatusOnCloud(reportId, "IN_PROGRESS", officerName);
    }
}

void MapController::onCloudReportsReceived(const QVector<DetailedReportItem> &reports)
{
    m_incidentModel.updateFromCloud(reports);
    updateFilters();

    // Auto-center map on real grievance locations if available
    for (const auto &rep : reports) {
        if (!qFuzzyIsNull(rep.latitude) && !qFuzzyIsNull(rep.longitude)) {
            m_centerLat = rep.latitude;
            m_centerLng = rep.longitude;
            emit viewCenterChanged();
            break;
        }
    }
}
