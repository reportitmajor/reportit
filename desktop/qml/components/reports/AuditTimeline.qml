import QtQuick
import QtQuick.Controls

Item {
    id: root

    property var events: []

    implicitWidth: 320
    implicitHeight: eventCol.height

    Column {
        id: eventCol
        width: parent.width
        spacing: 0

        Repeater {
            model: root.events

            Item {
                width: parent.width
                height: 58

                // Vertical Connector Line
                Rectangle {
                    x: 16
                    y: 18
                    width: 2
                    height: parent.height - 18
                    color: "#E2E8F0"
                    visible: index < root.events.length - 1
                }

                // Step Icon / Circle
                Rectangle {
                    x: 6
                    y: 4
                    width: 22
                    height: 22
                    radius: 11
                    color: index === root.events.length - 1 ? "#EEF2FF" : "#F8FAFC"
                    border.color: index === root.events.length - 1 ? "#4338CA" : "#CBD5E1"
                    border.width: 1.5

                    Text {
                        anchors.centerIn: parent
                        text: modelData.icon || "•"
                        font.pixelSize: 10
                    }
                }

                // Event Content
                Column {
                    anchors.left: parent.left
                    anchors.leftMargin: 36
                    anchors.right: parent.right
                    anchors.top: parent.top
                    spacing: 2

                    Row {
                        width: parent.width
                        spacing: 6

                        Text {
                            text: modelData.action || ""
                            font.pixelSize: 11
                            font.weight: Font.DemiBold
                            color: "#0F172A"
                            elide: Text.ElideRight
                            width: parent.width - timeTxt.implicitWidth - 10
                        }

                        Text {
                            id: timeTxt
                            text: modelData.time || ""
                            font.pixelSize: 10
                            color: "#94A3B8"
                        }
                    }

                    Text {
                        text: (modelData.actor || "") + " • " + (modelData.role || "")
                        font.pixelSize: 10
                        color: "#64748B"
                        elide: Text.ElideRight
                        width: parent.width
                    }
                }
            }
        }
    }
}
