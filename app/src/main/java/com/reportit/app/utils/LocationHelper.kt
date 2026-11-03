package com.reportit.app.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import java.util.Locale
import java.util.concurrent.Executors

object LocationHelper {
    private const val TAG = "LocationHelper"

    fun hasLocationPermission(context: Context): Boolean {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    fun isLocationProviderEnabled(context: Context): Boolean {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return false
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER) || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    /**
     * Actively requests real-time GPS fixes from the phone's hardware antenna.
     * Never silently falls back to a hardcoded ward. Returns isRealGps = true only when an actual GPS antenna/network fix is achieved.
     */
    @SuppressLint("MissingPermission")
    fun fetchCurrentLocation(
        context: Context,
        onResult: (lat: Double, lon: Double, address: String?, isRealGps: Boolean) -> Unit
    ) {
        if (!hasLocationPermission(context)) {
            Log.w(TAG, "Location permission not granted.")
            onResult(0.0, 0.0, null, false)
            return
        }

        val appContext = context.applicationContext
        val fusedClient = LocationServices.getFusedLocationProviderClient(appContext)
        val mainHandler = Handler(Looper.getMainLooper())
        var isCompleted = false

        fun deliverSuccess(location: Location) {
            if (isCompleted) return
            isCompleted = true

            val lat = location.latitude
            val lon = location.longitude
            Log.d(TAG, "Real GPS fix locked: lat=$lat, lon=$lon, accuracy=${location.accuracy}m")

            // Reverse geocode asynchronously
            Executors.newSingleThreadExecutor().execute {
                var addressStr: String? = null
                try {
                    val geocoder = Geocoder(appContext, Locale.getDefault())
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        geocoder.getFromLocation(lat, lon, 1) { addresses ->
                            val addr = addresses.firstOrNull()
                            val line = addr?.let {
                                val area = it.subLocality ?: it.thoroughfare ?: it.subAdminArea ?: ""
                                val city = it.locality ?: "Bengaluru"
                                if (area.isNotEmpty()) "$area, $city" else city
                            }
                            mainHandler.post { onResult(lat, lon, line, true) }
                        }
                        return@execute
                    } else {
                        @Suppress("DEPRECATION")
                        val addresses = geocoder.getFromLocation(lat, lon, 1)
                        val addr = addresses?.firstOrNull()
                        addressStr = addr?.let {
                            val area = it.subLocality ?: it.thoroughfare ?: it.subAdminArea ?: ""
                            val city = it.locality ?: "Bengaluru"
                            if (area.isNotEmpty()) "$area, $city" else city
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Geocoder reverse-lookup failed: ${e.message}")
                }
                mainHandler.post { onResult(lat, lon, addressStr, true) }
            }
        }

        // 1. Check if we have an immediate fresh lastLocation (< 60 seconds old with good accuracy)
        fusedClient.lastLocation.addOnSuccessListener { lastLoc: Location? ->
            if (lastLoc != null && (System.currentTimeMillis() - lastLoc.time) < 60000 && lastLoc.accuracy < 80f) {
                Log.d(TAG, "Recent accurate lastLocation found: ${lastLoc.latitude}, ${lastLoc.longitude}")
                deliverSuccess(lastLoc)
                return@addOnSuccessListener
            }
        }

        // 2. Start active live GPS requests
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L)
            .setMinUpdateIntervalMillis(500L)
            .setMaxUpdateDelayMillis(2000L)
            .setWaitForAccurateLocation(true)
            .build()

        var callbackRef: LocationCallback? = null
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val loc = result.lastLocation ?: return
                if (loc.accuracy < 100f || !isCompleted) {
                    callbackRef?.let { fusedClient.removeLocationUpdates(it) }
                    deliverSuccess(loc)
                }
            }
        }
        callbackRef = callback

        try {
            fusedClient.requestLocationUpdates(locationRequest, callback, Looper.getMainLooper())
        } catch (e: Exception) {
            Log.e(TAG, "Failed to request location updates: ${e.message}")
        }

        // 3. Safety timeout: if after 7 seconds no high-accuracy fix arrived, check if any fix or native provider has a location
        mainHandler.postDelayed({
            if (!isCompleted) {
                callbackRef?.let { fusedClient.removeLocationUpdates(it) }
                val lm = appContext.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                val gpsLoc = try { lm?.getLastKnownLocation(LocationManager.GPS_PROVIDER) } catch (e: SecurityException) { null }
                val netLoc = try { lm?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) } catch (e: SecurityException) { null }
                val fallback = gpsLoc ?: netLoc

                if (fallback != null) {
                    deliverSuccess(fallback)
                } else {
                    Log.w(TAG, "Unable to lock GPS satellites. Reporting failure to UI.")
                    isCompleted = true
                    onResult(0.0, 0.0, null, false)
                }
            }
        }, 7000L)
    }
}
