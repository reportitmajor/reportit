#include "AnalyticsController.h"
#include <QFile>
#include <QTextStream>
#include <QStandardPaths>
#include <QDateTime>

AnalyticsController::AnalyticsController(QObject *parent)
    : QObject(parent)
    , m_selectedPeriod("30 Days")
    , m_selectedConstituency("All Constituencies")
    , m_selectedDepartment("All Departments")
    , m_overallSlaCompliance(91.4)
    , m_avgTurnaroundHours(18.4)
    , m_totalGrievances(1890)
    , m_csatRating(4.65)
    , m_budgetUtilizedFormatted("₹3.82 Cr")
    , m_costPerResolutionFormatted("₹2,840")
    , m_topConstituencyName("Rajarajeshwari Nagar (97.8%)")
{
    updateMetrics();
}

void AnalyticsController::setSelectedPeriod(const QString &period)
{
    if (m_selectedPeriod != period) {
        m_selectedPeriod = period;
        m_departmentModel.refreshData(period, m_selectedConstituency);
        m_leaderboardModel.refreshData(period, m_selectedConstituency);
        updateMetrics();
        emit periodChanged();
    }
}

void AnalyticsController::setSelectedConstituency(const QString &acName)
{
    if (m_selectedConstituency != acName) {
        m_selectedConstituency = acName;
        m_departmentModel.refreshData(m_selectedPeriod, acName);
        m_leaderboardModel.refreshData(m_selectedPeriod, acName);
        updateMetrics();
        emit filterChanged();
    }
}

void AnalyticsController::setSelectedDepartment(const QString &deptName)
{
    if (m_selectedDepartment != deptName) {
        m_selectedDepartment = deptName;
        updateMetrics();
        emit filterChanged();
    }
}

void AnalyticsController::updateMetrics()
{
    if (m_selectedPeriod == "7 Days") {
        m_totalGrievances = 472;
        m_overallSlaCompliance = 93.6;
        m_avgTurnaroundHours = 14.8;
        m_budgetUtilizedFormatted = "₹0.95 Cr";
    } else if (m_selectedPeriod == "90 Days") {
        m_totalGrievances = 5420;
        m_overallSlaCompliance = 89.8;
        m_avgTurnaroundHours = 21.2;
        m_budgetUtilizedFormatted = "₹11.40 Cr";
    } else if (m_selectedPeriod == "1 Year") {
        m_totalGrievances = 21850;
        m_overallSlaCompliance = 88.2;
        m_avgTurnaroundHours = 24.5;
        m_budgetUtilizedFormatted = "₹42.80 Cr";
    } else { // 30 Days default
        m_totalGrievances = 1890;
        m_overallSlaCompliance = 91.4;
        m_avgTurnaroundHours = 18.4;
        m_budgetUtilizedFormatted = "₹3.82 Cr";
    }

    // Monthly SLA Series for Spline Chart (6 data points)
    m_monthlySlaSeries = {
        QVariantMap{{"label", "May"}, {"total", 1420}, {"resolved", 1210}, {"breached", 180}, {"compliancePct", 87.3}},
        QVariantMap{{"label", "Jun"}, {"total", 1680}, {"resolved", 1480}, {"breached", 160}, {"compliancePct", 88.9}},
        QVariantMap{{"label", "Jul"}, {"total", 1950}, {"resolved", 1750}, {"breached", 170}, {"compliancePct", 90.1}},
        QVariantMap{{"label", "Aug"}, {"total", 2120}, {"resolved", 1940}, {"breached", 150}, {"compliancePct", 91.5}},
        QVariantMap{{"label", "Sep"}, {"total", 1840}, {"resolved", 1710}, {"breached", 110}, {"compliancePct", 93.2}},
        QVariantMap{{"label", "Oct (Curr)"}, {"total", 1890}, {"resolved", 1730}, {"breached", 98}, {"compliancePct", 91.4}}
    };

    // AI-Detected Chronic Failure Hotspots
    m_chronicHotspots = {
        QVariantMap{
            {"title", "5th Cross Road, Ideal Homes (RR Nagar)"},
            {"category", "Pothole & Bitumen Failure"},
            {"count", 8},
            {"riskLevel", "HIGH CHRONIC"},
            {"officer", "Er. R. Suresh (AEE)"},
            {"recommendation", "Substandard bitumen detected; contractor penalty notice recommended."}
        },
        QVariantMap{
            {"title", "Outer Ring Road Underpass (Marathahalli)"},
            {"category", "Stormwater Stagnation"},
            {"count", 5},
            {"riskLevel", "CRITICAL"},
            {"officer", "Er. K. Nataraj (AEE)"},
            {"recommendation", "High-power automated submersible pump installation required."}
        },
        QVariantMap{
            {"title", "1st Main Road (Kengeri Satellite Town)"},
            {"category", "Drain Slab Structural Rupture"},
            {"count", 4},
            {"riskLevel", "MODERATE"},
            {"officer", "Er. P. Venkatesh (AEE)"},
            {"recommendation", "Continuous RCC box drain remodeling sanctioned."}
        }
    };

    emit metricsChanged();
}

