import QtQuick
import QtQuick.Controls

Rectangle {
    id: root

    implicitWidth: 700
    implicitHeight: 560
    radius: 10
    color: "#FFFFFF"
    border.color: "#E2E8F0"
    border.width: 1

    signal saved(string message)

    Column {
        anchors.fill: parent
        anchors.margins: 20
        spacing: 14

        // Title Header
        Column {
            spacing: 2
            Text {
                text: "State KGIS Cloud Sync & System Backup"
                font.pixelSize: 14
                font.weight: Font.Bold
                color: "#0F172A"
            }
            Text {
                text: "Monitor Karnataka GIS gateway health, trigger encrypted database backups, and inspect administrative audit logs"
                font.pixelSize: 11
                color: "#64748B"
            }
        }

        Rectangle { width: parent.width; height: 1; color: "#F1F5F9" }

        // Top Row: Server Status + Backup Button
        Row {
            width: parent.width
            spacing: 12

            // Server Status Card
            Rectangle {
                width: (parent.width - 12) / 2
                height: 80
                radius: 8
                color: "#F0FDF4"
                border.color: "#BBF7D0"
                border.width: 1

                Row {
                    anchors.fill: parent
                    anchors.margins: 12
                    spacing: 10

                    Column {
                        width: parent.width - 90
                        spacing: 2
                        Text { text: "🌐 State KGIS Gateway"; font.pixelSize: 11; font.weight: Font.Bold; color: "#166534" }
                        Text { text: settingsController.kgisServerStatus; font.pixelSize: 10; font.weight: Font.DemiBold; color: "#15803D" }
                        Text { text: "Last handshake: " + settingsController.lastSyncedTime; font.pixelSize: 9; color: "#64748B" }
                    }

                    Rectangle {
                        width: 80
                        height: 28
                        radius: 6
                        color: "#FFFFFF"
                        border.color: "#86EFAC"
                        border.width: 1
                        anchors.verticalCenter: parent.verticalCenter

                        Text {
                            anchors.centerIn: parent
                            text: "Test Ping"
                            font.pixelSize: 10
                            font.weight: Font.Bold
                            color: "#166534"
                        }

                        MouseArea {
                            anchors.fill: parent
                            cursorShape: Qt.PointingHandCursor
                            onClicked: settingsController.testKgisConnection()
                        }
                    }
                }
            }

            // System Backup Card
            Rectangle {
                width: (parent.width - 12) / 2
                height: 80
                radius: 8
                color: "#F8FAFC"
                border.color: "#E2E8F0"
                border.width: 1

                Row {
                    anchors.fill: parent
                    anchors.margins: 12
                    spacing: 10

                    Column {
                        width: parent.width - 100
                        spacing: 2
                        Text { text: "💾 Encrypted Database Backup"; font.pixelSize: 11; font.weight: Font.Bold; color: "#0F172A" }
                        Text { text: "Storage: " + settingsController.databaseStorageUsed; font.pixelSize: 9; color: "#64748B" }
                        Text { text: "Full schema + 243-ward GIS layer"; font.pixelSize: 9; color: "#64748B" }
                    }

                    Rectangle {
                        width: 90
                        height: 28
                        radius: 6
                        color: "#1E293B"
                        anchors.verticalCenter: parent.verticalCenter

                        Text {
                            anchors.centerIn: parent
                            text: "📥 Export Now"
                            font.pixelSize: 10
                            font.weight: Font.Bold
                            color: "#FFFFFF"
                        }

                        MouseArea {
                            anchors.fill: parent
                            cursorShape: Qt.PointingHandCursor
                            onClicked: {
                                var path = settingsController.triggerBackup();
                                root.saved("Encrypted Backup exported to " + path);
                            }
                        }
                    }
                }
            }
        }

        // Section: System Audit Trail
        Column {
            width: parent.width
            spacing: 6

            Text {
                text: "IMMUTABLE ADMINISTRATIVE SECURITY AUDIT LOGS"
                font.pixelSize: 10
                font.weight: Font.Bold
                font.letterSpacing: 0.5
                color: "#94A3B8"
            }

            Rectangle {
                width: parent.width
                height: 210
                radius: 8
                color: "#F8FAFC"
                border.color: "#E2E8F0"
                border.width: 1
                clip: true

                ListView {
                    anchors.fill: parent
                    anchors.margins: 8
                    model: settingsController.auditLogs
                    spacing: 6

                    delegate: Rectangle {
                        width: parent.width
                        height: 42
                        radius: 6
                        color: "#FFFFFF"
                        border.color: "#F1F5F9"
                        border.width: 1

                        Row {
                            anchors.fill: parent
                            anchors.leftMargin: 10
                            anchors.rightMargin: 10
                            spacing: 8

                            Text {
                                text: "🔒"
                                font.pixelSize: 10
                                anchors.verticalCenter: parent.verticalCenter
                            }

                            Column {
                                width: parent.width - 150
                                anchors.verticalCenter: parent.verticalCenter
                                spacing: 2

                                Text {
                                    text: modelData.action
                                    font.pixelSize: 10
                                    font.weight: Font.DemiBold
                                    color: "#0F172A"
                                    elide: Text.ElideRight
                                    width: parent.width
                                }
                                Text {
                                    text: modelData.user + " • Gateway IP: " + modelData.ip
                                    font.pixelSize: 9
                                    color: "#64748B"
                                }
                            }

                            Text {
                                text: modelData.time
                                font.pixelSize: 9
                                color: "#94A3B8"
                                anchors.verticalCenter: parent.verticalCenter
                            }
                        }
                    }
                }
            }
        }
    }
}
