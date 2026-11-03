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
                text: "Statutory SLA Mandates & Auto-Escalation Protocols"
                font.pixelSize: 14
                font.weight: Font.Bold
                color: "#0F172A"
            }
            Text {
                text: "Define legal turnaround time limits per civic category and emergency escalation triggers"
                font.pixelSize: 11
                color: "#64748B"
            }
        }

        Rectangle { width: parent.width; height: 1; color: "#F1F5F9" }

        // Category SLA Limits Grid
        Grid {
            width: parent.width
            columns: 2
            spacing: 12

            // Item 1: Critical Water
            Rectangle {
                width: (parent.width - 12) / 2
                height: 58
                radius: 8
                color: "#F8FAFC"
                border.color: "#E2E8F0"
                border.width: 1

                Row {
                    anchors.fill: parent
                    anchors.margins: 10
                    spacing: 10

                    Column {
                        width: parent.width - 90
                        spacing: 2
                        Text { text: "🔴 Emergency Water Rupture"; font.pixelSize: 11; font.weight: Font.Bold; color: "#B91C1C" }
                        Text { text: "BWSSB statutory critical SLA"; font.pixelSize: 9; color: "#64748B" }
                    }

                    Rectangle {
                        width: 76
                        height: 32
                        radius: 6
                        color: "#FFFFFF"
                        border.color: "#CBD5E1"
                        border.width: 1
                        anchors.verticalCenter: parent.verticalCenter

                        TextInput {
                            id: waterSlaInput
                            anchors.centerIn: parent
                            text: "" + settingsController.slaCriticalWaterHours
                            font.pixelSize: 12
                            font.weight: Font.Bold
                            color: "#0F172A"
                        }
                        Text { anchors.right: parent.right; anchors.rightMargin: 6; anchors.verticalCenter: parent.verticalCenter; text: "hrs"; font.pixelSize: 9; color: "#94A3B8" }
                    }
                }
            }

            // Item 2: Potholes
            Rectangle {
                width: (parent.width - 12) / 2
                height: 58
                radius: 8
                color: "#F8FAFC"
                border.color: "#E2E8F0"
                border.width: 1

                Row {
                    anchors.fill: parent
                    anchors.margins: 10
                    spacing: 10

                    Column {
                        width: parent.width - 90
                        spacing: 2
                        Text { text: "🟡 Road Potholes & Craters"; font.pixelSize: 11; font.weight: Font.Bold; color: "#B45309" }
                        Text { text: "BBMP Road Infrastructure Wing"; font.pixelSize: 9; color: "#64748B" }
                    }

                    Rectangle {
                        width: 76
                        height: 32
                        radius: 6
                        color: "#FFFFFF"
                        border.color: "#CBD5E1"
                        border.width: 1
                        anchors.verticalCenter: parent.verticalCenter

                        TextInput {
                            id: potholeSlaInput
                            anchors.centerIn: parent
                            text: "" + settingsController.slaPotholeHours
                            font.pixelSize: 12
                            font.weight: Font.Bold
                            color: "#0F172A"
                        }
                        Text { anchors.right: parent.right; anchors.rightMargin: 6; anchors.verticalCenter: parent.verticalCenter; text: "hrs"; font.pixelSize: 9; color: "#94A3B8" }
                    }
                }
            }

            // Item 3: Streetlights
            Rectangle {
                width: (parent.width - 12) / 2
                height: 58
                radius: 8
                color: "#F8FAFC"
                border.color: "#E2E8F0"
                border.width: 1

                Row {
                    anchors.fill: parent
                    anchors.margins: 10
                    spacing: 10

                    Column {
                        width: parent.width - 90
                        spacing: 2
                        Text { text: "💡 Streetlights & Electrical"; font.pixelSize: 11; font.weight: Font.Bold; color: "#1E293B" }
                        Text { text: "BESCOM Electrical Cell"; font.pixelSize: 9; color: "#64748B" }
                    }

                    Rectangle {
                        width: 76
                        height: 32
                        radius: 6
                        color: "#FFFFFF"
                        border.color: "#CBD5E1"
                        border.width: 1
                        anchors.verticalCenter: parent.verticalCenter

                        TextInput {
                            id: lightSlaInput
                            anchors.centerIn: parent
                            text: "" + settingsController.slaStreetlightHours
                            font.pixelSize: 12
                            font.weight: Font.Bold
                            color: "#0F172A"
                        }
                        Text { anchors.right: parent.right; anchors.rightMargin: 6; anchors.verticalCenter: parent.verticalCenter; text: "hrs"; font.pixelSize: 9; color: "#94A3B8" }
                    }
                }
            }

            // Item 4: Solid Waste
            Rectangle {
                width: (parent.width - 12) / 2
                height: 58
                radius: 8
                color: "#F8FAFC"
                border.color: "#E2E8F0"
                border.width: 1

                Row {
                    anchors.fill: parent
                    anchors.margins: 10
                    spacing: 10

                    Column {
                        width: parent.width - 90
                        spacing: 2
                        Text { text: "🧹 Solid Waste & Garbage"; font.pixelSize: 11; font.weight: Font.Bold; color: "#166534" }
                        Text { text: "SWM Marshall & Health Squad"; font.pixelSize: 9; color: "#64748B" }
                    }

                    Rectangle {
                        width: 76
                        height: 32
                        radius: 6
                        color: "#FFFFFF"
                        border.color: "#CBD5E1"
                        border.width: 1
                        anchors.verticalCenter: parent.verticalCenter

                        TextInput {
                            id: garbageSlaInput
                            anchors.centerIn: parent
                            text: "" + settingsController.slaGarbageHours
                            font.pixelSize: 12
                            font.weight: Font.Bold
                            color: "#0F172A"
                        }
                        Text { anchors.right: parent.right; anchors.rightMargin: 6; anchors.verticalCenter: parent.verticalCenter; text: "hrs"; font.pixelSize: 9; color: "#94A3B8" }
                    }
                }
            }
        }

        // Escalation Policy Box
        Rectangle {
            width: parent.width
            height: 74
            radius: 8
            color: "#FFFBEB"
            border.color: "#FDE68A"
            border.width: 1

            Column {
                anchors.fill: parent
                anchors.margins: 10
                spacing: 4

                Row {
                    width: parent.width
                    Text { text: "⚠️ HIERARCHICAL AUTO-ESCALATION ENGINE"; font.pixelSize: 10; font.weight: Font.Bold; color: "#92400E" }
                    Item { width: parent.width - 290; height: 1 }
                    Text { text: "Auto-Escalate in:"; font.pixelSize: 10; color: "#78350F"; anchors.verticalCenter: parent.verticalCenter }
                    Rectangle {
                        width: 44; height: 22; radius: 4; color: "#FFFFFF"; border.color: "#F59E0B"; border.width: 1
                        TextInput { id: escalateHoursInput; anchors.centerIn: parent; text: "" + settingsController.escalateThresholdHours; font.pixelSize: 10; font.weight: Font.Bold; color: "#0F172A" }
                    }
                    Text { text: "Hours"; font.pixelSize: 10; color: "#78350F"; anchors.verticalCenter: parent.verticalCenter }
                }

                Text {
                    text: "If a Critical or High-Severity grievance is unacknowledged after " + escalateHoursInput.text + " hours, it is automatically escalated to the Executive Engineer (EE) & Zonal Commissioner with emergency SMS dispatch."
                    font.pixelSize: 10
                    color: "#78350F"
                    wrapMode: Text.WordWrap
                    width: parent.width
                }
            }
        }

        // Notification Channels Checkboxes
        Row {
            spacing: 20
            CheckBox {
                id: smsCheck
                text: "SMS Alerts to Nodal AEE"
                checked: settingsController.smsAlertsEnabled
                font.pixelSize: 11
            }
            CheckBox {
                id: whatsAppCheck
                text: "Official WhatsApp Dispatch"
                checked: settingsController.whatsAppAlertsEnabled
                font.pixelSize: 11
            }
            CheckBox {
                id: emailCheck
                text: "Executive Email Digest"
                checked: settingsController.emailAlertsEnabled
                font.pixelSize: 11
            }
        }

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
                    text: "Save SLA Policies"
                    font.pixelSize: 11
                    font.weight: Font.Bold
                    color: "#FFFFFF"
                }
            }

            MouseArea {
                anchors.fill: parent
                cursorShape: Qt.PointingHandCursor
                onClicked: {
                    settingsController.saveSlaHours(
                        parseInt(waterSlaInput.text) || 4,
                        parseInt(potholeSlaInput.text) || 24,
                        parseInt(lightSlaInput.text) || 24,
                        parseInt(garbageSlaInput.text) || 12,
                        36,
                        parseInt(escalateHoursInput.text) || 2,
                        smsCheck.checked,
                        whatsAppCheck.checked,
                        emailCheck.checked
                    );
                    root.saved("SLA Thresholds & Escalation Policies updated successfully!");
                }
            }
        }
    }
}
