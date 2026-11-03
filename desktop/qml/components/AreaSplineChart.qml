import QtQuick
import QtQuick.Controls

Rectangle {
    id: root

    property string title: "Reports Over Last 7 Days"
    property var dataPoints: [24, 26, 22, 75, 48, 62, 40]
    property var labels: ["Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"]
    property string activePeriod: "weekly" // "weekly" or "monthly"

    signal periodSelected(string period)

    implicitWidth: 600
    implicitHeight: 330
    radius: 12
    color: "#FFFFFF"
    border.color: "#EAEFF6"
    border.width: 1

    Column {
        anchors.fill: parent
        anchors.margins: 20
        spacing: 16

        // Header: Title on Left and Toggle Pill on Right
        Item {
            width: parent.width
            height: 32

            Text {
                anchors.left: parent.left
                anchors.verticalCenter: parent.verticalCenter
                text: root.title
                font.pixelSize: 16
                font.weight: Font.Bold
                color: "#0F172A"
            }

            Rectangle {
                id: toggleContainer
                anchors.right: parent.right
                anchors.verticalCenter: parent.verticalCenter
                width: 130
                height: 30
                radius: 15
                color: "#F1F5F9"

                Row {
                    anchors.centerIn: parent
                    spacing: 2

                    // Weekly Button
                    Rectangle {
                        width: 60
                        height: 24
                        radius: 12
                        color: root.activePeriod === "weekly" ? "#FFFFFF" : "transparent"
                        
                        Text {
                            anchors.centerIn: parent
                            text: "Weekly"
                            font.pixelSize: 12
                            font.weight: root.activePeriod === "weekly" ? Font.DemiBold : Font.Normal
                            color: root.activePeriod === "weekly" ? "#0F172A" : "#64748B"
                        }

                        MouseArea {
                            anchors.fill: parent
                            cursorShape: Qt.PointingHandCursor
                            onClicked: {
                                root.activePeriod = "weekly"
                                root.periodSelected("weekly")
                            }
                        }
                    }

                    // Monthly Button
                    Rectangle {
                        width: 60
                        height: 24
                        radius: 12
                        color: root.activePeriod === "monthly" ? "#FFFFFF" : "transparent"

                        Text {
                            anchors.centerIn: parent
                            text: "Monthly"
                            font.pixelSize: 12
                            font.weight: root.activePeriod === "monthly" ? Font.DemiBold : Font.Normal
                            color: root.activePeriod === "monthly" ? "#0F172A" : "#64748B"
                        }

                        MouseArea {
                            anchors.fill: parent
                            cursorShape: Qt.PointingHandCursor
                            onClicked: {
                                root.activePeriod = "monthly"
                                root.periodSelected("monthly")
                            }
                        }
                    }
                }
            }
        }

        // Chart Canvas Area
        Item {
            width: parent.width
            height: parent.height - 70

            Canvas {
                id: chartCanvas
                anchors.fill: parent
                antialiasing: true

                onPaint: {
                    var ctx = getContext("2d");
                    ctx.reset();

                    var w = width;
                    var h = height - 30; // Leave space for labels
                    var paddingTop = 15;
                    var paddingBottom = 10;
                    var drawHeight = h - paddingTop - paddingBottom;

                    var pts = root.dataPoints;
                    if (!pts || pts.length === 0) return;

                    var maxVal = 100;
                    var minVal = 0;

                    // Draw Horizontal Gridlines
                    ctx.strokeStyle = "#F1F5F9";
                    ctx.lineWidth = 1;
                    var gridSteps = 3;
                    for (var g = 0; g <= gridSteps; ++g) {
                        var gy = paddingTop + (drawHeight / gridSteps) * g;
                        ctx.beginPath();
                        ctx.moveTo(0, gy);
                        ctx.lineTo(w, gy);
                        ctx.stroke();
                    }

                    // Calculate point coordinates
                    var coords = [];
                    var stepX = w / (pts.length - 1);
                    for (var i = 0; i < pts.length; ++i) {
                        var px = i * stepX;
                        var py = paddingTop + drawHeight - ((pts[i] - minVal) / (maxVal - minVal)) * drawHeight;
                        coords.push({x: px, y: py});
                    }

                    // 1. Draw Gradient Area Fill under spline
                    var gradient = ctx.createLinearGradient(0, paddingTop, 0, h);
                    gradient.addColorStop(0, "rgba(59, 76, 232, 0.28)");
                    gradient.addColorStop(0.6, "rgba(59, 76, 232, 0.08)");
                    gradient.addColorStop(1, "rgba(59, 76, 232, 0.0)");

                    ctx.fillStyle = gradient;
                    ctx.beginPath();
                    ctx.moveTo(coords[0].x, h);
                    ctx.lineTo(coords[0].x, coords[0].y);

                    for (var k = 0; k < coords.length - 1; ++k) {
                        var p0 = coords[k];
                        var p1 = coords[k + 1];
                        var cpX1 = p0.x + (p1.x - p0.x) / 2;
                        var cpY1 = p0.y;
                        var cpX2 = p0.x + (p1.x - p0.x) / 2;
                        var cpY2 = p1.y;
                        ctx.bezierCurveTo(cpX1, cpY1, cpX2, cpY2, p1.x, p1.y);
                    }

                    ctx.lineTo(coords[coords.length - 1].x, h);
                    ctx.closePath();
                    ctx.fill();

                    // 2. Draw Smooth Spline Stroke
                    ctx.strokeStyle = "#3B4CE8";
                    ctx.lineWidth = 3.2;
                    ctx.lineCap = "round";
                    ctx.beginPath();
                    ctx.moveTo(coords[0].x, coords[0].y);

                    for (var m = 0; m < coords.length - 1; ++m) {
                        var pStart = coords[m];
                        var pEnd = coords[m + 1];
                        var cp1X = pStart.x + (pEnd.x - pStart.x) / 2;
                        var cp1Y = pStart.y;
                        var cp2X = pStart.x + (pEnd.x - pStart.x) / 2;
                        var cp2Y = pEnd.y;
                        ctx.bezierCurveTo(cp1X, cp1Y, cp2X, cp2Y, pEnd.x, pEnd.y);
                    }
                    ctx.stroke();
                }
            }

            // X-Axis Labels Row
            Row {
                anchors.bottom: parent.bottom
                width: parent.width
                
                Repeater {
                    model: root.labels
                    Item {
                        width: parent.width / root.labels.length
                        height: 20

                        Text {
                            anchors.centerIn: parent
                            text: modelData
                            font.pixelSize: 11
                            font.weight: Font.Medium
                            color: "#94A3B8"
                        }
                    }
                }
            }
        }
    }

    onDataPointsChanged: chartCanvas.requestPaint()
    onActivePeriodChanged: chartCanvas.requestPaint()
    Component.onCompleted: chartCanvas.requestPaint()
}
