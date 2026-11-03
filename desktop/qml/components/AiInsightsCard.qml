import QtQuick
import QtQuick.Controls

Rectangle {
    id: root

    property var insights: [
        {
            "title": "Unusual Activity Detected",
            "description": "High concentration of \"Street Light\" reports in District 4 over the last 6 hours.",
            "accentColor": "#3B49DF"
        },
        {
            "title": "Resolution Efficiency",
            "description": "Maintenance teams are closing \"Pothole\" issues 15% faster than average today.",
            "accentColor": "#0EA5E9"
        },
        {
            "title": "Peak Reporting Hours",
            "description": "Expect a surge in traffic-related reports between 5:00 PM and 7:00 PM based on historical data.",
            "accentColor": "#6366F1"
        }
    ]

    implicitWidth: 320
    implicitHeight: 330
    radius: 12
    color: "#FFFFFF"
    border.color: "#EAEFF6"
    border.width: 1

    Column {
        anchors.fill: parent
        anchors.margins: 18
        spacing: 12

        // Header Row
        Row {
            spacing: 8
            anchors.left: parent.left
            
            // Sparkle icon
            Text {
                text: "✦"
                font.pixelSize: 14
                font.weight: Font.Bold
                color: "#4338CA"
                anchors.verticalCenter: parent.verticalCenter
            }

            Text {
                text: "AI-GENERATED INSIGHTS"
                font.pixelSize: 12
                font.weight: Font.Bold
                font.letterSpacing: 0.8
                color: "#1E293B"
                anchors.verticalCenter: parent.verticalCenter
            }
        }

        // Insights List
        Repeater {
            model: root.insights

            Rectangle {
                width: parent.width
                height: 72
                radius: 8
                color: "#F8FAFC"
                border.color: "#EDF2F7"
                border.width: 1

                // Left colored vertical accent line
                Rectangle {
                    width: 4
                    height: parent.height - 16
                    anchors.left: parent.left
                    anchors.leftMargin: 6
                    anchors.verticalCenter: parent.verticalCenter
                    radius: 2
                    color: modelData.accentColor ? modelData.accentColor : "#3B49DF"
                }

                Column {
                    anchors.left: parent.left
                    anchors.right: parent.right
                    anchors.leftMargin: 20
                    anchors.rightMargin: 12
                    anchors.verticalCenter: parent.verticalCenter
                    spacing: 3

                    Text {
                        text: modelData.title
                        font.pixelSize: 12
                        font.weight: Font.Bold
                        color: "#0F172A"
                    }

                    Text {
                        width: parent.width
                        text: modelData.description
                        font.pixelSize: 11
                        font.weight: Font.Normal
                        color: "#64748B"
                        wrapMode: Text.WordWrap
                        maximumLineCount: 2
                        elide: Text.ElideRight
                    }
                }
            }
        }
    }
}
