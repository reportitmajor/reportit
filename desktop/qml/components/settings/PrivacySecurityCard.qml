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
                text: "Citizen Privacy & System Data Security"
                font.pixelSize: 14
                font.weight: Font.Bold
                color: "#0F172A"
            }
            Text {
                text: "Configure citizen PII data masking from field contractors, session security, and transparency policies"
                font.pixelSize: 11
                color: "#64748B"
            }
        }

        Rectangle { width: parent.width; height: 1; color: "#F1F5F9" }

        // Privacy Controls List
        Column {
            width: parent.width
            spacing: 12

            // Row 1: Phone Masking
            Rectangle {
                width: parent.width
                height: 60
                radius: 8
                color: "#F8FAFC"
                border.color: "#E2E8F0"
                border.width: 1

                Row {
                    anchors.fill: parent
                    anchors.margins: 12

                    Column {
                        width: parent.width - 60
                        spacing: 2
                        Text { text: "🔒 Mask Citizen Phone Numbers from External Contractors"; font.pixelSize: 11; font.weight: Font.Bold; color: "#0F172A" }
                        Text { text: "Displays masked numbers (+91 98860 •••••) on field worker mobile app to prevent citizen harassment."; font.pixelSize: 9; color: "#64748B" }
                    }

                    Switch {
                        id: maskPhoneSwitch
                        checked: settingsController.maskCitizenPhone
                        anchors.verticalCenter: parent.verticalCenter
                    }
                }
            }

            // Row 2: Address Masking
            Rectangle {
                width: parent.width
                height: 60
                radius: 8
                color: "#F8FAFC"
                border.color: "#E2E8F0"
                border.width: 1

                Row {
                    anchors.fill: parent
                    anchors.margins: 12

                    Column {
                        width: parent.width - 60
                        spacing: 2
                        Text { text: "🏠 Approximate GPS Coordinates for Public Inspection Map"; font.pixelSize: 11; font.weight: Font.Bold; color: "#0F172A" }
                        Text { text: "Rounds public GPS markers to street centroids to protect private residential addresses."; font.pixelSize: 9; color: "#64748B" }
                    }

                    Switch {
                        id: maskAddressSwitch
                        checked: settingsController.maskCitizenAddress
                        anchors.verticalCenter: parent.verticalCenter
                    }
                }
            }

            // Row 3: Auto Feedback CSAT
            Rectangle {
                width: parent.width
                height: 60
                radius: 8
                color: "#F8FAFC"
                border.color: "#E2E8F0"
                border.width: 1

                Row {
                    anchors.fill: parent
                    anchors.margins: 12

                    Column {
                        width: parent.width - 60
                        spacing: 2
                        Text { text: "⭐ Automated Post-Resolution Citizen Feedback Prompt"; font.pixelSize: 11; font.weight: Font.Bold; color: "#0F172A" }
                        Text { text: "Sends an automated push notification to the citizen asking for 1-5 star verification upon resolution."; font.pixelSize: 9; color: "#64748B" }
                    }

                    Switch {
                        id: autoFeedbackSwitch
                        checked: settingsController.autoFeedbackNotification
                        anchors.verticalCenter: parent.verticalCenter
                    }
                }
            }

            // Row 4: Session Inactivity Timeout
            Rectangle {
                width: parent.width
                height: 60
                radius: 8
                color: "#F8FAFC"
                border.color: "#E2E8F0"
                border.width: 1

                Row {
                    anchors.fill: parent
                    anchors.margins: 12

                    Column {
                        width: parent.width - 120
                        spacing: 2
                        Text { text: "⏱ Command Center Session Inactivity Auto-Lock"; font.pixelSize: 11; font.weight: Font.Bold; color: "#0F172A" }
                        Text { text: "Automatically locks the admin workstation after inactivity to ensure data compliance."; font.pixelSize: 9; color: "#64748B" }
                    }

                    Rectangle {
                        width: 90
                        height: 32
                        radius: 6
                        color: "#FFFFFF"
                        border.color: "#CBD5E1"
                        border.width: 1
                        anchors.verticalCenter: parent.verticalCenter

                        TextInput {
                            id: timeoutInput
                            anchors.centerIn: parent
                            text: "" + settingsController.sessionTimeoutMinutes
                            font.pixelSize: 12
                            font.weight: Font.Bold
                            color: "#0F172A"
                        }
                        Text { anchors.right: parent.right; anchors.rightMargin: 6; anchors.verticalCenter: parent.verticalCenter; text: "min"; font.pixelSize: 9; color: "#94A3B8" }
                    }
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
                    text: "Save Security Rules"
                    font.pixelSize: 11
                    font.weight: Font.Bold
                    color: "#FFFFFF"
                }
            }

            MouseArea {
                anchors.fill: parent
                cursorShape: Qt.PointingHandCursor
                onClicked: {
                    settingsController.savePrivacy(
                        maskPhoneSwitch.checked,
                        maskAddressSwitch.checked,
                        autoFeedbackSwitch.checked,
                        parseInt(timeoutInput.text) || 30
                    );
                    root.saved("Privacy & Citizen Security settings updated successfully!");
                }
            }
        }
    }
}
