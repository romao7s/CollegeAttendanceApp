package com.example.collegeattendanceapp.service

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.location.Location
import androidx.annotation.RequiresApi
import com.google.android.gms.location.LocationResult
import java.util.Date
class LocationUpdatesReceiver : BroadcastReceiver() {
    private val TAG = "LocationReceiver"
    private val TAG_DETAILED = "LOCATION_RECEIVER_DETAILED"

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG_DETAILED, "Broadcast received - Action: ${intent.action}")
        Log.d(TAG_DETAILED, "Intent extras keys: ${intent.extras?.keySet()}")

        intent.extras?.keySet()?.forEach { key ->
            Log.d(TAG_DETAILED, "Extra [$key]: ${intent.extras?.get(key)}")
        }

        if (intent.action != "LOCATION_UPDATE") {
            Log.e(TAG_DETAILED, "Wrong action: ${intent.action}")
            return
        }

        val result = LocationResult.extractResult(intent)
        Log.d(TAG_DETAILED, "LocationResult extracted: $result")

        if (result == null) {
            Log.e(TAG_DETAILED, "LocationResult is null - checking for manual location data")

            val lat = intent.getDoubleExtra("lat", 0.0)
            val lon = intent.getDoubleExtra("lon", 0.0)
            if (lat != 0.0 && lon != 0.0) {
                Log.d(TAG_DETAILED, "Manual location data found: $lat, $lon")
                processLocationUpdate(context, lat, lon)
                return
            }

            Log.e(TAG_DETAILED, "No location data found in intent")
            return
        }

        val locations = result.locations
        Log.d(TAG_DETAILED, "Number of locations in batch: ${locations.size}")

        if (locations.isNotEmpty()) {
            locations.forEachIndexed { index, location ->
                Log.d(TAG_DETAILED, "Location $index: ${location.latitude}, ${location.longitude}")
                Log.d(TAG_DETAILED, "Accuracy: ${location.accuracy}m, Time: ${Date(location.time)}")
                Log.d(TAG_DETAILED, "Provider: ${location.provider}, Speed: ${location.speed} m/s")

                processLocationUpdate(context, location.latitude, location.longitude)
            }
        } else {
            val lastLocation = result.lastLocation
            if (lastLocation != null) {
                Log.d(TAG_DETAILED, "LastLocation: ${lastLocation.latitude}, ${lastLocation.longitude}")
                Log.d(TAG_DETAILED, "Accuracy: ${lastLocation.accuracy}m, Time: ${Date(lastLocation.time)}")
                processLocationUpdate(context, lastLocation.latitude, lastLocation.longitude)
            } else {
                Log.e(TAG_DETAILED, "Both locations list and lastLocation are null")
            }
        }
    }

    private fun processLocationUpdate(context: Context, lat: Double, lon: Double) {
        Log.d(TAG_DETAILED, "Processing location update: $lat, $lon")

        val serviceIntent = Intent(context, AttendanceService::class.java).apply {
            putExtra("lat", lat)
            putExtra("lon", lon)
            putExtra("timestamp", System.currentTimeMillis())
            action = "ACTION_LOCATION_UPDATE"
        }

        try {
            Log.d(TAG_DETAILED, "Starting AttendanceService with location data...")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
            Log.d(TAG_DETAILED, " AttendanceService started successfully with location")
        } catch (e: Exception) {
            Log.e(TAG_DETAILED, " Could not start service: ${e.message}", e)
        }
    }
}
//



