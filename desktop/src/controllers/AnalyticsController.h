#pragma once

#include <QObject>
#include <QString>
#include <QVariantList>
#include <QVariantMap>
#include "../models/DepartmentPerformanceModel.h"
#include "../models/WardLeaderboardModel.h"

class AnalyticsController : public QObject {
    Q_OBJECT

    Q_PROPERTY(QString selectedPeriod READ selectedPeriod WRITE setSelectedPeriod NOTIFY periodChanged)
    Q_PROPERTY(QString selectedConstituency READ selectedConstituency WRITE setSelectedConstituency NOTIFY filterChanged)
    Q_PROPERTY(QString selectedDepartment READ selectedDepartment WRITE setSelectedDepartment NOTIFY filterChanged)

    Q_PROPERTY(qreal overallSlaCompliance READ overallSlaCompliance NOTIFY metricsChanged)
    Q_PROPERTY(qreal avgTurnaroundHours READ avgTurnaroundHours NOTIFY metricsChanged)
    Q_PROPERTY(int totalGrievances READ totalGrievances NOTIFY metricsChanged)
    Q_PROPERTY(qreal csatRating READ csatRating NOTIFY metricsChanged)
    Q_PROPERTY(QString budgetUtilizedFormatted READ budgetUtilizedFormatted NOTIFY metricsChanged)
    Q_PROPERTY(QString costPerResolutionFormatted READ costPerResolutionFormatted NOTIFY metricsChanged)
    Q_PROPERTY(QString topConstituencyName READ topConstituencyName NOTIFY metricsChanged)

    Q_PROPERTY(DepartmentPerformanceModel* departmentModel READ departmentModel CONSTANT)
    Q_PROPERTY(WardLeaderboardModel* leaderboardModel READ leaderboardModel CONSTANT)

    Q_PROPERTY(QVariantList chronicHotspots READ chronicHotspots NOTIFY metricsChanged)
    Q_PROPERTY(QVariantList monthlySlaSeries READ monthlySlaSeries NOTIFY metricsChanged)

public:
    explicit AnalyticsController(QObject *parent = nullptr);

    QString selectedPeriod() const { return m_selectedPeriod; }
    QString selectedConstituency() const { return m_selectedConstituency; }
    QString selectedDepartment() const { return m_selectedDepartment; }

    qreal overallSlaCompliance() const { return m_overallSlaCompliance; }
    qreal avgTurnaroundHours() const { return m_avgTurnaroundHours; }
    int totalGrievances() const { return m_totalGrievances; }
    qreal csatRating() const { return m_csatRating; }
    QString budgetUtilizedFormatted() const { return m_budgetUtilizedFormatted; }
    QString costPerResolutionFormatted() const { return m_costPerResolutionFormatted; }
    QString topConstituencyName() const { return m_topConstituencyName; }

    DepartmentPerformanceModel* departmentModel() { return &m_departmentModel; }
    WardLeaderboardModel* leaderboardModel() { return &m_leaderboardModel; }

    QVariantList chronicHotspots() const { return m_chronicHotspots; }
    QVariantList monthlySlaSeries() const { return m_monthlySlaSeries; }

    Q_INVOKABLE void setSelectedPeriod(const QString &period);
    Q_INVOKABLE void setSelectedConstituency(const QString &acName);
    Q_INVOKABLE void setSelectedDepartment(const QString &deptName);
    Q_INVOKABLE QString exportExecutiveReport();

signals:
    void periodChanged();
    void filterChanged();
    void metricsChanged();

private:
    QString m_selectedPeriod;
    QString m_selectedConstituency;
    QString m_selectedDepartment;

    qreal m_overallSlaCompliance;
    qreal m_avgTurnaroundHours;
    int m_totalGrievances;
    qreal m_csatRating;
    QString m_budgetUtilizedFormatted;
    QString m_costPerResolutionFormatted;
    QString m_topConstituencyName;

    DepartmentPerformanceModel m_departmentModel;
    WardLeaderboardModel m_leaderboardModel;

    QVariantList m_chronicHotspots;
    QVariantList m_monthlySlaSeries;

    void updateMetrics();
};
