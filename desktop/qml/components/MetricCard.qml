import QtQuick
import QtQuick.Controls
import QtQuick.Shapes

Rectangle {
    id: root

    property string title: "Metric"
    property string value: "0"
    property string subtitle: "+0%"
    property color subtitleColor: "#64748B"
    property color iconBg: "#EDE9FE"
    property color iconColor: "#6D28D9"
    property string iconType: "document" // "document", "clipboard", "check", "timer"

    implicitWidth: 240
    implicitHeight: 120
    radius: 12
    color: "#FFFFFF"
    border.color: "#EAEFF6"
    border.width: 1

    Column {
        anchors.fill: parent
        anchors.margins: 16
        spacing: 8

        // Top Row: Title + Icon Bubble
        Row {
            width: parent.width
            
            Text {
                width: parent.width - 36
                anchors.verticalCenter: parent.verticalCenter
                text: root.title
                font.pixelSize: 13
                font.weight: Font.DemiBold
                color: "#64748B"
                elide: Text.ElideRight
            }

            Rectangle {
                width: 34
                height: 34
                radius: 8
                color: root.iconBg
                anchors.verticalCenter: parent.verticalCenter

                // Draw custom clean geometric icon matching type
                Canvas {
                    anchors.centerIn: parent
                    width: 18
                    height: 18
                    onPaint: {
                        var ctx = getContext("2d");
                        ctx.reset();
                        ctx.strokeStyle = root.iconColor;
                        ctx.fillStyle = root.iconColor;
                        ctx.lineWidth = 1.8;
                        ctx.lineCap = "round";
                        ctx.lineJoin = "round";

                        if (root.iconType === "document") {
                            // Document icon
                            ctx.beginPath();
                            ctx.rect(3, 2, 12, 14);
                            ctx.stroke();
                            ctx.beginPath();
                            ctx.moveTo(6, 6); ctx.lineTo(12, 6);
                            ctx.moveTo(6, 9); ctx.lineTo(12, 9);
                            ctx.moveTo(6, 12); ctx.lineTo(10, 12);
                            ctx.stroke();
                        } else if (root.iconType === "clipboard") {
                            // Clipboard / task icon
                            ctx.beginPath();
                            ctx.rect(3, 4, 12, 12);
                            ctx.stroke();
                            ctx.beginPath();
                            ctx.moveTo(6, 2); ctx.lineTo(12, 2);
                            ctx.stroke();
                            ctx.beginPath();
                            ctx.moveTo(6, 8); ctx.lineTo(10, 8);
                            ctx.moveTo(6, 11); ctx.lineTo(12, 11);
                            ctx.stroke();
                        } else if (root.iconType === "check") {
                            // Checkmark circle icon
                            ctx.beginPath();
                            ctx.arc(9, 9, 7, 0, 2 * Math.PI);
                            ctx.stroke();
                            ctx.beginPath();
                            ctx.moveTo(6, 9);
                            ctx.lineTo(8.5, 11.5);
                            ctx.lineTo(12.5, 6.5);
                            ctx.stroke();
                        } else if (root.iconType === "timer") {
                            // Timer / stopwatch icon
                            ctx.beginPath();
                            ctx.arc(9, 10, 6.5, 0, 2 * Math.PI);
                            ctx.stroke();
                            ctx.beginPath();
                            ctx.moveTo(9, 10); ctx.lineTo(9, 6.5);
                            ctx.moveTo(9, 10); ctx.lineTo(12, 10);
                            ctx.stroke();
                            ctx.beginPath();
                            ctx.moveTo(7, 2); ctx.lineTo(11, 2);
                            ctx.stroke();
                        }
                    }
                }
            }
        }

        // Middle: Large Value
        Text {
            text: root.value
            font.pixelSize: 24
            font.weight: Font.Bold
            color: "#0F172A"
        }

        // Bottom: Subtitle / Trend
        Text {
            text: root.subtitle
            font.pixelSize: 12
            font.weight: Font.Medium
            color: root.subtitleColor
        }
    }
}
