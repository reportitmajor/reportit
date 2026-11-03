import QtQuick
import QtQuick.Controls

Rectangle {
    id: root

    property alias model: listView.model
    signal viewAllClicked()

    implicitWidth: 600
    implicitHeight: 290
    radius: 12
    color: "#FFFFFF"
    border.color: "#EAEFF6"
    border.width: 1

    Column {
        anchors.fill: parent
        anchors.margins: 20
        spacing: 12

        // Card Header: Title on Left, View All on Right
        Item {
            width: parent.width
            height: 26

            Text {
                anchors.left: parent.left
                anchors.verticalCenter: parent.verticalCenter
                text: "Recent Reports"
                font.pixelSize: 16
                font.weight: Font.Bold
                color: "#0F172A"
            }

            Text {
                id: viewAllBtn
                anchors.right: parent.right
                anchors.verticalCenter: parent.verticalCenter
                text: "View All"
                font.pixelSize: 12
                font.weight: Font.DemiBold
                color: "#3B49DF"

                MouseArea {
                    anchors.fill: parent
                    cursorShape: Qt.PointingHandCursor
                    onClicked: root.viewAllClicked()
                }
            }
        }

        // Table Column Headers
        Rectangle {
            width: parent.width
            height: 28
            color: "#F8FAFC"
            radius: 6

            Item {
                anchors.fill: parent
                anchors.leftMargin: 12
                anchors.rightMargin: 12

                Text {
                    id: colHeaderId
                    anchors.left: parent.left
                    anchors.verticalCenter: parent.verticalCenter
                    width: 150
                    text: "REPORT ID"
                    font.pixelSize: 10
                    font.weight: Font.Bold
                    font.letterSpacing: 0.5
                    color: "#94A3B8"
                }

                Text {
                    anchors.left: colHeaderId.right
                    anchors.leftMargin: 8
                    anchors.right: colHeaderStatus.left
                    anchors.rightMargin: 12
                    anchors.verticalCenter: parent.verticalCenter
                    text: "ISSUE"
                    font.pixelSize: 10
                    font.weight: Font.Bold
                    font.letterSpacing: 0.5
                    color: "#94A3B8"
                    elide: Text.ElideRight
                }

                Text {
                    id: colHeaderStatus
                    anchors.right: colHeaderDate.left
                    anchors.rightMargin: 12
                    anchors.verticalCenter: parent.verticalCenter
                    width: 100
                    text: "STATUS"
                    font.pixelSize: 10
                    font.weight: Font.Bold
                    font.letterSpacing: 0.5
                    color: "#94A3B8"
                    horizontalAlignment: Text.AlignHCenter
                }

                Text {
                    id: colHeaderDate
                    anchors.right: parent.right
                    anchors.verticalCenter: parent.verticalCenter
                    width: 140
                    text: "DATE"
                    font.pixelSize: 10
                    font.weight: Font.Bold
                    font.letterSpacing: 0.5
                    color: "#94A3B8"
                    horizontalAlignment: Text.AlignRight
                }
            }
        }

        // Table Rows List
        ListView {
            id: listView
            width: parent.width
            height: parent.height - 75
            clip: true
            spacing: 0

            delegate: Rectangle {
                width: listView.width
                height: 54
                color: mouseArea.containsMouse ? "#F8FAFC" : "transparent"

                Rectangle {
                    anchors.bottom: parent.bottom
                    width: parent.width
                    height: 1
                    color: "#F1F5F9"
                }

                Item {
                    anchors.fill: parent
                    anchors.leftMargin: 12
                    anchors.rightMargin: 12

                    // Report ID
                    Text {
                        id: cellReportId
                        anchors.left: parent.left
                        anchors.verticalCenter: parent.verticalCenter
                        width: 150
                        text: model.reportId
                        font.pixelSize: 12
                        font.weight: Font.Bold
                        color: "#0F172A"
                        elide: Text.ElideRight
                    }

                    // Issue & Location
                    Column {
                        anchors.left: cellReportId.right
                        anchors.leftMargin: 8
                        anchors.right: cellStatus.left
                        anchors.rightMargin: 12
                        anchors.verticalCenter: parent.verticalCenter
                        spacing: 2

                        Text {
                            text: model.issueTitle
                            font.pixelSize: 12
                            font.weight: Font.DemiBold
                            color: "#1E293B"
                            elide: Text.ElideRight
                            width: parent.width
                        }

                        Text {
                            text: model.location
                            font.pixelSize: 11
                            font.weight: Font.Normal
                            color: "#94A3B8"
                            elide: Text.ElideRight
                            width: parent.width
                        }
                    }

                    // Status Badge
                    Item {
                        id: cellStatus
                        anchors.right: cellDate.left
                        anchors.rightMargin: 12
                        anchors.verticalCenter: parent.verticalCenter
                        width: 100
                        height: 24

                        StatusBadge {
                            anchors.centerIn: parent
                            statusText: model.status
                        }
                    }

                    // Date & Time
                    Text {
                        id: cellDate
                        anchors.right: parent.right
                        anchors.verticalCenter: parent.verticalCenter
                        width: 140
                        text: model.date
                        font.pixelSize: 11
                        font.weight: Font.Medium
                        color: "#64748B"
                        horizontalAlignment: Text.AlignRight
                        elide: Text.ElideRight
                    }
                }

                MouseArea {
                    id: mouseArea
                    anchors.fill: parent
                    hoverEnabled: true
                }
            }
        }
    }
}
