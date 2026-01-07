package com.example.collegeattendanceapp.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.IBinder
import com.google.android.gms.location.LocationRequest

import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import android.app.Notification
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.collegeattendanceapp.R
import com.example.collegeattendanceapp.main.MainActivity
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority

import java.util.Locale

//for circular and radius
//class AttendanceService : Service() {
//
//    private lateinit var fused: FusedLocationProviderClient
//    private val fs = FirebaseFirestore.getInstance()
//    private val prefs by lazy { getSharedPreferences("attendance_prefs", MODE_PRIVATE) }
//    private val TAG = "AttendanceService"
//
//    private val TAG_LIFECYCLE = "ATTN_SERVICE_LIFECYCLE"
//    private val TAG_LOCATION = "ATTN_SERVICE_LOCATION"
//    private val TAG_FIREBASE = "ATTN_SERVICE_FIREBASE"
//    private val TAG_NOTIFICATION = "ATTN_SERVICE_NOTIFICATION"
//    private val TAG_ATTENDANCE = "ATTN_SERVICE_ATTENDANCE"
//    private val TAG_PREFS = "ATTN_SERVICE_PREFS"
//    private var lastAttendanceProcessingTime: Long = 0
//    private val ATTENDANCE_DEBOUNCE_INTERVAL = 30000L
//    private val LOCATION_PENDING_INTENT_REQUEST_CODE = 1001
//
//
//    companion object {
//        const val LOCATION_UPDATE_INTERVAL = 15000L
//        const val FASTEST_UPDATE_INTERVAL = 10000L
//        const val MAX_WAIT_TIME = 30000L
//    }
//
//    override fun onBind(intent: Intent?): IBinder? {
//        Log.d(TAG_LIFECYCLE, "onBind() called - Service bound")
//        return null
//    }
//
//    override fun onCreate() {
//        super.onCreate()
//        Log.d(TAG_LIFECYCLE, "onCreate() - Service instance created")
//
//        try {
//            fused = LocationServices.getFusedLocationProviderClient(this)
//            Log.d(TAG_LIFECYCLE, "FusedLocationProviderClient initialized successfully")
//        } catch (e: Exception) {
//            Log.e(TAG_LIFECYCLE, "Failed to initialize FusedLocationProviderClient: ${e.message}", e)
//        }
//
//        Log.d(TAG_LIFECYCLE, "Service onCreate completed")
//    }
//
//    @RequiresApi(Build.VERSION_CODES.O)
//    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        Log.d(TAG_LIFECYCLE, "onStartCommand() called - flags: $flags, startId: $startId")
//        Log.d(TAG_LIFECYCLE, "Intent action: ${intent?.action}, extras: ${intent?.extras}")
//
//        try {
//            Log.d(TAG_NOTIFICATION, "Creating foreground notification...")
//            createForegroundNotification()
//            Log.d(TAG_NOTIFICATION, "Foreground notification created successfully")
//
//            if (intent?.action == "ACTION_LOCATION_UPDATE" && intent.hasExtra("lat") && intent.hasExtra("lon")) {
//                val lat = intent.getDoubleExtra("lat", 0.0)
//                val lon = intent.getDoubleExtra("lon", 0.0)
//                Log.d(TAG_LOCATION, " Location data received in intent - lat: $lat, lon: $lon")
//
//                if (lat != 0.0 && lon != 0.0) {
//                    Log.d(TAG_ATTENDANCE, "Processing attendance with received location")
//                    handleAttendance(lat, lon)
//                } else {
//                    Log.w(TAG_LOCATION, "Invalid location coordinates received: lat=$lat, lon=$lon")
//                }
//            } else {
//                Log.d(TAG_LOCATION, "No location data in intent, starting location updates")
//            }
//
//            Log.d(TAG_LOCATION, "Requesting location updates via PendingIntent...")
//            requestLocationUpdatesWithPI()
//            Log.d(TAG_LOCATION, "Location updates requested successfully")
//
//            getLastKnownLocationForDebugging()
//
//        } catch (e: Exception) {
//            Log.e(TAG_LIFECYCLE, "Error in onStartCommand: ${e.message}", e)
//        }
//
//        Log.d(TAG_LIFECYCLE, "Returning START_STICKY - service will be restarted if killed")
//        return START_STICKY
//    }
//
//    @RequiresApi(Build.VERSION_CODES.O)
//    @SuppressLint("MissingPermission")
//    private fun getLastKnownLocationForDebugging() {
//        Log.d(TAG_LOCATION, "🧪 Getting last known location for debugging...")
//
//        fused.lastLocation
//            .addOnSuccessListener { location ->
//                if (location != null) {
//                    Log.d(TAG_LOCATION, " Last known location found:")
//                    Log.d(TAG_LOCATION, " Lat: ${location.latitude}, Lon: ${location.longitude}")
//                    Log.d(TAG_LOCATION, "Accuracy: ${location.accuracy}m")
//                    Log.d(TAG_LOCATION, "Time: ${Date(location.time)}")
//                    Log.d(TAG_LOCATION, "Provider: ${location.provider}")
//
//                    handleAttendance(location.latitude, location.longitude)
//                } else {
//                    Log.w(TAG_LOCATION, "Last known location is null - no recent location available")
//                    Log.w(TAG_LOCATION, "This is normal if no apps have requested location recently")
//                    Log.w(TAG_LOCATION, "Location updates should start soon via PendingIntent")
//                }
//            }
//            .addOnFailureListener { e ->
//                Log.e(TAG_LOCATION, " Failed to get last known location: ${e.message}", e)
//            }
//    }
//
//    @SuppressLint("MissingPermission")
//    private fun requestLocationUpdatesWithPI() {
//        Log.d(TAG_LOCATION, "requestLocationUpdatesWithPI() - Configuring location request")
//
//        try {
//            val req = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, LOCATION_UPDATE_INTERVAL)
//                .setMinUpdateIntervalMillis(FASTEST_UPDATE_INTERVAL)
//                .setMaxUpdateDelayMillis(MAX_WAIT_TIME)
//                .setWaitForAccurateLocation(true)
//                .build()
//
//            Log.d(TAG_LOCATION, "📍 Location request built - " +
//                    "Priority: ${req.priority}, " +
//                    "Interval: ${req.intervalMillis}ms, " +
//                    "MinInterval: ${req.minUpdateIntervalMillis}ms, " +
//                    "MaxDelay: ${req.maxUpdateDelayMillis}ms")
//
//            Log.d(TAG_LOCATION, "🔗 Building PendingIntent for location updates...")
//            val pi = buildPendingIntent()
//            Log.d(TAG_LOCATION, "PendingIntent built successfully")
//
//            Log.d(TAG_LOCATION, "Requesting location updates from FusedLocationProviderClient...")
//            fused.requestLocationUpdates(req, pi)
//                .addOnSuccessListener {
//                    Log.i(TAG_LOCATION, " Location updates requested successfully via PendingIntent $pi")
//
//                    Log.i(TAG_LOCATION, "Location updates requested successfully via PendingIntent")
//                    Log.d(TAG_LOCATION, "Location updates should now be delivered to broadcast receiver")
//                    Log.d(TAG_LOCATION, "First location update should arrive within ${LOCATION_UPDATE_INTERVAL}ms")
//                }
//                .addOnFailureListener { e ->
//                    Log.e(TAG_LOCATION, "Failed to request location updates: ${e.message}", e)
//                    if (e is SecurityException) {
//                        Log.e(TAG_LOCATION, "Location permission issue, cannot retry")
//                        showPermissionNotification()
//                    } else {
//                        retryLocationUpdates()
//                    }
//                }
//
//        } catch (e: SecurityException) {
//            Log.e(TAG_LOCATION, "SecurityException - Location permission not granted: ${e.message}", e)
//            showPermissionNotification()
//        } catch (e: Exception) {
//            Log.e(TAG_LOCATION, "Error requesting location updates: ${e.message}", e)
//        }
//    }
//
//    private fun retryLocationUpdates() {
//        Log.d(TAG_LOCATION, "Scheduling retry of location updates in 30 seconds...")
//
//        Handler(Looper.getMainLooper()).postDelayed({
//            Log.d(TAG_LOCATION, "Retrying location updates...")
//            requestLocationUpdatesWithPI()
//        }, 30000)
//    }
//
//
//
//    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
//    private fun showPermissionNotification() {
//        Log.d(TAG_NOTIFICATION, "Showing location permission notification")
//
//        val notification = NotificationCompat.Builder(this, "ATTN_CHANNEL")
//            .setSmallIcon(R.mipmap.ic_launcher_round)
//            .setContentTitle("Location Permission Required")
//            .setContentText("Please grant location permission for attendance tracking")
//            .setAutoCancel(true)
//            .build()
//
//        NotificationManagerCompat.from(this).notify(1003, notification)
//    }
//
//
//    override fun onDestroy() {
//        super.onDestroy()
//        Log.d(TAG_LIFECYCLE, "onDestroy() - Service is being destroyed")
//        Log.w(TAG_LIFECYCLE, "Service destroyed but PendingIntent location updates may continue")
//    }
//
//    override fun onTaskRemoved(rootIntent: Intent?) {
//        super.onTaskRemoved(rootIntent)
//        Log.w(TAG_LIFECYCLE, "onTaskRemoved() - App task removed from recent list")
//        Log.d(TAG_LIFECYCLE, "Root intent: ${rootIntent?.action}")
//        Log.i(TAG_LIFECYCLE, "Service should continue running due to START_STICKY")
//    }
//
//    private fun getUid(): String? {
//        Log.d(TAG_PREFS, "getUid() - Retrieving user ID")
//
//        val currentUser = FirebaseAuth.getInstance().currentUser
//        if (currentUser != null) {
//            Log.d(TAG_PREFS, "User ID from Firebase Auth: ${currentUser.uid}")
//            return currentUser.uid
//        }
//
//        val savedUid = prefs.getString("uid", null)
//        if (savedUid != null) {
//            Log.d(TAG_PREFS, "User ID from SharedPreferences: $savedUid")
//        } else {
//            Log.w(TAG_PREFS, "No user ID found in Firebase Auth or SharedPreferences")
//        }
//
//        return savedUid
//    }
//
//    private fun saveUid(uid: String) {
//        Log.d(TAG_PREFS, "saveUid() - Saving user ID: $uid")
//        try {
//            prefs.edit().putString("uid", uid).apply()
//            Log.d(TAG_PREFS, "User ID saved successfully to SharedPreferences")
//        } catch (e: Exception) {
//            Log.e(TAG_PREFS, "Failed to save user ID to SharedPreferences: ${e.message}", e)
//        }
//    }
//
//
//    private fun createForegroundNotification() {
//        Log.d(TAG_NOTIFICATION, "createForegroundNotification() - Starting notification setup")
//
//        try {
//            val notificationManager = getSystemService(NotificationManager::class.java)
//            Log.d(TAG_NOTIFICATION, "Notification manager obtained")
//
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                Log.d(TAG_NOTIFICATION, "Creating notification channel for Android O+")
//                val channel = NotificationChannel(
//                    "ATTN_CHANNEL",
//                    "Attendance Tracking",
//                    NotificationManager.IMPORTANCE_HIGH
//                ).apply {
//                    description = "Background location tracking for attendance"
//                }
//
//                notificationManager.createNotificationChannel(channel)
//                Log.d(TAG_NOTIFICATION, "Notification channel created: ATTN_CHANNEL")
//            } else {
//                Log.d(TAG_NOTIFICATION, "Android version < O, no channel creation needed")
//            }
//
//            val notif = NotificationCompat.Builder(this, "ATTN_CHANNEL")
//                .setSmallIcon(R.mipmap.ic_launcher_round)
//                .setContentTitle("Attendance Running")
//                .setContentText("Tracking location…")
//                .setOngoing(true)
//                .setPriority(NotificationCompat.PRIORITY_LOW)
//                .build()
//
//            Log.d(TAG_NOTIFICATION, "Notification built, starting foreground service...")
//            startForeground(1001, notif)
//            Log.d(TAG_NOTIFICATION, "Foreground service started with notification ID: 1001")
//
//        } catch (e: Exception) {
//            Log.e(TAG_NOTIFICATION, "Error creating foreground notification: ${e.message}", e)
//            throw e
//        }
//    }
//
//    private fun buildPendingIntent(): PendingIntent {
//        val intent = Intent(this, LocationUpdatesReceiver::class.java).apply {
//            action = "LOCATION_UPDATE"
//        }
//
//        return PendingIntent.getBroadcast(
//            this,
//            LOCATION_PENDING_INTENT_REQUEST_CODE,  // Unique request code
//            intent,
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
//            } else {
//                PendingIntent.FLAG_UPDATE_CURRENT
//            }
//        )
//    }
//
////    @RequiresApi(Build.VERSION_CODES.O)
////    private fun handleAttendance(lat: Double, lon: Double) {
////        Log.d(TAG_ATTENDANCE, "handleAttendance() - Processing attendance for location: ($lat, $lon)")
////        val now = System.currentTimeMillis()
////
////        if (now - lastAttendanceProcessingTime < ATTENDANCE_DEBOUNCE_INTERVAL) {
////            Log.d(TAG_ATTENDANCE, "Debouncing attendance processing. Too soon since last: ${now - lastAttendanceProcessingTime}ms")
////            return
////        }
////
////        lastAttendanceProcessingTime = now
////        Log.d(TAG_ATTENDANCE, "handleAttendance() - Processing attendance for location: ($lat, $lon)")
////
////        val uid = getUid()
////        if (uid == null) {
////            Log.e(TAG_ATTENDANCE, "Cannot handle attendance - User ID is null")
////            return
////        }
////
////        Log.d(TAG_ATTENDANCE, "User ID obtained: $uid, saving to preferences...")
////        saveUid(uid)
////
////        Log.d(TAG_FIREBASE, "Fetching user role from Firestore for UID: $uid")
////        fs.collection("users").document(uid).get()
////            .addOnSuccessListener { snap ->
////                if (snap.exists()) {
////                    val role = snap.getString("role") ?: "Student"
////                    Log.d(TAG_FIREBASE, "User role retrieved: $role")
////
////                    val coll = if (role == "Student") "student_attendance" else "staff_attendance"
////                    Log.d(TAG_ATTENDANCE, "Using collection: $coll for role: $role")
////
////                    saveAttendance(coll, uid, lat, lon)
////                } else {
////                    Log.e(TAG_FIREBASE, "User document does not exist for UID: $uid")
////                }
////            }
////            .addOnFailureListener { e ->
////                Log.e(TAG_FIREBASE, "Failed to fetch user role from Firestore: ${e.message}", e)
////            }
////    }
////
//
//    @RequiresApi(Build.VERSION_CODES.O)
//    private fun handleAttendance(lat: Double, lon: Double) {
//        Log.d(TAG_ATTENDANCE, "handleAttendance() - Processing attendance for location: ($lat, $lon)")
//        val now = System.currentTimeMillis()
//
//        if (now - lastAttendanceProcessingTime < ATTENDANCE_DEBOUNCE_INTERVAL) {
//            Log.d(TAG_ATTENDANCE, "Debouncing attendance processing. Too soon since last: ${now - lastAttendanceProcessingTime}ms")
//            return
//        }
//
//        lastAttendanceProcessingTime = now
//
//        val uid = getUid()
//        if (uid == null) {
//            Log.e(TAG_ATTENDANCE, "Cannot handle attendance - User ID is null")
//            return
//        }
//
//        Log.d(TAG_ATTENDANCE, "User ID obtained: $uid, saving to preferences...")
//        saveUid(uid)
//
//        Log.d(TAG_FIREBASE, "Fetching user role from Firestore for UID: $uid")
//        fs.collection("users").document(uid).get()
//            .addOnSuccessListener { snap ->
//                if (snap.exists()) {
//                    val role = snap.getString("role") ?: "Student"
//                    Log.d(TAG_FIREBASE, "User role retrieved: $role")
//
//                    val coll = if (role == "Student") "student_attendance" else "staff_attendance"
//                    Log.d(TAG_ATTENDANCE, "Using collection: $coll for role: $role")
//
//                    saveAttendance(coll, uid, lat, lon)
//                } else {
//                    Log.e(TAG_FIREBASE, "User document does not exist for UID: $uid")
//                }
//            }
//            .addOnFailureListener { e ->
//                Log.e(TAG_FIREBASE, "Failed to fetch user role from Firestore: ${e.message}", e)
//            }
//    }
//
//    @RequiresApi(Build.VERSION_CODES.O)
//    private fun saveAttendance(coll: String, uid: String, lat: Double, lon: Double) {
//        Log.d(TAG_ATTENDANCE, "saveAttendance() - Collection: $coll, UID: $uid, Location: ($lat, $lon)")
//
//        val today = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
//        val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
//
//        Log.d(TAG_ATTENDANCE, "Date: $today, Time: $time")
//
//        val ref = fs.collection(coll).document(uid).collection("dates").document(today)
//        Log.d(TAG_FIREBASE, "Firestore reference: ${ref.path}")
//
//        val centerLat = 31.3136
//        val centerLon = 75.5909
//        val radius = 80.0
//        val markOutDistance = 300.0
//
//        val dist = FloatArray(1)
//        Location.distanceBetween(lat, lon, centerLat, centerLon, dist)
//        val distance = dist[0].toDouble()
//
//        val isInsideRadius = distance <= radius
//        val isWithinMarkOutRange = distance >= radius && distance <= markOutDistance
//        val isOutsideMarkOutRange = distance > markOutDistance
//
//        Log.d(TAG_ATTENDANCE, "Distance from center: ${String.format("%.2f", distance)} meters")
//        Log.d(TAG_ATTENDANCE, "Inside radius (${radius}m): $isInsideRadius")
//        Log.d(TAG_ATTENDANCE, "Within mark-out range (${radius}-${markOutDistance}m): $isWithinMarkOutRange")
//        Log.d(TAG_ATTENDANCE, "Outside mark-out range (>${markOutDistance}m): $isOutsideMarkOutRange")
//
//        Log.d(TAG_FIREBASE, "Checking existing attendance record for today...")
//        ref.get()
//            .addOnSuccessListener { snap ->
//                Log.d(TAG_FIREBASE, "Firestore document retrieval successful - Exists: ${snap.exists()}")
//
//                if (!snap.exists()) {
//                    Log.d(TAG_ATTENDANCE, "No existing record found for today")
//
//                    if (isInsideRadius) {
//                        getAddressFromLatLng(lat, lon) { address ->
//                            val markInData = mapOf(
//                                "markIn" to time,
//                                "isMarkedIn" to true,
//                                "isMarkedOut" to false,
//                                "markInLocation" to mapOf("lat" to lat, "lon" to lon),
//                                "markInAddress" to (address ?: "Unknown location"),
//                                "markInDistance" to distance,
//                                "timestamp" to System.currentTimeMillis(),
//                                "centerLat" to centerLat,
//                                "centerLon" to centerLon,
//                                "radius" to radius
//                            )
//
//                            ref.set(markInData)
//                                .addOnSuccessListener {
//                                    Log.i(
//                                        TAG_ATTENDANCE,
//                                        "MARK IN recorded successfully at $time (Distance: ${String.format("%.2f", distance)}m)"
//                                    )
//                                    showPopup("Marked IN at $time ")
//                                    //(${String.format("%.2f", distance)}m from center
//                                }
//                                .addOnFailureListener { e ->
//                                    Log.e(
//                                        TAG_ATTENDANCE,
//                                        "Failed to record MARK IN: ${e.message}",
//                                        e
//                                    )
//                                }
//                        }
//                    } else {
//                        Log.d(TAG_ATTENDANCE, "User outside radius ($distance m), cannot mark IN")
//                    }
//                    return@addOnSuccessListener
//                }
//
//                Log.d(TAG_ATTENDANCE, "Existing record found, checking status")
//                val isMarkedIn = snap.getBoolean("isMarkedIn") ?: false
//                val isMarkedOut = snap.getBoolean("isMarkedOut") ?: false
//
//                Log.d(TAG_ATTENDANCE, "Current status - isMarkedIn: $isMarkedIn, isMarkedOut: $isMarkedOut")
//
//                if (isMarkedIn && !isMarkedOut) {
//                    val lastUpdate = snap.getLong("lastMarkInLocationUpdate") ?: 0L
//                    val currentTime = System.currentTimeMillis()
//
//                    if (currentTime - lastUpdate >= 30000) {
//                        Log.d(TAG_ATTENDANCE, "Updating mark-in location (30 seconds elapsed)")
//
//                        val updateData = mutableMapOf<String, Any>(
//                            "markInLocation" to mapOf("lat" to lat, "lon" to lon),
//                            "markInDistance" to distance,
//                            "lastMarkInLocationUpdate" to currentTime
//                        )
//
//                        if (isInsideRadius) {
//                            getAddressFromLatLng(lat, lon) { address ->
//                                if (address != null) {
//                                    updateData["markInAddress"] = address
//                                }
//                                ref.update(updateData)
//                                    .addOnSuccessListener {
//                                        Log.d(TAG_ATTENDANCE, "Mark-in location updated (Distance: ${String.format("%.2f", distance)}m)")
//                                    }
//                                    .addOnFailureListener { e ->
//                                        Log.e(TAG_ATTENDANCE, "Failed to update mark-in location: ${e.message}", e)
//                                    }
//                            }
//                        } else {
//                            ref.update(updateData)
//                                .addOnSuccessListener {
//                                    Log.d(TAG_ATTENDANCE, "Mark-in location updated (Distance: ${String.format("%.2f", distance)}m)")
//                                }
//                                .addOnFailureListener { e ->
//                                    Log.e(TAG_ATTENDANCE, "Failed to update mark-in location: ${e.message}", e)
//                                }
//                        }
//                    }
//                }
//
//
//                if (isMarkedIn && !isMarkedOut) {
//                    if (isOutsideMarkOutRange) {
//                        getAddressFromLatLng(lat, lon) { address ->
//                            val updateData = mapOf(
//                                "markOut" to time,
//                                "isMarkedOut" to true,
//                                "markOutLocation" to mapOf("lat" to lat, "lon" to lon),
//                                "markOutAddress" to (address ?: "Unknown location"),
//                                "markOutDistance" to distance,
//                                "lastUpdate" to System.currentTimeMillis()
//                            )
//
//                            ref.update(updateData)
//                                .addOnSuccessListener {
//                                    Log.i(TAG_ATTENDANCE, "MARK OUT recorded successfully at $time (Distance: ${String.format("%.2f", distance)}m)")
//                                    showPopup("Marked OUT at $time ")
//                                    //(${String.format("%.2f", distance)}m from location)
//                                }
//                                .addOnFailureListener { e ->
//                                    Log.e(TAG_ATTENDANCE, "Failed to record MARK OUT: ${e.message}", e)
//                                }
//                        }
//                    } else if (isOutsideMarkOutRange) {
//                        Log.d(TAG_ATTENDANCE, "User too far away (>${markOutDistance}m) for mark-out")
//                    } else {
//                        Log.d(TAG_ATTENDANCE, "User still inside radius ($distance m), cannot mark out")
//                    }
//                } else if (isMarkedIn && isMarkedOut) {
//                    Log.d(TAG_ATTENDANCE, "Attendance already completed for today")
//                } else if (!isMarkedIn && isInsideRadius) {
//                    getAddressFromLatLng(lat, lon) { address ->
//                        val markInData = mapOf(
//                            "markIn" to time,
//                            "isMarkedIn" to true,
//                            "isMarkedOut" to false,
//                            "markInLocation" to mapOf("lat" to lat, "lon" to lon),
//                            "markInAddress" to (address ?: "Unknown location"),
//                            "markInDistance" to distance,
//                            "timestamp" to System.currentTimeMillis(),
//                            "lastMarkInLocationUpdate" to System.currentTimeMillis()
//                        )
//
//                        ref.set(markInData)
//                            .addOnSuccessListener {
//                                Log.i(
//                                    TAG_ATTENDANCE,
//                                    "Late MARK IN recorded at $time (Distance: ${String.format("%.2f", distance)}m)"
//                                )
//                                showPopup("Marked IN at $time (${String.format("%.2f", distance)}m from center)")
//                            }
//                            .addOnFailureListener { e ->
//                                Log.e(
//                                    TAG_ATTENDANCE,
//                                    "Failed to record late MARK IN: ${e.message}",
//                                    e
//                                )
//                            }
//                    }
//                }
//            }
//            .addOnFailureListener { e ->
//                Log.e(TAG_FIREBASE, "Failed to retrieve attendance document: ${e.message}", e)
//            }
//    }
//
//    @SuppressLint("MissingPermission")
//    private fun getAddressFromLatLng(
//        lat: Double,
//        lon: Double,
//        callback: (String?) -> Unit
//    ) {
//        try {
//            val geocoder = Geocoder(this, Locale.getDefault())
//
//            Thread {
//                try {
//                    val list = geocoder.getFromLocation(lat, lon, 1)
//
//                    if (!list.isNullOrEmpty()) {
//                        callback(list[0].getAddressLine(0))
//                    } else {
//                        callback(null)
//                    }
//                } catch (e: Exception) {
//                    callback(null)
//                }
//            }.start()
//
//        } catch (e: Exception) {
//            callback(null)
//        }
//    }
//
////
////
////    @RequiresApi(Build.VERSION_CODES.O)
////    private fun saveAttendance(coll: String, uid: String, lat: Double, lon: Double) {
////        Log.d(TAG_ATTENDANCE, "saveAttendance() - Collection: $coll, UID: $uid, Location: ($lat, $lon)")
////
////        val today = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
////        val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
////
////        Log.d(TAG_ATTENDANCE, "Date: $today, Time: $time")
////
////        val ref = fs.collection(coll).document(uid).collection("dates").document(today)
////        Log.d(TAG_FIREBASE, "Firestore reference: ${ref.path}")
////
////        val dist = FloatArray(1)
////        Location.distanceBetween(lat, lon, 31.3136, 75.5909, dist)
////        val inside = dist[0] <= 80
////        Log.d(TAG_ATTENDANCE, "Distance from target: ${dist[0]} meters, Inside geofence: $inside")
////
////        Log.d(TAG_FIREBASE, "Checking existing attendance record for today...")
////        ref.get()
////            .addOnSuccessListener { snap ->
////                Log.d(TAG_FIREBASE, "Firestore document retrieval successful - Exists: ${snap.exists()}")
////
////
////                if (!snap.exists()) {
////                    Log.d(TAG_ATTENDANCE, "No existing record found for today")
////                    if (!snap.exists()) {
////                        if (inside) {
////                            getAddressFromLatLng(lat, lon) { address ->
////                                val markInData = mapOf(
////                                    "markIn" to time,
////                                    "isMarkedIn" to true,
////                                    "isMarkedOut" to false,
////                                    "markInLocation" to mapOf("lat" to lat, "lon" to lon),
////                                    "markInAddress" to (address ?: "Unknown location"),
////                                    "timestamp" to System.currentTimeMillis()
////                                )
////
////                                ref.set(markInData)
////                                    .addOnSuccessListener {
////                                        Log.i(
////                                            TAG_ATTENDANCE,
////                                            "MARK IN recorded successfully at $time"
////                                        )
////                                        showPopup("Marked IN at $time")
////                                    }
////                                    .addOnFailureListener { e ->
////                                        Log.e(
////                                            TAG_ATTENDANCE,
////                                            "Failed to record MARK IN: ${e.message}",
////                                            e
////                                        )
////                                    }
////                            }
////                        }
////                    } else {
////                        Log.d(TAG_ATTENDANCE, "User outside geofence, no MARK IN recorded")
////                    }
////                    return@addOnSuccessListener
////                }
////
////                Log.d(TAG_ATTENDANCE, "Existing record found, checking MARK OUT conditions")
////
////                val isMarkedIn = snap.getBoolean("isMarkedIn") ?: false
////                val hasMarkedOut = snap.getBoolean("isMarkedOut") ?: false
////
////                Log.d(TAG_ATTENDANCE, "Full document data: ${snap.data}")
////                Log.d(TAG_ATTENDANCE, "Current status - isMarkedIn: $isMarkedIn, hasMarkedOut: $hasMarkedOut, insideGeofence: $inside")
////
////
////                val canMarkOut = isMarkedIn && !hasMarkedOut && !inside
////
////                Log.d(TAG_ATTENDANCE, "MARK OUT condition check:")
////                Log.d(TAG_ATTENDANCE, "   - User has marked IN: $isMarkedIn ${if (!isMarkedIn) "no" else "true"}")
////                Log.d(TAG_ATTENDANCE, "   - User has NOT marked OUT: ${!hasMarkedOut} ${if (hasMarkedOut) "no" else "true"}")
////                Log.d(TAG_ATTENDANCE, "   - User is OUTSIDE geofence: ${!inside} ${if (inside) "no" else "true"}")
////                Log.d(TAG_ATTENDANCE, "   - CAN mark OUT: $canMarkOut")
////
////                if (canMarkOut) {
////                    getAddressFromLatLng(lat, lon) { address ->
////                        val updateData = mapOf(
////                            "markOut" to time,
////                            "isMarkedOut" to true,
////                            "markOutLocation" to mapOf("lat" to lat, "lon" to lon),
////                            "markOutAddress" to (address ?: "Unknown location"),
////                            "lastUpdate" to System.currentTimeMillis()
////                        )
////
////                        ref.update(updateData)
////                            .addOnSuccessListener {
////                                showPopup("Marked OUT at $time")
////                            }
////
////
////                            .addOnFailureListener { e ->
////                                Log.e(TAG_ATTENDANCE, " Failed to record MARK OUT: ${e.message}", e)
////                            }
////                    }
////
////                } else {
////                    Log.d(TAG_ATTENDANCE, " MARK OUT conditions not satisfied")
////
////                    if (isMarkedIn && hasMarkedOut) {
////                        Log.d(TAG_ATTENDANCE, "User has already completed attendance for today")
////                    } else if (!isMarkedIn) {
////                        Log.d(TAG_ATTENDANCE, "User hasn't marked IN yet")
////                    } else if (inside) {
////                        Log.d(TAG_ATTENDANCE, "User is still inside geofence")
////                    }
////                }
////            }
////            .addOnFailureListener { e ->
////                Log.e(TAG_FIREBASE, " Failed to retrieve attendance document: ${e.message}", e)
////            }
////    }
//    private fun showPopup(msg: String) {
//        Log.d(TAG_NOTIFICATION, "showPopup() - Creating attendance notification: $msg")
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
//                Log.w(TAG_NOTIFICATION, "Notification permission not granted, cannot show popup")
//                return
//            }
//            Log.d(TAG_NOTIFICATION, "Notification permission granted for Android 13+")
//        }
//
//        try {
//            val notificationManager = getSystemService(NotificationManager::class.java)
//
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                Log.d(TAG_NOTIFICATION, "Creating high priority notification channel")
//                val channel = NotificationChannel(
//                    "ATTN_POP",
//                    "Attendance Alerts",
//                    NotificationManager.IMPORTANCE_HIGH
//                ).apply {
//                    description = "Important attendance status updates"
//                    enableVibration(true)
//                    setShowBadge(true)
//                }
//                notificationManager.createNotificationChannel(channel)
//                Log.d(TAG_NOTIFICATION, "High priority notification channel created")
//            }
//
//            val notificationId = (2000..9999).random()
//            Log.d(TAG_NOTIFICATION, "Generated notification ID: $notificationId")
//
//            val notif = NotificationCompat.Builder(this, "ATTN_POP")
//                .setSmallIcon(R.mipmap.ic_launcher_round)
//                .setContentTitle("Attendance Update")
//                .setContentText(msg)
//                .setPriority(NotificationCompat.PRIORITY_HIGH)
//                .setAutoCancel(true)
//                .build()
//
//            NotificationManagerCompat.from(this).notify(notificationId, notif)
//            Log.i(TAG_NOTIFICATION, "Attendance notification shown with ID: $notificationId - Message: $msg")
//
//        } catch (e: Exception) {
//            Log.e(TAG_NOTIFICATION, "Error showing attendance notification: ${e.message}", e)
//        }
//    }
//
//}



