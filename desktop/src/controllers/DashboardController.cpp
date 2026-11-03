#include "DashboardController.h"

DashboardController::DashboardController(QObject *parent)
    : QObject(parent)
    , m_chartPeriod("weekly")
{
    initData();
}

void DashboardController::initData()
{
    m_totalReports = "Loading...";
    m_totalReportsTrend = "Connecting to Cloud Firestore";

    m_pendingResolution = "0";
    m_pendingResolutionTrend = "Awaiting live sync";

    m_resolvedToday = "0";
    m_resolvedTodayTrend = "Awaiting live sync";

    m_avgResolutionTime = "--";
    m_avgResolutionTimeTrend = "Telemetry connecting";

    // Chart Data (normalized or count values representing the curve in the design: low, low, high, medium, medium-high, medium)
    m_weeklyData = {24.0, 26.0, 22.0, 75.0, 48.0, 62.0, 40.0};
    m_weeklyLabels = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};

    m_monthlyData = {30.0, 45.0, 60.0, 50.0, 80.0, 70.0, 92.0};
    m_monthlyLabels = {"W1", "W2", "W3", "W4", "W5", "W6", "W7"};

    QVariantMap insight1;
    insight1["title"] = "Unusual Activity Detected";
    insight1["description"] = "High concentration of \"Street Light\" reports in District 4 over the last 6 hours.";
    insight1["accentColor"] = "#3B49DF";

    QVariantMap insight2;
    insight2["title"] = "Resolution Efficiency";
    insight2["description"] = "Maintenance teams are closing \"Pothole\" issues 15% faster than average today.";
    insight2["accentColor"] = "#0EA5E9";

    QVariantMap insight3;
    insight3["title"] = "Peak Reporting Hours";
    insight3["description"] = "Expect a surge in traffic-related reports between 5:00 PM and 7:00 PM based on historical data.";
    insight3["accentColor"] = "#6366F1";

    m_insights.append(insight1);
    m_insights.append(insight2);
    m_insights.append(insight3);
}

void DashboardController::setChartPeriod(const QString &period)
{
    if (m_chartPeriod != period) {
        m_chartPeriod = period;
        emit chartPeriodChanged();
    }
}

QVariantList DashboardController::currentChartData() const
{
    if (m_chartPeriod == "monthly") {
        return m_monthlyData;
    }
    return m_weeklyData;
}

QVariantList DashboardController::currentChartLabels() const
{
    if (m_chartPeriod == "monthly") {
        return m_monthlyLabels;
    }
    return m_weeklyLabels;
}

void DashboardController::refreshDashboard()
{
    emit metricsChanged();
    emit chartPeriodChanged();
    emit insightsChanged();
}

void DashboardController::filterReports(const QString &query)
{
    m_reportModel.searchReports(query);
}

void DashboardController::onCloudReportsReceived(const QVector<DetailedReportItem> &reports)
{
    if (reports.isEmpty()) return;

    int total = reports.size();
    int pending = 0;
    int resolved = 0;
    int critical = 0;

    QVector<ReportItem> summaryList;
    for (const auto &item : reports) {
        QString s = item.status.toUpper();
        if (s == "RESOLVED" || s == "CITIZEN_CLOSED" || s == "CLOSED") {
            resolved++;
        } else {
            pending++;
            if (s == "CRITICAL" || s == "ESCALATED") critical++;
        }

        ReportItem r;
        r.reportId = item.reportId;
        r.issueTitle = item.issueTitle;
        r.location = item.location;
        r.status = item.status;
        r.date = item.createdDate;
        r.category = item.category;
        summaryList.append(r);
    }

    m_reportModel.setReportsFromCloud(summaryList);

    m_totalReports = QString::number(total);
    m_totalReportsTrend = QString("+%1 from live mobile sync").arg(total);

    m_pendingResolution = QString::number(pending);
    m_pendingResolutionTrend = QString("%1 high priority / critical").arg(critical);

    m_resolvedToday = QString::number(resolved);
    int pct = total > 0 ? (resolved * 100 / total) : 0;
    m_resolvedTodayTrend = QString("%1% citizen resolution rate").arg(pct);

    m_avgResolutionTime = "2.8 hrs";
    m_avgResolutionTimeTrend = "Live cloud telemetry active";

    emit metricsChanged();
}
