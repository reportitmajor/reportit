import QtQuick
import QtQuick.Controls

Rectangle {
    id: root

    implicitWidth: 380
    implicitHeight: 320
    radius: 10
    color: "#FFFFFF"
    border.color: "#E2E8F0"
    border.width: 1

    Column {
        anchors.fill: parent
        anchors.margins: 14
        spacing: 10

        // Header
        Row {
            width: parent.width
            Column {
                width: parent.width - 70
                spacing: 2
                Text {
                    text: "Department Scorecard"
                    font.pixelSize: 13
                    font.weight: Font.Bold
                    color: "#0F172A"
                }
                Text {
                    text: "SLA compliance & average resolution turnaround"
                    font.pixelSize: 10
                    color: "#64748B"
                }
            }

            Rectangle {
                width: 60
                height: 22
                radius: 11
                color: "#F1F5F9"
                anchors.verticalCenter: parent.verticalCenter
                Text { anchors.centerIn: parent; text: "6 WINGS"; font.pixelSize: 9; font.weight: Font.Bold; color: "#475569" }
            }
        }

        // List of Departments
        ListView {
            id: deptList
            width: parent.width
            height: parent.height - 45
            model: analyticsController.departmentModel
            clip: true
            spacing: 8

            delegate: Rectangle {
                width: deptList.width
                height: 48
                radius: 6
                color: "#F8FAFC"
                border.color: "#E2E8F0"
                border.width: 1

                Column {
                    anchors.fill: parent
                    anchors.margins: 6
                    spacing: 4

                    // Top Row: Dept Name + Turnaround Hours + SLA %
                    Row {
                        width: parent.width

                        Text {
                            width: parent.width - 140
                            text: model.departmentName
                            font.pixelSize: 11
                            font.weight: Font.DemiBold
                            color: "#1E293B"
                            elide: Text.ElideRight
                        }

                        Text {
                            width: 65
                            text: "⏱ " + model.avgResolutionHours.toFixed(1) + "h avg"
                            font.pixelSize: 9
                            color: "#64748B"
                            horizontalAlignment: Text.AlignRight
                        }

                        Text {
                            width: 65
                            text: model.slaCompliancePct.toFixed(1) + "% SLA"
                            font.pixelSize: 10
                            font.weight: Font.Bold
                            color: model.slaCompliancePct >= 90.0 ? "#16A34A" : (model.slaCompliancePct >= 85.0 ? "#D97706" : "#DC2626")
                            horizontalAlignment: Text.AlignRight
                        }
                    }

                    // Bottom Row: Progress Bar + Counts
                    Row {
                        width: parent.width
                        spacing: 8

                        // Progress Bar
                        Rectangle {
                            width: parent.width - 90
                            height: 6
                            radius: 3
                            color: "#E2E8F0"
                            anchors.verticalCenter: parent.verticalCenter

                            Rectangle {
                                width: parent.width * (model.slaCompliancePct / 100.0)
                                height: parent.height
                                radius: 3
                                color: model.slaCompliancePct >= 90.0 ? "#16A34A" : (model.slaCompliancePct >= 85.0 ? "#F59E0B" : "#EF4444")
                            }
                        }

                        Text {
                            text: model.resolvedGrievances + " / " + model.totalGrievances + " done"
                            font.pixelSize: 9
                            color: "#64748B"
                            anchors.verticalCenter: parent.verticalCenter
                        }
                    }
                }
            }
        }
    }
}
