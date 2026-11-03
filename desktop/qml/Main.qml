import QtQuick
import QtQuick.Controls
import QtQuick.Layouts
import "components"
import "views"

ApplicationWindow {
    id: window
    visible: true
    width: 1360
    height: 860
    minimumWidth: 1100
    minimumHeight: 720
    title: "ReportIt Admin - City Oversight"
    color: "#F4F6FB"

    function toggleFullScreen() {
        if (window.visibility === Window.FullScreen) {
            window.showNormal()
        } else {
            window.showFullScreen()
        }
    }

    // F11 Fullscreen Toggle
    Shortcut {
        sequence: "F11"
        context: Qt.ApplicationShortcut
        onActivated: window.toggleFullScreen()
    }

    // Escape to exit Fullscreen
    Shortcut {
        sequence: "Escape"
        context: Qt.ApplicationShortcut
        enabled: window.visibility === Window.FullScreen
        onActivated: window.showNormal()
    }

    Item {
        id: rootContainer
        anchors.fill: parent

        // Left Sidebar Navigation
        Sidebar {
            id: sidebar
            anchors.left: parent.left
            anchors.top: parent.top
            anchors.bottom: parent.bottom
            width: 210
            currentRoute: navigationController.currentRoute
            onRouteSelected: function(route) {
                navigationController.currentRoute = route
            }
        }

        // Top Header Bar
        TopHeader {
            id: topHeader
            anchors.top: parent.top
            anchors.left: sidebar.right
            anchors.right: parent.right
            height: 64
            isFullscreen: window.visibility === Window.FullScreen
            onFullscreenClicked: window.toggleFullScreen()
            onSearchSubmitted: function(query) {
                dashboardController.filterReports(query)
            }
        }

        // Main View Area
        Item {
            anchors.top: topHeader.bottom
            anchors.left: sidebar.right
            anchors.right: parent.right
            anchors.bottom: parent.bottom

            DashboardView {
                anchors.fill: parent
                visible: navigationController.currentRoute === "Dashboard"
            }

            MapView {
                anchors.fill: parent
                visible: navigationController.currentRoute === "Map View"
            }

            ReportsView {
                anchors.fill: parent
                visible: navigationController.currentRoute === "Reports"
            }

            AnalyticsView {
                anchors.fill: parent
                visible: navigationController.currentRoute === "Analytics"
            }

            SettingsView {
                anchors.fill: parent
                visible: navigationController.currentRoute === "Settings"
            }

            PlaceholderView {
                anchors.fill: parent
                visible: navigationController.currentRoute !== "Dashboard" && navigationController.currentRoute !== "Map View" && navigationController.currentRoute !== "Reports" && navigationController.currentRoute !== "Analytics" && navigationController.currentRoute !== "Settings"
                title: navigationController.currentRoute
                description: "The " + navigationController.currentRoute + " section will be implemented next."
            }
        }
    }
}
