#include "DetailedReportModel.h"
#include <algorithm>
#include <QDateTime>
#include <QRegularExpression>

DetailedReportModel::DetailedReportModel(QObject *parent)
    : QAbstractListModel(parent)
    , m_currentSortKey("newest")
{
    loadMasterCivicGrievances();
    applyCurrentSort();
}

void DetailedReportModel::loadMasterCivicGrievances()
{
    // Real-time Cloud Mode: reports are populated dynamically and exclusively via Cloud Firestore
    m_allReports.clear();
    m_displayedReports.clear();
}

int DetailedReportModel::rowCount(const QModelIndex &parent) const
{
    if (parent.isValid()) return 0;
    return static_cast<int>(m_displayedReports.size());
}

QVariant DetailedReportModel::data(const QModelIndex &index, int role) const
{
    if (!index.isValid() || index.row() < 0 || index.row() >= m_displayedReports.size())
        return QVariant();

    const DetailedReportItem &item = m_displayedReports.at(index.row());

    switch (role) {
    case ReportIdRole: return item.reportId;
    case IssueTitleRole: return item.issueTitle;
    case CategoryRole: return item.category;
    case DepartmentRole: return item.department;
    case SeverityRole: return item.severity;
    case StatusRole: return item.status;
    case SlaHoursLeftRole: return item.slaHoursLeft;
    case SlaBreachedRole: return item.slaBreached;
    case SlaDeadlineTextRole: return item.slaDeadlineText;
    case WardNumberRole: return item.wardNumber;
    case WardNameRole: return item.wardName;
    case AcCodeRole: return item.acCode;
    case AcNameRole: return item.acName;
    case MlaNameRole: return item.mlaName;
    case LocationRole: return item.location;
    case LatitudeRole: return item.latitude;
    case LongitudeRole: return item.longitude;
    case CitizenNameRole: return item.citizenName;
    case CitizenPhoneRole: return item.citizenPhone;
    case DescriptionRole: return item.description;
    case CreatedDateRole: return item.createdDate;
    case AssignedOfficerRole: return item.assignedOfficer;
    case AssignedRoleRole: return item.assignedRole;
    case WorkOrderTokenRole: return item.workOrderToken;
    case ResolutionNotesRole: return item.resolutionNotes;
    case CitizenPhotoUrlRole: return item.citizenPhotoUrl;
    case ResolutionPhotoUrlRole: return item.resolutionPhotoUrl;
    case TimelineEventsRole: return item.timelineEvents;
    default: return QVariant();
    }
}

QHash<int, QByteArray> DetailedReportModel::roleNames() const
{
    QHash<int, QByteArray> roles;
    roles[ReportIdRole] = "reportId";
    roles[IssueTitleRole] = "issueTitle";
    roles[CategoryRole] = "category";
    roles[DepartmentRole] = "department";
    roles[SeverityRole] = "severity";
    roles[StatusRole] = "status";
    roles[SlaHoursLeftRole] = "slaHoursLeft";
    roles[SlaBreachedRole] = "slaBreached";
    roles[SlaDeadlineTextRole] = "slaDeadlineText";
    roles[WardNumberRole] = "wardNumber";
    roles[WardNameRole] = "wardName";
    roles[AcCodeRole] = "acCode";
    roles[AcNameRole] = "acName";
    roles[MlaNameRole] = "mlaName";
    roles[LocationRole] = "location";
    roles[LatitudeRole] = "latitude";
    roles[LongitudeRole] = "longitude";
    roles[CitizenNameRole] = "citizenName";
    roles[CitizenPhoneRole] = "citizenPhone";
    roles[DescriptionRole] = "description";
    roles[CreatedDateRole] = "createdDate";
    roles[AssignedOfficerRole] = "assignedOfficer";
    roles[AssignedRoleRole] = "assignedRole";
    roles[WorkOrderTokenRole] = "workOrderToken";
    roles[ResolutionNotesRole] = "resolutionNotes";
    roles[CitizenPhotoUrlRole] = "citizenPhotoUrl";
    roles[ResolutionPhotoUrlRole] = "resolutionPhotoUrl";
    roles[TimelineEventsRole] = "timelineEvents";
    return roles;
}