QString AnalyticsController::exportExecutiveReport()
{
    QString desktopPath = QStandardPaths::writableLocation(QStandardPaths::DesktopLocation);
    QString filePath = desktopPath + "/BBMP_Executive_Civic_Analytics_Briefing.csv";

    QFile file(filePath);
    if (file.open(QIODevice::WriteOnly | QIODevice::Text)) {
        QTextStream stream(&file);
        stream << "BBMP CIVIC PERFORMANCE & SLA INTELLIGENCE REPORT\n";
        stream << QString("Generated: %1\n").arg(QDateTime::currentDateTime().toString("yyyy-MM-dd hh:mm:ss"));
        stream << QString("Reporting Period: %1\n\n").arg(m_selectedPeriod);

        stream << "EXECUTIVE KEY PERFORMANCE INDICATORS\n";
        stream << QString("Overall SLA Compliance: %1%\n").arg(m_overallSlaCompliance);
        stream << QString("Average Turnaround Time: %1 Hours\n").arg(m_avgTurnaroundHours);
        stream << QString("Total Grievances Processed: %1\n").arg(m_totalGrievances);
        stream << QString("Citizen Satisfaction Index: %1 / 5.0\n").arg(m_csatRating);
        stream << QString("Municipal Budget Utilized: %1\n").arg(m_budgetUtilizedFormatted);
        stream << QString("Average Cost per Resolution: %1\n\n").arg(m_costPerResolutionFormatted);

        stream << "DEPARTMENTAL PERFORMANCE BREAKDOWN\n";
        stream << "Department,Total Complaints,Resolved,Pending,Avg Hours,SLA Compliance,Budget Spent\n";
        for (int i = 0; i < m_departmentModel.rowCount(); ++i) {
            QModelIndex idx = m_departmentModel.index(i);
            stream << QString("\"%1\",%2,%3,%4,%5,%6%,\"%7\"\n")
                      .arg(m_departmentModel.data(idx, DepartmentPerformanceModel::DepartmentNameRole).toString())
                      .arg(m_departmentModel.data(idx, DepartmentPerformanceModel::TotalGrievancesRole).toInt())
                      .arg(m_departmentModel.data(idx, DepartmentPerformanceModel::ResolvedGrievancesRole).toInt())
                      .arg(m_departmentModel.data(idx, DepartmentPerformanceModel::PendingGrievancesRole).toInt())
                      .arg(m_departmentModel.data(idx, DepartmentPerformanceModel::AvgResolutionHoursRole).toDouble())
                      .arg(m_departmentModel.data(idx, DepartmentPerformanceModel::SlaCompliancePctRole).toDouble())
                      .arg(m_departmentModel.data(idx, DepartmentPerformanceModel::BudgetAllocatedRole).toString());
        }

        file.close();
        return filePath;
    }
    return "";
}
