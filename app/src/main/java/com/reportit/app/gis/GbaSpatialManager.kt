package com.reportit.app.gis

import android.content.Context
import android.util.Log
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.Executors
import kotlin.math.hypot

data class GbaWard(
    val wardNumber: Int,
    val wardName: String,
    val acCode: String,
    val acName: String,
    val mlaName: String,
    val gbaZone: String,
    val aeeName: String,
    val aeeContact: String,
    val healthInspector: String,
    val centroidLng: Double,
    val centroidLat: Double,
    val minLng: Double,
    val minLat: Double,
    val maxLng: Double,
    val maxLat: Double,
    val polygon: List<Pair<Double, Double>> // Pair(lng, lat)
)

object GbaSpatialManager {

    private const val TAG = "GbaSpatialManager"
    private const val ASSET_FILE = "gis/bengaluru_all_wards.geojson"

    private val executor = Executors.newSingleThreadExecutor()
    private val wardsList = mutableListOf<GbaWard>()
    @Volatile private var isLoaded = false

    fun init(context: Context, onReady: (() -> Unit)? = null) {
        if (isLoaded) {
            onReady?.invoke()
            return
        }

        val appContext = context.applicationContext
        executor.execute {
            try {
                loadGeoJson(appContext)
                isLoaded = true
                Log.d(TAG, "GBA Ward Boundaries loaded successfully: ${wardsList.size} wards parsed.")
                onReady?.invoke()
            } catch (e: Exception) {
                Log.e(TAG, "Error loading GBA GeoJSON: ${e.message}", e)
            }
        }
    }

    private fun loadGeoJson(context: Context) {
        val inputStream = context.assets.open(ASSET_FILE)
        val reader = BufferedReader(InputStreamReader(inputStream))
        val sb = StringBuilder()
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            sb.append(line)
        }
        reader.close()

        val root = JSONObject(sb.toString())
        val features = root.optJSONArray("features") ?: return

        synchronized(wardsList) {
            wardsList.clear()
            for (i in 0 until features.length()) {
                val feat = features.getJSONObject(i)
                val props = feat.optJSONObject("properties") ?: continue
                val geom = feat.optJSONObject("geometry") ?: continue

                val wardNumber = props.optInt("ward_number", 0)
                val wardName = props.optString("ward_name", "Ward $wardNumber")
                val acCode = props.optString("ac_code", "")
                val acName = props.optString("ac_name", "Bengaluru Constituency")
                val mlaName = props.optString("mla_name", "Member of Legislative Assembly")
                val gbaZone = props.optString("gba_zone", "Greater Bengaluru Authority")
                val aeeName = props.optString("aee_name", "AEE Junior Engineer")
                val aeeContact = props.optString("aee_contact", "")
                val healthInspector = props.optString("health_inspector", "")

                val centroidArr = props.optJSONArray("centroid")
                val centroidLng = centroidArr?.optDouble(0, 77.5946) ?: 77.5946
                val centroidLat = centroidArr?.optDouble(1, 12.9716) ?: 12.9716

                val boundsArr = props.optJSONArray("bounds")
                val minLng = boundsArr?.optDouble(0, 77.45) ?: 77.45
                val minLat = boundsArr?.optDouble(1, 12.80) ?: 12.80
                val maxLng = boundsArr?.optDouble(2, 77.78) ?: 77.78
                val maxLat = boundsArr?.optDouble(3, 13.15) ?: 13.15

                val coordsArr = geom.optJSONArray("coordinates")
                val ringList = mutableListOf<Pair<Double, Double>>()
                if (coordsArr != null && coordsArr.length() > 0) {
                    val ring = coordsArr.optJSONArray(0)
                    if (ring != null) {
                        for (j in 0 until ring.length()) {
                            val pt = ring.optJSONArray(j)
                            if (pt != null && pt.length() >= 2) {
                                ringList.add(Pair(pt.optDouble(0), pt.optDouble(1)))
                            }
                        }
                    }
                }

                wardsList.add(
                    GbaWard(
                        wardNumber = wardNumber,
                        wardName = wardName,
                        acCode = acCode,
                        acName = acName,
                        mlaName = mlaName,
                        gbaZone = gbaZone,
                        aeeName = aeeName,
                        aeeContact = aeeContact,
                        healthInspector = healthInspector,
                        centroidLng = centroidLng,
                        centroidLat = centroidLat,
                        minLng = minLng,
                        minLat = minLat,
                        maxLng = maxLng,
                        maxLat = maxLat,
                        polygon = ringList
                    )
                )
            }
        }
    }

    /**
     * Resolves GPS Coordinates (Latitude, Longitude) into exact GBA Ward, MLA, and Zone
     * using Ray-Casting Point-in-Polygon (PIP) with Bounding-Box filtering.
     * Falls back to nearest centroid if point is near city boundary.
     */
    fun resolvePoint(context: Context, latitude: Double, longitude: Double): GbaWard? {
        if (!isLoaded) {
            // Synchronously ensure loaded if accessed before async init completes
            loadGeoJson(context.applicationContext)
            isLoaded = true
        }

        synchronized(wardsList) {
            if (wardsList.isEmpty()) return null

            // 1. Point-in-polygon lookup with bounding box pre-filter
            for (ward in wardsList) {
                if (longitude < ward.minLng || longitude > ward.maxLng ||
                    latitude < ward.minLat || latitude > ward.maxLat) {
                    continue
                }

                if (isPointInPolygon(longitude, latitude, ward.polygon)) {
                    return ward
                }
            }

            // 2. Fallback: Find closest ward by centroid distance
            return wardsList.minByOrNull { ward ->
                hypot(ward.centroidLat - latitude, ward.centroidLng - longitude)
            }
        }
    }

    private fun isPointInPolygon(x: Double, y: Double, polygon: List<Pair<Double, Double>>): Boolean {
        if (polygon.size < 3) return false
        var inside = false
        var j = polygon.size - 1
        for (i in polygon.indices) {
            val xi = polygon[i].first
            val yi = polygon[i].second
            val xj = polygon[j].first
            val yj = polygon[j].second

            val intersect = ((yi > y) != (yj > y)) &&
                    (x < (xj - xi) * (y - yi) / (yj - yi) + xi)
            if (intersect) {
                inside = !inside
            }
            j = i
        }
        return inside
    }
}
