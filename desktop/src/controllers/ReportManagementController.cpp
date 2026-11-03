#include "ReportManagementController.h"
#include <QFile>
#include <QTextStream>
#include <QDateTime>
#include <QStandardPaths>
#include <QDir>
#include <QDebug>

ReportManagementController::ReportManagementController(QObject *parent)
    : QObject(parent)
    , m_selectedReportId("#REP-8291")
    , m_searchQuery("")
    , m_statusTab("All")
    , m_categoryFilter("All")
    , m_constituencyFilter("All")
    , m_wardFilter(0)
    , m_sortBy("newest")
{
    m_selectedReportData = m_reportModel.getReportById(m_selectedReportId);

    // Live bidirectional sync: Merge incoming citizen mobile reports into Desktop Model
    connect(&m_syncService, &FirestoreSyncService::reportsReceived, this, [this](const QVector<DetailedReportItem> &reports) {
        m_reportModel.mergeCloudReports(reports);
        emit countsChanged();
    });
}

void ReportManagementController::selectReport(const QString &reportId)
{
    m_selectedReportId = reportId;
    m_selectedReportData = m_reportModel.getReportById(reportId);
    emit selectionChanged();
}

void ReportManagementController::clearSelection()
{
    m_selectedReportId = "";
    m_selectedReportData.clear();
    emit selectionChanged();
}

void ReportManagementController::setSearchQuery(const QString &query)
{
    if (m_searchQuery != query) {
        m_searchQuery = query;
        updateFilter();
        emit filterChanged();
    }
}

void ReportManagementController::setStatusTab(const QString &tab)
{
    if (m_statusTab != tab) {
        m_statusTab = tab;
        updateFilter();
        emit filterChanged();
    }
}

void ReportManagementController::setCategoryFilter(const QString &cat)
{
    if (m_categoryFilter != cat) {
        m_categoryFilter = cat;
        updateFilter();
        emit filterChanged();
    }
}

void ReportManagementController::setConstituencyFilter(const QString &ac)
{
    if (m_constituencyFilter != ac) {
        m_constituencyFilter = ac;
        updateFilter();
        emit filterChanged();
    }
}

void ReportManagementController::setWardFilter(int ward)
{
    if (m_wardFilter != ward) {
        m_wardFilter = ward;
        updateFilter();
        emit filterChanged();
    }
}

void ReportManagementController::setSortBy(const QString &sortKey)
{
    if (m_sortBy != sortKey) {
        m_sortBy = sortKey;
        m_reportModel.sort(sortKey);
        emit sortChanged();
    }
}

void ReportManagementController::updateFilter()
{
    m_reportModel.filter(m_searchQuery, m_statusTab, m_categoryFilter, m_constituencyFilter, m_wardFilter);
}

void ReportManagementController::assignWorkOrder(const QString &reportId, const QString &officerName, const QString &role, const QString &notes)
{
    QString token = QString("WO-2026-DISP-%1").arg(QDateTime::currentMSecsSinceEpoch() % 10000);
    m_reportModel.assignWorkOrder(reportId, officerName, role, token, notes);
    if (m_selectedReportId == reportId) {
        m_selectedReportData = m_reportModel.getReportById(reportId);
        emit selectionChanged();
    }
    emit countsChanged();

    // Push live update to Cloud Firestore
    m_syncService.updateStatusOnCloud(reportId, "IN_PROGRESS", officerName);
}

void ReportManagementController::updateReportStatus(const QString &reportId, const QString &newStatus)
{
    m_reportModel.updateStatus(reportId, newStatus);
    if (m_selectedReportId == reportId) {
        m_selectedReportData = m_reportModel.getReportById(reportId);
        emit selectionChanged();
    }
    emit countsChanged();

    // Push live update to Cloud Firestore
    m_syncService.updateStatusOnCloud(reportId, newStatus);
}

void ReportManagementController::resolveGrievance(const QString &reportId, const QString &resolutionNotes)
{
    resolveGrievanceWithProof(reportId, resolutionNotes, QString());
}

void ReportManagementController::resolveGrievanceWithProof(const QString &reportId, const QString &resolutionNotes, const QString &resolutionPhotoUrl)
{
    m_reportModel.resolveGrievance(reportId, resolutionNotes, resolutionPhotoUrl);
    if (m_selectedReportId == reportId) {
        m_selectedReportData = m_reportModel.getReportById(reportId);
        emit selectionChanged();
    }
    emit countsChanged();

    // Push live update to Cloud Firestore
    m_syncService.updateStatusOnCloud(reportId, "RESOLVED", "Authority Field Team", resolutionPhotoUrl, resolutionNotes);
}

QString ReportManagementController::exportToCsv()
{
    QString desktopPath = QStandardPaths::writableLocation(QStandardPaths::DesktopLocation);
    QString filePath = desktopPath + "/BBMP_Civic_Grievances_Export.csv";

    QFile file(filePath);
    if (file.open(QIODevice::WriteOnly | QIODevice::Text)) {
        QTextStream stream(&file);
        stream << "Report ID,Issue Title,Category,Department,Severity,Status,Ward Number,Ward Name,Constituency,MLA,Citizen Name,Citizen Phone,Location,Assigned Officer,Created Date\n";

        for (int i = 0; i < m_reportModel.rowCount(); ++i) {
            QModelIndex idx = m_reportModel.index(i);
            stream << QString("\"%1\",\"%2\",\"%3\",\"%4\",\"%5\",\"%6\",\"%7\",\"%8\",\"%9\",\"%10\",\"%11\",\"%12\",\"%13\",\"%14\",\"%15\"\n")
                      .arg(m_reportModel.data(idx, DetailedReportModel::ReportIdRole).toString())
                      .arg(m_reportModel.data(idx, DetailedReportModel::IssueTitleRole).toString())
                      .arg(m_reportModel.data(idx, DetailedReportModel::CategoryRole).toString())
                      .arg(m_reportModel.data(idx, DetailedReportModel::DepartmentRole).toString())
                      .arg(m_reportModel.data(idx, DetailedReportModel::SeverityRole).toString())
                      .arg(m_reportModel.data(idx, DetailedReportModel::StatusRole).toString())
                      .arg(m_reportModel.data(idx, DetailedReportModel::WardNumberRole).toString())
                      .arg(m_reportModel.data(idx, DetailedReportModel::WardNameRole).toString())
                      .arg(m_reportModel.data(idx, DetailedReportModel::AcNameRole).toString())
                      .arg(m_reportModel.data(idx, DetailedReportModel::MlaNameRole).toString())
                      .arg(m_reportModel.data(idx, DetailedReportModel::CitizenNameRole).toString())
                      .arg(m_reportModel.data(idx, DetailedReportModel::CitizenPhoneRole).toString())
                      .arg(m_reportModel.data(idx, DetailedReportModel::LocationRole).toString())
                      .arg(m_reportModel.data(idx, DetailedReportModel::AssignedOfficerRole).toString())
                      .arg(m_reportModel.data(idx, DetailedReportModel::CreatedDateRole).toString());
        }
        file.close();
        return filePath;
    }
    return "";
}
