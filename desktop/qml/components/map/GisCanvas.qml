import QtQuick
import QtQuick.Controls

Item {
    id: root

    property real centerLat: mapController.centerLat
    property real centerLng: mapController.centerLng
    property int tileZoom: 12
    property real zoomScale: 1.0
    property real panOffsetX: 0
    property real panOffsetY: 0

    property int hoveredWardNumber: -1
    property string hoveredReportId: ""
    property var visibleTilesList: []

    signal wardClicked(int wardNumber)
    signal pinClicked(string reportId)

    clip: true

    // ================= PROJECTION MATHEMATICS =================
    function latLngToWorld(lat, lng, z) {
        var n = Math.pow(2, z);
        var x = (lng + 180.0) / 360.0 * n * 256.0;
        var latRad = lat * Math.PI / 180.0;
        var y = (1.0 - Math.log(Math.tan(latRad) + 1.0 / Math.cos(latRad)) / Math.PI) / 2.0 * n * 256.0;
        return { x: x, y: y };
    }

    function latLngToScreen(lat, lng) {
        var p = latLngToWorld(lat, lng, root.tileZoom);
        var centerP = latLngToWorld(root.centerLat, root.centerLng, root.tileZoom);
        var cx = width / 2 + root.panOffsetX;
        var cy = height / 2 + root.panOffsetY;
        return {
            x: cx + (p.x - centerP.x) * root.zoomScale,
            y: cy + (p.y - centerP.y) * root.zoomScale
        };
    }

    function screenToLatLng(sx, sy) {
        var cx = width / 2 + root.panOffsetX;
        var cy = height / 2 + root.panOffsetY;
        var centerP = latLngToWorld(root.centerLat, root.centerLng, root.tileZoom);
        var wx = centerP.x + (sx - cx) / root.zoomScale;
        var wy = centerP.y + (sy - cy) / root.zoomScale;

        var n = Math.pow(2, root.tileZoom);
        var lng = (wx / (n * 256.0)) * 360.0 - 180.0;
        var yNorm = 1.0 - (2.0 * wy) / (n * 256.0);
        var lat = Math.atan(Math.sinh(Math.PI * yNorm)) * 180.0 / Math.PI;
        return { lat: lat, lng: lng };
    }

    // ================= VISIBLE TILE CALCULATOR =================
    function updateVisibleTiles() {
        if (width <= 0 || height <= 0) return;
        var z = root.tileZoom;
        var n = Math.pow(2, z);
        var centerW = latLngToWorld(root.centerLat, root.centerLng, z);
        var cx = width / 2 + root.panOffsetX;
        var cy = height / 2 + root.panOffsetY;
        var s = root.zoomScale;

        var minWx = centerW.x - cx / s;
        var maxWx = centerW.x + (width - cx) / s;
        var minWy = centerW.y - cy / s;
        var maxWy = centerW.y + (height - cy) / s;

        var minTx = Math.max(0, Math.floor(minWx / 256.0));
        var maxTx = Math.min(n - 1, Math.floor(maxWx / 256.0));
        var minTy = Math.max(0, Math.floor(minWy / 256.0));
        var maxTy = Math.min(n - 1, Math.floor(maxWy / 256.0));

        var tiles = [];
        var subdomains = ["a", "b", "c"];
        for (var tx = minTx; tx <= maxTx; ++tx) {
            for (var ty = minTy; ty <= maxTy; ++ty) {
                var screenX = cx + (tx * 256.0 - centerW.x) * s;
                var screenY = cy + (ty * 256.0 - centerW.y) * s;
                var size = 256.0 * s;
                var sub = subdomains[Math.abs(tx + ty) % 3];
                tiles.push({
                    x: screenX,
                    y: screenY,
                    width: Math.ceil(size + 0.6),
                    height: Math.ceil(size + 0.6),
                    url: "https://" + sub + ".tile.openstreetmap.fr/hot/" + z + "/" + tx + "/" + ty + ".png"
                });
            }
        }
        root.visibleTilesList = tiles;
    }

    function zoomIn() {
        var newScale = root.zoomScale * 1.25;
        if (newScale >= 1.6 && root.tileZoom < 17) {
            root.tileZoom += 1;
            root.zoomScale = newScale / 2.0;
        } else {
            root.zoomScale = Math.min(newScale, 3.5);
        }
        updateVisibleTiles();
        canvas.requestPaint();
    }

    function zoomOut() {
        var newScale = root.zoomScale / 1.25;
        if (newScale <= 0.8 && root.tileZoom > 11) {
            root.tileZoom -= 1;
            root.zoomScale = newScale * 2.0;
        } else {
            root.zoomScale = Math.max(newScale, 0.4);
        }
        updateVisibleTiles();
        canvas.requestPaint();
    }

    function resetView() {
        root.tileZoom = 12;
        root.zoomScale = 1.0;
        root.panOffsetX = 0;
        root.panOffsetY = 0;
        updateVisibleTiles();
        canvas.requestPaint();
    }

    onWidthChanged: updateVisibleTiles()
    onHeightChanged: updateVisibleTiles()
    onCenterLatChanged: { updateVisibleTiles(); canvas.requestPaint(); }
    onCenterLngChanged: { updateVisibleTiles(); canvas.requestPaint(); }
    onPanOffsetXChanged: { updateVisibleTiles(); canvas.requestPaint(); }
    onPanOffsetYChanged: { updateVisibleTiles(); canvas.requestPaint(); }
    onZoomScaleChanged: { updateVisibleTiles(); canvas.requestPaint(); }

    Component.onCompleted: {
        updateVisibleTiles();
        canvas.requestPaint();
    }

    // ================= BASE MAP CANVAS & TILES =================
    Rectangle {
        id: bgFallback
        anchors.fill: parent
        color: "#F1F5F9"
    }

    // Real OpenStreetMap (CartoDB Voyager) Map Tile Layer
    Item {
        id: tileLayer
        anchors.fill: parent
        clip: true

        Repeater {
            model: root.visibleTilesList

            Image {
                x: modelData.x
                y: modelData.y
                width: modelData.width
                height: modelData.height
                source: modelData.url
                asynchronous: true
                cache: true
                fillMode: Image.Stretch
                smooth: true
            }
        }
    }

    // ================= VECTOR GIS & OVERLAY CANVAS =================
    Canvas {
        id: canvas
        anchors.fill: parent
        antialiasing: true

        onPaint: {
            var ctx = getContext("2d");
            ctx.reset();

            // 1. Draw BBMP Ward Polygons & Boundaries
            if (mapController.showBorders) {
                var wardCount = mapController.wardModel.rowCount();
                for (var w = 0; w < wardCount; ++w) {
                    var idx = mapController.wardModel.index(w, 0);
                    var wNum = mapController.wardModel.data(idx, 257); // WardNumberRole
                    var wName = mapController.wardModel.data(idx, 258); // WardNameRole
                    var pts = mapController.wardModel.data(idx, 272);  // CoordinatesRole
                    var wColor = mapController.wardModel.data(idx, 271); // ColorRole

                    if (!pts || pts.length < 3) continue;

                    var isSelected = (mapController.selectedWardNumber === wNum);
                    var isHovered = (root.hoveredWardNumber === wNum);

                    ctx.beginPath();
                    var p0 = root.latLngToScreen(pts[0].lat, pts[0].lng);
                    ctx.moveTo(p0.x, p0.y);

                    var avgX = p0.x;
                    var avgY = p0.y;

                    for (var p = 1; p < pts.length; ++p) {
                        var pt = root.latLngToScreen(pts[p].lat, pts[p].lng);
                        ctx.lineTo(pt.x, pt.y);
                        avgX += pt.x;
                        avgY += pt.y;
                    }
                    ctx.closePath();

                    avgX /= pts.length;
                    avgY /= pts.length;

                    // Semi-transparent ward fills so OpenStreetMap streets remain visible
                    if (isSelected) {
                        ctx.fillStyle = "rgba(67, 56, 202, 0.28)";
                    } else if (isHovered) {
                        ctx.fillStyle = "rgba(79, 70, 229, 0.20)";
                    } else {
                        ctx.fillStyle = "rgba(99, 102, 241, 0.08)";
                    }
                    ctx.fill();

                    // Ward Boundary Stroke
                    if (isSelected) {
                        ctx.strokeStyle = "#4338CA";
                        ctx.lineWidth = 3.0;
                    } else if (isHovered) {
                        ctx.strokeStyle = "#6366F1";
                        ctx.lineWidth = 2.4;
                    } else {
                        ctx.strokeStyle = "rgba(79, 70, 229, 0.55)";
                        ctx.lineWidth = 1.4;
                    }
                    ctx.stroke();

                    // Center Ward Pill Label
                    // When in master city view ("All Bengaluru"): show ONLY on hover or selection to keep the map pristine.
                    // When a specific constituency is selected (e.g. RR Nagar, Mahadevapura, etc.): show ALL ward labels in that constituency!
                    var isMasterCityView = (mapController.selectedConstituency.indexOf("All") !== -1);
                    var shouldShowLabel = isMasterCityView ? (isSelected || isHovered) : true;

                    if (shouldShowLabel) {
                        var labelText = isSelected
                            ? ("✓ Ward " + wNum + " • " + wName)
                            : (isHovered ? ("🏛️ Ward " + wNum + " • " + wName) : ("W" + wNum + " " + wName));
                        ctx.font = isSelected ? "bold 11px Segoe UI, sans-serif" : (isHovered ? "600 10px Segoe UI, sans-serif" : "600 9.5px Segoe UI, sans-serif");
                        var textW = ctx.measureText(labelText).width;

                        // Pill background
                        var rx = avgX - textW / 2 - 7;
                        var ry = avgY - 10;
                        var rw = textW + 14;
                        var rh = 20;
                        var rrad = 5;
                        ctx.fillStyle = isSelected
                            ? "rgba(49, 46, 129, 0.95)"
                            : (isHovered ? "rgba(255, 255, 255, 0.95)" : "rgba(255, 255, 255, 0.90)");
                        ctx.beginPath();
                        ctx.moveTo(rx + rrad, ry);
                        ctx.lineTo(rx + rw - rrad, ry);
                        ctx.arcTo(rx + rw, ry, rx + rw, ry + rrad, rrad);
                        ctx.lineTo(rx + rw, ry + rh - rrad);
                        ctx.arcTo(rx + rw, ry + rh, rx + rw - rrad, ry + rh, rrad);
                        ctx.lineTo(rx + rrad, ry + rh);
                        ctx.arcTo(rx, ry + rh, rx, ry + rh - rrad, rrad);
                        ctx.lineTo(rx, ry + rrad);
                        ctx.arcTo(rx, ry, rx + rrad, ry, rrad);
                        ctx.closePath();
                        ctx.fill();

                        ctx.strokeStyle = isSelected ? "#312E81" : (isHovered ? "#4338CA" : "#CBD5E1");
                        ctx.lineWidth = isSelected ? 1.5 : (isHovered ? 1.2 : 0.8);
                        ctx.stroke();

                        // Pill text
                        ctx.fillStyle = isSelected ? "#FFFFFF" : (isHovered ? "#4338CA" : "#1E293B");
                        ctx.textAlign = "center";
                        ctx.textBaseline = "middle";
                        ctx.fillText(labelText, avgX, avgY);
                    }
                }
            }

            // 2. Dynamic Grievance Heatmap Layer
            if (mapController.showHeatmap) {
                var incCount = mapController.incidentModel.rowCount();
                for (var h = 0; h < incCount; ++h) {
                    var hIdx = mapController.incidentModel.index(h, 0);
                    var hLat = mapController.incidentModel.data(hIdx, 263);
                    var hLng = mapController.incidentModel.data(hIdx, 264);
                    var hStatus = mapController.incidentModel.data(hIdx, 265);

                    var hPos = root.latLngToScreen(hLat, hLng);
                    var hStatusUpper = String(hStatus).toUpperCase();
                    var isCritical = (hStatusUpper === "CRITICAL" || hStatusUpper === "SUBMITTED" || hStatusUpper === "PENDING" || hStatusUpper === "ESCALATED");
                    var rad = (isCritical ? 70 : 50) * Math.min(root.zoomScale, 1.8);

                    var heatGrad = ctx.createRadialGradient(hPos.x, hPos.y, 2, hPos.x, hPos.y, rad);
                    if (isCritical) {
                        heatGrad.addColorStop(0, "rgba(239, 68, 68, 0.70)");
                        heatGrad.addColorStop(0.4, "rgba(249, 115, 22, 0.40)");
                        heatGrad.addColorStop(0.8, "rgba(234, 179, 8, 0.15)");
                        heatGrad.addColorStop(1, "rgba(239, 68, 68, 0.0)");
                    } else {
                        heatGrad.addColorStop(0, "rgba(245, 158, 11, 0.60)");
                        heatGrad.addColorStop(0.5, "rgba(234, 179, 8, 0.30)");
                        heatGrad.addColorStop(1, "rgba(245, 158, 11, 0.0)");
                    }

                    ctx.fillStyle = heatGrad;
                    ctx.beginPath();
                    ctx.arc(hPos.x, hPos.y, rad, 0, 2 * Math.PI);
                    ctx.fill();
                }
            }

            // 3. Status-Coded Incident Marker Pins
            if (mapController.showPins) {
                var pinCount = mapController.incidentModel.rowCount();
                for (var i = 0; i < pinCount; ++i) {
                    var pIdx = mapController.incidentModel.index(i, 0);
                    var rId = mapController.incidentModel.data(pIdx, 257);
                    var lat = mapController.incidentModel.data(pIdx, 263);
                    var lng = mapController.incidentModel.data(pIdx, 264);
                    var stat = mapController.incidentModel.data(pIdx, 265);

                    var pos = root.latLngToScreen(lat, lng);
                    var isPinSelected = (mapController.selectedReportId === rId);

                    var statUpper = String(stat).toUpperCase();
                    var pinColor = "#F59E0B"; // Default amber for IN PROGRESS / ASSIGNED
                    if (statUpper === "CRITICAL" || statUpper === "SUBMITTED" || statUpper === "PENDING" || statUpper === "ESCALATED") {
                        pinColor = "#DC2626"; // Red for pending / critical
                    } else if (statUpper === "RESOLVED" || statUpper === "CLOSED" || statUpper === "CITIZEN_CLOSED") {
                        pinColor = "#16A34A"; // Green for resolved / closed
                    }

                    // Pulsing Outer Aura
                    var isPulsing = (statUpper === "CRITICAL" || statUpper === "SUBMITTED" || statUpper === "ESCALATED" || isPinSelected);
                    if (isPulsing) {
                        ctx.fillStyle = (statUpper === "CRITICAL" || statUpper === "SUBMITTED" || statUpper === "ESCALATED")
                            ? "rgba(220, 38, 38, 0.30)"
                            : "rgba(67, 56, 202, 0.30)";
                        ctx.beginPath();
                        ctx.arc(pos.x, pos.y, isPinSelected ? 20 : 15, 0, 2 * Math.PI);
                        ctx.fill();
                    }

                    // Shadow
                    ctx.fillStyle = "rgba(0, 0, 0, 0.20)";
                    ctx.beginPath();
                    ctx.arc(pos.x, pos.y + 1.5, isPinSelected ? 9 : 7, 0, 2 * Math.PI);
                    ctx.fill();

                    // Main Pin Circle
                    ctx.fillStyle = pinColor;
                    ctx.beginPath();
                    ctx.arc(pos.x, pos.y, isPinSelected ? 8.5 : 7, 0, 2 * Math.PI);
                    ctx.fill();

                    // White Border
                    ctx.strokeStyle = "#FFFFFF";
                    ctx.lineWidth = 2.0;
                    ctx.stroke();

                    // Inner Dot
                    ctx.fillStyle = "#FFFFFF";
                    ctx.beginPath();
                    ctx.arc(pos.x, pos.y, isPinSelected ? 3.0 : 2.2, 0, 2 * Math.PI);
                    ctx.fill();
                }
            }
        }
    }

    // ================= MOUSE & TOUCH INTERACTION =================
    MouseArea {
        anchors.fill: parent
        hoverEnabled: true
        acceptedButtons: Qt.LeftButton | Qt.RightButton

        property real startX: 0
        property real startY: 0
        property bool isDragging: false

        onPressed: function(mouse) {
            startX = mouse.x - root.panOffsetX;
            startY = mouse.y - root.panOffsetY;
            isDragging = true;
        }

        onPositionChanged: function(mouse) {
            if (isDragging && (mouse.buttons & Qt.LeftButton)) {
                root.panOffsetX = mouse.x - startX;
                root.panOffsetY = mouse.y - startY;
                return;
            }

            // 1. Proximity hit-test for pins
            var foundPin = "";
            var pinCount = mapController.incidentModel.rowCount();
            for (var i = 0; i < pinCount; ++i) {
                var pIdx = mapController.incidentModel.index(i, 0);
                var rId = mapController.incidentModel.data(pIdx, 257);
                var lat = mapController.incidentModel.data(pIdx, 263);
                var lng = mapController.incidentModel.data(pIdx, 264);
                var pos = root.latLngToScreen(lat, lng);
                var dist = Math.hypot(pos.x - mouse.x, pos.y - mouse.y);
                if (dist <= 14) {
                    foundPin = rId;
                    break;
                }
            }

            // 2. Fast bounding-box & ray-casting hit-test for wards (if not hovering a pin)
            var foundWard = -1;
            if (foundPin === "" && mapController.showBorders) {
                var mLatLng = root.screenToLatLng(mouse.x, mouse.y);
                var wardCount = mapController.wardModel.rowCount();
                for (var w = 0; w < wardCount; ++w) {
                    var wIdx = mapController.wardModel.index(w, 0);
                    var pts = mapController.wardModel.data(wIdx, 272);
                    if (!pts || pts.length < 3) continue;

                    var wMinLat = mapController.wardModel.data(wIdx, 275);
                    var wMaxLat = mapController.wardModel.data(wIdx, 276);
                    var wMinLng = mapController.wardModel.data(wIdx, 277);
                    var wMaxLng = mapController.wardModel.data(wIdx, 278);

                    if (mLatLng.lng < wMinLng || mLatLng.lng > wMaxLng ||
                        mLatLng.lat < wMinLat || mLatLng.lat > wMaxLat) {
                        continue;
                    }

                    var inside = false;
                    for (var j = 0, k = pts.length - 1; j < pts.length; k = j++) {
                        var pj = pts[j];
                        var pk = pts[k];
                        var intersect = ((pj.lat > mLatLng.lat) !== (pk.lat > mLatLng.lat)) &&
                                        (mLatLng.lng < (pk.lng - pj.lng) * (mLatLng.lat - pj.lat) / (pk.lat - pj.lat) + pj.lng);
                        if (intersect) inside = !inside;
                    }

                    if (inside) {
                        foundWard = mapController.wardModel.data(wIdx, 257);
                        break;
                    }
                }
            }

            var needsRepaint = false;
            if (foundPin !== root.hoveredReportId) {
                root.hoveredReportId = foundPin;
                needsRepaint = true;
            }
            if (foundWard !== root.hoveredWardNumber) {
                root.hoveredWardNumber = foundWard;
                needsRepaint = true;
            }

            cursorShape = (foundPin !== "" || foundWard !== -1) ? Qt.PointingHandCursor : Qt.ArrowCursor;

            if (needsRepaint) {
                canvas.requestPaint();
            }
        }

        onExited: {
            if (root.hoveredWardNumber !== -1 || root.hoveredReportId !== "") {
                root.hoveredWardNumber = -1;
                root.hoveredReportId = "";
                canvas.requestPaint();
            }
        }

        onReleased: {
            isDragging = false;
        }

        onClicked: function(mouse) {
            // 1. Check if clicked on a marker pin
            var pinCount = mapController.incidentModel.rowCount();
            for (var i = 0; i < pinCount; ++i) {
                var pIdx = mapController.incidentModel.index(i, 0);
                var rId = mapController.incidentModel.data(pIdx, 257);
                var lat = mapController.incidentModel.data(pIdx, 263);
                var lng = mapController.incidentModel.data(pIdx, 264);
                var pos = root.latLngToScreen(lat, lng);
                var dist = Math.hypot(pos.x - mouse.x, pos.y - mouse.y);
                if (dist <= 16) {
                    mapController.selectIncident(rId);
                    root.pinClicked(rId);
                    canvas.requestPaint();
                    return;
                }
            }

            // 2. Check if clicked inside a ward polygon (Ray casting)
            var wardCount = mapController.wardModel.rowCount();
            for (var w = 0; w < wardCount; ++w) {
                var wIdx = mapController.wardModel.index(w, 0);
                var wNum = mapController.wardModel.data(wIdx, 257);
                var pts = mapController.wardModel.data(wIdx, 272);
                if (!pts || pts.length < 3) continue;

                var inside = false;
                for (var j = 0, k = pts.length - 1; j < pts.length; k = j++) {
                    var pj = root.latLngToScreen(pts[j].lat, pts[j].lng);
                    var pk = root.latLngToScreen(pts[k].lat, pts[k].lng);

                    var intersect = ((pj.y > mouse.y) !== (pk.y > mouse.y)) &&
                                    (mouse.x < (pk.x - pj.x) * (mouse.y - pj.y) / (pk.y - pj.y) + pj.x);
                    if (intersect) inside = !inside;
                }

                if (inside) {
                    mapController.selectWard(wNum);
                    root.wardClicked(wNum);
                    canvas.requestPaint();
                    return;
                }
            }
        }

        onWheel: function(wheel) {
            if (wheel.angleDelta.y > 0) {
                root.zoomIn();
            } else {
                root.zoomOut();
            }
        }
    }

    // ================= MAP ATTRIBUTION & ACCREDITATION =================
    Rectangle {
        anchors.left: parent.left
        anchors.bottom: parent.bottom
        anchors.margins: 12
        height: 22
        width: attrText.implicitWidth + 14
        radius: 11
        color: "#E6FFFFFF"
        border.color: "#CBD5E1"
        border.width: 1

        Text {
            id: attrText
            anchors.centerIn: parent
            text: "© OpenStreetMap contributors • Humanitarian OSM Team • Greater Bengaluru Authority"
            font.pixelSize: 9
            color: "#64748B"
        }
    }

    // ================= CONTROLLER EVENT LISTENERS =================
    Connections {
        target: mapController
        function onConstituencyChanged() {
            root.resetView();
        }
        function onBoundsChanged() {
            root.resetView();
        }
        function onWardSelectionChanged() { canvas.requestPaint(); }
        function onIncidentSelectionChanged() { canvas.requestPaint(); }
        function onFilterChanged() { canvas.requestPaint(); }
        function onLayerToggled() { canvas.requestPaint(); }
        function onViewCenterChanged() {
            root.centerLat = mapController.centerLat;
            root.centerLng = mapController.centerLng;
            root.updateVisibleTiles();
            canvas.requestPaint();
        }
    }
}
