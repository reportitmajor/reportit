#include "IncidentMarkerModel.h"

IncidentMarkerModel::IncidentMarkerModel(QObject *parent)
    : QAbstractListModel(parent)
{
    loadSampleIncidents();
}

void IncidentMarkerModel::loadSampleIncidents()
{
    m_allIncidents.clear();
    m_displayedIncidents.clear();
}

int IncidentMarkerModel::rowCount(const QModelIndex &parent) const
{
    if (parent.isValid()) return 0;
    return static_cast<int>(m_displayedIncidents.size());
}

QVariant IncidentMarkerModel::data(const QModelIndex &index, int role) const
{
    if (!index.isValid() || index.row() < 0 || index.row() >= m_displayedIncidents.size())
        return QVariant();

    const IncidentMarker &item = m_displayedIncidents.at(index.row());

    switch (role) {
    case ReportIdRole: return item.reportId;
    case IssueTitleRole: return item.issueTitle;
    case LocationRole: return item.location;
    case WardNumberRole: return item.wardNumber;
    case WardNameRole: return item.wardName;
    case AcNameRole: return item.acName;
    case LatitudeRole: return item.latitude;
    case LongitudeRole: return item.longitude;
    case StatusRole: return item.status;
    case CategoryRole: return item.category;
    case DateRole: return item.date;
    case CitizenNameRole: return item.citizenName;
    case CitizenContactRole: return item.citizenContact;
    case AssignedToRole: return item.assignedTo;
    case DescriptionRole: return item.description;
    default: return QVariant();
    }
}

QHash<int, QByteArray> IncidentMarkerModel::roleNames() const
{
    QHash<int, QByteArray> roles;
    roles[ReportIdRole] = "reportId";
    roles[IssueTitleRole] = "issueTitle";
    roles[LocationRole] = "location";
    roles[WardNumberRole] = "wardNumber";
    roles[WardNameRole] = "wardName";
    roles[AcNameRole] = "acName";
    roles[LatitudeRole] = "latitude";
    roles[LongitudeRole] = "longitude";
    roles[StatusRole] = "status";
    roles[CategoryRole] = "category";
    roles[DateRole] = "date";
    roles[CitizenNameRole] = "citizenName";
    roles[CitizenContactRole] = "citizenContact";
    roles[AssignedToRole] = "assignedTo";
    roles[DescriptionRole] = "description";
    return roles;
}

void IncidentMarkerModel::filter(const QString &constituency, int wardNumber, const QString &status, const QString &category)
{
    beginResetModel();
    m_displayedIncidents.clear();

    for (const auto &item : m_allIncidents) {
        bool matchAC = constituency.isEmpty() || constituency.contains("All", Qt::CaseInsensitive) || item.acName.contains(constituency, Qt::CaseInsensitive);
        bool matchWard = (wardNumber <= 0) || (item.wardNumber == wardNumber);
        bool matchStatus = status.isEmpty() || status.contains("All", Qt::CaseInsensitive) || item.status.compare(status, Qt::CaseInsensitive) == 0;
        bool matchCat = category.isEmpty() || category.contains("All", Qt::CaseInsensitive) || item.category.contains(category, Qt::CaseInsensitive);

        if (matchAC && matchWard && matchStatus && matchCat) {
            m_displayedIncidents.append(item);
        }
    }
    endResetModel();
}

QVariantMap IncidentMarkerModel::getIncidentById(const QString &reportId) const
{
    for (const auto &item : m_allIncidents) {
        if (item.reportId.compare(reportId, Qt::CaseInsensitive) == 0) {
            QVariantMap map;
            map["reportId"] = item.reportId;
            map["issueTitle"] = item.issueTitle;
            map["location"] = item.location;
            map["wardNumber"] = item.wardNumber;
            map["wardName"] = item.wardName;
            map["acName"] = item.acName;
            map["latitude"] = item.latitude;
            map["longitude"] = item.longitude;
            map["status"] = item.status;
            map["category"] = item.category;
            map["date"] = item.date;
            map["citizenName"] = item.citizenName;
            map["citizenContact"] = item.citizenContact;
            map["assignedTo"] = item.assignedTo;
            map["description"] = item.description;
            return map;
        }
    }
    return QVariantMap();
}

void IncidentMarkerModel::updateStatus(const QString &reportId, const QString &newStatus)
{
    for (int i = 0; i < m_allIncidents.size(); ++i) {
        if (m_allIncidents[i].reportId.compare(reportId, Qt::CaseInsensitive) == 0) {
            m_allIncidents[i].status = newStatus;
            break;
        }
    }
    for (int i = 0; i < m_displayedIncidents.size(); ++i) {
        if (m_displayedIncidents[i].reportId.compare(reportId, Qt::CaseInsensitive) == 0) {
            m_displayedIncidents[i].status = newStatus;
            QModelIndex idx = index(i);
            emit dataChanged(idx, idx, {StatusRole});
            break;
        }
    }
}

void IncidentMarkerModel::assignOfficer(const QString &reportId, const QString &officerName)
{
    for (int i = 0; i < m_allIncidents.size(); ++i) {
        if (m_allIncidents[i].reportId.compare(reportId, Qt::CaseInsensitive) == 0) {
            m_allIncidents[i].assignedTo = officerName;
            break;
        }
    }
    for (int i = 0; i < m_displayedIncidents.size(); ++i) {
        if (m_displayedIncidents[i].reportId.compare(reportId, Qt::CaseInsensitive) == 0) {
            m_displayedIncidents[i].assignedTo = officerName;
            QModelIndex idx = index(i);
            emit dataChanged(idx, idx, {AssignedToRole});
            break;
        }
    }
}

void IncidentMarkerModel::updateFromCloud(const QVector<DetailedReportItem> &reports)
{
    beginResetModel();
    m_allIncidents.clear();
    for (const auto &rep : reports) {
        if (qFuzzyIsNull(rep.latitude) && qFuzzyIsNull(rep.longitude)) continue;

        IncidentMarker m;
        m.reportId = rep.reportId;
        m.issueTitle = rep.issueTitle;
        m.location = rep.location;
        m.wardNumber = rep.wardNumber > 0 ? rep.wardNumber : 49;
        m.wardName = !rep.wardName.isEmpty() ? rep.wardName : "Rajarajeshwari Nagar";
        m.acName = !rep.acName.isEmpty() ? rep.acName : "Rajarajeshwari Nagar";
        m.latitude = rep.latitude;
        m.longitude = rep.longitude;
        m.status = rep.status;
        m.category = rep.category;
        m.date = rep.createdDate;
        m.citizenName = rep.citizenName;
        m.citizenContact = rep.citizenPhone;
        m.assignedTo = rep.assignedOfficer;
        m.description = rep.description;
        m_allIncidents.append(m);
    }
    m_displayedIncidents = m_allIncidents;
    endResetModel();
}