import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import java.util.*


class AttendanceService : Service() {
    private lateinit var fused: FusedLocationProviderClient
    private val fs = FirebaseFirestore.getInstance()
    private val prefs by lazy { getSharedPreferences("attendance_prefs", MODE_PRIVATE) }
    private val TAG = "AttendanceService"
    private var retryCount = 0
    private val MAX_RETRIES = 3
    private val TAG_LIFECYCLE = "ATTN_SERVICE_LIFECYCLE"
    private val TAG_LOCATION = "ATTN_SERVICE_LOCATION"
    private val TAG_FIREBASE = "ATTN_SERVICE_FIREBASE"
    private val TAG_NOTIFICATION = "ATTN_SERVICE_NOTIFICATION"
    private val TAG_ATTENDANCE = "ATTN_SERVICE_ATTENDANCE"
    private val TAG_PREFS = "ATTN_SERVICE_PREFS"
    private var lastAttendanceProcessingTime: Long = 0
    private val ATTENDANCE_DEBOUNCE_INTERVAL = 30000L
    private val LOCATION_PENDING_INTENT_REQUEST_CODE = 1001
    var centerLat: Double? = null
    var centerLon: Double? = null

    companion object {
        const val LOCATION_UPDATE_INTERVAL = 15000L
        const val FASTEST_UPDATE_INTERVAL = 10000L
        const val MAX_WAIT_TIME = 30000L
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.d(TAG_LIFECYCLE, "onBind() called - Service bound")
        return null
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG_LIFECYCLE, "onCreate() - Service instance created")

