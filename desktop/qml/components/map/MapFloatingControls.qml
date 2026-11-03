import QtQuick
import QtQuick.Controls

Item {
    id: root

    signal zoomInClicked()
    signal zoomOutClicked()
    signal resetClicked()

    implicitWidth: 160
    implicitHeight: 180

    Column {
        anchors.right: parent.right
        anchors.bottom: parent.bottom
        anchors.margins: 16
        spacing: 8

        // Zoom Controls Card
        Rectangle {
            width: 38
            height: 76
            radius: 8
            color: "#FFFFFF"
            border.color: "#CBD5E1"
            border.width: 1
            anchors.right: parent.right

            Column {
                anchors.fill: parent

                // Zoom In
                Item {
                    width: parent.width
                    height: 38

                    Text {
                        anchors.centerIn: parent
                        text: "+"
                        font.pixelSize: 18
                        font.weight: Font.Bold
                        color: "#1E293B"
                    }

                    MouseArea {
                        anchors.fill: parent
                        cursorShape: Qt.PointingHandCursor
                        onClicked: root.zoomInClicked()
                    }
                }

                // Divider
                Rectangle {
                    width: parent.width
                    height: 1
                    color: "#E2E8F0"
                }

                // Zoom Out
                Item {
                    width: parent.width
                    height: 37

                    Text {
                        anchors.centerIn: parent
                        text: "−"
                        font.pixelSize: 18
                        font.weight: Font.Bold
                        color: "#1E293B"
                    }

                    MouseArea {
                        anchors.fill: parent
                        cursorShape: Qt.PointingHandCursor
                        onClicked: root.zoomOutClicked()
                    }
                }
            }
        }

        // Reset Center Button
        Rectangle {
            width: 38
            height: 38
            radius: 8
            color: "#FFFFFF"
            border.color: "#CBD5E1"
            border.width: 1
            anchors.right: parent.right

            Text {
                anchors.centerIn: parent
                text: "🧭"
                font.pixelSize: 15
            }

            MouseArea {
                anchors.fill: parent
                cursorShape: Qt.PointingHandCursor
                onClicked: {
                    mapController.resetView()
                    root.resetClicked()
                }
            }
        }
    }
}
