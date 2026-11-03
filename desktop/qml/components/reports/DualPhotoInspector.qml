import QtQuick
import QtQuick.Controls

Rectangle {
    id: root

    property string citizenPhoto: ""
    property string resolutionPhoto: ""
    property string reportId: ""

    implicitWidth: 320
    implicitHeight: 160
    radius: 8
    color: "#F8FAFC"
    border.color: "#E2E8F0"
    border.width: 1

    Row {
        anchors.fill: parent
        anchors.margins: 8
        spacing: 8

        // ================= BEFORE PHOTO (CITIZEN EVIDENCE) =================
        Rectangle {
            id: citizenBox
            width: (parent.width - 8) / 2
            height: parent.height
            radius: 6
            color: "#0F172A"
            clip: true

            // 1. Placeholder Gradient & Icon (shows when loading or no photo)
            Rectangle {
                anchors.fill: parent
                gradient: Gradient {
                    GradientStop { position: 0.0; color: "#334155" }
                    GradientStop { position: 1.0; color: "#1E293B" }
                }

                Column {
                    anchors.centerIn: parent
                    spacing: 4
                    visible: citizenImage.status !== Image.Ready
                    Text { text: "📷"; font.pixelSize: 22; anchors.horizontalCenter: parent.horizontalCenter }
                    Text {
                        text: citizenImage.status === Image.Loading ? "LOADING PHOTO..." : "CITIZEN PHOTO"
                        font.pixelSize: 9
                        font.weight: Font.Bold
                        color: "#CBD5E1"
                        anchors.horizontalCenter: parent.horizontalCenter
                    }
                    Text {
                        text: citizenImage.status === Image.Loading ? "Fetching CDN..." : "GPS Verified"
                        font.pixelSize: 8
                        color: "#38BDF8"
                        anchors.horizontalCenter: parent.horizontalCenter
                    }
                }
            }

            // 2. Actual Evidence Image (rendered when source is present)
            Image {
                id: citizenImage
                anchors.fill: parent
                source: root.citizenPhoto
                fillMode: Image.PreserveAspectCrop
                asynchronous: true
                cache: true
                visible: status === Image.Ready
            }

            // 3. Loading Indicator
            BusyIndicator {
                anchors.centerIn: parent
                width: 32
                height: 32
                running: citizenImage.status === Image.Loading
                visible: citizenImage.status === Image.Loading
            }

            // 4. "BEFORE" Top-Left Badge
            Rectangle {
                anchors.left: parent.left
                anchors.top: parent.top
                anchors.margins: 6
                height: 18
                width: 54
                radius: 4
                color: "#D90F172A"

                Text {
                    anchors.centerIn: parent
                    text: "BEFORE"
                    font.pixelSize: 8
                    font.weight: Font.Bold
                    color: "#F87171"
                }
            }

            // 5. Expand Hint Bottom-Right Overlay
            Rectangle {
                anchors.right: parent.right
                anchors.bottom: parent.bottom
                anchors.margins: 4
                height: 16
                width: 44
                radius: 3
                color: "#CC0F172A"
                visible: citizenImage.status === Image.Ready

                Row {
                    anchors.centerIn: parent
                    spacing: 2
                    Text { text: "🔍"; font.pixelSize: 8 }
                    Text { text: "View"; font.pixelSize: 8; color: "#FFFFFF"; font.weight: Font.DemiBold }
                }
            }

            // Click to expand
            MouseArea {
                anchors.fill: parent
                cursorShape: citizenImage.status === Image.Ready ? Qt.PointingHandCursor : Qt.ArrowCursor
                onClicked: {
                    if (citizenImage.status === Image.Ready && root.citizenPhoto !== "") {
                        imagePreviewPopup.previewSource = root.citizenPhoto
                        imagePreviewPopup.previewTitle = "Citizen Evidence Photo — " + root.reportId
                        imagePreviewPopup.open()
                    }
                }
            }
        }

        // ================= AFTER PHOTO (OFFICER RESOLUTION PROOF) =================
        Rectangle {
            id: resolutionBox
            width: (parent.width - 8) / 2
            height: parent.height
            radius: 6
            color: "#0F172A"
            clip: true

            // 1. Placeholder Gradient & Icon
            Rectangle {
                anchors.fill: parent
                gradient: Gradient {
                    GradientStop { position: 0.0; color: root.resolutionPhoto !== "" ? "#064E3B" : "#334155" }
                    GradientStop { position: 1.0; color: root.resolutionPhoto !== "" ? "#022C22" : "#1E293B" }
                }

                Column {
                    anchors.centerIn: parent
                    spacing: 4
                    visible: resolutionImage.status !== Image.Ready
                    Text { text: root.resolutionPhoto !== "" ? "🔍" : "⏳"; font.pixelSize: 22; anchors.horizontalCenter: parent.horizontalCenter }
                    Text {
                        text: root.resolutionPhoto !== "" ? "RESOLVED PROOF" : "INSPECTION PENDING"
                        font.pixelSize: 9
                        font.weight: Font.Bold
                        color: root.resolutionPhoto !== "" ? "#A7F3D0" : "#CBD5E1"
                        anchors.horizontalCenter: parent.horizontalCenter
                    }
                    Text {
                        text: root.resolutionPhoto !== "" ? "Photo Uploaded" : "Field team working"
                        font.pixelSize: 8
                        color: "#94A3B8"
                        anchors.horizontalCenter: parent.horizontalCenter
                    }
                }
            }

            // 2. Actual Resolution Image
            Image {
                id: resolutionImage
                anchors.fill: parent
                source: root.resolutionPhoto
                fillMode: Image.PreserveAspectCrop
                asynchronous: true
                cache: true
                visible: status === Image.Ready
            }

            // 3. Loading Indicator
            BusyIndicator {
                anchors.centerIn: parent
                width: 32
                height: 32
                running: resolutionImage.status === Image.Loading
                visible: resolutionImage.status === Image.Loading
            }

            // 4. "AFTER" Top-Left Badge
            Rectangle {
                anchors.left: parent.left
                anchors.top: parent.top
                anchors.margins: 6
                height: 18
                width: 48
                radius: 4
                color: "#D90F172A"

                Text {
                    anchors.centerIn: parent
                    text: "AFTER"
                    font.pixelSize: 8
                    font.weight: Font.Bold
                    color: "#34D399"
                }
            }

            // 5. Expand Hint
            Rectangle {
                anchors.right: parent.right
                anchors.bottom: parent.bottom
                anchors.margins: 4
                height: 16
                width: 44
                radius: 3
                color: "#CC0F172A"
                visible: resolutionImage.status === Image.Ready

                Row {
                    anchors.centerIn: parent
                    spacing: 2
                    Text { text: "🔍"; font.pixelSize: 8 }
                    Text { text: "View"; font.pixelSize: 8; color: "#FFFFFF"; font.weight: Font.DemiBold }
                }
            }

            // Click to expand
            MouseArea {
                anchors.fill: parent
                cursorShape: resolutionImage.status === Image.Ready ? Qt.PointingHandCursor : Qt.ArrowCursor
                onClicked: {
                    if (resolutionImage.status === Image.Ready && root.resolutionPhoto !== "") {
                        imagePreviewPopup.previewSource = root.resolutionPhoto
                        imagePreviewPopup.previewTitle = "Municipal Resolution Proof — " + root.reportId
                        imagePreviewPopup.open()
                    }
                }
            }
        }
    }

    // ================= HIGH-RESOLUTION ZOOM POPUP MODAL =================
    Popup {
        id: imagePreviewPopup
        parent: Overlay.overlay
        anchors.centerIn: parent
        width: Math.min(Overlay.overlay ? Overlay.overlay.width - 60 : 750, 720)
        height: Math.min(Overlay.overlay ? Overlay.overlay.height - 60 : 540, 500)
        modal: true
        focus: true
        closePolicy: Popup.CloseOnEscape | Popup.CloseOnPressOutside

        property string previewSource: ""
        property string previewTitle: ""

        background: Rectangle {
            color: "#0F172A"
            radius: 12
            border.color: "#334155"
            border.width: 1
        }

        contentItem: Column {
            spacing: 10
            anchors.fill: parent
            anchors.margins: 12

            // Modal Header
            Row {
                width: parent.width
                Text {
                    width: parent.width - 32
                    text: imagePreviewPopup.previewTitle
                    font.pixelSize: 13
                    font.weight: Font.Bold
                    color: "#F8FAFC"
                    elide: Text.ElideRight
                    anchors.verticalCenter: parent.verticalCenter
                }

                Rectangle {
                    width: 24
                    height: 24
                    radius: 12
                    color: "#1E293B"
                    anchors.verticalCenter: parent.verticalCenter

                    Text {
                        anchors.centerIn: parent
                        text: "✕"
                        color: "#94A3B8"
                        font.pixelSize: 12
                    }

                    MouseArea {
                        anchors.fill: parent
                        cursorShape: Qt.PointingHandCursor
                        onClicked: imagePreviewPopup.close()
                    }
                }
            }

            // Full Resolution Image Container
            Rectangle {
                width: parent.width
                height: parent.height - 40
                color: "#020617"
                radius: 8
                clip: true

                Image {
                    anchors.fill: parent
                    source: imagePreviewPopup.previewSource
                    fillMode: Image.PreserveAspectFit
                    asynchronous: true
                }
            }
        }
    }
}
