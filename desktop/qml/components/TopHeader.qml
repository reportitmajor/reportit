import QtQuick
import QtQuick.Controls

Rectangle {
    id: root

    property string searchQuery: searchField.text
    property bool isFullscreen: false
    signal searchSubmitted(string query)
    signal notificationsClicked()
    signal profileClicked()
    signal fullscreenClicked()

    implicitWidth: 1000
    implicitHeight: 64
    color: "#FFFFFF"
    border.color: "#E8EEF5"
    border.width: 1

    Row {
        anchors.fill: parent
        anchors.leftMargin: 24
        anchors.rightMargin: 24

        // Search Input Container
        Item {
            width: Math.min(parent.width * 0.48, 480)
            height: 38
            anchors.verticalCenter: parent.verticalCenter

            Rectangle {
                anchors.fill: parent
                radius: 8
                color: "#FFFFFF"
                border.color: searchField.activeFocus ? "#3B49DF" : "#E2E8F0"
                border.width: searchField.activeFocus ? 1.5 : 1

                Row {
                    anchors.fill: parent
                    anchors.leftMargin: 12
                    anchors.rightMargin: 12
                    spacing: 8

                    // Search Icon
                    Canvas {
                        width: 16
                        height: 16
                        anchors.verticalCenter: parent.verticalCenter
                        onPaint: {
                            var ctx = getContext("2d");
                            ctx.reset();
                            ctx.strokeStyle = "#94A3B8";
                            ctx.lineWidth = 1.6;
                            ctx.beginPath();
                            ctx.arc(6, 6, 4.5, 0, 2 * Math.PI);
                            ctx.stroke();
                            ctx.beginPath();
                            ctx.moveTo(9.5, 9.5);
                            ctx.lineTo(13.5, 13.5);
                            ctx.stroke();
                        }
                    }

                    TextInput {
                        id: searchField
                        width: parent.width - 24
                        anchors.verticalCenter: parent.verticalCenter
                        font.pixelSize: 13
                        color: "#0F172A"
                        clip: true
                        selectByMouse: true

                        Text {
                            anchors.verticalCenter: parent.verticalCenter
                            text: "Search reports, users, or locations..."
                            color: "#94A3B8"
                            font.pixelSize: 13
                            visible: !searchField.text && !searchField.activeFocus
                        }

                        onTextChanged: root.searchSubmitted(text)
                        onAccepted: root.searchSubmitted(text)
                    }
                }
            }
        }

        Item {
            width: parent.width - Math.min(parent.width * 0.48, 480) - rightSection.width
            height: parent.height
        }

        // Right Section: Fullscreen + Bell + Profile
        Row {
            id: rightSection
            anchors.verticalCenter: parent.verticalCenter
            spacing: 14

            // Live Cloud Firestore Sync Indicator & Trigger
            Rectangle {
                width: syncRow.implicitWidth + 16
                height: 32
                radius: 16
                anchors.verticalCenter: parent.verticalCenter
                color: syncArea.containsMouse ? "#EFF6FF" : "#F8FAFC"
                border.color: (typeof reportController !== "undefined" && reportController.syncService && reportController.syncService.isSyncing) ? "#3B82F6" : "#E2E8F0"
                border.width: 1

                Row {
                    id: syncRow
                    anchors.centerIn: parent
                    spacing: 6

                    Rectangle {
                        width: 8
                        height: 8
                        radius: 4
                        anchors.verticalCenter: parent.verticalCenter
                        color: (typeof reportController !== "undefined" && reportController.syncService && reportController.syncService.isSyncing) ? "#3B82F6" : "#10B981"
                    }

                    Text {
                        text: (typeof reportController !== "undefined" && reportController.syncService && reportController.syncService.isSyncing) ? "Syncing..." : "Cloud Live"
                        font.pixelSize: 11
                        font.bold: true
                        color: "#334155"
                        anchors.verticalCenter: parent.verticalCenter
                    }
                }

                ToolTip.visible: syncArea.containsMouse
                ToolTip.text: "Click to refresh citizen reports from Cloud Firestore\nLast sync: " + ((typeof reportController !== "undefined" && reportController.syncService) ? reportController.syncService.lastSyncTime : "Never")
                ToolTip.delay: 200

                MouseArea {
                    id: syncArea
                    anchors.fill: parent
                    hoverEnabled: true
                    cursorShape: Qt.PointingHandCursor
                    onClicked: {
                        if (typeof reportController !== "undefined") {
                            reportController.refreshFromCloud();
                        }
                    }
                }
            }

            // Fullscreen Button
            Item {
                width: 36
                height: 36
                anchors.verticalCenter: parent.verticalCenter

                Rectangle {
                    anchors.fill: parent
                    radius: 18
                    color: fullscreenMouseArea.containsMouse ? "#F1F5F9" : "transparent"

                    Canvas {
                        id: fsCanvas
                        anchors.centerIn: parent
                        width: 18
                        height: 18
                        property bool isFs: root.isFullscreen
                        onIsFsChanged: requestPaint()
                        onPaint: {
                            var ctx = getContext("2d");
                            ctx.reset();
                            ctx.strokeStyle = "#475569";
                            ctx.lineWidth = 1.6;
                            ctx.lineCap = "round";
                            ctx.lineJoin = "round";

                            if (!isFs) {
                                // Expand / Enter Fullscreen icon
                                ctx.beginPath(); ctx.moveTo(3, 7); ctx.lineTo(3, 3); ctx.lineTo(7, 3); ctx.stroke();
                                ctx.beginPath(); ctx.moveTo(11, 3); ctx.lineTo(15, 3); ctx.lineTo(15, 7); ctx.stroke();
                                ctx.beginPath(); ctx.moveTo(3, 11); ctx.lineTo(3, 15); ctx.lineTo(7, 15); ctx.stroke();
                                ctx.beginPath(); ctx.moveTo(11, 15); ctx.lineTo(15, 15); ctx.lineTo(15, 11); ctx.stroke();
                            } else {
                                // Compress / Exit Fullscreen icon
                                ctx.beginPath(); ctx.moveTo(7, 3); ctx.lineTo(7, 7); ctx.lineTo(3, 7); ctx.stroke();
                                ctx.beginPath(); ctx.moveTo(11, 3); ctx.lineTo(11, 7); ctx.lineTo(15, 7); ctx.stroke();
                                ctx.beginPath(); ctx.moveTo(7, 15); ctx.lineTo(7, 11); ctx.lineTo(3, 11); ctx.stroke();
                                ctx.beginPath(); ctx.moveTo(11, 15); ctx.lineTo(11, 11); ctx.lineTo(15, 11); ctx.stroke();
                            }
                        }
                    }

                    ToolTip.visible: fullscreenMouseArea.containsMouse
                    ToolTip.text: root.isFullscreen ? "Exit Fullscreen (F11 / Esc)" : "Fullscreen (F11)"
                    ToolTip.delay: 300

                    MouseArea {
                        id: fullscreenMouseArea
                        anchors.fill: parent
                        hoverEnabled: true
                        cursorShape: Qt.PointingHandCursor
                        onClicked: root.fullscreenClicked()
                    }
                }
            }

            // Notification Bell with badge
            Item {
                width: 36
                height: 36
                anchors.verticalCenter: parent.verticalCenter

                Rectangle {
                    anchors.fill: parent
                    radius: 18
                    color: bellMouseArea.containsMouse ? "#F1F5F9" : "transparent"

                    // Bell Icon
                    Canvas {
                        anchors.centerIn: parent
                        width: 18
                        height: 18
                        onPaint: {
                            var ctx = getContext("2d");
                            ctx.reset();
                            ctx.strokeStyle = "#475569";
                            ctx.lineWidth = 1.6;
                            ctx.lineCap = "round";
                            ctx.lineJoin = "round";

                            ctx.beginPath();
                            ctx.moveTo(9, 2.5); ctx.lineTo(9, 1.5);
                            ctx.stroke();

                            ctx.beginPath();
                            ctx.arc(9, 7.5, 4.5, Math.PI, 0);
                            ctx.lineTo(15, 13);
                            ctx.lineTo(3, 13);
                            ctx.closePath();
                            ctx.stroke();

                            ctx.beginPath();
                            ctx.arc(9, 13, 2, 0, Math.PI);
                            ctx.stroke();
                        }
                    }

                    // Red Notification Dot
                    Rectangle {
                        width: 7
                        height: 7
                        radius: 3.5
                        color: "#EF4444"
                        anchors.top: parent.top
                        anchors.right: parent.right
                        anchors.topMargin: 4
                        anchors.rightMargin: 4
                    }

                    MouseArea {
                        id: bellMouseArea
                        anchors.fill: parent
                        hoverEnabled: true
                        cursorShape: Qt.PointingHandCursor
                        onClicked: root.notificationsClicked()
                    }
                }
            }

            // Subtle Vertical Divider
            Rectangle {
                width: 1
                height: 28
                color: "#E2E8F0"
                anchors.verticalCenter: parent.verticalCenter
            }

            // User Info
            Row {
                anchors.verticalCenter: parent.verticalCenter
                spacing: 10

                Column {
                    anchors.verticalCenter: parent.verticalCenter
                    spacing: 1

                    Text {
                        text: "Alex Rivera"
                        font.pixelSize: 13
                        font.weight: Font.Bold
                        color: "#0F172A"
                        horizontalAlignment: Text.AlignRight
                    }

                    Text {
                        text: "Administrator Profile"
                        font.pixelSize: 11
                        font.weight: Font.Normal
                        color: "#94A3B8"
                        horizontalAlignment: Text.AlignRight
                    }
                }

                // Avatar Icon Circle
                Rectangle {
                    width: 36
                    height: 36
                    radius: 18
                    color: "#EEF2FF"
                    border.color: "#C7D2FE"
                    border.width: 1
                    anchors.verticalCenter: parent.verticalCenter

                    Canvas {
                        anchors.centerIn: parent
                        width: 20
                        height: 20
                        onPaint: {
                            var ctx = getContext("2d");
                            ctx.reset();
                            ctx.strokeStyle = "#4338CA";
                            ctx.fillStyle = "#4338CA";
                            ctx.lineWidth = 1.4;

                            // Head
                            ctx.beginPath();
                            ctx.arc(10, 6.5, 3.5, 0, 2 * Math.PI);
                            ctx.stroke();

                            // Shoulders
                            ctx.beginPath();
                            ctx.arc(10, 16.5, 6, Math.PI, 0);
                            ctx.stroke();
                        }
                    }

                    MouseArea {
                        anchors.fill: parent
                        cursorShape: Qt.PointingHandCursor
                        onClicked: root.profileClicked()
                    }
                }
            }
        }
    }
}
