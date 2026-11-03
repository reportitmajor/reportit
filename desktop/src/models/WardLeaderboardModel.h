#pragma once

#include <QAbstractListModel>
#include <QString>
#include <QVector>

struct WardLeaderboardItem {
    bool isTopPerformer;     // true: Star Ward, false: Bottleneck Ward
    int rank;
    int wardNumber;
    QString wardName;
    QString acName;
    qreal slaCompliancePct;
    qreal avgResolutionHours;
    qreal csatRating;
    QString nodalOfficer;
    int backlogCount;
};

class WardLeaderboardModel : public QAbstractListModel {
    Q_OBJECT

public:
    enum LeaderboardRoles {
        IsTopPerformerRole = Qt::UserRole + 1,
        RankRole,
        WardNumberRole,
        WardNameRole,
        AcNameRole,
        SlaCompliancePctRole,
        AvgResolutionHoursRole,
        CsatRatingRole,
        NodalOfficerRole,
        BacklogCountRole
    };

    explicit WardLeaderboardModel(QObject *parent = nullptr);

    int rowCount(const QModelIndex &parent = QModelIndex()) const override;
    QVariant data(const QModelIndex &index, int role = Qt::DisplayRole) const override;
    QHash<int, QByteArray> roleNames() const override;

    void refreshData(const QString &period, const QString &constituency);

private:
    QVector<WardLeaderboardItem> m_items;
    void loadDefaultLeaderboard();
};
