import QtQuick
import QtQuick.Controls

Rectangle {
    id: root

    implicitWidth: 380
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
                width: parent.width - 70
                spacing: 2
                Text {
                    text: "Chronic Failure Hotspots"
                    font.pixelSize: 13
                    font.weight: Font.Bold
                    color: "#0F172A"
                }
                Text {
                    text: "AI detection of repeat infrastructure failures"
                    font.pixelSize: 10
                    color: "#64748B"
                }
            }

            Rectangle {
                width: 60
                height: 22
                radius: 11
                color: "#FEF2F2"
                anchors.verticalCenter: parent.verticalCenter
                Text { anchors.centerIn: parent; text: "3 ALERTS"; font.pixelSize: 9; font.weight: Font.Bold; color: "#DC2626" }
            }
        }

        // Hotspot Items
        ListView {
            id: hotspotList
            width: parent.width
            height: parent.height - 45
            model: analyticsController.chronicHotspots
            clip: true
            spacing: 8

            delegate: Rectangle {
                width: hotspotList.width
                height: 58
                radius: 6
                color: "#FFFBEB"
                border.color: "#FDE68A"
                border.width: 1

                Column {
                    anchors.fill: parent
                    anchors.margins: 8
                    spacing: 3

                    Row {
                        width: parent.width

                        Text {
                            width: parent.width - 90
                            text: "📍 " + modelData.title
                            font.pixelSize: 11
                            font.weight: Font.Bold
                            color: "#92400E"
                            elide: Text.ElideRight
                        }

                        Rectangle {
                            width: 80
                            height: 16
                            radius: 4
                            color: "#FEF3C7"
                            Text {
                                anchors.centerIn: parent
                                text: modelData.count + " REPEATS"
                                font.pixelSize: 8
                                font.weight: Font.Bold
                                color: "#B45309"
                            }
                        }
                    }

                    Text {
                        width: parent.width
                        text: modelData.recommendation
                        font.pixelSize: 10
                        color: "#78350F"
                        elide: Text.ElideRight
                    }

                    Text {
                        text: "Nodal: " + modelData.officer + " • " + modelData.category
                        font.pixelSize: 9
                        color: "#A16207"
                    }
                }
            }
        }
    }
}
