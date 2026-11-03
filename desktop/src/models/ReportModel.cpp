#include "ReportModel.h"

ReportModel::ReportModel(QObject *parent)
    : QAbstractListModel(parent)
{
    loadSampleData();
}

void ReportModel::loadSampleData()
{
    // Start clean: populated dynamically via Cloud Firestore
    m_allReports.clear();
    m_displayedReports.clear();
}

void ReportModel::setReportsFromCloud(const QVector<ReportItem> &items)
{
    beginResetModel();
    m_allReports = items;
    m_displayedReports = m_allReports;
    endResetModel();
}

int ReportModel::rowCount(const QModelIndex &parent) const
{
    if (parent.isValid())
        return 0;
    return static_cast<int>(m_displayedReports.size());
}

QVariant ReportModel::data(const QModelIndex &index, int role) const
{
    if (!index.isValid() || index.row() < 0 || index.row() >= m_displayedReports.size())
        return QVariant();

    const ReportItem &item = m_displayedReports.at(index.row());

    switch (role) {
    case ReportIdRole:
        return item.reportId;
    case IssueTitleRole:
        return item.issueTitle;
    case LocationRole:
        return item.location;
    case StatusRole:
        return item.status;
    case DateRole:
        return item.date;
    case CategoryRole:
        return item.category;
    default:
        return QVariant();
    }
}

QHash<int, QByteArray> ReportModel::roleNames() const
{
    QHash<int, QByteArray> roles;
    roles[ReportIdRole] = "reportId";
    roles[IssueTitleRole] = "issueTitle";
    roles[LocationRole] = "location";
    roles[StatusRole] = "status";
    roles[DateRole] = "date";
    roles[CategoryRole] = "category";
    return roles;
}

void ReportModel::addReport(const QString &id, const QString &issue, const QString &location, const QString &status, const QString &date, const QString &category)
{
    beginInsertRows(QModelIndex(), 0, 0);
    ReportItem item = {id, issue, location, status, date, category};
    m_allReports.prepend(item);
    m_displayedReports.prepend(item);
    endInsertRows();
}

void ReportModel::filterByCategory(const QString &category)
{
    beginResetModel();
    if (category.isEmpty() || category.toLower() == "all") {
        m_displayedReports = m_allReports;
    } else {
        m_displayedReports.clear();
        for (const auto &rep : m_allReports) {
            if (rep.category.compare(category, Qt::CaseInsensitive) == 0) {
                m_displayedReports.append(rep);
            }
        }
    }
    endResetModel();
}

void ReportModel::searchReports(const QString &keyword)
{
    beginResetModel();
    if (keyword.trimmed().isEmpty()) {
        m_displayedReports = m_allReports;
    } else {
        m_displayedReports.clear();
        for (const auto &rep : m_allReports) {
            if (rep.reportId.contains(keyword, Qt::CaseInsensitive) ||
                rep.issueTitle.contains(keyword, Qt::CaseInsensitive) ||
                rep.location.contains(keyword, Qt::CaseInsensitive) ||
                rep.category.contains(keyword, Qt::CaseInsensitive)) {
                m_displayedReports.append(rep);
            }
        }
    }
    endResetModel();
}

void ReportModel::resetFilter()
{
    beginResetModel();
    m_displayedReports = m_allReports;
    endResetModel();
}
