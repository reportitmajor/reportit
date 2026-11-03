package com.reportit.app.gis

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RadialGradient
import android.graphics.Shader
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import com.reportit.app.data.model.GrievanceReport
import com.reportit.app.data.model.ReportSeverity
import com.reportit.app.data.model.ReportStatus
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Overlay
import java.io.File
import kotlin.math.hypot

class InteractiveCivicMapView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    companion object {
        // Greater Bengaluru (GBA/BBMP) Metropolitan Bounding Box
        const val MIN_LAT = 12.7500
        const val MAX_LAT = 13.1500
        const val MIN_LNG = 77.4000
        const val MAX_LNG = 77.8000

        // Bengaluru Central Coordinates
        const val BENGALURU_CENTER_LAT = 12.9716
        const val BENGALURU_CENTER_LNG = 77.5946
        const val DEFAULT_ZOOM = 13.0
        const val MIN_ZOOM = 11.2
        const val MAX_ZOOM = 19.5
    }

    private val density = resources.displayMetrics.density
    val osMap: MapView

    // Data State
    private val reportsList = mutableListOf<GrievanceReport>()
    private var selectedReport: GrievanceReport? = null
    private var userLat: Double? = null
    private var userLng: Double? = null
    private var showHeatmap = false

    // Listeners
    var onReportSelected: ((GrievanceReport) -> Unit)? = null
    var onMapClicked: (() -> Unit)? = null

    init {
        // 1. Initialize OSM Configuration
        val config = Configuration.getInstance()
        config.userAgentValue = context.packageName
        val basePath = File(context.cacheDir, "osmdroid")
        config.osmdroidBasePath = basePath
        config.osmdroidTileCache = File(basePath, "tiles")

        // 2. Initialize MapView
        osMap = MapView(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
            isTilesScaledToDpi = true

            // Restrict strictly to Bengaluru
            val bengaluruBounds = BoundingBox(MAX_LAT, MAX_LNG, MIN_LAT, MIN_LNG)
            setScrollableAreaLimitDouble(bengaluruBounds)
            minZoomLevel = MIN_ZOOM
            maxZoomLevel = MAX_ZOOM

            controller.setZoom(DEFAULT_ZOOM)
            controller.setCenter(GeoPoint(BENGALURU_CENTER_LAT, BENGALURU_CENTER_LNG))
        }
        addView(osMap)

        // 3. Attach Overlays in exact visual hierarchy:
        // Layer A: Exterior Dimming Mask (Focus on Bengaluru)
        osMap.overlays.add(BoundaryMaskOverlay())

        // Layer B: Live Civic Heatmap Layer
        osMap.overlays.add(CivicHeatmapOverlay())

        // Layer C: Interactive Civic Complaint Pins
        osMap.overlays.add(CivicMarkersOverlay())

        // Layer D: Citizen User GPS Beacon
        osMap.overlays.add(UserLocationOverlay())
    }

    // =========================================================================
    // LAYER A: EXTERIOR VIGNETTE / DIMMING MASK FOR BENGALURU FOCUS
    // =========================================================================
    private inner class BoundaryMaskOverlay : Overlay() {
        private val maskPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = Color.parseColor("#400066FF")
            strokeWidth = 3f * density
        }

        private val pMin = Point()
        private val pMax = Point()

        override fun draw(canvas: Canvas, mapView: MapView, shadow: Boolean) {
            if (shadow) return
            // Draw a subtle border indicating Greater Bengaluru Municipal Authority perimeter
            mapView.projection.toPixels(GeoPoint(MAX_LAT, MIN_LNG), pMin)
            mapView.projection.toPixels(GeoPoint(MIN_LAT, MAX_LNG), pMax)

            val left = pMin.x.toFloat()
            val top = pMin.y.toFloat()
            val right = pMax.x.toFloat()
            val bottom = pMax.y.toFloat()

            canvas.drawRoundRect(left, top, right, bottom, 24f * density, 24f * density, maskPaint)
        }
    }

    // =========================================================================
    // LAYER B: DYNAMIC CIVIC INCIDENT ACCUMULATING HEATMAP
    // =========================================================================
    private inner class CivicHeatmapOverlay : Overlay() {
        private val heatmapPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            xfermode = PorterDuffXfermode(PorterDuff.Mode.SCREEN)
        }
        private val screenPt = Point()

        override fun draw(canvas: Canvas, mapView: MapView, shadow: Boolean) {
            if (shadow || !showHeatmap) return

            val zoom = mapView.zoomLevelDouble
            val baseRadius = (36f * density * (zoom / 13.0).toFloat()).coerceIn(40f * density, 160f * density)

            for (report in reportsList) {
                if (report.latitude == 0.0 && report.longitude == 0.0) continue
                mapView.projection.toPixels(GeoPoint(report.latitude, report.longitude), screenPt)

                // Viewport culling
                if (screenPt.x < -baseRadius || screenPt.x > mapView.width + baseRadius ||
                    screenPt.y < -baseRadius || screenPt.y > mapView.height + baseRadius
                ) continue

                val isCritical = report.severity == ReportSeverity.CRITICAL || report.status == ReportStatus.ESCALATED
                val startColor = if (isCritical) {
                    Color.argb(165, 239, 68, 68) // Crimson Red for critical waterlogging/potholes
                } else {
                    Color.argb(120, 245, 158, 11) // Amber for moderate issues
                }

                val grad = RadialGradient(
                    screenPt.x.toFloat(),
                    screenPt.y.toFloat(),
                    baseRadius,
                    startColor,
                    Color.TRANSPARENT,
                    Shader.TileMode.CLAMP
                )
                heatmapPaint.shader = grad
                canvas.drawCircle(screenPt.x.toFloat(), screenPt.y.toFloat(), baseRadius, heatmapPaint)
            }
            heatmapPaint.shader = null
        }
    }

    // =========================================================================
    // LAYER C: INTERACTIVE CIVIC MARKERS & COMPLAINT PINS
    // =========================================================================
    private inner class CivicMarkersOverlay : Overlay() {
        private val pinFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }
        private val pinStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2.5f * density
            color = Color.WHITE
        }
        private val haloPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }
        private val pinShadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = Color.argb(60, 0, 0, 0)
        }

        private val screenPt = Point()

        override fun draw(canvas: Canvas, mapView: MapView, shadow: Boolean) {
            if (shadow) return

            val pinRadius = 13f * density
            val haloRadius = 20f * density

            for (report in reportsList) {
                if (report.latitude == 0.0 && report.longitude == 0.0) continue
                mapView.projection.toPixels(GeoPoint(report.latitude, report.longitude), screenPt)

                val x = screenPt.x.toFloat()
                val y = screenPt.y.toFloat()

                // Culling
                if (x < -50f || x > mapView.width + 50f || y < -50f || y > mapView.height + 50f) continue

                val isSelected = selectedReport?.id == report.id
                val isCritical = report.severity == ReportSeverity.CRITICAL || report.status == ReportStatus.ESCALATED

                // Base Shadow
                canvas.drawCircle(x, y + 4f * density, pinRadius * 0.85f, pinShadowPaint)

                // Pulsing Halo for Critical or Selected reports
                if (isCritical || isSelected) {
                    haloPaint.color = if (isSelected) Color.argb(90, 0, 102, 255) else Color.argb(80, 220, 38, 38)
                    canvas.drawCircle(x, y, if (isSelected) haloRadius * 1.2f else haloRadius, haloPaint)
                }

                // Main Marker Circle
                val pinColor = when (report.status) {
                    ReportStatus.RESOLVED, ReportStatus.CITIZEN_CLOSED -> Color.parseColor("#059669") // Green
                    ReportStatus.IN_PROGRESS, ReportStatus.ASSIGNED, ReportStatus.AI_ANALYZED, ReportStatus.VERIFIED -> Color.parseColor("#F59E0B") // Amber
                    ReportStatus.ESCALATED -> Color.parseColor("#DC2626") // Red
                    else -> Color.parseColor(report.severity.colorHex)
                }

                pinFillPaint.color = pinColor
                canvas.drawCircle(x, y, if (isSelected) pinRadius * 1.15f else pinRadius, pinFillPaint)
                canvas.drawCircle(x, y, if (isSelected) pinRadius * 1.15f else pinRadius, pinStrokePaint)
            }
        }

        override fun onSingleTapConfirmed(e: MotionEvent, mapView: MapView): Boolean {
            val tapThreshold = 32f * density
            var hitReport: GrievanceReport? = null
            var closestDist = Float.MAX_VALUE

            // Hit test in reverse order (top-most pin first)
            for (i in reportsList.indices.reversed()) {
                val rep = reportsList[i]
                if (rep.latitude == 0.0 && rep.longitude == 0.0) continue

                mapView.projection.toPixels(GeoPoint(rep.latitude, rep.longitude), screenPt)
                val dist = hypot(screenPt.x.toFloat() - e.x, screenPt.y.toFloat() - e.y)
                if (dist <= tapThreshold && dist < closestDist) {
                    closestDist = dist
                    hitReport = rep
                }
            }

            if (hitReport != null) {
                selectedReport = hitReport
                onReportSelected?.invoke(hitReport)
                mapView.invalidate()
                return true
            } else {
                selectedReport = null
                onMapClicked?.invoke()
                mapView.invalidate()
                return false
            }
        }
    }

    // =========================================================================
    // LAYER D: CITIZEN GPS LOCATION BEACON
    // =========================================================================
    private inner class UserLocationOverlay : Overlay() {
        private val userCenterPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = Color.parseColor("#0066FF")
        }
        private val userWhiteRingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 3f * density
            color = Color.WHITE
        }
        private val userAccuracyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = Color.argb(40, 0, 102, 255)
        }
        private val screenPt = Point()

        override fun draw(canvas: Canvas, mapView: MapView, shadow: Boolean) {
            if (shadow) return
            val lat = userLat ?: return
            val lng = userLng ?: return

            mapView.projection.toPixels(GeoPoint(lat, lng), screenPt)
            val x = screenPt.x.toFloat()
            val y = screenPt.y.toFloat()

            canvas.drawCircle(x, y, 22f * density, userAccuracyPaint)
            canvas.drawCircle(x, y, 8f * density, userWhiteRingPaint)
            canvas.drawCircle(x, y, 7f * density, userCenterPaint)
        }
    }

    // =========================================================================
    // PUBLIC APIS FOR MAP CONTROL AND NAVIGATION
    // =========================================================================
    fun setReports(reports: List<GrievanceReport>) {
        reportsList.clear()
        reportsList.addAll(reports)
        osMap.invalidate()
    }

    fun setSelectedReport(report: GrievanceReport?, centerOnIt: Boolean = false) {
        selectedReport = report
        if (centerOnIt && report != null && report.latitude != 0.0 && report.longitude != 0.0) {
            centerOnCoordinates(report.latitude, report.longitude, animated = true)
        } else {
            osMap.invalidate()
        }
    }

    fun setUserLocation(lat: Double, lng: Double, centerOnIt: Boolean = false) {
        userLat = lat
        userLng = lng
        if (centerOnIt) {
            centerOnCoordinates(lat, lng, animated = true)
        } else {
            osMap.invalidate()
        }
    }

    fun toggleHeatmap(show: Boolean? = null) {
        showHeatmap = show ?: !showHeatmap
        osMap.invalidate()
    }

    fun zoomIn() {
        osMap.controller.zoomIn()
    }

    fun zoomOut() {
        osMap.controller.zoomOut()
    }

    fun centerOnCoordinates(lat: Double, lng: Double, animated: Boolean = true) {
        val clampedLat = lat.coerceIn(MIN_LAT, MAX_LAT)
        val clampedLng = lng.coerceIn(MIN_LNG, MAX_LNG)
        val targetPoint = GeoPoint(clampedLat, clampedLng)

        if (animated) {
            osMap.controller.animateTo(targetPoint)
        } else {
            osMap.controller.setCenter(targetPoint)
        }
    }

    fun resume() {
        osMap.onResume()
    }

    fun pause() {
        osMap.onPause()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        osMap.onDetach()
    }
}
