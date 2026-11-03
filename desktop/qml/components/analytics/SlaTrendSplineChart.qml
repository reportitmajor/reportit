import QtQuick
import QtQuick.Controls

Rectangle {
    id: root

    implicitWidth: 620
    implicitHeight: 320
    radius: 10
    color: "#FFFFFF"
    border.color: "#E2E8F0"
    border.width: 1

    Column {
        anchors.fill: parent
        anchors.margins: 14
        spacing: 10

        // Header + Legend
        Row {
            width: parent.width

            Column {
                width: parent.width - legendRow.width
                spacing: 2
                Text {
                    text: "SLA Compliance Velocity & Intake Trend"
                    font.pixelSize: 13
                    font.weight: Font.Bold
                    color: "#0F172A"
                }
                Text {
                    text: "6-Month historical intake vs statutory SLA resolution curve"
                    font.pixelSize: 10
                    color: "#64748B"
                }
            }

            Row {
                id: legendRow
                spacing: 12
                anchors.verticalCenter: parent.verticalCenter

                Row {
                    spacing: 4
                    Rectangle { width: 10; height: 10; radius: 5; color: "#4338CA"; anchors.verticalCenter: parent.verticalCenter }
                    Text { text: "Total Grievances"; font.pixelSize: 10; color: "#475569" }
                }
                Row {
                    spacing: 4
                    Rectangle { width: 10; height: 10; radius: 5; color: "#16A34A"; anchors.verticalCenter: parent.verticalCenter }
                    Text { text: "Resolved (SLA Met)"; font.pixelSize: 10; color: "#475569" }
                }
                Row {
                    spacing: 4
                    Rectangle { width: 10; height: 3; color: "#EF4444"; anchors.verticalCenter: parent.verticalCenter }
                    Text { text: "90% Mandate"; font.pixelSize: 10; color: "#EF4444" }
                }
            }
        }

        // Canvas Spline Chart
        Canvas {
            id: chartCanvas
            width: parent.width
            height: parent.height - 45
            antialiasing: true

            readonly property var dataSeries: analyticsController.monthlySlaSeries

            onPaint: {
                var ctx = getContext("2d");
                ctx.reset();

                var w = width;
                var h = height;
                var paddingLeft = 45;
                var paddingRight = 20;
                var paddingTop = 20;
                var paddingBottom = 30;

                var plotW = w - paddingLeft - paddingRight;
                var plotH = h - paddingTop - paddingBottom;
                var maxVal = 2500;

                // 1. Draw horizontal grid lines & Y-axis labels
                ctx.strokeStyle = "#F1F5F9";
                ctx.lineWidth = 1;
                ctx.fillStyle = "#94A3B8";
                ctx.font = "9px 'Segoe UI', sans-serif";
                ctx.textAlign = "right";

                var ySteps = [0, 500, 1000, 1500, 2000, 2500];
                for (var i = 0; i < ySteps.length; ++i) {
                    var yVal = ySteps[i];
                    var yPos = paddingTop + plotH - (yVal / maxVal) * plotH;

                    ctx.beginPath();
                    ctx.moveTo(paddingLeft, yPos);
                    ctx.lineTo(paddingLeft + plotW, yPos);
                    ctx.stroke();

                    ctx.fillText("" + yVal, paddingLeft - 8, yPos + 3);
                }

                if (!dataSeries || dataSeries.length === 0) return;

                var count = dataSeries.length;
                var stepX = plotW / Math.max(1, count - 1);

                // Helper to get coordinates
                function getPoint(idx, key) {
                    var val = dataSeries[idx][key];
                    var x = paddingLeft + idx * stepX;
                    var y = paddingTop + plotH - (val / maxVal) * plotH;
                    return {x: x, y: y};
                }

                // 2. Draw Total Grievances Spline + Gradient Area
                var gradTotal = ctx.createLinearGradient(0, paddingTop, 0, paddingTop + plotH);
                gradTotal.addColorStop(0, "#2B4338CA");
                gradTotal.addColorStop(1, "#004338CA");

                ctx.beginPath();
                var p0 = getPoint(0, "total");
                ctx.moveTo(p0.x, p0.y);

                for (var j = 1; j < count; ++j) {
                    var prevP = getPoint(j - 1, "total");
                    var currP = getPoint(j, "total");
                    var cpX = (prevP.x + currP.x) / 2;
                    ctx.bezierCurveTo(cpX, prevP.y, cpX, currP.y, currP.x, currP.y);
                }

                ctx.lineTo(paddingLeft + plotW, paddingTop + plotH);
                ctx.lineTo(paddingLeft, paddingTop + plotH);
                ctx.closePath();
                ctx.fillStyle = gradTotal;
                ctx.fill();

                // Draw line stroke for Total
                ctx.beginPath();
                ctx.moveTo(p0.x, p0.y);
                for (var k = 1; k < count; ++k) {
                    var prevK = getPoint(k - 1, "total");
                    var currK = getPoint(k, "total");
                    var cpXK = (prevK.x + currK.x) / 2;
                    ctx.bezierCurveTo(cpXK, prevK.y, cpXK, currK.y, currK.x, currK.y);
                }
                ctx.strokeStyle = "#4338CA";
                ctx.lineWidth = 2.5;
                ctx.stroke();

                // 3. Draw Resolved Spline Line (Green)
                ctx.beginPath();
                var r0 = getPoint(0, "resolved");
                ctx.moveTo(r0.x, r0.y);
                for (var m = 1; m < count; ++m) {
                    var prevM = getPoint(m - 1, "resolved");
                    var currM = getPoint(m, "resolved");
                    var cpXM = (prevM.x + currM.x) / 2;
                    ctx.bezierCurveTo(cpXM, prevM.y, cpXM, currM.y, currM.x, currM.y);
                }
                ctx.strokeStyle = "#16A34A";
                ctx.lineWidth = 2.5;
                ctx.stroke();

                // 4. Draw Point Markers & X-Axis Labels
                ctx.textAlign = "center";
                for (var n = 0; n < count; ++n) {
                    var ptTotal = getPoint(n, "total");
                    var ptResolved = getPoint(n, "resolved");

                    // Point on Total
                    ctx.fillStyle = "#4338CA";
                    ctx.beginPath();
                    ctx.arc(ptTotal.x, ptTotal.y, 4, 0, Math.PI * 2);
                    ctx.fill();

                    // Point on Resolved
                    ctx.fillStyle = "#16A34A";
                    ctx.beginPath();
                    ctx.arc(ptResolved.x, ptResolved.y, 4, 0, Math.PI * 2);
                    ctx.fill();

                    // X-Axis Label
                    ctx.fillStyle = "#64748B";
                    ctx.font = "10px 'Segoe UI', sans-serif";
                    ctx.fillText(dataSeries[n].label, ptTotal.x, h - 8);
                }
            }
        }
    }

    Connections {
        target: analyticsController
        function onMetricsChanged() { chartCanvas.requestPaint(); }
        function onPeriodChanged() { chartCanvas.requestPaint(); }
    }
}
