#pragma once

#include <QAbstractListModel>
#include <QString>
#include <QVector>

struct DepartmentPerfItem {
    QString departmentName;     // E.g. "Solid Waste Management (SWM)", "BESCOM Electrical Wing"
    QString shortCode;          // E.g. "SWM", "BESCOM"
    int totalGrievances;
    int resolvedGrievances;
    int pendingGrievances;
    qreal avgResolutionHours;   // E.g. 4.2
    qreal slaCompliancePct;     // E.g. 96.2
    QString statusGrade;        // "EXEMPLARY", "GOOD", "NEEDS_IMPROVEMENT"
    QString benchmarkSlaHours;  // "12 Hours"
    QString budgetAllocated;    // "₹1.42 Cr"
};

class DepartmentPerformanceModel : public QAbstractListModel {
    Q_OBJECT

public:
    enum PerfRoles {
        DepartmentNameRole = Qt::UserRole + 1,
        ShortCodeRole,
        TotalGrievancesRole,
        ResolvedGrievancesRole,
        PendingGrievancesRole,
        AvgResolutionHoursRole,
        SlaCompliancePctRole,
        StatusGradeRole,
        BenchmarkSlaHoursRole,
        BudgetAllocatedRole
    };

    explicit DepartmentPerformanceModel(QObject *parent = nullptr);

    int rowCount(const QModelIndex &parent = QModelIndex()) const override;
    QVariant data(const QModelIndex &index, int role = Qt::DisplayRole) const override;
    QHash<int, QByteArray> roleNames() const override;

    void refreshData(const QString &period, const QString &constituency);

private:
    QVector<DepartmentPerfItem> m_items;
    void loadDefaultDepartmentStats();
};