void DetailedReportModel::filter(const QString &searchQuery, const QString &statusTab, const QString &category, const QString &constituency, int wardNumber)
{
    beginResetModel();
    m_displayedReports.clear();

    QString query = searchQuery.trimmed();

    for (const auto &item : m_allReports) {
        // Multi-field search matching
        bool matchQuery = query.isEmpty();
        if (!matchQuery) {
            QStringList tokens = query.split(QRegularExpression("\\s+"), Qt::SkipEmptyParts);
            bool allTokensMatch = true;
            for (const QString &token : tokens) {
                bool tokenMatches = item.reportId.contains(token, Qt::CaseInsensitive) ||
                                    item.issueTitle.contains(token, Qt::CaseInsensitive) ||
                                    item.description.contains(token, Qt::CaseInsensitive) ||
                                    item.citizenName.contains(token, Qt::CaseInsensitive) ||
                                    item.citizenPhone.contains(token, Qt::CaseInsensitive) ||
                                    item.category.contains(token, Qt::CaseInsensitive) ||
                                    item.department.contains(token, Qt::CaseInsensitive) ||
                                    item.location.contains(token, Qt::CaseInsensitive) ||
                                    item.wardName.contains(token, Qt::CaseInsensitive) ||
                                    item.acName.contains(token, Qt::CaseInsensitive) ||
                                    item.mlaName.contains(token, Qt::CaseInsensitive) ||
                                    item.assignedOfficer.contains(token, Qt::CaseInsensitive) ||
                                    item.status.contains(token, Qt::CaseInsensitive) ||
                                    item.severity.contains(token, Qt::CaseInsensitive) ||
                                    QString::number(item.wardNumber).contains(token);
                if (!tokenMatches) {
                    allTokensMatch = false;
                    break;
                }
            }
            matchQuery = allTokensMatch;
        }

        // Status tab matching
        bool matchStatus = true;
        if (statusTab.compare("Needs Action", Qt::CaseInsensitive) == 0) {
            matchStatus = (item.status == "PENDING" || item.severity == "CRITICAL" || item.slaBreached);
        } else if (statusTab.compare("In Progress", Qt::CaseInsensitive) == 0) {
            matchStatus = (item.status == "IN PROGRESS");
        } else if (statusTab.compare("Resolved", Qt::CaseInsensitive) == 0) {
            matchStatus = (item.status == "RESOLVED");
        }

        // Category matching
        bool matchCat = category.isEmpty() || category.contains("All", Qt::CaseInsensitive) || item.category.compare(category, Qt::CaseInsensitive) == 0;

        // AC matching
        bool matchAC = constituency.isEmpty() || constituency.contains("All", Qt::CaseInsensitive) || item.acName.contains(constituency, Qt::CaseInsensitive);

        // Ward matching
        bool matchWard = (wardNumber <= 0) || (item.wardNumber == wardNumber);

        if (matchQuery && matchStatus && matchCat && matchAC && matchWard) {
            m_displayedReports.append(item);
        }
    }

    applyCurrentSort();
    endResetModel();
}

void DetailedReportModel::sort(const QString &sortKey)
{
    beginResetModel();
    m_currentSortKey = sortKey;
    applyCurrentSort();
    endResetModel();
}

void DetailedReportModel::applyCurrentSort()
{
    if (m_currentSortKey == "newest" || m_currentSortKey.isEmpty() || m_currentSortKey == "time") {
        // Chronological: newest reports first
        std::sort(m_displayedReports.begin(), m_displayedReports.end(), [](const DetailedReportItem &a, const DetailedReportItem &b) {
            return a.createdAtEpoch > b.createdAtEpoch;
        });
    } else if (m_currentSortKey == "sla") {
        // Breached & lowest SLA hours left first
        std::sort(m_displayedReports.begin(), m_displayedReports.end(), [](const DetailedReportItem &a, const DetailedReportItem &b) {
            if (a.status == "RESOLVED" && b.status != "RESOLVED") return false;
            if (a.status != "RESOLVED" && b.status == "RESOLVED") return true;
            return a.slaHoursLeft < b.slaHoursLeft;
        });
    } else if (m_currentSortKey == "severity") {
        auto severityWeight = [](const QString &s) {
            if (s == "CRITICAL") return 4;
            if (s == "HIGH") return 3;
            if (s == "MEDIUM") return 2;
            return 1;
        };
        std::sort(m_displayedReports.begin(), m_displayedReports.end(), [&](const DetailedReportItem &a, const DetailedReportItem &b) {
            return severityWeight(a.severity) > severityWeight(b.severity);
        });
    } else if (m_currentSortKey == "ward") {
        std::sort(m_displayedReports.begin(), m_displayedReports.end(), [](const DetailedReportItem &a, const DetailedReportItem &b) {
            return a.wardNumber < b.wardNumber;
        });
    }
}