        try {
            fused = LocationServices.getFusedLocationProviderClient(this)
            Log.d(TAG_LIFECYCLE, "FusedLocationProviderClient initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG_LIFECYCLE, "Failed to initialize FusedLocationProviderClient: ${e.message}", e)
        }

        Log.d(TAG_LIFECYCLE, "Service onCreate completed")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG_LIFECYCLE, "onStartCommand() called - flags: $flags, startId: $startId")
        Log.d(TAG_LIFECYCLE, "Intent action: ${intent?.action}, extras: ${intent?.extras}")

        try {
            Log.d(TAG_NOTIFICATION, "Creating foreground notification...")
            createForegroundNotification()
            Log.d(TAG_NOTIFICATION, "Foreground notification created successfully")

            if (intent?.action == "ACTION_LOCATION_UPDATE" && intent.hasExtra("lat") && intent.hasExtra("lon")) {
                val lat = intent.getDoubleExtra("lat", 0.0)
                val lon = intent.getDoubleExtra("lon", 0.0)
                Log.d(TAG_LOCATION, " Location data received in intent - lat: $lat, lon: $lon")

                if (lat != 0.0 && lon != 0.0) {
                    Log.d(TAG_ATTENDANCE, "Processing attendance with received location")
                    handleAttendance(lat, lon)
                } else {
                    Log.w(TAG_LOCATION, "Invalid location coordinates received: lat=$lat, lon=$lon")
                }
            } else {
                Log.d(TAG_LOCATION, "No location data in intent, starting location updates")
            }

            Log.d(TAG_LOCATION, "Requesting location updates via PendingIntent...")
            requestLocationUpdatesWithPI()
            Log.d(TAG_LOCATION, "Location updates requested successfully")

            getLastKnownLocationForDebugging()

        } catch (e: Exception) {
            Log.e(TAG_LIFECYCLE, "Error in onStartCommand: ${e.message}", e)
        }

