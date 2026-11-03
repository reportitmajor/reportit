import QtQuick
import QtQuick.Controls
import QtQuick.Layouts
import "../components"

Item {
    id: root

    implicitWidth: 1000
    implicitHeight: 800

    Flickable {
        id: flickable
        anchors.fill: parent
        contentWidth: width
        contentHeight: mainColumn.height + 40
        clip: true
        boundsBehavior: Flickable.StopAtBounds

        Column {
            id: mainColumn
            width: parent.width
            anchors.horizontalCenter: parent.horizontalCenter
            anchors.top: parent.top
            anchors.topMargin: 20
            spacing: 20
            padding: 24

            // ROW 1: 4 Key Metric Cards
            RowLayout {
                width: parent.width - 48
                spacing: 16

                MetricCard {
                    Layout.fillWidth: true
                    title: "Total Reports"
                    value: dashboardController.totalReports
                    subtitle: dashboardController.totalReportsTrend
                    subtitleColor: "#16A34A"
                    iconBg: "#EDE9FE"
                    iconColor: "#6D28D9"
                    iconType: "document"
                }

                MetricCard {
                    Layout.fillWidth: true
                    title: "Pending Resolution"
                    value: dashboardController.pendingResolution
                    subtitle: dashboardController.pendingResolutionTrend
                    subtitleColor: "#D97706"
                    iconBg: "#E0F2FE"
                    iconColor: "#0284C7"
                    iconType: "clipboard"
                }

                MetricCard {
                    Layout.fillWidth: true
                    title: "Resolved Today"
                    value: dashboardController.resolvedToday
                    subtitle: dashboardController.resolvedTodayTrend
                    subtitleColor: "#64748B"
                    iconBg: "#DCFCE7"
                    iconColor: "#16A34A"
                    iconType: "check"
                }

                MetricCard {
                    Layout.fillWidth: true
                    title: "Avg. Resolution Time"
                    value: dashboardController.avgResolutionTime
                    subtitle: dashboardController.avgResolutionTimeTrend
                    subtitleColor: "#16A34A"
                    iconBg: "#F3E8FF"
                    iconColor: "#7C3AED"
                    iconType: "timer"
                }
            }

            // ROW 2: Chart & AI Insights
            RowLayout {
                width: parent.width - 48
                spacing: 16

                AreaSplineChart {
                    Layout.fillWidth: true
                    Layout.preferredWidth: 620
                    Layout.minimumHeight: 330
                    dataPoints: dashboardController.currentChartData
                    labels: dashboardController.currentChartLabels
                    activePeriod: dashboardController.chartPeriod
                    onPeriodSelected: function(period) {
                        dashboardController.setChartPeriod(period)
                    }
                }

                AiInsightsCard {
                    Layout.preferredWidth: 340
                    Layout.minimumHeight: 330
                    insights: dashboardController.insightsList
                }
            }

            // ROW 3: Recent Reports & Category Breakdown
            RowLayout {
                width: parent.width - 48
                spacing: 16

                RecentReportsTable {
                    Layout.fillWidth: true
                    Layout.preferredWidth: 620
                    Layout.minimumHeight: 290
                    model: reportModel
                    onViewAllClicked: {
                        navigationController.currentRoute = "Reports"
                    }
                }

                CategoryBreakdown {
                    Layout.preferredWidth: 340
                    Layout.minimumHeight: 290
                    model: categoryModel
                }
            }
        }
    }
}
