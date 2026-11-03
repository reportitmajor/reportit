import QtQuick
import QtQuick.Controls
import "../"

Rectangle {
    id: root

    implicitWidth: 1000
    implicitHeight: 600
    color: "#FFFFFF"
    border.color: "#E8EEF5"
    border.width: 1
    radius: 10
    clip: true

    Column {
        anchors.fill: parent

        // 1. Table Header
        Rectangle {
            width: parent.width
            height: 42
            color: "#F8FAFC"
            border.color: "#E2E8F0"
            border.width: 1

            Row {
                anchors.fill: parent
                anchors.leftMargin: 14
                anchors.rightMargin: 14
                spacing: 10

                Text { width: 250; text: "GRIEVANCE & CITIZEN"; font.pixelSize: 10; font.weight: Font.Bold; font.letterSpacing: 0.5; color: "#64748B"; anchors.verticalCenter: parent.verticalCenter }
                Text { width: 145; text: "CATEGORY / DEPT"; font.pixelSize: 10; font.weight: Font.Bold; font.letterSpacing: 0.5; color: "#64748B"; anchors.verticalCenter: parent.verticalCenter }
                Text { width: 155; text: "WARD & MLA"; font.pixelSize: 10; font.weight: Font.Bold; font.letterSpacing: 0.5; color: "#64748B"; anchors.verticalCenter: parent.verticalCenter }
                Text { width: 115; text: "SLA TIMELINE"; font.pixelSize: 10; font.weight: Font.Bold; font.letterSpacing: 0.5; color: "#64748B"; anchors.verticalCenter: parent.verticalCenter }
                Text { width: 105; text: "STATUS"; font.pixelSize: 10; font.weight: Font.Bold; font.letterSpacing: 0.5; color: "#64748B"; anchors.verticalCenter: parent.verticalCenter }
                Text { width: 140; text: "ASSIGNED OFFICER"; font.pixelSize: 10; font.weight: Font.Bold; font.letterSpacing: 0.5; color: "#64748B"; anchors.verticalCenter: parent.verticalCenter }
                Text { width: 85; text: "ACTION"; font.pixelSize: 10; font.weight: Font.Bold; font.letterSpacing: 0.5; color: "#64748B"; anchors.verticalCenter: parent.verticalCenter }
            }
        }

        // 2. Table Rows List
        ListView {
            id: listView
            width: parent.width
            height: parent.height - 42
            model: reportController.reportModel
            clip: true
            spacing: 0

            delegate: Rectangle {
                width: listView.width
                height: 64
                readonly property bool isSelected: reportController.selectedReportId === model.reportId
                color: isSelected ? "#F1F5F9" : (index % 2 === 0 ? "#FFFFFF" : "#FAFCFF")
                border.color: isSelected ? "#4338CA" : "#F1F5F9"
                border.width: isSelected ? 1 : 0

                Row {
                    anchors.fill: parent
                    anchors.leftMargin: 14
                    anchors.rightMargin: 14
                    spacing: 10

                    // Evidence Thumbnail Preview
                    Rectangle {
                        width: 40
                        height: 40
                        radius: 6
                        color: "#E2E8F0"
                        clip: true
                        anchors.verticalCenter: parent.verticalCenter

                        Image {
                            anchors.fill: parent
                            source: model.citizenPhotoUrl || ""
                            fillMode: Image.PreserveAspectCrop
                            asynchronous: true
                            visible: status === Image.Ready
                        }

                        Text {
                            anchors.centerIn: parent
                            text: "📷"
                            font.pixelSize: 13
                            visible: !model.citizenPhotoUrl || model.citizenPhotoUrl === ""
                        }
                    }

                    // Column 1: Grievance ID & Title + Citizen (width 200)
                    Column {
                        width: 200
                        spacing: 2
                        anchors.verticalCenter: parent.verticalCenter

                        Row {
                            spacing: 6
                            Text {
                                text: model.reportId
                                font.pixelSize: 11
                                font.weight: Font.Bold
                                color: "#4338CA"
                            }
                            Rectangle {
                                width: sevTxt.implicitWidth + 8
                                height: 16
                                radius: 4
                                color: model.severity === "CRITICAL" ? "#FEE2E2" : (model.severity === "HIGH" ? "#FEF3C7" : "#F1F5F9")
                                anchors.verticalCenter: parent.verticalCenter

                                Text {
                                    id: sevTxt
                                    anchors.centerIn: parent
                                    text: model.severity
                                    font.pixelSize: 8
                                    font.weight: Font.Bold
                                    color: model.severity === "CRITICAL" ? "#B91C1C" : (model.severity === "HIGH" ? "#B45309" : "#475569")
                                }
                            }
                        }

                        Text {
                            width: parent.width
                            text: model.issueTitle
                            font.pixelSize: 11
                            font.weight: Font.DemiBold
                            color: "#0F172A"
                            elide: Text.ElideRight
                        }

                        Text {
                            width: parent.width
                            text: "By " + model.citizenName + " • " + model.createdDate
                            font.pixelSize: 9
                            color: "#64748B"
                            elide: Text.ElideRight
                        }
                    }

                    // Column 2: Category & Dept (width 145)
                    Column {
                        width: 145
                        spacing: 2
                        anchors.verticalCenter: parent.verticalCenter

                        Text {
                            width: parent.width
                            text: model.category
                            font.pixelSize: 11
                            font.weight: Font.DemiBold
                            color: "#1E293B"
                            elide: Text.ElideRight
                        }
                        Text {
                            width: parent.width
                            text: model.department
                            font.pixelSize: 9
                            color: "#64748B"
                            elide: Text.ElideRight
                        }
                    }

                    // Column 3: Ward & MLA (width 155)
                    Column {
                        width: 155
                        spacing: 2
                        anchors.verticalCenter: parent.verticalCenter

                        Text {
                            width: parent.width
                            text: {
                                var wName = model.wardName || ""
                                if (wName.toLowerCase().startsWith("ward")) return wName
                                return "Ward " + model.wardNumber + (wName ? " - " + wName : "")
                            }
                            font.pixelSize: 11
                            font.weight: Font.DemiBold
                            color: "#1E293B"
                            elide: Text.ElideRight
                        }
                        Text {
                            width: parent.width
                            text: (model.acName ? model.acName : "BBMP") + (model.mlaName ? " (MLA: " + model.mlaName + ")" : "")
                            font.pixelSize: 9
                            color: "#64748B"
                            elide: Text.ElideRight
                        }
                    }

                    // Column 4: SLA Clock (width 115)
                    Column {
                        width: 115
                        spacing: 2
                        anchors.verticalCenter: parent.verticalCenter

                        Rectangle {
                            width: Math.min(parent.width, slaTxt.implicitWidth + 10)
                            height: 20
                            radius: 10
                            color: {
                                if (model.status === "RESOLVED") return "#DCFCE7"
                                if (model.slaBreached) return "#FEE2E2"
                                if (model.slaHoursLeft <= 4) return "#FEF3C7"
                                return "#F1F5F9"
                            }

                            Row {
                                id: slaTxt
                                anchors.centerIn: parent
                                spacing: 4

                                Text {
                                    text: {
                                        if (model.status === "RESOLVED") return "✓ SLA Met"
                                        if (model.slaBreached) return "⚠️ Breached"
                                        return "⏱ " + model.slaHoursLeft + "h left"
                                    }
                                    font.pixelSize: 9
                                    font.weight: Font.Bold
                                    color: {
                                        if (model.status === "RESOLVED") return "#15803D"
                                        if (model.slaBreached) return "#B91C1C"
                                        if (model.slaHoursLeft <= 4) return "#B45309"
                                        return "#475569"
                                    }
                                }
                            }
                        }

                        Text {
                            width: parent.width
                            text: model.slaDeadlineText
                            font.pixelSize: 8
                            color: "#94A3B8"
                            elide: Text.ElideRight
                        }
                    }

                    // Column 5: Status Badge (width 105)
                    Item {
                        width: 105
                        height: parent.height
                        anchors.verticalCenter: parent.verticalCenter

                        StatusBadge {
                            anchors.verticalCenter: parent.verticalCenter
                            statusText: model.status
                        }
                    }

                    // Column 6: Assigned Officer (width 140)
                    Column {
                        width: 140
                        spacing: 2
                        anchors.verticalCenter: parent.verticalCenter

                        Row {
                            width: parent.width
                            spacing: 4

                            Text { text: "👷"; font.pixelSize: 10 }
                            Text {
                                width: parent.width - 18
                                text: model.assignedOfficer !== "" ? model.assignedOfficer : "Unassigned"
                                font.pixelSize: 11
                                font.weight: Font.DemiBold
                                color: model.assignedOfficer !== "" ? "#0F172A" : "#EF4444"
                                elide: Text.ElideRight
                            }
                        }
                        Text {
                            width: parent.width
                            text: model.workOrderToken !== "" ? model.workOrderToken : "Pending Work Order"
                            font.pixelSize: 9
                            color: "#64748B"
                            elide: Text.ElideRight
                        }
                    }

                    // Column 7: Action Button (width 85)
                    Rectangle {
                        width: 85
                        height: 28
                        radius: 6
                        color: "#EEF2FF"
                        border.color: "#C7D2FE"
                        border.width: 1
                        anchors.verticalCenter: parent.verticalCenter

                        Text {
                            anchors.centerIn: parent
                            text: "Inspect ➔"
                            font.pixelSize: 10
                            font.weight: Font.Bold
                            color: "#4338CA"
                        }

                        MouseArea {
                            anchors.fill: parent
                            cursorShape: Qt.PointingHandCursor
                            onClicked: reportController.selectReport(model.reportId)
                        }
                    }
                }

                // Row Click
                MouseArea {
                    anchors.fill: parent
                    cursorShape: Qt.PointingHandCursor
                    z: -1
                    onClicked: reportController.selectReport(model.reportId)
                }

                // Divider
                Rectangle {
                    anchors.bottom: parent.bottom
                    width: parent.width
                    height: 1
                    color: "#F1F5F9"
                }
            }
        }
    }
}
