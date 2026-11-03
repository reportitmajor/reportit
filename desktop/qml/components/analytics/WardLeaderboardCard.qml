import QtQuick
import QtQuick.Controls

Rectangle {
    id: root

    implicitWidth: 620
    implicitHeight: 260
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
                width: parent.width - 120
                spacing: 2
                Text {
                    text: "Ward Performance Leaderboard & Defaulter Watch"
                    font.pixelSize: 13
                    font.weight: Font.Bold
                    color: "#0F172A"
                }
                Text {
                    text: "Benchmarking top performing nodal wards vs chronic intervention zones"
                    font.pixelSize: 10
                    color: "#64748B"
                }
            }

            Rectangle {
                width: 110
                height: 22
                radius: 11
                color: "#EEF2FF"
                anchors.verticalCenter: parent.verticalCenter
                Text { anchors.centerIn: parent; text: "243 WARDS AUDITED"; font.pixelSize: 8; font.weight: Font.Bold; color: "#4338CA" }
            }
        }

        // Two Columns: Top 5 vs Bottom 5
        Row {
            width: parent.width
            height: parent.height - 45
            spacing: 14

            // Left Column: Top 5 Star Wards
            Rectangle {
                width: (parent.width - 14) / 2
                height: parent.height
                radius: 8
                color: "#F0FDF4"
                border.color: "#BBF7D0"
                border.width: 1

                Column {
                    anchors.fill: parent
                    anchors.margins: 10
                    spacing: 6

                    Text {
                        text: "⭐ TOP 5 STAR PERFORMERS"
                        font.pixelSize: 10
                        font.weight: Font.Bold
                        font.letterSpacing: 0.5
                        color: "#166534"
                    }

                    Repeater {
                        model: 4 // Top 4 items

                        Rectangle {
                            width: parent.width
                            height: 34
                            radius: 6
                            color: "#FFFFFF"
                            border.color: "#DCFCE7"
                            border.width: 1

                            readonly property var topWards: [
                                {rank: "#1", name: "Ward 49 (RR Nagar)", sla: "97.8% SLA", tat: "8.4h", csat: "4.8 ★"},
                                {rank: "#2", name: "Ward 114 (Marathahalli)", sla: "96.4% SLA", tat: "11.2h", csat: "4.7 ★"},
                                {rank: "#3", name: "Ward 48 (Jnana Bharathi)", sla: "95.6% SLA", tat: "12.8h", csat: "4.7 ★"},
                                {rank: "#4", name: "Ward 243 (Kudlu Gate)", sla: "94.8% SLA", tat: "13.5h", csat: "4.6 ★"}
                            ]

                            Row {
                                anchors.fill: parent
                                anchors.leftMargin: 8
                                anchors.rightMargin: 8
                                spacing: 6

                                Text {
                                    text: parent.parent.topWards[index].rank
                                    font.pixelSize: 10
                                    font.weight: Font.Bold
                                    color: "#15803D"
                                    anchors.verticalCenter: parent.verticalCenter
                                }
                                Text {
                                    width: parent.width - 145
                                    text: parent.parent.topWards[index].name
                                    font.pixelSize: 10
                                    font.weight: Font.DemiBold
                                    color: "#1E293B"
                                    elide: Text.ElideRight
                                    anchors.verticalCenter: parent.verticalCenter
                                }
                                Text {
                                    text: parent.parent.topWards[index].sla
                                    font.pixelSize: 10
                                    font.weight: Font.Bold
                                    color: "#16A34A"
                                    anchors.verticalCenter: parent.verticalCenter
                                }
                                Text {
                                    text: parent.parent.topWards[index].csat
                                    font.pixelSize: 9
                                    color: "#D97706"
                                    anchors.verticalCenter: parent.verticalCenter
                                }
                            }
                        }
                    }
                }
            }

            // Right Column: Bottom 5 Bottleneck Wards
            Rectangle {
                width: (parent.width - 14) / 2
                height: parent.height
                radius: 8
                color: "#FEF2F2"
                border.color: "#FECACA"
                border.width: 1

                Column {
                    anchors.fill: parent
                    anchors.margins: 10
                    spacing: 6

                    Text {
                        text: "⚠️ BOTTLENECK WARDS (NEEDS INTERVENTION)"
                        font.pixelSize: 10
                        font.weight: Font.Bold
                        font.letterSpacing: 0.5
                        color: "#991B1B"
                    }

                    Repeater {
                        model: 4 // Bottom 4 items

                        Rectangle {
                            width: parent.width
                            height: 34
                            radius: 6
                            color: "#FFFFFF"
                            border.color: "#FEE2E2"
                            border.width: 1

                            readonly property var botWards: [
                                {rank: "⚠️", name: "Ward 33 (Kengeri)", sla: "78.4% SLA", backlog: "28 backlogs", tat: "44.2h"},
                                {rank: "⚠️", name: "Ward 149 (Varthur)", sla: "79.2% SLA", backlog: "34 backlogs", tat: "41.8h"},
                                {rank: "⚠️", name: "Ward 192 (Begur)", sla: "81.0% SLA", backlog: "22 backlogs", tat: "36.4h"},
                                {rank: "⚠️", name: "Ward 32 (Ullal)", sla: "82.5% SLA", backlog: "19 backlogs", tat: "32.1h"}
                            ]

                            Row {
                                anchors.fill: parent
                                anchors.leftMargin: 8
                                anchors.rightMargin: 8
                                spacing: 6

                                Text {
                                    text: parent.parent.botWards[index].rank
                                    font.pixelSize: 9
                                    anchors.verticalCenter: parent.verticalCenter
                                }
                                Text {
                                    width: parent.width - 165
                                    text: parent.parent.botWards[index].name
                                    font.pixelSize: 10
                                    font.weight: Font.DemiBold
                                    color: "#1E293B"
                                    elide: Text.ElideRight
                                    anchors.verticalCenter: parent.verticalCenter
                                }
                                Text {
                                    text: parent.parent.botWards[index].sla
                                    font.pixelSize: 10
                                    font.weight: Font.Bold
                                    color: "#DC2626"
                                    anchors.verticalCenter: parent.verticalCenter
                                }
                                Text {
                                    text: parent.parent.botWards[index].backlog
                                    font.pixelSize: 9
                                    color: "#991B1B"
                                    anchors.verticalCenter: parent.verticalCenter
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
