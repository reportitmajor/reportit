import QtQuick
import QtQuick.Controls
import QtQuick.Layouts
import "../components/reports"

Item {
    id: root

    implicitWidth: 1000
    implicitHeight: 800

    Column {
        anchors.fill: parent
        spacing: 14

        // 1. Top Control Bar (Search, Status Tabs, Category Filter & Export)
        ReportControlBar {
            id: controlBar
            width: parent.width
            height: 110

            onExportSuccess: function(path) {
                toastNotification.message = "Successfully exported to " + path;
                toastNotification.visible = true;
                toastTimer.restart();
            }
        }

        // 2. Main Content Area (Table + Detail Drawer)
        Item {
            width: parent.width
            height: parent.height - 124

            // Table Container
            ReportTable {
                anchors.left: parent.left
                anchors.right: detailDrawer.visible ? detailDrawer.left : parent.right
                anchors.rightMargin: detailDrawer.visible ? 14 : 0
                anchors.top: parent.top
                anchors.bottom: parent.bottom
            }

            // Right Slide-Out Inspection & Work Order Drawer
            ReportDetailDrawer {
                id: detailDrawer
                anchors.right: parent.right
                anchors.top: parent.top
                anchors.bottom: parent.bottom
                width: 380
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
            interval: 4000
            onTriggered: toastNotification.visible = false
        }
    }
}
