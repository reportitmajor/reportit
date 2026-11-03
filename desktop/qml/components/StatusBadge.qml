import QtQuick
import QtQuick.Controls

Item {
    id: root

    property string statusText: "PENDING"
    property real preferredHeight: 24

    implicitWidth: badgeRect.width
    implicitHeight: preferredHeight

    readonly property string normalizedStatus: statusText.toUpperCase().trim()

    readonly property color bgColor: {
        if (normalizedStatus === "IN PROGRESS" || normalizedStatus === "IN\nPROGRESS") return "#FEF3C7"
        if (normalizedStatus === "RESOLVED") return "#DCFCE7"
        if (normalizedStatus === "CRITICAL") return "#FEE2E2"
        return "#F1F5F9"
    }

    readonly property color fgColor: {
        if (normalizedStatus === "IN PROGRESS" || normalizedStatus === "IN\nPROGRESS") return "#B45309"
        if (normalizedStatus === "RESOLVED") return "#15803D"
        if (normalizedStatus === "CRITICAL") return "#B91C1C"
        return "#475569"
    }

    Rectangle {
        id: badgeRect
        anchors.verticalCenter: parent.verticalCenter
        height: root.preferredHeight
        width: Math.max(label.implicitWidth + 16, 68)
        radius: height / 2
        color: root.bgColor

        Text {
            id: label
            anchors.centerIn: parent
            text: root.normalizedStatus
            color: root.fgColor
            font.pixelSize: 10
            font.weight: Font.Bold
            font.letterSpacing: 0.5
            horizontalAlignment: Text.AlignHCenter
            verticalAlignment: Text.AlignVCenter
        }
    }
}
