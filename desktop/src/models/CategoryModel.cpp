#include "CategoryModel.h"

CategoryModel::CategoryModel(QObject *parent)
    : QAbstractListModel(parent)
{
    m_categories = {
        {"Infrastructure", 45, "#4338CA"},
        {"Public Safety", 28, "#3B82F6"},
        {"Sanitation", 15, "#06B6D4"},
        {"Environment", 12, "#38BDF8"}
    };
}

int CategoryModel::rowCount(const QModelIndex &parent) const
{
    if (parent.isValid())
        return 0;
    return static_cast<int>(m_categories.size());
}

QVariant CategoryModel::data(const QModelIndex &index, int role) const
{
    if (!index.isValid() || index.row() < 0 || index.row() >= m_categories.size())
        return QVariant();

    const CategoryItem &item = m_categories.at(index.row());

    switch (role) {
    case NameRole:
        return item.name;
    case PercentageRole:
        return item.percentage;
    case ColorRole:
        return item.color;
    default:
        return QVariant();
    }
}

QHash<int, QByteArray> CategoryModel::roleNames() const
{
    QHash<int, QByteArray> roles;
    roles[NameRole] = "name";
    roles[PercentageRole] = "percentage";
    roles[ColorRole] = "color";
    return roles;
}
