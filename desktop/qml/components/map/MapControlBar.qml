import QtQuick
import QtQuick.Controls

Rectangle {
    id: root

    implicitWidth: 1000
    implicitHeight: 64
    color: "#FFFFFF"
    border.color: "#E8EEF5"
    border.width: 1

    // Left Controls: Constituency, Ward, Status Filter Pills
    Row {
        anchors.left: parent.left
        anchors.leftMargin: 20
        anchors.verticalCenter: parent.verticalCenter
        spacing: 12

        // 1. Constituency / Zone Selector
        Item {
            width: 210
            height: 38
            anchors.verticalCenter: parent.verticalCenter

            Rectangle {
                anchors.fill: parent
                radius: 8
                color: "#F8FAFC"
                border.color: "#CBD5E1"
                border.width: 1

                Row {
                    anchors.fill: parent
                    anchors.leftMargin: 10
                    anchors.rightMargin: 10
                    spacing: 8

                    Text {
                        text: "🏛️"
                        font.pixelSize: 13
                        anchors.verticalCenter: parent.verticalCenter
                    }

                    Text {
                        width: parent.width - 44
                        text: mapController.selectedConstituency
                        font.pixelSize: 12
                        font.weight: Font.DemiBold
                        color: "#0F172A"
                        elide: Text.ElideRight
                        anchors.verticalCenter: parent.verticalCenter
                    }

                    Text {
                        text: "▾"
                        font.pixelSize: 12
                        color: "#64748B"
                        anchors.verticalCenter: parent.verticalCenter
                    }
                }

                MouseArea {
                    anchors.fill: parent
                    cursorShape: Qt.PointingHandCursor
                    onClicked: acMenu.open()
                }

                Menu {
                    id: acMenu
                    y: parent.height + 4
                    width: 240

                    Repeater {
                        model: mapController.constituencyList
                        MenuItem {
                            text: modelData.name
                            font.pixelSize: 12
                            onTriggered: {
                                mapController.selectConstituency(modelData.name)
                            }
                        }
                    }
                }
            }
        }

        // 2. Ward Selector
        Item {
            width: 180
            height: 38
            anchors.verticalCenter: parent.verticalCenter

            Rectangle {
                anchors.fill: parent
                radius: 8
                color: "#F8FAFC"
                border.color: "#CBD5E1"
                border.width: 1

                Row {
                    anchors.fill: parent
                    anchors.leftMargin: 10
                    anchors.rightMargin: 10
                    spacing: 8

                    Text {
                        text: "📍"
                        font.pixelSize: 13
                        anchors.verticalCenter: parent.verticalCenter
                    }

                    Text {
                        width: parent.width - 44
                        text: mapController.selectedWardNumber > 0 ? ("Ward " + mapController.selectedWardNumber) : "All Wards"
                        font.pixelSize: 12
                        font.weight: Font.DemiBold
                        color: "#0F172A"
                        elide: Text.ElideRight
                        anchors.verticalCenter: parent.verticalCenter
                    }

                    Text {
                        text: "▾"
                        font.pixelSize: 12
                        color: "#64748B"
                        anchors.verticalCenter: parent.verticalCenter
                    }
                }

                MouseArea {
                    anchors.fill: parent
                    cursorShape: Qt.PointingHandCursor
                    onClicked: wardMenu.open()
                }

                Menu {
                    id: wardMenu
                    y: parent.height + 4
                    width: 220

                    Repeater {
                        model: mapController.wardListForCurrentAC
                        MenuItem {
                            text: modelData.wardName
                            font.pixelSize: 12
                            onTriggered: {
                                mapController.selectWard(modelData.wardNumber)
                            }
                        }
                    }
                }
            }
        }

        // 3. Status Filter Pills
        Row {
            anchors.verticalCenter: parent.verticalCenter
            spacing: 6

            readonly property var statuses: ["All", "CRITICAL", "IN PROGRESS", "RESOLVED"]

            Repeater {
                model: parent.statuses

                Rectangle {
                    width: statusLabel.implicitWidth + 18
                    height: 32
                    radius: 16
                    readonly property bool isSelected: mapController.statusFilter.toUpperCase() === modelData.toUpperCase()

                    color: {
                        if (isSelected) {
                            if (modelData === "CRITICAL") return "#FEE2E2"
                            if (modelData === "IN PROGRESS") return "#FEF3C7"
                            if (modelData === "RESOLVED") return "#DCFCE7"
                            return "#EEF2FF"
                        }
                        return "#F8FAFC"
                    }

                    border.color: isSelected ? "#4338CA" : "#E2E8F0"
                    border.width: isSelected ? 1.5 : 1

                    Text {
                        id: statusLabel
                        anchors.centerIn: parent
                        text: {
                            if (modelData === "CRITICAL") return "🔴 Critical"
                            if (modelData === "IN PROGRESS") return "🟡 In Progress"
                            if (modelData === "RESOLVED") return "🟢 Resolved"
                            return "All Status"
                        }
                        font.pixelSize: 11
                        font.weight: parent.isSelected ? Font.Bold : Font.Medium
                        color: {
                            if (modelData === "CRITICAL") return "#B91C1C"
                            if (modelData === "IN PROGRESS") return "#B45309"
                            if (modelData === "RESOLVED") return "#15803D"
                            return "#1E293B"
                        }
                    }

                    MouseArea {
                        anchors.fill: parent
                        cursorShape: Qt.PointingHandCursor
                        onClicked: mapController.setStatusFilter(modelData)
                    }
                }
            }
        }
    }

    // 4. Heatmap Toggle Button (Pinned Right)
    Rectangle {
        id: heatmapBtn
        anchors.right: parent.right
        anchors.rightMargin: 20
        anchors.verticalCenter: parent.verticalCenter
        width: 125
        height: 34
        radius: 8
        color: mapController.showHeatmap ? "#FEF2F2" : "#F8FAFC"
        border.color: mapController.showHeatmap ? "#EF4444" : "#CBD5E1"
        border.width: 1.5

        Row {
            anchors.centerIn: parent
            spacing: 6

            Text {
                text: "🔥"
                font.pixelSize: 13
            }

            Text {
                text: "Heatmap"
                font.pixelSize: 12
                font.weight: Font.Bold
                color: mapController.showHeatmap ? "#DC2626" : "#475569"
            }
        }

        MouseArea {
            anchors.fill: parent
            cursorShape: Qt.PointingHandCursor
            onClicked: mapController.toggleHeatmap()
        }
    }
}
