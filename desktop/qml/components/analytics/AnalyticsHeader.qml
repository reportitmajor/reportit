import QtQuick
import QtQuick.Controls

Rectangle {
    id: root

    implicitWidth: 1000
    implicitHeight: 68
    color: "#FFFFFF"
    border.color: "#E8EEF5"
    border.width: 1

    signal exportReportClicked()

    Row {
        anchors.fill: parent
        anchors.margins: 14
        spacing: 14

        // Title and Subtitle
        Column {
            width: 320
            spacing: 2
            anchors.verticalCenter: parent.verticalCenter

            Text {
                text: "Performance Intelligence & Civic SLA Analytics"
                font.pixelSize: 15
                font.weight: Font.Bold
                color: "#0F172A"
            }
            Text {
                text: "Citywide municipal velocity & statutory compliance monitoring"
                font.pixelSize: 11
                color: "#64748B"
            }
        }

        Item {
            width: parent.width - 320 - periodSelector.width - exportBtn.width - 50
            height: 1
        }

        // Time Period Segmented Buttons
        Row {
            id: periodSelector
            spacing: 4
            anchors.verticalCenter: parent.verticalCenter

            readonly property var periods: ["7 Days", "30 Days", "90 Days", "1 Year"]

            Repeater {
                model: parent.periods

                Rectangle {
                    width: periodTxt.implicitWidth + 20
                    height: 34
                    radius: 7
                    readonly property bool isSelected: analyticsController.selectedPeriod === modelData

                    color: isSelected ? "#4338CA" : "#F8FAFC"
                    border.color: isSelected ? "#4338CA" : "#CBD5E1"
                    border.width: 1

                    Text {
                        id: periodTxt
                        anchors.centerIn: parent
                        text: modelData
                        font.pixelSize: 11
                        font.weight: parent.isSelected ? Font.Bold : Font.Medium
                        color: parent.isSelected ? "#FFFFFF" : "#475569"
                    }

                    MouseArea {
                        anchors.fill: parent
                        cursorShape: Qt.PointingHandCursor
                        onClicked: analyticsController.setSelectedPeriod(modelData)
                    }
                }
            }
        }

        // Export Executive Briefing Button
        Rectangle {
            id: exportBtn
            width: 170
            height: 36
            radius: 8
            color: "#1E293B"
            anchors.verticalCenter: parent.verticalCenter

            Row {
                anchors.centerIn: parent
                spacing: 6

                Text { text: "📥"; font.pixelSize: 12 }
                Text {
                    text: "Export Executive Briefing"
                    font.pixelSize: 11
                    font.weight: Font.Bold
                    color: "#FFFFFF"
                }
            }

            MouseArea {
                anchors.fill: parent
                cursorShape: Qt.PointingHandCursor
                onClicked: root.exportReportClicked()
            }
        }
    }
}
