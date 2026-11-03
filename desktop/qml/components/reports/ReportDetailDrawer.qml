import QtQuick
import QtQuick.Controls
import "../"

Rectangle {
    id: root

    readonly property var report: reportController.selectedReportData
    readonly property bool hasReport: reportController.selectedReportId !== "" && root.report && root.report.reportId !== undefined

    property int currentTab: 0 // 0: Overview & Evidence, 1: Work Order, 2: Timeline

    implicitWidth: 380
    implicitHeight: 700
    color: "#FFFFFF"
    border.color: "#E2E8F0"
    border.width: 1
    radius: 12
    visible: hasReport

    // 1. TOP HEADER & TABS SECTION (Pinned to Top)
    Item {
        id: topSection
        anchors.top: parent.top
        anchors.left: parent.left
        anchors.right: parent.right
        anchors.margins: 16
        height: topCol.implicitHeight

        Column {
            id: topCol
            width: parent.width
            spacing: 10

            // 1. Drawer Header (Report ID, Status Badge, Severity, Close Button)
            Item {
                width: parent.width
                height: 30

                Row {
                    anchors.left: parent.left
                    anchors.right: closeBtn.left
                    anchors.rightMargin: 8
                    spacing: 8
                    anchors.verticalCenter: parent.verticalCenter

                    Text {
                        text: root.hasReport ? root.report.reportId : ""
                        font.pixelSize: 15
                        font.weight: Font.Bold
                        color: "#4338CA"
                    }

                    StatusBadge {
                        statusText: root.hasReport ? root.report.status : "PENDING"
                    }

                    Rectangle {
                        height: 20
                        implicitWidth: sevTxt.implicitWidth + 10
                        width: implicitWidth
                        radius: 4
                        color: root.hasReport && root.report.severity === "CRITICAL" ? "#FEE2E2" : "#FEF3C7"

                        Text {
                            id: sevTxt
                            anchors.centerIn: parent
                            text: root.hasReport ? root.report.severity : ""
                            font.pixelSize: 9
                            font.weight: Font.Bold
                            color: root.hasReport && root.report.severity === "CRITICAL" ? "#B91C1C" : "#B45309"
                        }
                    }
                }

                Rectangle {
                    id: closeBtn
                    anchors.right: parent.right
                    anchors.verticalCenter: parent.verticalCenter
                    width: 26
                    height: 26
                    radius: 13
                    color: "#F1F5F9"

                    Text {
                        anchors.centerIn: parent
                        text: "✕"
                        font.pixelSize: 11
                        color: "#64748B"
                    }

                    MouseArea {
                        anchors.fill: parent
                        cursorShape: Qt.PointingHandCursor
                        onClicked: reportController.clearSelection()
                    }
                }
            }

            // 2. Issue Title
            Text {
                width: parent.width
                text: root.hasReport ? root.report.issueTitle : ""
                font.pixelSize: 13
                font.weight: Font.Bold
                color: "#0F172A"
                wrapMode: Text.WordWrap
                maximumLineCount: 2
                elide: Text.ElideRight
            }

            // 3. Tab Buttons
            Row {
                width: parent.width
                spacing: 6

                Repeater {
                    model: ["Overview & Photos", "Work Order & Assign", "Audit Trail"]

                    Rectangle {
                        id: tabItem
                        width: (topSection.width - 12) / 3
                        height: 28
                        radius: 6
                        readonly property bool isTabActive: root.currentTab === index
                        color: isTabActive ? "#4338CA" : "#F1F5F9"

                        Text {
                            anchors.centerIn: parent
                            text: modelData
                            font.pixelSize: 10
                            font.weight: tabItem.isTabActive ? Font.Bold : Font.Medium
                            color: tabItem.isTabActive ? "#FFFFFF" : "#475569"
                        }

                        MouseArea {
                            anchors.fill: parent
                            cursorShape: Qt.PointingHandCursor
                            onClicked: root.currentTab = index
                        }
                    }
                }
            }

            // Divider
            Rectangle {
                width: parent.width
                height: 1
                color: "#F1F5F9"
            }
        }
    }

    // 2. BOTTOM ACTION SECTION (Pinned to Bottom)
    Item {
        id: bottomSection
        anchors.bottom: parent.bottom
        anchors.left: parent.left
        anchors.right: parent.right
        anchors.margins: 16
        height: 48

        // Top border divider
        Rectangle {
            anchors.top: parent.top
            anchors.left: parent.left
            anchors.right: parent.right
            height: 1
            color: "#F1F5F9"
        }

        // Action controls
        Item {
            anchors.bottom: parent.bottom
            anchors.left: parent.left
            anchors.right: parent.right
            height: 36

            // Status Selector Dropdown
            Rectangle {
                id: statusSelectorBtn
                anchors.left: parent.left
                anchors.verticalCenter: parent.verticalCenter
                width: 140
                height: 36
                radius: 6
                color: "#F8FAFC"
                border.color: "#CBD5E1"
                border.width: 1

                Row {
                    anchors.centerIn: parent
                    spacing: 4

                    Text {
                        text: "Status: " + (root.hasReport ? root.report.status : "PENDING")
                        font.pixelSize: 10
                        font.weight: Font.Bold
                        color: "#334155"
                    }
                    Text { text: "▾"; font.pixelSize: 9; color: "#64748B" }
                }

                MouseArea {
                    anchors.fill: parent
                    cursorShape: Qt.PointingHandCursor
                    onClicked: drawerStatusMenu.open()
                }

                Menu {
                    id: drawerStatusMenu
                    y: parent.height + 2
                    width: 140

                    MenuItem { text: "🔴 Critical"; onTriggered: reportController.updateReportStatus(root.report.reportId, "CRITICAL") }
                    MenuItem { text: "🟡 In Progress"; onTriggered: reportController.updateReportStatus(root.report.reportId, "IN PROGRESS") }
                    MenuItem { text: "🟢 Resolved"; onTriggered: resolveProofPopup.open() }
                    MenuItem { text: "✕ Reject / Duplicate"; onTriggered: reportController.updateReportStatus(root.report.reportId, "REJECTED") }
                }
            }

            // Quick Resolve Button
            Rectangle {
                anchors.left: statusSelectorBtn.right
                anchors.leftMargin: 8
                anchors.right: parent.right
                anchors.verticalCenter: parent.verticalCenter
                height: 36
                radius: 6
                color: "#16A34A"

                Row {
                    anchors.centerIn: parent
                    spacing: 4
                    Text { text: "✓"; font.pixelSize: 13; font.weight: Font.Bold; color: "#FFFFFF" }
                    Text {
                        text: "Mark as Resolved"
                        font.pixelSize: 11
                        font.weight: Font.Bold
                        color: "#FFFFFF"
                    }
                }

                MouseArea {
                    anchors.fill: parent
                    cursorShape: Qt.PointingHandCursor
                    onClicked: resolveProofPopup.open()
                }
            }
        }
    }

    // 3. MIDDLE SCROLLABLE CONTENT (Cleanly Anchored between Top and Bottom)
    Flickable {
        id: scrollContent
        anchors.top: topSection.bottom
        anchors.topMargin: 10
        anchors.bottom: bottomSection.top
        anchors.bottomMargin: 10
        anchors.left: parent.left
        anchors.leftMargin: 16
        anchors.right: parent.right
        anchors.rightMargin: 16
        contentHeight: contentCol.implicitHeight
        clip: true

        Column {
            id: contentCol
            width: parent.width
            spacing: 12

            // TAB 0: Overview & Evidence Photos
            Column {
                width: parent.width
                spacing: 10
                visible: root.currentTab === 0

                DualPhotoInspector {
                    width: parent.width
                    height: 160
                    citizenPhoto: root.hasReport ? (root.report.citizenPhotoUrl || "") : ""
                    resolutionPhoto: root.hasReport ? (root.report.resolutionPhotoUrl || "") : ""
                    reportId: root.hasReport ? root.report.reportId : ""
                }

                // Location & Jurisdiction Card
                Rectangle {
                    width: parent.width
                    height: 72
                    radius: 8
                    color: "#F8FAFC"
                    border.color: "#E2E8F0"
                    border.width: 1

                    Column {
                        anchors.fill: parent
                        anchors.margins: 8
                        spacing: 4

                        // Location row (anchored inside Item to avoid any polish loops)
                        Item {
                            width: parent.width
                            height: 18

                            Text {
                                id: locPin
                                text: "📍"
                                font.pixelSize: 11
                                anchors.left: parent.left
                                anchors.verticalCenter: parent.verticalCenter
                            }

                            Text {
                                anchors.left: locPin.right
                                anchors.leftMargin: 6
                                anchors.right: parent.right
                                anchors.verticalCenter: parent.verticalCenter
                                text: root.hasReport ? root.report.location : ""
                                font.pixelSize: 11
                                font.weight: Font.DemiBold
                                color: "#1E293B"
                                elide: Text.ElideRight
                            }
                        }

                        // Ward row
                        Item {
                            width: parent.width
                            height: 16

                            Text {
                                id: wardIcon
                                text: "🏛️"
                                font.pixelSize: 11
                                anchors.left: parent.left
                                anchors.verticalCenter: parent.verticalCenter
                            }

                            Text {
                                anchors.left: wardIcon.right
                                anchors.leftMargin: 6
                                anchors.right: parent.right
                                anchors.verticalCenter: parent.verticalCenter
                                text: root.hasReport ? ("Ward " + root.report.wardNumber + " (" + root.report.wardName + ") • " + root.report.acName) : ""
                                font.pixelSize: 10
                                color: "#64748B"
                                elide: Text.ElideRight
                            }
                        }
                    }
                }

                // Citizen Details & Call Button Card
                Rectangle {
                    width: parent.width
                    height: 54
                    radius: 8
                    color: "#F8FAFC"
                    border.color: "#E2E8F0"
                    border.width: 1

                    Item {
                        anchors.fill: parent
                        anchors.margins: 8

                        Column {
                            anchors.left: parent.left
                            anchors.right: callBtn.left
                            anchors.rightMargin: 8
                            anchors.verticalCenter: parent.verticalCenter
                            spacing: 2

                            Text {
                                text: root.hasReport ? ("👤 " + root.report.citizenName) : ""
                                font.pixelSize: 11
                                font.weight: Font.DemiBold
                                color: "#1E293B"
                                elide: Text.ElideRight
                            }
                            Text {
                                text: root.hasReport ? ("📞 " + root.report.citizenPhone) : ""
                                font.pixelSize: 10
                                color: "#4338CA"
                            }
                        }

                        Rectangle {
                            id: callBtn
                            width: 80
                            height: 28
                            radius: 6
                            color: "#DCFCE7"
                            anchors.right: parent.right
                            anchors.verticalCenter: parent.verticalCenter

                            Text {
                                anchors.centerIn: parent
                                text: "Call Citizen"
                                font.pixelSize: 10
                                font.weight: Font.Bold
                                color: "#15803D"
                            }
                        }
                    }
                }

                // Description Box
                Column {
                    width: parent.width
                    spacing: 4

                    Text {
                        text: "CITIZEN DESCRIPTION"
                        font.pixelSize: 9
                        font.weight: Font.Bold
                        font.letterSpacing: 0.5
                        color: "#94A3B8"
                    }

                    Rectangle {
                        width: parent.width
                        height: 64
                        radius: 6
                        color: "#F8FAFC"
                        border.color: "#E2E8F0"
                        border.width: 1

                        Text {
                            anchors.fill: parent
                            anchors.margins: 8
                            text: root.hasReport ? root.report.description : ""
                            font.pixelSize: 11
                            color: "#334155"
                            wrapMode: Text.WordWrap
                        }
                    }
                }
            }

            // TAB 1: Work Order & Officer Assignment
            Column {
                width: parent.width
                spacing: 12
                visible: root.currentTab === 1

                Text {
                    text: "OFFICIAL DISPATCH & CONTRACTOR ALLOCATION"
                    font.pixelSize: 10
                    font.weight: Font.Bold
                    font.letterSpacing: 0.5
                    color: "#94A3B8"
                }

                // Current Assignment status
                Rectangle {
                    width: parent.width
                    height: 56
                    radius: 8
                    color: root.hasReport && root.report.assignedOfficer !== "Unassigned" ? "#F0FDF4" : "#FEF2F2"
                    border.color: root.hasReport && root.report.assignedOfficer !== "Unassigned" ? "#86EFAC" : "#FCA5A5"
                    border.width: 1

                    Column {
                        anchors.fill: parent
                        anchors.margins: 8
                        spacing: 2

                        Text {
                            text: "Assigned To: " + (root.hasReport ? root.report.assignedOfficer : "Unassigned")
                            font.pixelSize: 11
                            font.weight: Font.Bold
                            color: root.hasReport && root.report.assignedOfficer !== "Unassigned" ? "#166534" : "#991B1B"
                        }
                        Text {
                            text: "Token: " + (root.hasReport && root.report.workOrderToken !== "" ? root.report.workOrderToken : "Pending Work Order Generation")
                            font.pixelSize: 10
                            color: "#64748B"
                        }
                    }
                }

                // Select Nodal Officer Dropdown
                Column {
                    width: parent.width
                    spacing: 4

                    Text {
                        text: "Select Field Engineer / Nodal Officer:"
                        font.pixelSize: 11
                        font.weight: Font.DemiBold
                        color: "#334155"
                    }

                    Rectangle {
                        width: parent.width
                        height: 36
                        radius: 6
                        color: "#F8FAFC"
                        border.color: "#CBD5E1"
                        border.width: 1

                        Item {
                            anchors.fill: parent
                            anchors.leftMargin: 10
                            anchors.rightMargin: 10

                            Text {
                                anchors.left: parent.left
                                anchors.right: officerArrow.left
                                anchors.rightMargin: 4
                                anchors.verticalCenter: parent.verticalCenter
                                text: selectedOfficerTxt.text
                                font.pixelSize: 11
                                font.weight: Font.DemiBold
                                color: "#0F172A"
                                elide: Text.ElideRight
                            }
                            Text { id: selectedOfficerTxt; text: "Er. R. Suresh (AEE - Roads & Infra)"; visible: false }
                            Text { id: officerArrow; text: "▾"; font.pixelSize: 11; color: "#64748B"; anchors.right: parent.right; anchors.verticalCenter: parent.verticalCenter }
                        }

                        MouseArea {
                            anchors.fill: parent
                            cursorShape: Qt.PointingHandCursor
                            onClicked: officerMenu.open()
                        }

                        Menu {
                            id: officerMenu
                            y: parent.height + 2
                            width: 340

                            MenuItem { text: "Er. R. Suresh (AEE - Roads & Infra)"; onTriggered: selectedOfficerTxt.text = text }
                            MenuItem { text: "Er. P. Venkatesh (AEE - Stormwater Drains)"; onTriggered: selectedOfficerTxt.text = text }
                            MenuItem { text: "Dr. K. Manjunath (Senior Health Inspector - SWM)"; onTriggered: selectedOfficerTxt.text = text }
                            MenuItem { text: "Er. H. Nagaraj (AEE - BESCOM Electrical)"; onTriggered: selectedOfficerTxt.text = text }
                            MenuItem { text: "Er. K. Nataraj (AEE - BWSSB Water & Sewage)"; onTriggered: selectedOfficerTxt.text = text }
                        }
                    }
                }

                // Dispatch Button
                Rectangle {
                    width: parent.width
                    height: 38
                    radius: 8
                    color: "#4338CA"

                    Row {
                        anchors.centerIn: parent
                        spacing: 6
                        Text { text: "📋"; font.pixelSize: 12 }
                        Text {
                            text: "Generate & Dispatch Work Order"
                            font.pixelSize: 11
                            font.weight: Font.Bold
                            color: "#FFFFFF"
                        }
                    }

                    MouseArea {
                        anchors.fill: parent
                        cursorShape: Qt.PointingHandCursor
                        onClicked: {
                            reportController.assignWorkOrder(root.report.reportId, selectedOfficerTxt.text, "Nodal Officer", "Dispatched via Portal");
                        }
                    }
                }
            }

            // TAB 2: Action Audit Timeline
            Column {
                width: parent.width
                spacing: 8
                visible: root.currentTab === 2

                Text {
                    text: "CHRONOLOGICAL AUDIT TRAIL"
                    font.pixelSize: 10
                    font.weight: Font.Bold
                    font.letterSpacing: 0.5
                    color: "#94A3B8"
                }

                AuditTimeline {
                    width: parent.width
                    events: root.hasReport ? (root.report.timelineEvents || []) : []
                }
            }
        }
    }

    // ================= RESOLUTION PROOF MODAL =================
    Popup {
        id: resolveProofPopup
        anchors.centerIn: parent
        width: Math.min(parent.width - 24, 380)
        modal: true
        focus: true
        closePolicy: Popup.CloseOnEscape | Popup.CloseOnPressOutside

        background: Rectangle {
            color: "#FFFFFF"
            radius: 12
            border.color: "#CBD5E1"
            border.width: 1
        }

        contentItem: Column {
            spacing: 12
            padding: 16
            width: parent.width

            Row {
                spacing: 8
                Rectangle {
                    width: 28; height: 28; radius: 14; color: "#DCFCE7"
                    Text { anchors.centerIn: parent; text: "✓"; color: "#16A34A"; font.weight: Font.Bold; font.pixelSize: 14 }
                }
                Column {
                    Text { text: "Official Grievance Resolution"; font.pixelSize: 13; font.weight: Font.Bold; color: "#0F172A" }
                    Text { text: "Upload after-repair evidence for 2-way citizen verification"; font.pixelSize: 10; color: "#64748B" }
                }
            }

            Rectangle { width: parent.width - 32; height: 1; color: "#F1F5F9" }

            Column {
                width: parent.width - 32
                spacing: 4

                Text { text: "Resolution Proof Photo URL"; font.pixelSize: 10; font.weight: Font.DemiBold; color: "#475569" }
                Rectangle {
                    width: parent.width
                    height: 34
                    radius: 6
                    color: "#F8FAFC"
                    border.color: photoInput.activeFocus ? "#3B82F6" : "#CBD5E1"
                    border.width: 1

                    TextInput {
                        id: photoInput
                        anchors.fill: parent
                        anchors.margins: 6
                        font.pixelSize: 10
                        color: "#0F172A"
                        clip: true
                        selectByMouse: true
                        text: "https://images.unsplash.com/photo-1515162816999-a0c47dc192f7?w=800"
                    }
                }

                Row {
                    spacing: 6
                    Text { text: "Presets:"; font.pixelSize: 9; color: "#94A3B8"; anchors.verticalCenter: parent.verticalCenter }
                    Rectangle {
                        height: 20; width: 68; radius: 4; color: "#EFF6FF"
                        Text { anchors.centerIn: parent; text: "Road Paved"; font.pixelSize: 9; color: "#2563EB" }
                        MouseArea {
                            anchors.fill: parent; cursorShape: Qt.PointingHandCursor
                            onClicked: photoInput.text = "https://images.unsplash.com/photo-1515162816999-a0c47dc192f7?w=800"
                        }
                    }
                    Rectangle {
                        height: 20; width: 72; radius: 4; color: "#F0FDF4"
                        Text { anchors.centerIn: parent; text: "Site Cleared"; font.pixelSize: 9; color: "#16A34A" }
                        MouseArea {
                            anchors.fill: parent; cursorShape: Qt.PointingHandCursor
                            onClicked: photoInput.text = "https://images.unsplash.com/photo-1530587191325-3db32d826c18?w=800"
                        }
                    }
                }
            }

            Column {
                width: parent.width - 32
                spacing: 4

                Text { text: "Official Inspection Notes"; font.pixelSize: 10; font.weight: Font.DemiBold; color: "#475569" }
                Rectangle {
                    width: parent.width
                    height: 44
                    radius: 6
                    color: "#F8FAFC"
                    border.color: notesInput.activeFocus ? "#3B82F6" : "#CBD5E1"
                    border.width: 1

                    TextInput {
                        id: notesInput
                        anchors.fill: parent
                        anchors.margins: 6
                        font.pixelSize: 10
                        color: "#0F172A"
                        clip: true
                        selectByMouse: true
                        text: "On-site repair completed by municipal crew. Inspected and approved."
                    }
                }
            }

            Row {
                width: parent.width - 32
                spacing: 8

                Rectangle {
                    width: (parent.width - 8) / 2
                    height: 32
                    radius: 6
                    color: "#F1F5F9"
                    Text { anchors.centerIn: parent; text: "Cancel"; font.pixelSize: 11; font.weight: Font.DemiBold; color: "#475569" }
                    MouseArea {
                        anchors.fill: parent; cursorShape: Qt.PointingHandCursor
                        onClicked: resolveProofPopup.close()
                    }
                }

                Rectangle {
                    width: (parent.width - 8) / 2
                    height: 32
                    radius: 6
                    color: "#16A34A"
                    Text { anchors.centerIn: parent; text: "Resolve & Sync"; font.pixelSize: 11; font.weight: Font.Bold; color: "#FFFFFF" }
                    MouseArea {
                        anchors.fill: parent; cursorShape: Qt.PointingHandCursor
                        onClicked: {
                            reportController.resolveGrievanceWithProof(root.report.reportId, notesInput.text, photoInput.text);
                            resolveProofPopup.close();
                        }
                    }
                }
            }
        }
    }
}
