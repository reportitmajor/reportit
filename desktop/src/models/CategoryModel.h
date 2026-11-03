#pragma once

#include <QAbstractListModel>
#include <QString>
#include <QVector>

struct CategoryItem {
    QString name;
    int percentage;
    QString color;
};

class CategoryModel : public QAbstractListModel {
    Q_OBJECT

public:
    enum CategoryRoles {
        NameRole = Qt::UserRole + 1,
        PercentageRole,
        ColorRole
    };

    explicit CategoryModel(QObject *parent = nullptr);

    int rowCount(const QModelIndex &parent = QModelIndex()) const override;
    QVariant data(const QModelIndex &index, int role = Qt::DisplayRole) const override;
    QHash<int, QByteArray> roleNames() const override;

private:
    QVector<CategoryItem> m_categories;
};
