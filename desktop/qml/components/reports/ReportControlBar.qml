import QtQuick
import QtQuick.Controls

Rectangle {
    id: root

    implicitWidth: 1000
    implicitHeight: 110
    color: "#FFFFFF"
    border.color: "#E8EEF5"
    border.width: 1

    signal exportSuccess(string path)

    Column {
        anchors.fill: parent
        anchors.margins: 14
        spacing: 12

        // 1. Top Row: Status Tabs on the Left, Search + Export on the Right
        Item {
            width: parent.width
            height: 36

            // Status Tabs (Pinned Left)
            Row {
                anchors.left: parent.left
                anchors.verticalCenter: parent.verticalCenter
                spacing: 8

                Repeater {
                    model: [
                        {tabKey: "All", label: "All Reports", count: reportController.totalReportsCount},
                        {tabKey: "Needs Action", label: "🔴 Needs Action", count: reportController.needsActionCount},
                        {tabKey: "In Progress", label: "🟡 In Progress", count: reportController.inProgressCount},
                        {tabKey: "Resolved", label: "🟢 Resolved", count: reportController.resolvedCount}
                    ]

                    Rectangle {
                        id: tabDelegateRoot
                        readonly property bool isSelected: reportController.statusTab === modelData.tabKey
                        width: tabContentRow.implicitWidth + 24
                        implicitWidth: width
                        height: 34
                        radius: 8

                        color: isSelected ? "#EEF2FF" : "#F8FAFC"
                        border.color: isSelected ? "#4338CA" : "#E2E8F0"
                        border.width: isSelected ? 1.5 : 1

                        Row {
                            id: tabContentRow
                            anchors.centerIn: parent
                            spacing: 6

                            Text {
                                text: modelData.label
                                font.pixelSize: 12
                                font.weight: tabDelegateRoot.isSelected ? Font.Bold : Font.Medium
                                color: tabDelegateRoot.isSelected ? "#4338CA" : "#475569"
                            }

                            Rectangle {
                                id: badgeRect
                                implicitWidth: countTxt.implicitWidth + 10
                                implicitHeight: 18
                                width: implicitWidth
                                height: implicitHeight
                                radius: 9
                                color: tabDelegateRoot.isSelected ? "#4338CA" : "#E2E8F0"
                                anchors.verticalCenter: parent.verticalCenter

                                Text {
                                    id: countTxt
                                    anchors.centerIn: parent
                                    text: "" + modelData.count
                                    font.pixelSize: 10
                                    font.weight: Font.Bold
                                    color: tabDelegateRoot.isSelected ? "#FFFFFF" : "#64748B"
                                }
                            }
                        }

                        MouseArea {
                            anchors.fill: parent
                            cursorShape: Qt.PointingHandCursor
                            onClicked: reportController.setStatusTab(modelData.tabKey)
                        }
                    }
                }
            }

            // Right Actions: Search Box + Export CSV Button (Pinned Right)
            Row {
                anchors.right: parent.right
                anchors.verticalCenter: parent.verticalCenter
                spacing: 10

                // Search Box
                Rectangle {
                    id: searchBox
                    width: 250
                    height: 34
                    radius: 8
                    color: "#F8FAFC"
                    border.color: searchInput.activeFocus ? "#4338CA" : "#CBD5E1"
                    border.width: searchInput.activeFocus ? 1.5 : 1

                    Item {
                        anchors.fill: parent
                        anchors.leftMargin: 10
                        anchors.rightMargin: 10

                        Text {
                            id: searchIcon
                            text: "🔍"
                            font.pixelSize: 12
                            anchors.left: parent.left
                            anchors.verticalCenter: parent.verticalCenter
                        }

                        TextInput {
                            id: searchInput
                            anchors.left: searchIcon.right
                            anchors.leftMargin: 8
                            anchors.right: clearSearchBtn.visible ? clearSearchBtn.left : parent.right
                            anchors.rightMargin: 4
                            anchors.verticalCenter: parent.verticalCenter
                            font.pixelSize: 11
                            color: "#0F172A"
                            clip: true
                            selectByMouse: true

                            Text {
                                anchors.fill: parent
                                text: "Search reports, ward, ID, citizen..."
                                font.pixelSize: 11
                                color: "#94A3B8"
                                visible: !searchInput.text && !searchInput.activeFocus
                            }

                            onTextChanged: reportController.setSearchQuery(text)
                        }

                        Text {
                            id: clearSearchBtn
                            text: "✕"
                            font.pixelSize: 11
                            color: "#64748B"
                            anchors.right: parent.right
                            anchors.verticalCenter: parent.verticalCenter
                            visible: searchInput.text.length > 0

                            MouseArea {
                                anchors.fill: parent
                                cursorShape: Qt.PointingHandCursor
                                onClicked: {
                                    searchInput.text = ""
                                    searchInput.focus = false
                                }
                            }
                        }
                    }
                }

                // Export CSV Button
                Rectangle {
                    id: exportBtn
                    width: 120
                    height: 34
                    radius: 8
                    color: "#1E293B"

                    Row {
                        anchors.centerIn: parent
                        spacing: 6

                        Text { text: "📥"; font.pixelSize: 12 }
                        Text {
                            text: "Export CSV"
                            font.pixelSize: 11
                            font.weight: Font.Bold
                            color: "#FFFFFF"
                        }
                    }

                    MouseArea {
                        anchors.fill: parent
                        cursorShape: Qt.PointingHandCursor
                        onClicked: {
                            var path = reportController.exportToCsv();
                            root.exportSuccess(path);
                        }
                    }
                }
            }
        }

        // 2. Bottom Row: Category Filter Chips on the Left, Sort Dropdown on the Right
        Item {
            width: parent.width
            height: 30

            // Category Filter Chips (Pinned Left)
            Row {
                anchors.left: parent.left
                anchors.verticalCenter: parent.verticalCenter
                spacing: 6

                Text {
                    text: "Category:"
                    font.pixelSize: 11
                    font.weight: Font.DemiBold
                    color: "#64748B"
                    anchors.verticalCenter: parent.verticalCenter
                }

                Repeater {
                    model: ["All", "Infrastructure", "Sanitation", "Water Supply", "Public Safety", "Electrical", "Environment"]

                    Rectangle {
                        id: catChip
                        width: catLabel.implicitWidth + 14
                        height: 26
                        radius: 13
                        readonly property bool isSelected: reportController.categoryFilter.toLowerCase() === modelData.toLowerCase()

                        color: isSelected ? "#4338CA" : "#F1F5F9"

                        Text {
                            id: catLabel
                            anchors.centerIn: parent
                            text: modelData
                            font.pixelSize: 11
                            font.weight: catChip.isSelected ? Font.Bold : Font.Medium
                            color: catChip.isSelected ? "#FFFFFF" : "#475569"
                        }

                        MouseArea {
                            anchors.fill: parent
                            cursorShape: Qt.PointingHandCursor
                            onClicked: reportController.setCategoryFilter(modelData)
                        }
                    }
                }
            }

            // Sort Dropdown (Pinned Right)
            Rectangle {
                id: sortBox
                anchors.right: parent.right
                anchors.verticalCenter: parent.verticalCenter
                width: 170
                height: 28
                radius: 6
                color: "#F8FAFC"
                border.color: "#CBD5E1"
                border.width: 1

                Row {
                    anchors.centerIn: parent
                    spacing: 6

                    Text {
                        text: {
                            if (reportController.sortBy === "newest") return "Sort: 🕒 Newest First"
                            if (reportController.sortBy === "sla") return "Sort: ⏱ SLA Urgency"
                            if (reportController.sortBy === "severity") return "Sort: 🔴 Severity"
                            return "Sort: 📍 Ward Number"
                        }
                        font.pixelSize: 11
                        font.weight: Font.DemiBold
                        color: "#334155"
                    }
                    Text { text: "▾"; font.pixelSize: 10; color: "#64748B" }
                }

                MouseArea {
                    anchors.fill: parent
                    cursorShape: Qt.PointingHandCursor
                    onClicked: sortMenu.open()
                }

                Menu {
                    id: sortMenu
                    y: parent.height + 2
                    width: 180

                    MenuItem {
                        text: "🕒 Newest First (Time)"
                        onTriggered: reportController.setSortBy("newest")
                    }
                    MenuItem {
                        text: "⏱ SLA Urgency"
                        onTriggered: reportController.setSortBy("sla")
                    }
                    MenuItem {
                        text: "🔴 Severity (Critical First)"
                        onTriggered: reportController.setSortBy("severity")
                    }
                    MenuItem {
                        text: "📍 Ward Number"
                        onTriggered: reportController.setSortBy("ward")
                    }
                }
            }
        }
    }
}
