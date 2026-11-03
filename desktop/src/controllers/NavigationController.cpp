#include "NavigationController.h"

NavigationController::NavigationController(QObject *parent)
    : QObject(parent)
    , m_currentRoute("Dashboard")
{
}

void NavigationController::setCurrentRoute(const QString &route)
{
    if (m_currentRoute != route) {
        m_currentRoute = route;
        emit currentRouteChanged();
    }
}
