import QtQuick
import QtQuick.Controls

Rectangle {
    id: root

    implicitWidth: 700
    implicitHeight: 520
    radius: 10
    color: "#FFFFFF"
    border.color: "#E2E8F0"
    border.width: 1

    signal saved(string message)

    Column {
        anchors.fill: parent
        anchors.margins: 20
        spacing: 16

        // Title Header
        Column {
            spacing: 2
            Text {
                text: "Administrative Profile & Nodal Office Credentials"
                font.pixelSize: 14
                font.weight: Font.Bold
                color: "#0F172A"
            }
            Text {
                text: "Configure officer identity, designation, and official jurisdiction registry"
                font.pixelSize: 11
                color: "#64748B"
            }
        }

        Rectangle { width: parent.width; height: 1; color: "#F1F5F9" }

        // Avatar + Badge Row
        Row {
            spacing: 14
            Rectangle {
                width: 56
                height: 56
                radius: 28
                color: "#4338CA"
                Text {
                    anchors.centerIn: parent
                    text: "AR"
                    font.pixelSize: 18
                    font.weight: Font.Bold
                    color: "#FFFFFF"
                }
            }

            Column {
                anchors.verticalCenter: parent.verticalCenter
                spacing: 4

                Row {
                    spacing: 8
                    Text {
                        text: nameInput.text
                        font.pixelSize: 14
                        font.weight: Font.Bold
                        color: "#0F172A"
                    }
                    Rectangle {
                        width: 78
                        height: 20
                        radius: 10
                        color: "#DCFCE7"
                        Text { anchors.centerIn: parent; text: "VERIFIED NODAL"; font.pixelSize: 8; font.weight: Font.Bold; color: "#15803D" }
                    }
                }

                Text {
                    text: "Karnataka Municipal Governance ID: BBMP-ADM-2026-09"
                    font.pixelSize: 10
                    color: "#64748B"
                }
            }
        }

        // Form Fields (2 columns)
        Grid {
            width: parent.width
            columns: 2
            spacing: 14

            // Field 1: Admin Name
            Column {
                width: (parent.width - 14) / 2
                spacing: 4

                Text { text: "Administrator Name:"; font.pixelSize: 11; font.weight: Font.DemiBold; color: "#334155" }
                Rectangle {
                    width: parent.width; height: 38; radius: 6; color: "#F8FAFC"; border.color: "#CBD5E1"; border.width: 1
                    TextInput {
                        id: nameInput
                        anchors.fill: parent; anchors.margins: 10
                        text: settingsController.adminName
                        font.pixelSize: 11; color: "#0F172A"
                    }
                }
            }

            // Field 2: Role
            Column {
                width: (parent.width - 14) / 2
                spacing: 4

                Text { text: "Official Designation:"; font.pixelSize: 11; font.weight: Font.DemiBold; color: "#334155" }
                Rectangle {
                    width: parent.width; height: 38; radius: 6; color: "#F8FAFC"; border.color: "#CBD5E1"; border.width: 1
                    TextInput {
                        id: roleInput
                        anchors.fill: parent; anchors.margins: 10
                        text: settingsController.adminRole
                        font.pixelSize: 11; color: "#0F172A"
                    }
                }
            }

            // Field 3: Official Email
            Column {
                width: (parent.width - 14) / 2
                spacing: 4

                Text { text: "Government Email:"; font.pixelSize: 11; font.weight: Font.DemiBold; color: "#334155" }
                Rectangle {
                    width: parent.width; height: 38; radius: 6; color: "#F8FAFC"; border.color: "#CBD5E1"; border.width: 1
                    TextInput {
                        id: emailInput
                        anchors.fill: parent; anchors.margins: 10
                        text: settingsController.adminEmail
                        font.pixelSize: 11; color: "#0F172A"
                    }
                }
            }

            // Field 4: Official Phone
            Column {
                width: (parent.width - 14) / 2
                spacing: 4

                Text { text: "Emergency Contact / Phone:"; font.pixelSize: 11; font.weight: Font.DemiBold; color: "#334155" }
                Rectangle {
                    width: parent.width; height: 38; radius: 6; color: "#F8FAFC"; border.color: "#CBD5E1"; border.width: 1
                    TextInput {
                        id: phoneInput
                        anchors.fill: parent; anchors.margins: 10
                        text: settingsController.adminPhone
                        font.pixelSize: 11; color: "#0F172A"
                    }
                }
            }
        }

        // Field 5: Jurisdiction
        Column {
            width: parent.width
            spacing: 4

            Text { text: "Assigned Jurisdiction / Zone:"; font.pixelSize: 11; font.weight: Font.DemiBold; color: "#334155" }
            Rectangle {
                width: parent.width; height: 38; radius: 6; color: "#F8FAFC"; border.color: "#CBD5E1"; border.width: 1
                TextInput {
                    id: jurisdictionInput
                    anchors.fill: parent; anchors.margins: 10
                    text: settingsController.jurisdiction
                    font.pixelSize: 11; color: "#0F172A"
                }
            }
        }

        Item { width: 1; height: 4 }

        // Save Button
        Rectangle {
            width: 160
            height: 38
            radius: 8
            color: "#4338CA"

            Row {
                anchors.centerIn: parent
                spacing: 6
                Text { text: "💾"; font.pixelSize: 12 }
                Text {
                    text: "Save Profile Changes"
                    font.pixelSize: 11
                    font.weight: Font.Bold
                    color: "#FFFFFF"
                }
            }

            MouseArea {
                anchors.fill: parent
                cursorShape: Qt.PointingHandCursor
                onClicked: {
                    settingsController.saveProfile(nameInput.text, roleInput.text, emailInput.text, phoneInput.text, jurisdictionInput.text);
                    root.saved("Administrator Profile successfully updated!");
                }
            }
        }
    }
}
