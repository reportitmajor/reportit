#pragma once

#include <QObject>
#include <QString>
#include <QVariantList>
#include <QVariantMap>
#include "../models/ReportModel.h"
#include "../models/CategoryModel.h"
#include "../models/DetailedReportModel.h"

class DashboardController : public QObject {
    Q_OBJECT

    Q_PROPERTY(QString totalReports READ totalReports NOTIFY metricsChanged)
    Q_PROPERTY(QString totalReportsTrend READ totalReportsTrend NOTIFY metricsChanged)
    Q_PROPERTY(QString pendingResolution READ pendingResolution NOTIFY metricsChanged)
    Q_PROPERTY(QString pendingResolutionTrend READ pendingResolutionTrend NOTIFY metricsChanged)
    Q_PROPERTY(QString resolvedToday READ resolvedToday NOTIFY metricsChanged)
    Q_PROPERTY(QString resolvedTodayTrend READ resolvedTodayTrend NOTIFY metricsChanged)
    Q_PROPERTY(QString avgResolutionTime READ avgResolutionTime NOTIFY metricsChanged)
    Q_PROPERTY(QString avgResolutionTimeTrend READ avgResolutionTimeTrend NOTIFY metricsChanged)
    
    Q_PROPERTY(QString chartPeriod READ chartPeriod WRITE setChartPeriod NOTIFY chartPeriodChanged)
    Q_PROPERTY(QVariantList currentChartData READ currentChartData NOTIFY chartPeriodChanged)
    Q_PROPERTY(QVariantList currentChartLabels READ currentChartLabels NOTIFY chartPeriodChanged)
    Q_PROPERTY(QVariantList insightsList READ insightsList NOTIFY insightsChanged)

    Q_PROPERTY(ReportModel* reportModel READ reportModel CONSTANT)
    Q_PROPERTY(CategoryModel* categoryModel READ categoryModel CONSTANT)

public:
    explicit DashboardController(QObject *parent = nullptr);

    QString totalReports() const { return m_totalReports; }
    QString totalReportsTrend() const { return m_totalReportsTrend; }
    QString pendingResolution() const { return m_pendingResolution; }
    QString pendingResolutionTrend() const { return m_pendingResolutionTrend; }
    QString resolvedToday() const { return m_resolvedToday; }
    QString resolvedTodayTrend() const { return m_resolvedTodayTrend; }
    QString avgResolutionTime() const { return m_avgResolutionTime; }
    QString avgResolutionTimeTrend() const { return m_avgResolutionTimeTrend; }

    QString chartPeriod() const { return m_chartPeriod; }
    Q_INVOKABLE void setChartPeriod(const QString &period);

    QVariantList currentChartData() const;
    QVariantList currentChartLabels() const;
    QVariantList insightsList() const { return m_insights; }

    ReportModel* reportModel() { return &m_reportModel; }
    CategoryModel* categoryModel() { return &m_categoryModel; }

    Q_INVOKABLE void refreshDashboard();
    Q_INVOKABLE void filterReports(const QString &query);

public slots:
    void onCloudReportsReceived(const QVector<DetailedReportItem> &reports);

signals:
    void metricsChanged();
    void chartPeriodChanged();
    void insightsChanged();

private:
    QString m_totalReports;
    QString m_totalReportsTrend;
    QString m_pendingResolution;
    QString m_pendingResolutionTrend;
    QString m_resolvedToday;
    QString m_resolvedTodayTrend;
    QString m_avgResolutionTime;
    QString m_avgResolutionTimeTrend;

    QString m_chartPeriod; // "weekly" or "monthly"
    QVariantList m_weeklyData;
    QVariantList m_monthlyData;
    QVariantList m_weeklyLabels;
    QVariantList m_monthlyLabels;
    QVariantList m_insights;

    ReportModel m_reportModel;
    CategoryModel m_categoryModel;

    void initData();
};
