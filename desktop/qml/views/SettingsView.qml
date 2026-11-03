import QtQuick
import QtQuick.Controls
import "../components/settings"

Item {
    id: root

    implicitWidth: 1000
    implicitHeight: 800

    Row {
        anchors.fill: parent
        anchors.margins: 20
        spacing: 16

        // Left Navigation Tabs
        SettingsSidebarNav {
            id: sidebarNav
            width: 260
            height: parent.height
        }

        // Right Settings Content Area
        Item {
            width: parent.width - 276
            height: parent.height

            ProfileSettingsCard {
                anchors.fill: parent
                visible: sidebarNav.activeTab === 0
                onSaved: function(msg) { root.showToast(msg); }
            }

            SlaConfigCard {
                anchors.fill: parent
                visible: sidebarNav.activeTab === 1
                onSaved: function(msg) { root.showToast(msg); }
            }

            AiRoutingCard {
                anchors.fill: parent
                visible: sidebarNav.activeTab === 2
                onSaved: function(msg) { root.showToast(msg); }
            }

            PrivacySecurityCard {
                anchors.fill: parent
                visible: sidebarNav.activeTab === 3
                onSaved: function(msg) { root.showToast(msg); }
            }

            SystemSyncCard {
                anchors.fill: parent
                visible: sidebarNav.activeTab === 4
                onSaved: function(msg) { root.showToast(msg); }
            }
        }
    }

    function showToast(msg) {
        toastNotification.message = msg;
        toastNotification.visible = true;
        toastTimer.restart();
    }

    // Toast Notification Banner
    Rectangle {
        id: toastNotification
        property string message: ""
        visible: false
        anchors.bottom: parent.bottom
        anchors.horizontalCenter: parent.horizontalCenter
        anchors.bottomMargin: 24
        width: toastText.implicitWidth + 36
        height: 40
        radius: 20
        color: "#0F172A"

        Row {
            anchors.centerIn: parent
            spacing: 8

            Text { text: "✅"; font.pixelSize: 13 }
            Text {
                id: toastText
                text: toastNotification.message
                font.pixelSize: 11
                font.weight: Font.DemiBold
                color: "#F8FAFC"
            }
        }

        Timer {
            id: toastTimer
            interval: 4000
            onTriggered: toastNotification.visible = false
        }
    }
}
