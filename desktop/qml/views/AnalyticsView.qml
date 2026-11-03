import QtQuick
import QtQuick.Controls
import "../components/analytics"

Flickable {
    id: root

    implicitWidth: 1000
    implicitHeight: 800
    contentWidth: width
    contentHeight: mainCol.height + 40
    clip: true

    Column {
        id: mainCol
        width: parent.width - 40
        anchors.horizontalCenter: parent.horizontalCenter
        anchors.top: parent.top
        anchors.topMargin: 16
        spacing: 16

        // 1. Header (Period Segmented Control + Export)
        AnalyticsHeader {
            id: header
            width: parent.width
            height: 64

            onExportReportClicked: {
                var path = analyticsController.exportExecutiveReport();
                toastNotification.message = "Executive Briefing exported to " + path;
                toastNotification.visible = true;
                toastTimer.restart();
            }
        }

        // 2. 4-Card Executive KPI Metric Grid
        KpiMetricGrid {
            width: parent.width
            height: 100
        }

        // 3. Middle Row: SLA Velocity Spline Chart + Department Scorecard
        Row {
            width: parent.width
            height: 310
            spacing: 14

            SlaTrendSplineChart {
                width: parent.width - 394
                height: parent.height
            }

            DepartmentScorecard {
                width: 380
                height: parent.height
            }
        }

        // 4. Bottom Row: Ward Leaderboard + Chronic Hotspots Card
        Row {
            width: parent.width
            height: 250
            spacing: 14

            WardLeaderboardCard {
                width: parent.width - 394
                height: parent.height
            }

            ChronicHotspotsCard {
                width: 380
                height: parent.height
            }
        }
    }

    // Export Toast Notification Banner
    Rectangle {
        id: toastNotification
        property string message: ""
        visible: false
        anchors.bottom: parent.bottom
        anchors.horizontalCenter: parent.horizontalCenter
        anchors.bottomMargin: 24
        width: toastText.implicitWidth + 36
        height: 40
        radius: 20
        color: "#0F172A"

        Row {
            anchors.centerIn: parent
            spacing: 8

            Text { text: "✅"; font.pixelSize: 13 }
            Text {
                id: toastText
                text: toastNotification.message
                font.pixelSize: 11
                font.weight: Font.DemiBold
                color: "#F8FAFC"
            }
        }

        Timer {
            id: toastTimer
            interval: 4500
            onTriggered: toastNotification.visible = false
        }
    }
}