        Log.d(TAG_LIFECYCLE, "Returning START_STICKY - service will be restarted if killed")
        return START_STICKY
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingPermission")
    private fun getLastKnownLocationForDebugging() {
        Log.d(TAG_LOCATION, "Getting last known location for debugging...")

        fused.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    Log.d(TAG_LOCATION, " Last known location found:")
                    Log.d(TAG_LOCATION, " Lat: ${location.latitude}, Lon: ${location.longitude}")
                    Log.d(TAG_LOCATION, "Accuracy: ${location.accuracy}m")
                    Log.d(TAG_LOCATION, "Time: ${Date(location.time)}")
                    Log.d(TAG_LOCATION, "Provider: ${location.provider}")

                    handleAttendance(location.latitude, location.longitude)
                } else {
                    Log.w(TAG_LOCATION, "Last known location is null - no recent location available")
                    Log.w(TAG_LOCATION, "This is normal if no apps have requested location recently")
                    Log.w(TAG_LOCATION, "Location updates should start soon via PendingIntent")
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG_LOCATION, " Failed to get last known location: ${e.message}", e)
            }
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationUpdatesWithPI() {
        Log.d(TAG_LOCATION, "requestLocationUpdatesWithPI() - Configuring HIGH-PRECISION location request")

        try {
            // For Google Maps-like accuracy, use these settings:
            val req = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, LOCATION_UPDATE_INTERVAL)
                .setMinUpdateIntervalMillis(FASTEST_UPDATE_INTERVAL)
                .setMaxUpdateDelayMillis(MAX_WAIT_TIME)
                .setWaitForAccurateLocation(true)
                // CRITICAL: Enable passive location updates for better accuracy
                .setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
                // OPTIONAL: For even better accuracy on movement (Android 12+)
                .apply {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        setMinUpdateDistanceMeters(1.0f) // Update every 1 meter movement
                    }
                }
                .build()

            Log.d(TAG_LOCATION, "HIGH-PRECISION Location request built - " +
                    "Priority: ${req.priority}, " +
                    "Interval: ${req.intervalMillis}ms, " +
                    "MinInterval: ${req.minUpdateIntervalMillis}ms, " +
                    "MaxDelay: ${req.maxUpdateDelayMillis}ms, " +
                    "Granularity: ${req.granularity}")

            val passiveReq = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L)
                .setMinUpdateIntervalMillis(1000L)
                .setWaitForAccurateLocation(true)
                .setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
                .build()

            Log.d(TAG_LOCATION, "Building PendingIntent for location updates...")
            val pi = buildPendingIntent()
            Log.d(TAG_LOCATION, "PendingIntent built successfully")

            // Request BOTH standard and passive updates
            Log.d(TAG_LOCATION, "Requesting HIGH-PRECISION location updates...")
            fused.requestLocationUpdates(req, pi)
                .addOnSuccessListener {
                    Log.i(TAG_LOCATION, "✅ Primary location updates requested successfully")

                    fused.requestLocationUpdates(passiveReq, pi)
                        .addOnSuccessListener {
                            Log.i(TAG_LOCATION, "✅ Passive location updates added for movement detection")
                        }
                        .addOnFailureListener { e ->
                            Log.w(TAG_LOCATION, "Passive updates failed (optional): ${e.message}")
                        }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG_LOCATION, "❌ Failed to request location updates: ${e.message}", e)
                    if (e is SecurityException) {
                        Log.e(TAG_LOCATION, "Location permission issue, cannot retry")
                        showPermissionNotification()
                    } else {
                        retryLocationUpdatesWithBackoff()
                    }
                }

        } catch (e: SecurityException) {
            Log.e(TAG_LOCATION, "SecurityException - Location permission not granted: ${e.message}", e)
            showPermissionNotification()
        } catch (e: Exception) {
            Log.e(TAG_LOCATION, "Error requesting location updates: ${e.message}", e)
        }
    }



    private fun retryLocationUpdatesWithBackoff() {
        if (retryCount >= MAX_RETRIES) {
            Log.w(TAG_LOCATION, "Max retries reached, stopping retry attempts")
            retryCount = 0
            return
        }

        retryCount++
        val delay = 30000L * retryCount

        Log.d(TAG_LOCATION, "Scheduling retry $retryCount/$MAX_RETRIES in ${delay/1000}s...")

        Handler(Looper.getMainLooper()).postDelayed({
            Log.d(TAG_LOCATION, "Retrying location updates (attempt $retryCount)...")
            requestLocationUpdatesWithPI()
        }, delay)
    }

    private fun retryLocationUpdates() {
        Log.d(TAG_LOCATION, "Scheduling retry of location updates in 30 seconds...")

        Handler(Looper.getMainLooper()).postDelayed({
            Log.d(TAG_LOCATION, "Retrying location updates...")
            requestLocationUpdatesWithPI()
        }, 30000)
    }



