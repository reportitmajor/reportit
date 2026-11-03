cmake_minimum_required(VERSION 3.20)

project(ReportItAdmin VERSION 1.0.0 LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

set(CMAKE_AUTOMOC ON)
set(CMAKE_AUTORCC ON)
set(CMAKE_AUTOUIC ON)

find_package(Qt6 6.5 REQUIRED COMPONENTS
    Core
    Gui
    Qml
    Quick
    QuickControls2
    Svg
    Network
)

qt_standard_project_setup(REQUIRES 6.5)

qt_add_executable(ReportItAdmin
    src/main.cpp
    src/controllers/DashboardController.h
    src/controllers/DashboardController.cpp
    src/controllers/NavigationController.h
    src/controllers/NavigationController.cpp
    src/controllers/MapController.h
    src/controllers/MapController.cpp
    src/controllers/ReportManagementController.h
    src/controllers/ReportManagementController.cpp
    src/controllers/FirestoreSyncService.h
    src/controllers/FirestoreSyncService.cpp
    src/controllers/AnalyticsController.h
    src/controllers/AnalyticsController.cpp
    src/controllers/SettingsController.h
    src/controllers/SettingsController.cpp
    src/models/ReportModel.h
    src/models/ReportModel.cpp
    src/models/CategoryModel.h
    src/models/CategoryModel.cpp
    src/models/WardBoundaryModel.h
    src/models/WardBoundaryModel.cpp
    src/models/IncidentMarkerModel.h
    src/models/IncidentMarkerModel.cpp
    src/models/DetailedReportModel.h
    src/models/DetailedReportModel.cpp
    src/models/DepartmentPerformanceModel.h
    src/models/DepartmentPerformanceModel.cpp
    src/models/WardLeaderboardModel.h
    src/models/WardLeaderboardModel.cpp
)

qt_add_qml_module(ReportItAdmin
    URI "ReportIt.Admin"
    VERSION 1.0
    QML_FILES
        qml/Main.qml
        qml/Theme.qml
        qml/components/Sidebar.qml
        qml/components/TopHeader.qml
        qml/components/MetricCard.qml
        qml/components/AreaSplineChart.qml
        qml/components/AiInsightsCard.qml
        qml/components/RecentReportsTable.qml
        qml/components/CategoryBreakdown.qml
        qml/components/StatusBadge.qml
        qml/components/map/MapControlBar.qml
        qml/components/map/GisCanvas.qml
        qml/components/map/WardInspectorPanel.qml
        qml/components/map/IncidentDetailCard.qml
        qml/components/map/MapFloatingControls.qml
        qml/components/reports/ReportControlBar.qml
        qml/components/reports/ReportTable.qml
        qml/components/reports/ReportDetailDrawer.qml
        qml/components/reports/DualPhotoInspector.qml
        qml/components/reports/AuditTimeline.qml
        qml/components/analytics/AnalyticsHeader.qml
        qml/components/analytics/KpiMetricGrid.qml
        qml/components/analytics/SlaTrendSplineChart.qml
        qml/components/analytics/DepartmentScorecard.qml
        qml/components/analytics/WardLeaderboardCard.qml
        qml/components/analytics/ChronicHotspotsCard.qml
        qml/components/settings/SettingsSidebarNav.qml
        qml/components/settings/ProfileSettingsCard.qml
        qml/components/settings/SlaConfigCard.qml
        qml/components/settings/AiRoutingCard.qml
        qml/components/settings/PrivacySecurityCard.qml
        qml/components/settings/SystemSyncCard.qml
        qml/views/DashboardView.qml
        qml/views/MapView.qml
        qml/views/ReportsView.qml
        qml/views/AnalyticsView.qml
        qml/views/SettingsView.qml
        qml/views/PlaceholderView.qml
    RESOURCES
        resources/gis/bengaluru_all_wards.geojson
        resources/images/logo.png
        resources/images/logo_dark.png
)

target_include_directories(ReportItAdmin PRIVATE src)

target_link_libraries(ReportItAdmin PRIVATE
    Qt6::Core
    Qt6::Gui
    Qt6::Qml
    Qt6::Quick
    Qt6::QuickControls2
    Qt6::Svg
    Qt6::Network
)

if (WIN32)
    set_target_properties(ReportItAdmin PROPERTIES
        WIN32_EXECUTABLE TRUE
    )
endif()