QVariantMap DetailedReportModel::getReportById(const QString &reportId) const
{
    for (const auto &item : m_allReports) {
        if (item.reportId.compare(reportId, Qt::CaseInsensitive) == 0) {
            QVariantMap map;
            map["reportId"] = item.reportId;
            map["issueTitle"] = item.issueTitle;
            map["category"] = item.category;
            map["department"] = item.department;
            map["severity"] = item.severity;
            map["status"] = item.status;
            map["slaHoursLeft"] = item.slaHoursLeft;
            map["slaBreached"] = item.slaBreached;
            map["slaDeadlineText"] = item.slaDeadlineText;
            map["wardNumber"] = item.wardNumber;
            map["wardName"] = item.wardName;
            map["acCode"] = item.acCode;
            map["acName"] = item.acName;
            map["mlaName"] = item.mlaName;
            map["location"] = item.location;
            map["latitude"] = item.latitude;
            map["longitude"] = item.longitude;
            map["citizenName"] = item.citizenName;
            map["citizenPhone"] = item.citizenPhone;
            map["description"] = item.description;
            map["createdDate"] = item.createdDate;
            map["assignedOfficer"] = item.assignedOfficer;
            map["assignedRole"] = item.assignedRole;
            map["workOrderToken"] = item.workOrderToken;
            map["resolutionNotes"] = item.resolutionNotes;
            map["citizenPhotoUrl"] = item.citizenPhotoUrl;
            map["resolutionPhotoUrl"] = item.resolutionPhotoUrl;
            map["timelineEvents"] = item.timelineEvents;
            return map;
        }
    }
    return QVariantMap();
}

void DetailedReportModel::updateStatus(const QString &reportId, const QString &newStatus)
{
    for (int i = 0; i < m_allReports.size(); ++i) {
        if (m_allReports[i].reportId.compare(reportId, Qt::CaseInsensitive) == 0) {
            m_allReports[i].status = newStatus;
            QVariantMap newEvent;
            newEvent["time"] = QDateTime::currentDateTime().toString("MMM dd, hh:mm AP");
            newEvent["action"] = QString("Status Updated to %1").arg(newStatus);
            newEvent["actor"] = "Alex Rivera (Admin)";
            newEvent["role"] = "Command Center";
            newEvent["icon"] = "🔄";
            m_allReports[i].timelineEvents.append(newEvent);
            break;
        }
    }

    for (int i = 0; i < m_displayedReports.size(); ++i) {
        if (m_displayedReports[i].reportId.compare(reportId, Qt::CaseInsensitive) == 0) {
            m_displayedReports[i].status = newStatus;
            QModelIndex idx = index(i);
            emit dataChanged(idx, idx, {StatusRole, TimelineEventsRole});
            break;
        }
    }
}

void DetailedReportModel::assignWorkOrder(const QString &reportId, const QString &officerName, const QString &role, const QString &workOrderToken, const QString &notes)
{
    for (int i = 0; i < m_allReports.size(); ++i) {
        if (m_allReports[i].reportId.compare(reportId, Qt::CaseInsensitive) == 0) {
            m_allReports[i].assignedOfficer = officerName;
            m_allReports[i].assignedRole = role;
            m_allReports[i].workOrderToken = workOrderToken;
            m_allReports[i].status = "IN PROGRESS";

            QVariantMap newEvent;
            newEvent["time"] = QDateTime::currentDateTime().toString("MMM dd, hh:mm AP");
            newEvent["action"] = QString("Work Order %1 Dispatched to %2").arg(workOrderToken, officerName);
            newEvent["actor"] = "Alex Rivera (Admin)";
            newEvent["role"] = "Command Center";
            newEvent["icon"] = "📋";
            m_allReports[i].timelineEvents.append(newEvent);
            break;
        }
    }

    for (int i = 0; i < m_displayedReports.size(); ++i) {
        if (m_displayedReports[i].reportId.compare(reportId, Qt::CaseInsensitive) == 0) {
            m_displayedReports[i].assignedOfficer = officerName;
            m_displayedReports[i].assignedRole = role;
            m_displayedReports[i].workOrderToken = workOrderToken;
            m_displayedReports[i].status = "IN PROGRESS";
            QModelIndex idx = index(i);
            emit dataChanged(idx, idx, {AssignedOfficerRole, AssignedRoleRole, WorkOrderTokenRole, StatusRole, TimelineEventsRole});
            break;
        }
    }
}