//    @SuppressLint("MissingPermission")
//    private fun checkLocationSettingsAndPrompt() {
//        val builder = LocationSettingsRequest.Builder()
//            .addLocationRequest(
//                LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L)
//                    .setMinUpdateIntervalMillis(1000L)
//                    .setWaitForAccurateLocation(true)
//                    .build()
//            )
//            .setAlwaysShow(true) // Important for high accuracy
//
//        val client: SettingsClient = LocationServices.getSettingsClient(this)
//        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())
//
//        task.addOnSuccessListener { response ->
//            val locationSettingsStates = response.locationSettingsStates
//            Log.d(TAG_LOCATION, "Location settings check passed")
//        }
//
//        task.addOnFailureListener { exception ->
//            if (exception is ResolvableApiException) {
//                try {
//                    exception.startResolutionForResult(
//
//                        Activity,
//                        1002
//                    )
//                } catch (sendEx: IntentSender.SendIntentException) {
//                    Log.e(TAG_LOCATION, "Error showing location settings dialog", sendEx)
//                }
//            }
//        }
//    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showPermissionNotification() {
        Log.d(TAG_NOTIFICATION, "Showing location permission notification")

        val notification = NotificationCompat.Builder(this, "ATTN_CHANNEL")
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle("Location Permission Required")
            .setContentText("Please grant location permission for attendance tracking")
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(this).notify(1003, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG_LIFECYCLE, "onDestroy() - Service is being destroyed")
        Log.w(TAG_LIFECYCLE, "Service destroyed but PendingIntent location updates may continue")
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        Log.w(TAG_LIFECYCLE, "onTaskRemoved() - App task removed from recent list")
        Log.d(TAG_LIFECYCLE, "Root intent: ${rootIntent?.action}")
        Log.i(TAG_LIFECYCLE, "Service should continue running due to START_STICKY")
    }

    private fun getUid(): String? {
        Log.d(TAG_PREFS, "getUid() - Retrieving user ID")

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            Log.d(TAG_PREFS, "User ID from Firebase Auth: ${currentUser.uid}")
            return currentUser.uid
        }

        val savedUid = prefs.getString("uid", null)
        if (savedUid != null) {
            Log.d(TAG_PREFS, "User ID from SharedPreferences: $savedUid")
        } else {
            Log.w(TAG_PREFS, "No user ID found in Firebase Auth or SharedPreferences")
        }

        return savedUid
    }

    private fun saveUid(uid: String) {
        Log.d(TAG_PREFS, "saveUid() - Saving user ID: $uid")
        try {
            prefs.edit().putString("uid", uid).apply()
            Log.d(TAG_PREFS, "User ID saved successfully to SharedPreferences")
        } catch (e: Exception) {
            Log.e(TAG_PREFS, "Failed to save user ID to SharedPreferences: ${e.message}", e)
        }
    }

    private fun createForegroundNotification() {
        Log.d(TAG_NOTIFICATION, "createForegroundNotification() - Starting notification setup")

        try {
            val notificationManager = getSystemService(NotificationManager::class.java)
            Log.d(TAG_NOTIFICATION, "Notification manager obtained")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Log.d(TAG_NOTIFICATION, "Creating notification channel for Android O+")
                val channel = NotificationChannel(
                    "ATTN_CHANNEL",
                    "Attendance Tracking",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Background location tracking for attendance"
                }

                notificationManager.createNotificationChannel(channel)
                Log.d(TAG_NOTIFICATION, "Notification channel created: ATTN_CHANNEL")
            } else {
                Log.d(TAG_NOTIFICATION, "Android version < O, no channel creation needed")
            }

            val notif = NotificationCompat.Builder(this, "ATTN_CHANNEL")
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle("Attendance Running")
                .setContentText("Tracking location…")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build()

            Log.d(TAG_NOTIFICATION, "Notification built, starting foreground service...")
            startForeground(1001,notif)
            Log.d(TAG_NOTIFICATION, "Foreground service started with notification ID: 1001")

        } catch (e: Exception) {
            Log.e(TAG_NOTIFICATION, "Error creating foreground notification: ${e.message}", e)
            throw e
        }
    }

    private fun buildPendingIntent(): PendingIntent {
        val intent = Intent(this, LocationUpdatesReceiver::class.java).apply {
            action = "LOCATION_UPDATE"
        }

        return PendingIntent.getBroadcast(
            this,
            LOCATION_PENDING_INTENT_REQUEST_CODE,
            intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun handleAttendance(lat: Double, lon: Double) {
        Log.d(TAG_ATTENDANCE, "handleAttendance() - Processing attendance for location: ($lat, $lon)")
        val now = System.currentTimeMillis()

        if (now - lastAttendanceProcessingTime < ATTENDANCE_DEBOUNCE_INTERVAL) {
            Log.d(TAG_ATTENDANCE, "Debouncing attendance processing. Too soon since last: ${now - lastAttendanceProcessingTime}ms")
            return
        }

        lastAttendanceProcessingTime = now

        val uid = getUid()
        if (uid == null) {
            Log.e(TAG_ATTENDANCE, "Cannot handle attendance - User ID is null")
            return
        }

        Log.d(TAG_ATTENDANCE, "User ID obtained: $uid, saving to preferences...")
        saveUid(uid)

        Log.d(TAG_FIREBASE, "Fetching user role from Firestore for UID: $uid")
        fs.collection("users").document(uid).get()
            .addOnSuccessListener { snap ->
                if (snap.exists()) {
                    val role = snap.getString("role") ?: "Student"
                    Log.d(TAG_FIREBASE, "User role retrieved: $role")

                    val coll = if (role == "Student") "student_attendance" else "staff_attendance"
                    Log.d(TAG_ATTENDANCE, "Using collection: $coll for role: $role")

                    saveAttendance(coll, uid, lat, lon)
                } else {
                    Log.e(TAG_FIREBASE, "User document does not exist for UID: $uid")
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG_FIREBASE, "Failed to fetch user role from Firestore: ${e.message}", e)
            }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun saveAttendance(coll: String, uid: String, lat: Double, lon: Double) {
        Log.d(TAG_ATTENDANCE, "saveAttendance() - Collection: $coll, UID: $uid, Location: ($lat, $lon)")

        val today = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
        val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())

        Log.d(TAG_ATTENDANCE, "Date: $today, Time: $time")

        val ref = fs.collection(coll).document(uid).collection("dates").document(today)
        Log.d(TAG_FIREBASE, "Firestore reference: ${ref.path}")

        fetchAreaConfiguration { areaConfig ->
            if (areaConfig == null) {
                Log.e(TAG_ATTENDANCE, "No area configuration found, using default radius logic")
                useRadiusFallbackLogic(ref, coll, uid, lat, lon, today, time)
                return@fetchAreaConfiguration
            }

            val areaType = areaConfig.getString("type") ?: "rectangle"
            val markOutDistance = areaConfig.getDouble("markOutDistance") ?: 150.0

            var isInsideArea = false
            var distanceToEdge = 0.0

            when (areaType) {
                "rectangle" -> {
                    val northEastLat = areaConfig.getDouble("northEast.lat") ?: 0.0
                    val northEastLon = areaConfig.getDouble("northEast.lon") ?: 0.0
                    val southWestLat = areaConfig.getDouble("southWest.lat") ?: 0.0
                    val southWestLon = areaConfig.getDouble("southWest.lon") ?: 0.0

                    if (northEastLat != 0.0 && southWestLat != 0.0) {
                        val area = PolygonGeofenceUtils.RectangularArea(
                            LatLng(northEastLat, northEastLon),
                            LatLng(southWestLat, southWestLon)
                        )

                        isInsideArea = PolygonGeofenceUtils.isInsideRectangularArea(lat, lon, area)
                        distanceToEdge = PolygonGeofenceUtils.distanceToAreaEdge(lat, lon, area)
                    }
                }

                "polygon" -> {
                    val pointsData = areaConfig.get("points") as? List<Map<String, Any>>
                    val points = pointsData?.map { point ->
                        LatLng(
                            point["lat"] as? Double ?: 31.3137,
                            point["lon"] as? Double ?: 75.5910
                        )
                    } ?: emptyList()

                    if (points.size >= 3) {
                        isInsideArea = PolygonGeofenceUtils.isInsidePolygon(lat, lon, points)
                        distanceToEdge =
                            PolygonGeofenceUtils.calculateDistanceToPolygonEdge(lat, lon, points)
                    }
                }
                //     val centerLat = 31.3138
                //        val centerLon = 75.5910
                "circle" -> {
                    val centerLat = areaConfig.getDouble("centerLat") ?: 31.3138
                    val centerLon = areaConfig.getDouble("centerLon") ?: 75.5910
                    val radius = areaConfig.getDouble("radius") ?: 30.0

                    val dist = FloatArray(1)
                    Location.distanceBetween(lat, lon, centerLat, centerLon, dist)
                    distanceToEdge = dist[0].toDouble()
                    isInsideArea = distanceToEdge <= radius
                }
            }

            Log.d(TAG_ATTENDANCE, "Area check - Type: $areaType, Inside: $isInsideArea, Distance to edge: ${String.format("%.2f", distanceToEdge)}m")

            val isWithinMarkOutRange = !isInsideArea && distanceToEdge <= markOutDistance
            val isOutsideMarkOutRange = !isInsideArea && distanceToEdge > markOutDistance

            processAttendanceWithAreaLogic(
                ref = ref,
                uid = uid,
                lat = lat,
                lon = lon,
                today = today,
                time = time,
                isInsideArea = isInsideArea,
                isWithinMarkOutRange = isWithinMarkOutRange,
                isOutsideMarkOutRange = isOutsideMarkOutRange,
                distanceToEdge = distanceToEdge,
                areaType = areaType
            )
        }
    }

    private fun fetchAreaConfiguration(callback: (DocumentSnapshot?) -> Unit) {
        fs.collection("attendance_config").document("area_config").get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    Log.d(TAG_ATTENDANCE, "Area configuration loaded successfully")
                    callback(snapshot)
                } else {
                    Log.w(TAG_ATTENDANCE, "No area configuration found")
                    callback(null)
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG_ATTENDANCE, "Failed to fetch area configuration: ${e.message}", e)
                callback(null)
            }
    }
    private fun getLocationFromFirestore(onSuccess: () -> Unit) {

        FirebaseFirestore.getInstance()
            .collection("centerloc")
            .document("location")
            .get()
            .addOnSuccessListener { document ->

                if (document.exists()) {
                    centerLat = document.getDouble("latitude")
                    centerLon = document.getDouble("longitude")

                    Log.d("LOCATION", "Lat: $centerLat, Lng: $centerLon")

                    if (centerLat != null && centerLon != null) {
                        onSuccess()
                    }
                }
            }
            .addOnFailureListener {
                Log.e("LOCATION", "Error fetching location", it)
            }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun useRadiusFallbackLogic(
        ref: com.google.firebase.firestore.DocumentReference,
        coll: String,
        uid: String,
        lat: Double,
        lon: Double,
        today: String,
        time: String
    ) {
        getLocationFromFirestore {

            val radius = 30.0
            val markOutDistance = 150.0

            val dist = FloatArray(1)
            Location.distanceBetween(
                lat,
                lon,
                centerLat?.toDouble() ?: 0.0,
                centerLon?.toDouble() ?: 0.0,
                dist
            )
            val distance = dist[0].toDouble()

            val isInsideRadius = distance <= radius
            val isWithinMarkOutRange = distance >= radius && distance <= markOutDistance
            val isOutsideMarkOutRange = distance > markOutDistance

            processAttendanceWithAreaLogic(
                ref = ref,
                uid = uid,
                lat = lat,
                lon = lon,
                today = today,
                time = time,
                isInsideArea = isInsideRadius,
                isWithinMarkOutRange = isWithinMarkOutRange,
                isOutsideMarkOutRange = isOutsideMarkOutRange,
                distanceToEdge = distance,
                areaType = "Polygon"
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun processAttendanceWithAreaLogic(
        ref: com.google.firebase.firestore.DocumentReference,
        uid: String,
        lat: Double,
        lon: Double,
        today: String,
        time: String,
        isInsideArea: Boolean,
        isWithinMarkOutRange: Boolean,
        isOutsideMarkOutRange: Boolean,
        distanceToEdge: Double,
        areaType: String
    ) {
        Log.d(TAG_FIREBASE, "Checking existing attendance record for today...")
        ref.get()
            .addOnSuccessListener { snap ->
                Log.d(TAG_FIREBASE, "Firestore document retrieval successful - Exists: ${snap.exists()}")

                if (!snap.exists()) {
                    Log.d(TAG_ATTENDANCE, "No existing record found for today")

                    if (isInsideArea) {
                        getAddressFromLatLng(lat, lon) { address ->
                            val markInData = mapOf(
                                "markIn" to time,
                                "isMarkedIn" to true,
                                "isMarkedOut" to false,
                                "markInLocation" to mapOf("lat" to lat, "lon" to lon),
                                "markInAddress" to (address ?: "Current location"),
                                "distanceToEdge" to distanceToEdge,
                                "isInsideArea" to true,
                                "areaType" to areaType,
                                "timestamp" to System.currentTimeMillis(),
                                "lastMarkInLocationUpdate" to System.currentTimeMillis()
                            )

                            ref.set(markInData)
                                .addOnSuccessListener {
                                    Log.i(TAG_ATTENDANCE, "MARK IN recorded (Inside area)")
                                    showPopup("Marked IN at $time")
                                }
                                .addOnFailureListener { e ->
                                    Log.e(TAG_ATTENDANCE, "Failed to record MARK IN: ${e.message}", e)
                                }
                        }
                    } else {
                        Log.d(TAG_ATTENDANCE, "User outside area, cannot mark IN")
                    }
                    return@addOnSuccessListener
                }

                val isMarkedIn = snap.getBoolean("isMarkedIn") ?: false
                val isMarkedOut = snap.getBoolean("isMarkedOut") ?: false

                Log.d(TAG_ATTENDANCE, "Current status - isMarkedIn: $isMarkedIn, isMarkedOut: $isMarkedOut")
                if (isMarkedIn && !isMarkedOut  && isWithinMarkOutRange || isInsideArea) {
                    val lastUpdate = snap.getLong("lastLocationUpdate") ?: 0L
                    val currentTime = System.currentTimeMillis()
                    Log.d(TAG_ATTENDANCE, "outside condition)")

                    if (currentTime - lastUpdate >= 30000) {
                        Log.d(TAG_ATTENDANCE, "Updating location (30 seconds elapsed)")

                        val updateData = mutableMapOf<String, Any>(
                            "markInLocation" to mapOf("lat" to lat, "lon" to lon),
                            "distanceToEdge" to distanceToEdge,
                            "lastMarkInLocationUpdate" to currentTime,
                        )

                        ref.update(updateData)
                            .addOnSuccessListener {
                                Log.d(TAG_ATTENDANCE, "Location updated (Distance to edge: ${String.format("%.2f", distanceToEdge)}m)")
                            }
                            .addOnFailureListener { e ->
                                Log.e(TAG_ATTENDANCE, "Failed to update location: ${e.message}", e)
                            }
                    }
                }

                if (isMarkedIn && !isMarkedOut) {
                    if (isOutsideMarkOutRange) {
                        getAddressFromLatLng(lat, lon) { address ->
                            val updateData = mapOf(
                                "markOut" to time,
                                "isMarkedOut" to true,
                                "markOutLocation" to mapOf("lat" to lat, "lon" to lon),
                                "markOutAddress" to (address ?: "Current location"),
                                "distanceToEdgeAtMarkOut" to distanceToEdge,
                                "wasInsideArea" to false,
                                "lastUpdate" to System.currentTimeMillis()
                            )

                            ref.update(updateData)
                                .addOnSuccessListener {
                                    Log.i(TAG_ATTENDANCE, "MARK OUT recorded (Left area)")
                                    showPopup("Marked OUT at $time")
                                }
                                .addOnFailureListener { e ->
                                    Log.e(TAG_ATTENDANCE, "Failed to record MARK OUT: ${e.message}", e)
                                }
                        }
                    } else if (isOutsideMarkOutRange) {
                        Log.d(TAG_ATTENDANCE, "User too far away for mark-out: ${String.format("%.2f", distanceToEdge)}m")
                    } else {
                        Log.d(TAG_ATTENDANCE, "User still inside area ($distanceToEdge m), cannot mark out")
                    }
                } else if (isMarkedIn && isMarkedOut) {
                    Log.d(TAG_ATTENDANCE, "Attendance already completed for today")
                } else if (!isMarkedIn && isInsideArea) {
                    getAddressFromLatLng(lat, lon) { address ->
                        val markInData = mapOf(
                            "markIn" to time,
                            "isMarkedIn" to true,
                            "isMarkedOut" to false,
                            "markInLocation" to mapOf("lat" to lat, "lon" to lon),
                            "markInAddress" to (address ?: "Current location"),
                            "distanceToEdge" to distanceToEdge,
                            "isInsideArea" to true,
                            "areaType" to areaType,
                            "timestamp" to System.currentTimeMillis(),
                            "lastLocationUpdate" to System.currentTimeMillis()
                        )

                        ref.set(markInData)
                            .addOnSuccessListener {
                                Log.i(TAG_ATTENDANCE, "Late MARK IN recorded")
                                showPopup("Marked IN at $time")
                            }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG_FIREBASE, "Failed to retrieve attendance document: ${e.message}", e)
            }
    }

    @SuppressLint("MissingPermission")
    private fun getAddressFromLatLng(
        lat: Double,
        lon: Double,
        callback: (String?) -> Unit
    ) {
        try {
            val geocoder = Geocoder(this, Locale.getDefault())

            Thread {
                try {
                    val list = geocoder.getFromLocation(lat, lon, 1)

                    if (!list.isNullOrEmpty()) {
                        callback(list[0].getAddressLine(0))
                    } else {
                        callback(null)
                    }
                } catch (e: Exception) {
                    callback(null)
                }
            }.start()

        } catch (e: Exception) {
            callback(null)
        }
    }

    private fun showPopup(msg: String) {
        Log.d(TAG_NOTIFICATION, "showPopup() - Creating attendance notification: $msg")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG_NOTIFICATION, "Notification permission not granted, cannot show popup")
                return
            }
            Log.d(TAG_NOTIFICATION, "Notification permission granted for Android 13+")
        }

        try {
            val notificationManager = getSystemService(NotificationManager::class.java)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Log.d(TAG_NOTIFICATION, "Creating high priority notification channel")
                val channel = NotificationChannel(
                    "ATTN_POP",
                    "Attendance Alerts",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Important attendance status updates"
                    enableVibration(true)
                    setShowBadge(true)
                }
                notificationManager.createNotificationChannel(channel)
                Log.d(TAG_NOTIFICATION, "High priority notification channel created")
            }

            val notificationId = (2000..9999).random()
            Log.d(TAG_NOTIFICATION, "Generated notification ID: $notificationId")

            val notif = NotificationCompat.Builder(this, "ATTN_POP")
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle("Attendance Update")
                .setContentText(msg)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build()

            NotificationManagerCompat.from(this).notify(notificationId, notif)
            Log.i(TAG_NOTIFICATION, "Attendance notification shown with ID: $notificationId - Message: $msg")

        } catch (e: Exception) {
            Log.e(TAG_NOTIFICATION, "Error showing attendance notification: ${e.message}", e)
        }
    }
}

