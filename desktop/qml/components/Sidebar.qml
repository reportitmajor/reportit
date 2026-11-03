import QtQuick
import QtQuick.Controls

Rectangle {
    id: root

    property string currentRoute: "Dashboard"
    signal routeSelected(string route)

    implicitWidth: 220
    implicitHeight: 800
    color: "#3A47D5"

    // App Branding Header
    Item {
        id: brandingHeader
        anchors.top: parent.top
        anchors.topMargin: 24
        anchors.left: parent.left
        anchors.leftMargin: 16
        anchors.right: parent.right
        anchors.rightMargin: 12
        height: 44

        // App Logo Emblem
        Rectangle {
            id: logoContainer
            width: 42
            height: 42
            radius: 10
            color: "#FFFFFF"
            clip: true
            anchors.left: parent.left
            anchors.verticalCenter: parent.verticalCenter

            Image {
                anchors.fill: parent
                source: "qrc:/qt/qml/ReportIt/Admin/resources/images/logo.png"
                fillMode: Image.PreserveAspectCrop
                smooth: true
                mipmap: true
            }
        }

        Column {
            anchors.left: logoContainer.right
            anchors.leftMargin: 12
            anchors.right: parent.right
            anchors.verticalCenter: parent.verticalCenter
            spacing: 2

            Text {
                text: "ReportIt Admin"
                font.pixelSize: 17
                font.weight: Font.Bold
                color: "#FFFFFF"
                elide: Text.ElideRight
                width: parent.width
            }

            Text {
                text: "City Oversight"
                font.pixelSize: 11
                font.weight: Font.Medium
                color: "#BAC3FF"
            }
        }
    }

    // Navigation Items
    Column {
        id: navContainer
        anchors.top: brandingHeader.bottom
        anchors.topMargin: 28
        anchors.left: parent.left
        anchors.leftMargin: 16
        anchors.right: parent.right
        anchors.rightMargin: 16
        spacing: 6

        Repeater {
            model: [
                {"name": "Dashboard", "icon": "dashboard"},
                {"name": "Map View", "icon": "map"},
                {"name": "Reports", "icon": "reports"},
                {"name": "Analytics", "icon": "analytics"},
                {"name": "Settings", "icon": "settings"}
            ]

            Rectangle {
                id: navBtn
                width: navContainer.width
                height: 42
                radius: 8
                
                readonly property bool isActive: root.currentRoute === modelData.name
                readonly property bool isHovered: mouseArea.containsMouse

                color: isActive ? "#4F5AE8" : (isHovered ? "#4652DF" : "transparent")

                Row {
                    anchors.fill: parent
                    anchors.leftMargin: 14
                    anchors.rightMargin: 14
                    spacing: 12

                    // Custom Clean Vector Nav Icons
                    Canvas {
                        width: 18
                        height: 18
                        anchors.verticalCenter: parent.verticalCenter
                        onPaint: {
                            var ctx = getContext("2d");
                            ctx.reset();
                            ctx.strokeStyle = navBtn.isActive ? "#FFFFFF" : "#C7CEFF";
                            ctx.fillStyle = navBtn.isActive ? "#FFFFFF" : "#C7CEFF";
                            ctx.lineWidth = 1.6;
                            ctx.lineCap = "round";
                            ctx.lineJoin = "round";

                            if (modelData.icon === "dashboard") {
                                // 4 grid blocks
                                ctx.strokeRect(2, 2, 5.5, 5.5);
                                ctx.strokeRect(10.5, 2, 5.5, 5.5);
                                ctx.strokeRect(2, 10.5, 5.5, 5.5);
                                ctx.strokeRect(10.5, 10.5, 5.5, 5.5);
                            } else if (modelData.icon === "map") {
                                // Map folds
                                ctx.beginPath();
                                ctx.moveTo(2, 4); ctx.lineTo(6.5, 2); ctx.lineTo(11.5, 4); ctx.lineTo(16, 2);
                                ctx.lineTo(16, 14); ctx.lineTo(11.5, 16); ctx.lineTo(6.5, 14); ctx.lineTo(2, 16);
                                ctx.closePath();
                                ctx.stroke();
                                ctx.beginPath();
                                ctx.moveTo(6.5, 2); ctx.lineTo(6.5, 14);
                                ctx.moveTo(11.5, 4); ctx.lineTo(11.5, 16);
                                ctx.stroke();
                            } else if (modelData.icon === "reports") {
                                // Document with folded corner
                                ctx.beginPath();
                                ctx.rect(3, 2, 12, 14);
                                ctx.stroke();
                                ctx.beginPath();
                                ctx.moveTo(6, 6); ctx.lineTo(12, 6);
                                ctx.moveTo(6, 9); ctx.lineTo(12, 9);
                                ctx.moveTo(6, 12); ctx.lineTo(10, 12);
                                ctx.stroke();
                            } else if (modelData.icon === "analytics") {
                                // Bar chart inside square
                                ctx.strokeRect(2, 2, 14, 14);
                                ctx.beginPath();
                                ctx.moveTo(5, 12); ctx.lineTo(5, 9);
                                ctx.moveTo(9, 12); ctx.lineTo(9, 6);
                                ctx.moveTo(13, 12); ctx.lineTo(13, 8);
                                ctx.stroke();
                            } else if (modelData.icon === "settings") {
                                // Gear icon
                                ctx.beginPath();
                                ctx.arc(9, 9, 3.5, 0, 2 * Math.PI);
                                ctx.stroke();
                                ctx.beginPath();
                                for (var a = 0; a < 8; ++a) {
                                    var angle = a * Math.PI / 4;
                                    var x1 = 9 + Math.cos(angle) * 5;
                                    var y1 = 9 + Math.sin(angle) * 5;
                                    var x2 = 9 + Math.cos(angle) * 7.5;
                                    var y2 = 9 + Math.sin(angle) * 7.5;
                                    ctx.moveTo(x1, y1); ctx.lineTo(x2, y2);
                                }
                                ctx.stroke();
                            }
                        }
                    }

                    Text {
                        anchors.verticalCenter: parent.verticalCenter
                        text: modelData.name
                        font.pixelSize: 13
                        font.weight: navBtn.isActive ? Font.DemiBold : Font.Normal
                        color: navBtn.isActive ? "#FFFFFF" : "#D7DCFF"
                    }
                }

                MouseArea {
                    id: mouseArea
                    anchors.fill: parent
                    hoverEnabled: true
                    cursorShape: Qt.PointingHandCursor
                    onClicked: {
                        root.currentRoute = modelData.name
                        root.routeSelected(modelData.name)
                    }
                }
            }
        }
    }

    // Footer Super Admin User Badge
    Row {
        id: footerUserBadge
        anchors.bottom: parent.bottom
        anchors.bottomMargin: 24
        anchors.left: parent.left
        anchors.leftMargin: 20
        anchors.right: parent.right
        anchors.rightMargin: 16
        spacing: 10

        // Dark avatar badge
        Rectangle {
            width: 34
            height: 34
            radius: 17
            color: "#0F172A"
            border.color: "#334155"
            border.width: 1
            anchors.verticalCenter: parent.verticalCenter

            // Badge inner symbol
            Canvas {
                anchors.centerIn: parent
                width: 18
                height: 18
                onPaint: {
                    var ctx = getContext("2d");
                    ctx.reset();
                    ctx.strokeStyle = "#38BDF8";
                    ctx.fillStyle = "#38BDF8";
                    ctx.lineWidth = 1.4;

                    ctx.beginPath();
                    ctx.arc(9, 6, 3, 0, 2 * Math.PI);
                    ctx.fill();

                    ctx.beginPath();
                    ctx.arc(9, 15, 5, Math.PI, 0);
                    ctx.fill();
                }
            }
        }

        Column {
            anchors.verticalCenter: parent.verticalCenter
            spacing: 2

            Text {
                text: "Admin User"
                font.pixelSize: 13
                font.weight: Font.Bold
                color: "#FFFFFF"
            }

            Text {
                text: "SUPER ADMIN"
                font.pixelSize: 9
                font.weight: Font.Bold
                font.letterSpacing: 0.6
                color: "#93C5FD"
            }
        }
    }
}