void DetailedReportModel::resolveGrievance(const QString &reportId, const QString &resolutionNotes, const QString &resolutionPhotoUrl)
{
    for (int i = 0; i < m_allReports.size(); ++i) {
        if (m_allReports[i].reportId.compare(reportId, Qt::CaseInsensitive) == 0) {
            m_allReports[i].status = "RESOLVED";
            m_allReports[i].resolutionNotes = resolutionNotes;
            if (!resolutionPhotoUrl.isEmpty()) {
                m_allReports[i].resolutionPhotoUrl = resolutionPhotoUrl;
            }
            m_allReports[i].slaHoursLeft = 0;
            m_allReports[i].slaBreached = false;

            QVariantMap newEvent;
            newEvent["time"] = QDateTime::currentDateTime().toString("MMM dd, hh:mm AP");
            newEvent["action"] = "Grievance Verified & Closed";
            newEvent["actor"] = m_allReports[i].assignedOfficer.isEmpty() ? "Field Officer" : m_allReports[i].assignedOfficer;
            newEvent["role"] = "Nodal Officer Sign-Off";
            newEvent["icon"] = "✅";
            m_allReports[i].timelineEvents.append(newEvent);
            break;
        }
    }

    for (int i = 0; i < m_displayedReports.size(); ++i) {
        if (m_displayedReports[i].reportId.compare(reportId, Qt::CaseInsensitive) == 0) {
            m_displayedReports[i].status = "RESOLVED";
            m_displayedReports[i].resolutionNotes = resolutionNotes;
            if (!resolutionPhotoUrl.isEmpty()) {
                m_displayedReports[i].resolutionPhotoUrl = resolutionPhotoUrl;
            }
            m_displayedReports[i].slaHoursLeft = 0;
            m_displayedReports[i].slaBreached = false;
            QModelIndex idx = index(i);
            emit dataChanged(idx, idx, {StatusRole, ResolutionNotesRole, ResolutionPhotoUrlRole, SlaHoursLeftRole, SlaBreachedRole, TimelineEventsRole});
            break;
        }
    }
}

int DetailedReportModel::needsActionCount() const
{
    int c = 0;
    for (const auto &item : m_allReports) {
        if (item.status == "PENDING" || item.severity == "CRITICAL" || item.slaBreached) c++;
    }
    return c;
}

int DetailedReportModel::inProgressCount() const
{
    int c = 0;
    for (const auto &item : m_allReports) {
        if (item.status == "IN PROGRESS") c++;
    }
    return c;
}

int DetailedReportModel::resolvedCount() const
{
    int c = 0;
    for (const auto &item : m_allReports) {
        if (item.status == "RESOLVED") c++;
    }
    return c;
}

void DetailedReportModel::mergeCloudReports(const QVector<DetailedReportItem> &cloudReports)
{
    beginResetModel();
    for (const auto &cloudItem : cloudReports) {
        DetailedReportItem item = cloudItem;
        if (item.status == "CITIZEN_CLOSED") {
            item.status = "RESOLVED"; // Verified & closed by citizen
        } else if (item.status == "ESCALATED") {
            item.status = "CRITICAL"; // Reopened by citizen dispute!
            item.severity = "CRITICAL";
        }

        int idx = -1;
        for (int i = 0; i < m_allReports.size(); ++i) {
            if (m_allReports[i].reportId == item.reportId) {
                idx = i;
                break;
            }
        }

        if (idx != -1) {
            // Update existing report with latest cloud status
            m_allReports[idx].status = item.status;
            m_allReports[idx].assignedOfficer = item.assignedOfficer;
            m_allReports[idx].citizenPhotoUrl = item.citizenPhotoUrl;
            if (!item.resolutionPhotoUrl.isEmpty()) {
                m_allReports[idx].resolutionPhotoUrl = item.resolutionPhotoUrl;
            }
            if (!item.resolutionNotes.isEmpty()) {
                m_allReports[idx].resolutionNotes = item.resolutionNotes;
            }
        } else {
            // Prepend new citizen report at the very top of the list
            m_allReports.prepend(item);
        }
    }
    m_displayedReports = m_allReports;
    applyCurrentSort();
    endResetModel();
}