object PolygonGeofenceUtils {

    data class RectangularArea(
        val northEast: LatLng,
        val southWest: LatLng,
        val name: String = "Attendance Area"
    )

    data class PolygonArea(
        val points: List<LatLng>,
        val name: String = "Attendance Area"
    )

    fun isInsideRectangularArea(
        userLat: Double,
        userLon: Double,
        areaBounds: RectangularArea
    ): Boolean {
        return userLat >= areaBounds.southWest.latitude &&
                userLat <= areaBounds.northEast.latitude &&
                userLon >= areaBounds.southWest.longitude &&
                userLon <= areaBounds.northEast.longitude
    }

    fun isInsidePolygon(
        userLat: Double,
        userLon: Double,
        polygonPoints: List<LatLng>
    ): Boolean {
        if (polygonPoints.size < 3) return false

        var crossings = 0
        val n = polygonPoints.size

        for (i in 0 until n) {
            val current = polygonPoints[i]
            val next = polygonPoints[(i + 1) % n]

            // Ray casting algorithm
            if (((current.latitude > userLat) != (next.latitude > userLat)) &&
                (userLon < (next.longitude - current.longitude) *
                        (userLat - current.latitude) / (next.latitude - current.latitude) + current.longitude)) {
                crossings++
            }
        }

        return crossings % 2 == 1
    }

