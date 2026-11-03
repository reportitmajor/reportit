#include "WardBoundaryModel.h"
#include <QFile>
#include <QJsonDocument>
#include <QJsonObject>
#include <QJsonArray>
#include <QDebug>

WardBoundaryModel::WardBoundaryModel(QObject *parent)
    : QAbstractListModel(parent)
{
    loadFromGeoJson(":/qt/qml/ReportIt/Admin/resources/gis/bengaluru_all_wards.geojson");
    if (m_allWards.isEmpty()) {
        // Fallback to direct relative path if QRC is loading
        loadFromGeoJson("resources/gis/bengaluru_all_wards.geojson");
    }
}

void WardBoundaryModel::loadFromGeoJson(const QString &filePath)
{
    QFile file(filePath);
    if (!file.exists()) {
        // Try looking in ../resources/gis or absolute paths
        file.setFileName("c:/Users/Karthik S/AndroidStudioProjects/reportit/desktop/resources/gis/bengaluru_all_wards.geojson");
    }

    if (!file.open(QIODevice::ReadOnly)) {
        qWarning() << "Could not open GeoJSON file:" << filePath;
        return;
    }

    QByteArray data = file.readAll();
    file.close();

    QJsonDocument doc = QJsonDocument::fromJson(data);
    if (!doc.isObject()) return;

    QJsonObject rootObj = doc.object();
    QJsonArray features = rootObj["features"].toArray();

    beginResetModel();
    m_allWards.clear();

    for (const auto &featVal : features) {
        QJsonObject featObj = featVal.toObject();
        QJsonObject props = featObj["properties"].toObject();
        QJsonObject geom = featObj["geometry"].toObject();

        WardFeature ward;
        ward.wardNumber = props["ward_number"].toInt();
        ward.wardName = props["ward_name"].toString();
        ward.acCode = props["ac_code"].toString();
        ward.acName = props["ac_name"].toString();
        ward.mlaName = props["mla_name"].toString();
        ward.gbaZone = props["gba_zone"].toString();
        ward.aeeName = props["aee_name"].toString();
        ward.aeeContact = props["aee_contact"].toString();
        ward.healthInspector = props["health_inspector"].toString();
        ward.totalComplaints = props["total_complaints"].toInt();
        ward.criticalCount = props["critical_count"].toInt();
        ward.inProgressCount = props["in_progress_count"].toInt();
        ward.resolvedCount = props["resolved_count"].toInt();
        ward.resolutionRate = props["resolution_rate"].toDouble();
        ward.color = props["color"].toString("#4338CA");

        QJsonArray centroidArr = props["centroid"].toArray();
        if (centroidArr.size() >= 2) {
            ward.centroidLng = centroidArr[0].toDouble();
            ward.centroidLat = centroidArr[1].toDouble();
        } else {
            ward.centroidLng = 77.5946;
            ward.centroidLat = 12.9716;
        }

        QJsonArray boundsArr = props["bounds"].toArray();
        if (boundsArr.size() >= 4) {
            ward.minLng = boundsArr[0].toDouble();
            ward.minLat = boundsArr[1].toDouble();
            ward.maxLng = boundsArr[2].toDouble();
            ward.maxLat = boundsArr[3].toDouble();
        } else {
            ward.minLng = 77.45; ward.minLat = 12.80;
            ward.maxLng = 77.78; ward.maxLat = 13.15;
        }

        // Parse polygon coordinates
        QJsonArray coordsArr = geom["coordinates"].toArray();
        if (!coordsArr.isEmpty()) {
            QJsonArray ring = coordsArr[0].toArray();
            for (const auto &ptVal : ring) {
                QJsonArray pt = ptVal.toArray();
                if (pt.size() >= 2) {
                    QVariantMap ptMap;
                    ptMap["lng"] = pt[0].toDouble();
                    ptMap["lat"] = pt[1].toDouble();
                    ward.polygonCoordinates.append(ptMap);
                }
            }
        }

        m_allWards.append(ward);
    }

    m_displayedWards = m_allWards;
    endResetModel();
}

int WardBoundaryModel::rowCount(const QModelIndex &parent) const
{
    if (parent.isValid()) return 0;
    return static_cast<int>(m_displayedWards.size());
}

