package com.reportit.app.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

object LocationHelper {
    private const val TAG = "LocationHelper"

    // Default Bengaluru coordinates (Vidhana Soudha / Central)
    const val DEFAULT_LAT = 12.9716
    const val DEFAULT_LON = 77.5946

    fun hasLocationPermission(context: Context): Boolean {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    @SuppressLint("MissingPermission")
    fun fetchCurrentLocation(
        context: Context,
        onResult: (lat: Double, lon: Double, isAccurate: Boolean) -> Unit
    ) {
        if (!hasLocationPermission(context)) {
            Log.w(TAG, "Location permission not granted, using fallback coordinates")
            onResult(DEFAULT_LAT, DEFAULT_LON, false)
            return
        }

        try {
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            val cts = CancellationTokenSource()

            // 1. Try getCurrentLocation with High Accuracy
            fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        Log.d(TAG, "FusedLocation getCurrentLocation success: (${location.latitude}, ${location.longitude})")
                        onResult(location.latitude, location.longitude, true)
                    } else {
                        // 2. Fallback to getLastLocation
                        fusedClient.lastLocation.addOnSuccessListener { lastLoc: Location? ->
                            if (lastLoc != null) {
                                Log.d(TAG, "FusedLocation lastLocation success: (${lastLoc.latitude}, ${lastLoc.longitude})")
                                onResult(lastLoc.latitude, lastLoc.longitude, true)
                            } else {
                                // 3. Fallback to native LocationManager
                                fallbackToSystemLocationManager(context, onResult)
                            }
                        }.addOnFailureListener {
                            fallbackToSystemLocationManager(context, onResult)
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "FusedLocation getCurrentLocation failed: ${e.message}")
                    fallbackToSystemLocationManager(context, onResult)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error obtaining location: ${e.message}")
            fallbackToSystemLocationManager(context, onResult)
        }
    }

    @SuppressLint("MissingPermission")
    private fun fallbackToSystemLocationManager(
        context: Context,
        onResult: (lat: Double, lon: Double, isAccurate: Boolean) -> Unit
    ) {
        try {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            if (lm != null) {
                val gpsLoc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                val netLoc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                val best = gpsLoc ?: netLoc

                if (best != null) {
                    Log.d(TAG, "System LocationManager fallback success: (${best.latitude}, ${best.longitude})")
                    onResult(best.latitude, best.longitude, true)
                    return
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "System LocationManager fallback failed: ${e.message}")
        }

        Log.w(TAG, "All location providers returned null, defaulting to Bengaluru central")
        onResult(DEFAULT_LAT, DEFAULT_LON, false)
    }
}