    fun distanceToAreaEdge(
        userLat: Double,
        userLon: Double,
        areaBounds: RectangularArea
    ): Double {
        val distances = listOf(
            distanceToLatitudeLine(userLat, userLon, areaBounds.northEast.latitude),
            distanceToLatitudeLine(userLat, userLon, areaBounds.southWest.latitude),
            distanceToLongitudeLine(userLat, userLon, areaBounds.northEast.longitude),
            distanceToLongitudeLine(userLat, userLon, areaBounds.southWest.longitude)
        )

        return distances.minOrNull() ?: Double.MAX_VALUE
    }

    fun calculateDistanceToPolygonEdge(lat: Double, lon: Double, polygonPoints: List<LatLng>): Double {
        var minDistance = Double.MAX_VALUE

        val n = polygonPoints.size
        for (i in 0 until n) {
            val current = polygonPoints[i]
            val next = polygonPoints[(i + 1) % n]

            // Calculate distance to line segment
            val distance = distanceToLineSegment(lat, lon, current, next)
            minDistance = minOf(minDistance, distance)
        }

        return minDistance
    }

    private fun distanceToLatitudeLine(userLat: Double, userLon: Double, targetLat: Double): Double {
        val results = FloatArray(1)
        Location.distanceBetween(userLat, userLon, targetLat, userLon, results)
        return results[0].toDouble()
    }

    private fun distanceToLongitudeLine(userLat: Double, userLon: Double, targetLon: Double): Double {
        val results = FloatArray(1)
        Location.distanceBetween(userLat, userLon, userLat, targetLon, results)
        return results[0].toDouble()
    }

    private fun distanceToLineSegment(
        pointLat: Double,
        pointLon: Double,
        lineStart: LatLng,
        lineEnd: LatLng
    ): Double {
        val results = FloatArray(1)

        Location.distanceBetween(pointLat, pointLon, lineStart.latitude, lineStart.longitude, results)
        var minDistance = results[0].toDouble()

        Location.distanceBetween(pointLat, pointLon, lineEnd.latitude, lineEnd.longitude, results)
        minDistance = minOf(minDistance, results[0].toDouble())

        return minDistance
    }
}
