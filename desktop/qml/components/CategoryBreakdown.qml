import QtQuick
import QtQuick.Controls

Rectangle {
    id: root

    property alias model: catRepeater.model

    implicitWidth: 320
    implicitHeight: 290
    radius: 12
    color: "#FFFFFF"
    border.color: "#EAEFF6"
    border.width: 1

    Column {
        anchors.fill: parent
        anchors.margins: 18
        spacing: 12

        // Card Header
        Text {
            text: "REPORTS BY CATEGORY"
            font.pixelSize: 12
            font.weight: Font.Bold
            font.letterSpacing: 0.8
            color: "#1E293B"
        }

        // Category Progress Bars
        Column {
            width: parent.width
            spacing: 10

            Repeater {
                id: catRepeater

                Column {
                    width: parent.width
                    spacing: 4

                    Item {
                        width: parent.width
                        height: 18

                        Text {
                            anchors.left: parent.left
                            anchors.verticalCenter: parent.verticalCenter
                            text: model.name ? model.name : ""
                            font.pixelSize: 12
                            font.weight: Font.DemiBold
                            color: "#334155"
                        }

                        Text {
                            anchors.right: parent.right
                            anchors.verticalCenter: parent.verticalCenter
                            text: (model.percentage !== undefined ? model.percentage : 0) + "%"
                            font.pixelSize: 12
                            font.weight: Font.DemiBold
                            color: "#64748B"
                        }
                    }

                    // Progress Bar Track
                    Rectangle {
                        width: parent.width
                        height: 6
                        radius: 3
                        color: "#F1F5F9"

                        // Progress Bar Fill
                        Rectangle {
                            height: parent.height
                            width: Math.max(0, Math.min(parent.width, parent.width * ((model.percentage || 0) / 100.0)))
                            radius: 3
                            color: model.color ? model.color : "#3B49DF"
                        }
                    }
                }
            }
        }

        // Concentric Radar / Donut Target Illustration at bottom
        Item {
            width: parent.width
            height: 70

            Rectangle {
                width: 60
                height: 60
                anchors.centerIn: parent
                radius: 30
                color: "#64748B"

                // Inner circle 1
                Rectangle {
                    anchors.centerIn: parent
                    width: 44
                    height: 44
                    radius: 22
                    color: "#E2E8F0"

                    // Inner circle 2
                    Rectangle {
                        anchors.centerIn: parent
                        width: 28
                        height: 28
                        radius: 14
                        color: "#FFFFFF"

                        // Radar sweep cone
                        Canvas {
                            anchors.fill: parent
                            onPaint: {
                                var ctx = getContext("2d");
                                ctx.reset();
                                ctx.fillStyle = "rgba(100, 116, 139, 0.4)";
                                ctx.beginPath();
                                ctx.moveTo(14, 14);
                                ctx.arc(14, 14, 14, -Math.PI / 4, Math.PI / 3);
                                ctx.closePath();
                                ctx.fill();

                                ctx.fillStyle = "#334155";
                                ctx.beginPath();
                                ctx.arc(14, 14, 4, 0, 2 * Math.PI);
                                ctx.fill();
                            }
                        }
                    }
                }
            }
        }
    }
}
