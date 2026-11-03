#pragma once

#include <QObject>
#include <QString>

class NavigationController : public QObject {
    Q_OBJECT

    Q_PROPERTY(QString currentRoute READ currentRoute WRITE setCurrentRoute NOTIFY currentRouteChanged)

public:
    explicit NavigationController(QObject *parent = nullptr);

    QString currentRoute() const { return m_currentRoute; }
    void setCurrentRoute(const QString &route);

signals:
    void currentRouteChanged();

private:
    QString m_currentRoute;
};
