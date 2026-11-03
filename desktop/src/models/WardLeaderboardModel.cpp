#include "WardLeaderboardModel.h"

WardLeaderboardModel::WardLeaderboardModel(QObject *parent)
    : QAbstractListModel(parent)
{
    loadDefaultLeaderboard();
}

void WardLeaderboardModel::loadDefaultLeaderboard()
{
    m_items = {
        // --- TOP 5 STAR PERFORMERS ---
        {
            true,
            1,
            49,
            "Rajarajeshwari Nagar",
            "Rajarajeshwari Nagar",
            97.8,
            8.4,
            4.85,
            "Er. R. Suresh (AEE)",
            2
        },
        {
            true,
            2,
            114,
            "Marathahalli",
            "Mahadevapura",
            96.4,
            11.2,
            4.75,
            "Er. S. Kumar (AEE)",
            4
        },
        {
            true,
            3,
            48,
            "Jnana Bharathi",
            "Rajarajeshwari Nagar",
            95.6,
            12.8,
            4.70,
            "Er. H. Nagaraj (AEE)",
            3
        },
        {
            true,
            4,
            243,
            "Kudlu",
            "Bommanahalli",
            94.8,
            13.5,
            4.65,
            "Smt. Geetha V. (Health Off.)",
            5
        },
        {
            true,
            5,
            65,
            "Kadu Malleshwaram",
            "Malleshwaram",
            94.2,
            14.1,
            4.60,
            "Er. B. Ramesh (AEE)",
            4
        },

        // --- BOTTOM 5 BOTTLENECK / DEFAULTER WARDS ---
        {
            false,
            1,
            33,
            "Kengeri",
            "Rajarajeshwari Nagar",
            78.4,
            44.2,
            3.40,
            "Er. P. Venkatesh (AEE)",
            28
        },
        {
            false,
            2,
            149,
            "Varthur",
            "Mahadevapura",
            79.2,
            41.8,
            3.55,
            "Er. C. Mohan (AEE)",
            34
        },
        {
            false,
            3,
            192,
            "Begur",
            "Bommanahalli",
            81.0,
            36.4,
            3.65,
            "Er. D. Prasad (AEE)",
            22
        },
        {
            false,
            4,
            32,
            "Ullal",
            "Rajarajeshwari Nagar",
            82.5,
            32.1,
            3.80,
            "Er. M. Shivakumar (AEE)",
            19
        },
        {
            false,
            5,
            174,
            "HSR Layout",
            "Bommanahalli",
            83.8,
            29.6,
            3.90,
            "Er. V. Gowda (AEE)",
            16
        }
    };
}

int WardLeaderboardModel::rowCount(const QModelIndex &parent) const
{
    if (parent.isValid()) return 0;
    return static_cast<int>(m_items.size());
}

QVariant WardLeaderboardModel::data(const QModelIndex &index, int role) const
{
    if (!index.isValid() || index.row() < 0 || index.row() >= m_items.size())
        return QVariant();

    const auto &item = m_items.at(index.row());
    switch (role) {
    case IsTopPerformerRole: return item.isTopPerformer;
    case RankRole: return item.rank;
    case WardNumberRole: return item.wardNumber;
    case WardNameRole: return item.wardName;
    case AcNameRole: return item.acName;
    case SlaCompliancePctRole: return item.slaCompliancePct;
    case AvgResolutionHoursRole: return item.avgResolutionHours;
    case CsatRatingRole: return item.csatRating;
    case NodalOfficerRole: return item.nodalOfficer;
    case BacklogCountRole: return item.backlogCount;
    default: return QVariant();
    }
}

QHash<int, QByteArray> WardLeaderboardModel::roleNames() const
{
    QHash<int, QByteArray> roles;
    roles[IsTopPerformerRole] = "isTopPerformer";
    roles[RankRole] = "rank";
    roles[WardNumberRole] = "wardNumber";
    roles[WardNameRole] = "wardName";
    roles[AcNameRole] = "acName";
    roles[SlaCompliancePctRole] = "slaCompliancePct";
    roles[AvgResolutionHoursRole] = "avgResolutionHours";
    roles[CsatRatingRole] = "csatRating";
    roles[NodalOfficerRole] = "nodalOfficer";
    roles[BacklogCountRole] = "backlogCount";
    return roles;
}

void WardLeaderboardModel::refreshData(const QString &period, const QString &constituency)
{
    beginResetModel();
    loadDefaultLeaderboard();
    endResetModel();
}
