#pragma once

#include <QAbstractListModel>
#include <QString>
#include <QVector>

struct ReportItem {
    QString reportId;
    QString issueTitle;
    QString location;
    QString status;      // "IN PROGRESS", "RESOLVED", "CRITICAL", "PENDING"
    QString date;
    QString category;
};

class ReportModel : public QAbstractListModel {
    Q_OBJECT

public:
    enum ReportRoles {
        ReportIdRole = Qt::UserRole + 1,
        IssueTitleRole,
        LocationRole,
        StatusRole,
        DateRole,
        CategoryRole
    };

    explicit ReportModel(QObject *parent = nullptr);

    int rowCount(const QModelIndex &parent = QModelIndex()) const override;
    QVariant data(const QModelIndex &index, int role = Qt::DisplayRole) const override;
    QHash<int, QByteArray> roleNames() const override;

    Q_INVOKABLE void addReport(const QString &id, const QString &issue, const QString &location, const QString &status, const QString &date, const QString &category);
    Q_INVOKABLE void filterByCategory(const QString &category);
    Q_INVOKABLE void searchReports(const QString &keyword);
    Q_INVOKABLE void resetFilter();
    void setReportsFromCloud(const QVector<ReportItem> &items);

private:
    QVector<ReportItem> m_allReports;
    QVector<ReportItem> m_displayedReports;
    void loadSampleData();
};
