import QtQuick
import QtQuick.Controls

Rectangle {
    id: root

    property int activeTab: 0

    implicitWidth: 260
    implicitHeight: 600
    color: "#FFFFFF"
    border.color: "#E2E8F0"
    border.width: 1
    radius: 10

    readonly property var navItems: [
        {icon: "🏛️", title: "Admin Profile", subtitle: "Nodal credentials & role"},
        {icon: "⏱️", title: "SLA & Escalation", subtitle: "Statutory response limits"},
        {icon: "🤖", title: "AI Auto-Triage", subtitle: "Geofencing & duplicate rules"},
        {icon: "🔒", title: "Privacy & Security", subtitle: "Citizen PII masking & RBAC"},
        {icon: "🌐", title: "KGIS Cloud & Backup", subtitle: "Sync status & encrypted backup"}
    ]

    Column {
        anchors.fill: parent
        anchors.margins: 12
        spacing: 6

        Text {
            text: "SETTINGS CATEGORIES"
            font.pixelSize: 10
            font.weight: Font.Bold
            font.letterSpacing: 0.5
            color: "#94A3B8"
            anchors.leftMargin: 4
        }

        Item { width: 1; height: 4 }

        Repeater {
            model: root.navItems

            Rectangle {
                width: parent.width
                height: 52
                radius: 8
                readonly property bool isSelected: root.activeTab === index

                color: isSelected ? "#EEF2FF" : "transparent"
                border.color: isSelected ? "#C7D2FE" : "transparent"
                border.width: 1

                Row {
                    anchors.fill: parent
                    anchors.leftMargin: 12
                    anchors.rightMargin: 12
                    spacing: 10

                    Text {
                        text: modelData.icon
                        font.pixelSize: 16
                        anchors.verticalCenter: parent.verticalCenter
                    }

                    Column {
                        width: parent.width - 34
                        anchors.verticalCenter: parent.verticalCenter
                        spacing: 2

                        Text {
                            text: modelData.title
                            font.pixelSize: 11
                            font.weight: parent.parent.parent.isSelected ? Font.Bold : Font.DemiBold
                            color: parent.parent.parent.isSelected ? "#4338CA" : "#1E293B"
                        }
                        Text {
                            text: modelData.subtitle
                            font.pixelSize: 9
                            color: "#64748B"
                        }
                    }
                }

                MouseArea {
                    anchors.fill: parent
                    cursorShape: Qt.PointingHandCursor
                    onClicked: root.activeTab = index
                }
            }
        }
    }
}
