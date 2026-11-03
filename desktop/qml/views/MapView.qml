import QtQuick
import QtQuick.Controls
import QtQuick.Layouts
import "../components/map"

Item {
    id: root

    implicitWidth: 1000
    implicitHeight: 800

    Column {
        anchors.fill: parent

        // 1. Top Filter and Constituency Control Bar
        MapControlBar {
            id: controlBar
            width: parent.width
            height: 60
        }

        // 2. Map Canvas + Floating Cards & Inspector Area
        Item {
            width: parent.width
            height: parent.height - 60

            // Main Interactive GIS Canvas
            GisCanvas {
                id: gisCanvas
                anchors.fill: parent
            }

            // Floating Zoom & Reset Buttons
            MapFloatingControls {
                anchors.right: inspectorPanel.visible ? inspectorPanel.left : parent.right
                anchors.bottom: parent.bottom
                onZoomInClicked: gisCanvas.zoomIn()
                onZoomOutClicked: gisCanvas.zoomOut()
                onResetClicked: gisCanvas.resetView()
            }

            // Floating Incident Detail Card (When pin is clicked)
            IncidentDetailCard {
                anchors.left: parent.left
                anchors.bottom: parent.bottom
                anchors.margins: 20
                visible: mapController.selectedReportId !== ""
            }

            // Right Ward Inspector Panel
            WardInspectorPanel {
                id: inspectorPanel
                anchors.right: parent.right
                anchors.top: parent.top
                anchors.bottom: parent.bottom
                anchors.margins: 16
                width: 320
            }
        }
    }
}
