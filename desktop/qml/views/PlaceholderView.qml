import QtQuick
import QtQuick.Controls

Item {
    id: root

    property string title: "Section"
    property string description: "This module will be connected as we build out subsequent sections."

    implicitWidth: 1000
    implicitHeight: 800

    Rectangle {
        anchors.fill: parent
        color: "#F4F6FB"

        Column {
            anchors.centerIn: parent
            spacing: 16

            Rectangle {
                width: 72
                height: 72
                radius: 36
                color: "#EEF2FF"
                anchors.horizontalCenter: parent.horizontalCenter

                Text {
                    anchors.centerIn: parent
                    text: "✦"
                    font.pixelSize: 28
                    color: "#4338CA"
                }
            }

            Text {
                text: root.title
                font.pixelSize: 22
                font.weight: Font.Bold
                color: "#0F172A"
                anchors.horizontalCenter: parent.horizontalCenter
            }

            Text {
                text: root.description
                font.pixelSize: 13
                font.weight: Font.Normal
                color: "#64748B"
                anchors.horizontalCenter: parent.horizontalCenter
            }
        }
    }
}
