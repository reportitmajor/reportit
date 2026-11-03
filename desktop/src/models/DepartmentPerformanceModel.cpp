#include "DepartmentPerformanceModel.h"

DepartmentPerformanceModel::DepartmentPerformanceModel(QObject *parent)
    : QAbstractListModel(parent)
{
    loadDefaultDepartmentStats();
}

void DepartmentPerformanceModel::loadDefaultDepartmentStats()
{
    m_items = {
        {
            "Solid Waste Management (SWM)",
            "SWM",
            420,
            404,
            16,
            4.1,
            96.2,
            "EXEMPLARY",
            "12 Hours",
            "₹1.15 Cr"
        },
        {
            "BESCOM Electrical & Streetlights",
            "BESCOM",
            285,
            264,
            21,
            8.6,
            92.8,
            "EXEMPLARY",
            "24 Hours",
            "₹0.85 Cr"
        },
        {
            "BBMP Forest Wing (Tree Snaps)",
            "FOREST",
            145,
            132,
            13,
            6.5,
            91.0,
            "GOOD",
            "12 Hours",
            "₹0.40 Cr"
        },
        {
            "BWSSB Water Supply & Drainage",
            "BWSSB",
            340,
            304,
            36,
            14.2,
            89.4,
            "GOOD",
            "24 Hours",
            "₹1.80 Cr"
        },
        {
            "BBMP Road Infrastructure (Potholes)",
            "ROADS",
            510,
            429,
            81,
            38.6,
            84.1,
            "NEEDS_IMPROVEMENT",
            "48 Hours",
            "₹3.20 Cr"
        },
        {
            "Stormwater Drains (SWD)",
            "SWD",
            190,
            156,
            34,
            28.4,
            82.1,
            "NEEDS_IMPROVEMENT",
            "36 Hours",
            "₹1.60 Cr"
        }
    };
}

int DepartmentPerformanceModel::rowCount(const QModelIndex &parent) const
{
    if (parent.isValid()) return 0;
    return static_cast<int>(m_items.size());
}

QVariant DepartmentPerformanceModel::data(const QModelIndex &index, int role) const
{
    if (!index.isValid() || index.row() < 0 || index.row() >= m_items.size())
        return QVariant();

    const auto &item = m_items.at(index.row());
    switch (role) {
    case DepartmentNameRole: return item.departmentName;
    case ShortCodeRole: return item.shortCode;
    case TotalGrievancesRole: return item.totalGrievances;
    case ResolvedGrievancesRole: return item.resolvedGrievances;
    case PendingGrievancesRole: return item.pendingGrievances;
    case AvgResolutionHoursRole: return item.avgResolutionHours;
    case SlaCompliancePctRole: return item.slaCompliancePct;
    case StatusGradeRole: return item.statusGrade;
    case BenchmarkSlaHoursRole: return item.benchmarkSlaHours;
    case BudgetAllocatedRole: return item.budgetAllocated;
    default: return QVariant();
    }
}

QHash<int, QByteArray> DepartmentPerformanceModel::roleNames() const
{
    QHash<int, QByteArray> roles;
    roles[DepartmentNameRole] = "departmentName";
    roles[ShortCodeRole] = "shortCode";
    roles[TotalGrievancesRole] = "totalGrievances";
    roles[ResolvedGrievancesRole] = "resolvedGrievances";
    roles[PendingGrievancesRole] = "pendingGrievances";
    roles[AvgResolutionHoursRole] = "avgResolutionHours";
    roles[SlaCompliancePctRole] = "slaCompliancePct";
    roles[StatusGradeRole] = "statusGrade";
    roles[BenchmarkSlaHoursRole] = "benchmarkSlaHours";
    roles[BudgetAllocatedRole] = "budgetAllocated";
    return roles;
}

void DepartmentPerformanceModel::refreshData(const QString &period, const QString &constituency)
{
    beginResetModel();
    // Simulate dynamic scaling based on selected period
    qreal multiplier = (period == "7 Days") ? 0.25 : (period == "90 Days" ? 2.8 : (period == "1 Year" ? 11.2 : 1.0));
    loadDefaultDepartmentStats();
    for (auto &item : m_items) {
        item.totalGrievances = static_cast<int>(item.totalGrievances * multiplier);
        item.resolvedGrievances = static_cast<int>(item.resolvedGrievances * multiplier);
        item.pendingGrievances = static_cast<int>(item.pendingGrievances * multiplier);
    }
    endResetModel();
}
