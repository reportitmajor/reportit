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
                text: "AI Auto-Triage & Geographic Geofencing Engine"
                font.pixelSize: 14
                font.weight: Font.Bold
                color: "#0F172A"
            }
            Text {
                text: "Configure autonomous classification thresholds, ward polygon geofencing, and duplicate photo filtering"
                font.pixelSize: 11
                color: "#64748B"
            }
        }

        Rectangle { width: parent.width; height: 1; color: "#F1F5F9" }

        // Section 1: AI Confidence Slider
        Rectangle {
            width: parent.width
            height: 90
            radius: 8
            color: "#F8FAFC"
            border.color: "#E2E8F0"
            border.width: 1

            Column {
                anchors.fill: parent
                anchors.margins: 12
                spacing: 8

                Row {
                    width: parent.width
                    Text { text: "⚡ AI Autonomous Triage Confidence:"; font.pixelSize: 11; font.weight: Font.Bold; color: "#1E293B" }
                    Item { width: parent.width - 320; height: 1 }
                    Text {
                        text: aiSlider.value.toFixed(0) + "% Threshold"
                        font.pixelSize: 12
                        font.weight: Font.Bold
                        color: "#4338CA"
                    }
                }

                Slider {
                    id: aiSlider
                    width: parent.width
                    from: 60
                    to: 98
                    stepSize: 1
                    value: settingsController.aiConfidenceThreshold
                }

                Text {
                    text: "Reports with AI classification confidence above " + aiSlider.value.toFixed(0) + "% are automatically routed to the nodal department without manual triage."
                    font.pixelSize: 9
                    color: "#64748B"
                }
            }
        }

        // Section 2: Geofencing & Duplicate Detection Toggles
        Grid {
            width: parent.width
            columns: 2
            spacing: 12

            // Toggle 1: 243-Ward Geofencing
            Rectangle {
                width: (parent.width - 12) / 2
                height: 80
                radius: 8
                color: "#F8FAFC"
                border.color: "#E2E8F0"
                border.width: 1

                Row {
                    anchors.fill: parent
                    anchors.margins: 10
                    spacing: 10

                    Column {
                        width: parent.width - 60
                        spacing: 2
                        Text { text: "📍 243-Ward Polygon Geofencing"; font.pixelSize: 11; font.weight: Font.Bold; color: "#0F172A" }
                        Text { text: "Auto-maps citizen GPS pins strictly inside official ward boundary polygons."; font.pixelSize: 9; color: "#64748B"; wrapMode: Text.WordWrap; width: parent.width }
                    }

                    Switch {
                        id: geofenceSwitch
                        checked: settingsController.autoGeofenceEnabled
                        anchors.verticalCenter: parent.verticalCenter
                    }
                }
            }

            // Toggle 2: Duplicate Photo Detection
            Rectangle {
                width: (parent.width - 12) / 2
                height: 80
                radius: 8
                color: "#F8FAFC"
                border.color: "#E2E8F0"
                border.width: 1

                Row {
                    anchors.fill: parent
                    anchors.margins: 10
                    spacing: 10

                    Column {
                        width: parent.width - 60
                        spacing: 2
                        Text { text: "🔍 AI Duplicate Image Filter"; font.pixelSize: 11; font.weight: Font.Bold; color: "#0F172A" }
                        Text { text: "Clusters multi-citizen photos of identical potholes within a 50-meter radius."; font.pixelSize: 9; color: "#64748B"; wrapMode: Text.WordWrap; width: parent.width }
                    }

                    Switch {
                        id: duplicateSwitch
                        checked: settingsController.duplicateDetectionEnabled
                        anchors.verticalCenter: parent.verticalCenter
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
                    text: "Save AI Rules"
                    font.pixelSize: 11
                    font.weight: Font.Bold
                    color: "#FFFFFF"
                }
            }

            MouseArea {
                anchors.fill: parent
                cursorShape: Qt.PointingHandCursor
                onClicked: {
                    settingsController.saveAiRouting(
                        Math.round(aiSlider.value),
                        geofenceSwitch.checked,
                        duplicateSwitch.checked,
                        90
                    );
                    root.saved("AI Auto-Triage & Geofence settings saved successfully!");
                }
            }
        }
    }
}
