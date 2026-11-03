import QtQuick
import QtQuick.Controls
import "../"

Rectangle {
    id: root

    readonly property var incident: mapController.selectedIncidentData
    readonly property bool hasIncident: mapController.selectedReportId !== ""

    implicitWidth: 380
    implicitHeight: 280
    radius: 12
    color: "#FFFFFF"
    border.color: "#E2E8F0"
    border.width: 1
    visible: hasIncident

    Column {
        anchors.fill: parent
        anchors.margins: 16
        spacing: 10

        // Header Row
        Row {
            width: parent.width

            Row {
                width: parent.width - closeBtn.width
                spacing: 8

                Text {
                    text: root.hasIncident ? root.incident.reportId : ""
                    font.pixelSize: 13
                    font.weight: Font.Bold
                    color: "#4338CA"
                    anchors.verticalCenter: parent.verticalCenter
                }

                StatusBadge {
                    anchors.verticalCenter: parent.verticalCenter
                    statusText: root.hasIncident ? root.incident.status : "PENDING"
                }

                Rectangle {
                    anchors.verticalCenter: parent.verticalCenter
                    height: 20
                    width: catText.implicitWidth + 10
                    radius: 4
                    color: "#EEF2FF"

                    Text {
                        id: catText
                        anchors.centerIn: parent
                        text: root.hasIncident ? root.incident.category : ""
                        font.pixelSize: 10
                        font.weight: Font.DemiBold
                        color: "#4338CA"
                    }
                }
            }

            Rectangle {
                id: closeBtn
                width: 24
                height: 24
                radius: 12
                color: "#F1F5F9"

                Text {
                    anchors.centerIn: parent
                    text: "✕"
                    font.pixelSize: 10
                    color: "#64748B"
                }

                MouseArea {
                    anchors.fill: parent
                    cursorShape: Qt.PointingHandCursor
                    onClicked: mapController.clearSelection()
                }
            }
        }

        // Title
        Text {
            width: parent.width
            text: root.hasIncident ? root.incident.issueTitle : ""
            font.pixelSize: 14
            font.weight: Font.Bold
            color: "#0F172A"
            wrapMode: Text.WordWrap
            maximumLineCount: 2
            elide: Text.ElideRight
        }

        // Location & Citizen Info
        Column {
            width: parent.width
            spacing: 3

            Item {
                width: parent.width
                height: 16

                Text {
                    id: incidentLocPin
                    text: "📍"
                    font.pixelSize: 11
                    anchors.left: parent.left
                    anchors.verticalCenter: parent.verticalCenter
                }
                Text {
                    anchors.left: incidentLocPin.right
                    anchors.leftMargin: 6
                    anchors.right: parent.right
                    anchors.verticalCenter: parent.verticalCenter
                    text: root.hasIncident ? (root.incident.location + " (" + root.incident.wardName + ")") : ""
                    font.pixelSize: 11
                    color: "#475569"
                    elide: Text.ElideRight
                }
            }

            Item {
                width: parent.width
                height: 16

                Text {
                    id: incidentCitizenIcon
                    text: "👤"
                    font.pixelSize: 11
                    anchors.left: parent.left
                    anchors.verticalCenter: parent.verticalCenter
                }
                Text {
                    anchors.left: incidentCitizenIcon.right
                    anchors.leftMargin: 6
                    anchors.right: parent.right
                    anchors.verticalCenter: parent.verticalCenter
                    text: root.hasIncident ? ("Reported by " + root.incident.citizenName + " • " + root.incident.date) : ""
                    font.pixelSize: 11
                    color: "#64748B"
                    elide: Text.ElideRight
                }
            }
        }

        // Description box
        Rectangle {
            width: parent.width
            height: 48
            radius: 6
            color: "#F8FAFC"
            border.color: "#EDF2F7"
            border.width: 1

            Text {
                anchors.fill: parent
                anchors.margins: 6
                text: root.hasIncident ? root.incident.description : ""
                font.pixelSize: 11
                color: "#475569"
                wrapMode: Text.WordWrap
                elide: Text.ElideRight
            }
        }

        // Action Buttons Row
        Row {
            width: parent.width
            spacing: 8

            // Status dropdown
            Rectangle {
                width: 130
                height: 32
                radius: 6
                color: "#F1F5F9"
                border.color: "#CBD5E1"
                border.width: 1

                Row {
                    anchors.centerIn: parent
                    spacing: 4

                    Text {
                        text: "Status: " + (root.hasIncident ? root.incident.status : "IN PROGRESS")
                        font.pixelSize: 10
                        font.weight: Font.Bold
                        color: "#334155"
                    }

                    Text { text: "▾"; font.pixelSize: 9; color: "#64748B" }
                }

                MouseArea {
                    anchors.fill: parent
                    cursorShape: Qt.PointingHandCursor
                    onClicked: statusMenu.open()
                }

                Menu {
                    id: statusMenu
                    y: parent.height + 2
                    width: 140

                    MenuItem {
                        text: "🔴 Critical"
                        onTriggered: mapController.updateIncidentStatus(root.incident.reportId, "CRITICAL")
                    }
                    MenuItem {
                        text: "🟡 In Progress"
                        onTriggered: mapController.updateIncidentStatus(root.incident.reportId, "IN PROGRESS")
                    }
                    MenuItem {
                        text: "🟢 Resolved"
                        onTriggered: mapController.updateIncidentStatus(root.incident.reportId, "RESOLVED")
                    }
                }
            }

            // Assign Field Team Button
            Rectangle {
                width: parent.width - 130 - 8
                height: 32
                radius: 6
                color: "#4338CA"

                Row {
                    anchors.centerIn: parent
                    spacing: 4

                    Text { text: "👷"; font.pixelSize: 11 }
                    Text {
                        text: root.hasIncident && root.incident.assignedTo !== "" ? root.incident.assignedTo : "Assign Officer"
                        font.pixelSize: 11
                        font.weight: Font.Bold
                        color: "#FFFFFF"
                        elide: Text.ElideRight
                    }
                }

                MouseArea {
                    anchors.fill: parent
                    cursorShape: Qt.PointingHandCursor
                    onClicked: {
                        mapController.assignOfficer(root.incident.reportId, "Er. R. Suresh (AEE)")
                    }
                }
            }
        }
    }
}
