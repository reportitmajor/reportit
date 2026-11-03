import QtQuick
import QtQuick.Controls

Rectangle {
    id: root

    readonly property var wardData: mapController.selectedWardData
    readonly property bool hasWard: mapController.selectedWardNumber > 0 && root.wardData && root.wardData.wardNumber !== undefined

    implicitWidth: 340
    implicitHeight: 600
    color: "#FFFFFF"
    border.color: "#E8EEF5"
    border.width: 1
    radius: 12

    Column {
        anchors.fill: parent
        anchors.margins: 18
        spacing: 14

        // 1. Header Row
        Row {
            width: parent.width

            Column {
                width: parent.width - closeBtn.width
                spacing: 2

                Text {
                    text: root.hasWard ? ("Ward " + (root.wardData.wardNumber || "") + " Details") : "City Overview"
                    font.pixelSize: 15
                    font.weight: Font.Bold
                    color: "#0F172A"
                }

                Text {
                    text: root.hasWard ? (root.wardData.wardName || "") : mapController.selectedConstituency
                    font.pixelSize: 12
                    font.weight: Font.DemiBold
                    color: "#4338CA"
                }
            }

            Rectangle {
                id: closeBtn
                width: 28
                height: 28
                radius: 14
                color: "#F1F5F9"
                visible: root.hasWard

                Text {
                    anchors.centerIn: parent
                    text: "✕"
                    font.pixelSize: 11
                    color: "#64748B"
                }

                MouseArea {
                    anchors.fill: parent
                    cursorShape: Qt.PointingHandCursor
                    onClicked: mapController.selectWard(0)
                }
            }
        }

        // Divider
        Rectangle {
            width: parent.width
            height: 1
            color: "#F1F5F9"
        }

        // 2. Official Government Jurisdiction Badges
        Rectangle {
            width: parent.width
            height: 72
            radius: 8
            color: "#F8FAFC"
            border.color: "#E2E8F0"
            border.width: 1

            Column {
                anchors.fill: parent
                anchors.margins: 10
                spacing: 4

                Row {
                    spacing: 6
                    Text { text: "🏛️"; font.pixelSize: 12 }
                    Text {
                        text: "MLA: " + (root.hasWard ? (root.wardData.mlaName || "Munirathna") : "Munirathna") + " (" + (root.hasWard ? (root.wardData.acName || mapController.selectedConstituency) : mapController.selectedConstituency) + ")"
                        font.pixelSize: 11
                        font.weight: Font.DemiBold
                        color: "#1E293B"
                    }
                }

                Row {
                    spacing: 6
                    Text { text: "🏢"; font.pixelSize: 12 }
                    Text {
                        text: "Zone: " + (root.hasWard ? (root.wardData.gbaZone || "Greater Bengaluru Authority") : "Greater Bengaluru Authority")
                        font.pixelSize: 11
                        font.weight: Font.Normal
                        color: "#64748B"
                    }
                }
            }
        }

        // 3. Official Ward Contacts
        Column {
            width: parent.width
            spacing: 6

            Text {
                text: "OFFICIAL WARD CONTACTS"
                font.pixelSize: 10
                font.weight: Font.Bold
                font.letterSpacing: 0.6
                color: "#94A3B8"
            }

            Rectangle {
                width: parent.width
                height: 60
                radius: 8
                color: "#F8FAFC"
                border.color: "#E2E8F0"
                border.width: 1

                Column {
                    anchors.fill: parent
                    anchors.margins: 8
                    spacing: 2

                    Text {
                        text: "👷 Nodal Engineer: " + (root.hasWard ? (root.wardData.aeeName || "Er. R. Suresh (AEE)") : "Er. R. Suresh (AEE)")
                        font.pixelSize: 11
                        font.weight: Font.DemiBold
                        color: "#1E293B"
                    }

                    Text {
                        text: "📞 Helpline: " + (root.hasWard ? (root.wardData.aeeContact || "+91 80 2860 4122") : "+91 80 2860 4122")
                        font.pixelSize: 11
                        color: "#4338CA"
                    }
                }
            }
        }

        // 4. Live Ward Incident KPI Cards
        Column {
            width: parent.width
            spacing: 8

            Text {
                text: "LIVE INCIDENT METRICS"
                font.pixelSize: 10
                font.weight: Font.Bold
                font.letterSpacing: 0.6
                color: "#94A3B8"
            }

            Grid {
                width: parent.width
                columns: 2
                spacing: 8

                // Total
                Rectangle {
                    width: (parent.width - 8) / 2
                    height: 54
                    radius: 8
                    color: "#F8FAFC"
                    border.color: "#E2E8F0"
                    border.width: 1

                    Column {
                        anchors.centerIn: parent
                        spacing: 1

                        Text {
                            text: root.hasWard && root.wardData.totalComplaints !== undefined ? ("" + root.wardData.totalComplaints) : "124"
                            font.pixelSize: 16
                            font.weight: Font.Bold
                            color: "#0F172A"
                            anchors.horizontalCenter: parent.horizontalCenter
                        }
                        Text {
                            text: "Total Complaints"
                            font.pixelSize: 10
                            color: "#64748B"
                            anchors.horizontalCenter: parent.horizontalCenter
                        }
                    }
                }

                // Critical
                Rectangle {
                    width: (parent.width - 8) / 2
                    height: 54
                    radius: 8
                    color: "#FEF2F2"
                    border.color: "#FCA5A5"
                    border.width: 1

                    Column {
                        anchors.centerIn: parent
                        spacing: 1

                        Text {
                            text: root.hasWard && root.wardData.criticalCount !== undefined ? ("" + root.wardData.criticalCount) : "14"
                            font.pixelSize: 16
                            font.weight: Font.Bold
                            color: "#DC2626"
                            anchors.horizontalCenter: parent.horizontalCenter
                        }
                        Text {
                            text: "🔴 Critical"
                            font.pixelSize: 10
                            font.weight: Font.Bold
                            color: "#B91C1C"
                            anchors.horizontalCenter: parent.horizontalCenter
                        }
                    }
                }

                // In Progress
                Rectangle {
                    width: (parent.width - 8) / 2
                    height: 54
                    radius: 8
                    color: "#FFFBEB"
                    border.color: "#FDE68A"
                    border.width: 1

                    Column {
                        anchors.centerIn: parent
                        spacing: 1

                        Text {
                            text: root.hasWard && root.wardData.inProgressCount !== undefined ? ("" + root.wardData.inProgressCount) : "68"
                            font.pixelSize: 16
                            font.weight: Font.Bold
                            color: "#D97706"
                            anchors.horizontalCenter: parent.horizontalCenter
                        }
                        Text {
                            text: "🟡 In Progress"
                            font.pixelSize: 10
                            font.weight: Font.Bold
                            color: "#B45309"
                            anchors.horizontalCenter: parent.horizontalCenter
                        }
                    }
                }

                // Resolved
                Rectangle {
                    width: (parent.width - 8) / 2
                    height: 54
                    radius: 8
                    color: "#F0FDF4"
                    border.color: "#86EFAC"
                    border.width: 1

                    Column {
                        anchors.centerIn: parent
                        spacing: 1

                        Text {
                            text: root.hasWard && root.wardData.resolvedCount !== undefined ? ("" + root.wardData.resolvedCount) : "42"
                            font.pixelSize: 16
                            font.weight: Font.Bold
                            color: "#16A34A"
                            anchors.horizontalCenter: parent.horizontalCenter
                        }
                        Text {
                            text: "🟢 Resolved"
                            font.pixelSize: 10
                            font.weight: Font.Bold
                            color: "#15803D"
                            anchors.horizontalCenter: parent.horizontalCenter
                        }
                    }
                }
            }
        }

        // 5. Resolution Rate Progress Bar
        Column {
            width: parent.width
            spacing: 4

            Row {
                width: parent.width
                Text {
                    text: "Resolution Rate"
                    font.pixelSize: 11
                    font.weight: Font.DemiBold
                    color: "#334155"
                }
                Text {
                    width: parent.width - 90
                    text: (root.hasWard ? root.wardData.resolutionRate : 78.4) + "%"
                    font.pixelSize: 11
                    font.weight: Font.Bold
                    color: "#16A34A"
                    horizontalAlignment: Text.AlignRight
                }
            }

            Rectangle {
                width: parent.width
                height: 6
                radius: 3
                color: "#F1F5F9"

                Rectangle {
                    height: parent.height
                    width: parent.width * ((root.hasWard ? root.wardData.resolutionRate : 78.4) / 100.0)
                    radius: 3
                    color: "#16A34A"
                }
            }
        }
    }
}