QVariant WardBoundaryModel::data(const QModelIndex &index, int role) const
{
    if (!index.isValid() || index.row() < 0 || index.row() >= m_displayedWards.size())
        return QVariant();

    const WardFeature &w = m_displayedWards.at(index.row());

    switch (role) {
    case WardNumberRole: return w.wardNumber;
    case WardNameRole: return w.wardName;
    case AcCodeRole: return w.acCode;
    case AcNameRole: return w.acName;
    case MlaNameRole: return w.mlaName;
    case GbaZoneRole: return w.gbaZone;
    case AeeNameRole: return w.aeeName;
    case AeeContactRole: return w.aeeContact;
    case HealthInspectorRole: return w.healthInspector;
    case TotalComplaintsRole: return w.totalComplaints;
    case CriticalCountRole: return w.criticalCount;
    case InProgressCountRole: return w.inProgressCount;
    case ResolvedCountRole: return w.resolvedCount;
    case ResolutionRateRole: return w.resolutionRate;
    case ColorRole: return w.color;
    case CoordinatesRole: return w.polygonCoordinates;
    case CentroidLatRole: return w.centroidLat;
    case CentroidLngRole: return w.centroidLng;
    case MinLatRole: return w.minLat;
    case MaxLatRole: return w.maxLat;
    case MinLngRole: return w.minLng;
    case MaxLngRole: return w.maxLng;
    default: return QVariant();
    }
}

QHash<int, QByteArray> WardBoundaryModel::roleNames() const
{
    QHash<int, QByteArray> roles;
    roles[WardNumberRole] = "wardNumber";
    roles[WardNameRole] = "wardName";
    roles[AcCodeRole] = "acCode";
    roles[AcNameRole] = "acName";
    roles[MlaNameRole] = "mlaName";
    roles[GbaZoneRole] = "gbaZone";
    roles[AeeNameRole] = "aeeName";
    roles[AeeContactRole] = "aeeContact";
    roles[HealthInspectorRole] = "healthInspector";
    roles[TotalComplaintsRole] = "totalComplaints";
    roles[CriticalCountRole] = "criticalCount";
    roles[InProgressCountRole] = "inProgressCount";
    roles[ResolvedCountRole] = "resolvedCount";
    roles[ResolutionRateRole] = "resolutionRate";
    roles[ColorRole] = "color";
    roles[CoordinatesRole] = "polygonCoordinates";
    roles[CentroidLatRole] = "centroidLat";
    roles[CentroidLngRole] = "centroidLng";
    roles[MinLatRole] = "minLat";
    roles[MaxLatRole] = "maxLat";
    roles[MinLngRole] = "minLng";
    roles[MaxLngRole] = "maxLng";
    return roles;
}

void WardBoundaryModel::filterByConstituency(const QString &acCode)
{
    beginResetModel();
    QString lower = acCode.toLower().trimmed();
    if (lower.isEmpty() || lower == "all" || lower.contains("all constituencies") || lower.contains("all bengaluru")) {
        m_displayedWards = m_allWards;
    } else {
        m_displayedWards.clear();
        for (const auto &w : m_allWards) {
            if (w.acCode.compare(acCode, Qt::CaseInsensitive) == 0 ||
                w.acName.contains(acCode, Qt::CaseInsensitive)) {
                m_displayedWards.append(w);
            }
        }
    }
    endResetModel();
}

void WardBoundaryModel::resetFilter()
{
    beginResetModel();
    m_displayedWards = m_allWards;
    endResetModel();
}

QVariantMap WardBoundaryModel::getWardByNumber(int wardNumber) const
{
    for (const auto &w : m_allWards) {
        if (w.wardNumber == wardNumber) {
            QVariantMap map;
            map["wardNumber"] = w.wardNumber;
            map["wardName"] = w.wardName;
            map["acCode"] = w.acCode;
            map["acName"] = w.acName;
            map["mlaName"] = w.mlaName;
            map["gbaZone"] = w.gbaZone;
            map["aeeName"] = w.aeeName;
            map["aeeContact"] = w.aeeContact;
            map["healthInspector"] = w.healthInspector;
            map["totalComplaints"] = w.totalComplaints;
            map["criticalCount"] = w.criticalCount;
            map["inProgressCount"] = w.inProgressCount;
            map["resolvedCount"] = w.resolvedCount;
            map["resolutionRate"] = w.resolutionRate;
            map["color"] = w.color;
            map["coordinates"] = w.polygonCoordinates;
            return map;
        }
    }
    return QVariantMap();
}
