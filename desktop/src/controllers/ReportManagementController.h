#pragma once

#include <QObject>
#include <QString>
#include <QVariantList>
#include <QVariantMap>
#include "../models/DetailedReportModel.h"
#include "FirestoreSyncService.h"

class ReportManagementController : public QObject {
    Q_OBJECT

    Q_PROPERTY(QString selectedReportId READ selectedReportId NOTIFY selectionChanged)
    Q_PROPERTY(QVariantMap selectedReportData READ selectedReportData NOTIFY selectionChanged)

    Q_PROPERTY(QString searchQuery READ searchQuery WRITE setSearchQuery NOTIFY filterChanged)
    Q_PROPERTY(QString statusTab READ statusTab WRITE setStatusTab NOTIFY filterChanged)
    Q_PROPERTY(QString categoryFilter READ categoryFilter WRITE setCategoryFilter NOTIFY filterChanged)
    Q_PROPERTY(QString constituencyFilter READ constituencyFilter WRITE setConstituencyFilter NOTIFY filterChanged)
    Q_PROPERTY(int wardFilter READ wardFilter WRITE setWardFilter NOTIFY filterChanged)
    Q_PROPERTY(QString sortBy READ sortBy WRITE setSortBy NOTIFY sortChanged)

    Q_PROPERTY(int totalReportsCount READ totalReportsCount NOTIFY countsChanged)
    Q_PROPERTY(int needsActionCount READ needsActionCount NOTIFY countsChanged)
    Q_PROPERTY(int inProgressCount READ inProgressCount NOTIFY countsChanged)
    Q_PROPERTY(int resolvedCount READ resolvedCount NOTIFY countsChanged)

    Q_PROPERTY(DetailedReportModel* reportModel READ reportModel CONSTANT)
    Q_PROPERTY(FirestoreSyncService* syncService READ syncService CONSTANT)

public:
    explicit ReportManagementController(QObject *parent = nullptr);

    QString selectedReportId() const { return m_selectedReportId; }
    QVariantMap selectedReportData() const { return m_selectedReportData; }

    QString searchQuery() const { return m_searchQuery; }
    QString statusTab() const { return m_statusTab; }
    QString categoryFilter() const { return m_categoryFilter; }
    QString constituencyFilter() const { return m_constituencyFilter; }
    int wardFilter() const { return m_wardFilter; }
    QString sortBy() const { return m_sortBy; }

    int totalReportsCount() const { return m_reportModel.totalCount(); }
    int needsActionCount() const { return m_reportModel.needsActionCount(); }
    int inProgressCount() const { return m_reportModel.inProgressCount(); }
    int resolvedCount() const { return m_reportModel.resolvedCount(); }

    DetailedReportModel* reportModel() { return &m_reportModel; }
    FirestoreSyncService* syncService() { return &m_syncService; }

    Q_INVOKABLE void selectReport(const QString &reportId);
    Q_INVOKABLE void clearSelection();

    Q_INVOKABLE void setSearchQuery(const QString &query);
    Q_INVOKABLE void setStatusTab(const QString &tab);
    Q_INVOKABLE void setCategoryFilter(const QString &cat);
    Q_INVOKABLE void setConstituencyFilter(const QString &ac);
    Q_INVOKABLE void setWardFilter(int ward);
    Q_INVOKABLE void setSortBy(const QString &sortKey);

    Q_INVOKABLE void assignWorkOrder(const QString &reportId, const QString &officerName, const QString &role, const QString &notes);
    Q_INVOKABLE void updateReportStatus(const QString &reportId, const QString &newStatus);
    Q_INVOKABLE void resolveGrievance(const QString &reportId, const QString &resolutionNotes);
    Q_INVOKABLE void resolveGrievanceWithProof(const QString &reportId, const QString &resolutionNotes, const QString &resolutionPhotoUrl);
    Q_INVOKABLE void refreshFromCloud() { m_syncService.fetchReports(); }
    Q_INVOKABLE QString exportToCsv();

signals:
    void selectionChanged();
    void filterChanged();
    void sortChanged();
    void countsChanged();

private:
    QString m_selectedReportId;
    QVariantMap m_selectedReportData;

    QString m_searchQuery;
    QString m_statusTab;
    QString m_categoryFilter;
    QString m_constituencyFilter;
    int m_wardFilter;
    QString m_sortBy;

    DetailedReportModel m_reportModel;
    FirestoreSyncService m_syncService;

    void updateFilter();
};
