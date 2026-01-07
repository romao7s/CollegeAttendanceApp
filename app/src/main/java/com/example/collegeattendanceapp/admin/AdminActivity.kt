package com.example.collegeattendanceapp.admin

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.collegeattendanceapp.R
import com.example.collegeattendanceapp.databinding.ActivityAdminBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolygonOptions

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicInteger

class AdminActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    lateinit var binding: ActivityAdminBinding
    private val LOCATION_PERMISSION = 101
    private val TAG = "AdminActivity"
    private val userMarkers = mutableMapOf<String, Marker>()
    private var autoRefreshHandler: Handler? = null
    private var autoRefreshRunnable: Runnable? = null
    private var isAutoRefreshActive = false
    private val AUTO_REFRESH_INTERVAL = 30000L
    var centerLat: Double? = null
    var centerLon: Double? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d(TAG, "onCreate: AdminActivity started")

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
//        binding.main.setOnRefreshListener {
//            binding.main.isRefreshing = false
//        }
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        Log.d(TAG, "onCreate: Map fragment initialized")
        mapFragment.getMapAsync(this)
        val today = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
        //loadTodayMarkIn()
//
//        findViewById<Chip>(R.id.chipStudent).setOnClickListener {
//            Log.d(TAG, "Student Chip Clicked → Loading student attendance")
//            // db.collection("student_attendance")
//            //                .document(userId)
//            //                .collection("dates")
//            //                .document(dateKey)
//            //                .get()
//
//
//            loadAttendance("student_attendance")
//        }
//
//        findViewById<Chip>(R.id.chipStaff).setOnClickListener {
//            Log.d(TAG, "Staff Chip Clicked → Loading staff attendance")
//            loadAttendance("staff_attendance")
//
//        }

        binding.logoutIcon.setOnClickListener {
//            androidx.appcompat.app.AlertDialog.Builder(this)
//                .setTitle("Logout")
//                .setMessage("Are you sure you want to logout?")
//                .setPositiveButton("Yes") { _, _ ->
//                    val serviceIntent = Intent(this, AttendanceService::class.java)
//                    stopService(serviceIntent)
//                    val prefs = getSharedPreferences("admin_prefs", Context.MODE_PRIVATE)
//                    prefs.edit().putBoolean("isAdminLoggedIn", false).apply()
//
//                    val intent = Intent(this, MainActivity::class.java)
//                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//                    startActivity(intent)
//                }
//                .setNegativeButton("No", null)
//                .show()
//
//        }
            this.finish()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        Log.d(TAG, "onMapReady: Google Map Ready")
        map = googleMap
        requestLocationPermission()
        map.uiSettings.isZoomControlsEnabled = true
        map.uiSettings.isCompassEnabled = true


    }

    private fun requestLocationPermission() {
        Log.d(TAG, "requestLocationPermission: Checking permissions...")

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w(TAG, "requestLocationPermission: Permission NOT granted → requesting")
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION
            )
        } else {
            Log.d(TAG, "requestLocationPermission: Permission already granted")
            showMapWithMyLocation()
        }
        loadAttendance("staff_attendance")
    }
//    private fun loadTodayMarkIn( ) {
//
//        val today = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
//        val db = FirebaseFirestore.getInstance()
//        db.collection("student_attendance")
//            .document("YHHvCpdu8xQyUAqfgfVa7yxDl6E3")
//            .collection("dates")
//            .document(today)
//            .get()
//            .addOnSuccessListener { doc ->
//
//                if (doc.exists()) {
//
//                    val markIn = doc.getString("markIn") ?: ""
//                    val isMarkedIn = doc.getBoolean("isMarkedIn") ?: false
//
//                    if (isMarkedIn && markIn.isNotEmpty()) {
//                        Log.e("Admin", "Failed to load mark-in: $markIn")
//
//                      //  tvName.text = "Mark-In: $markIn"
//
//                    } else {
//
////tvName.text = "Not Marked In"
//                    }
//
//                } else {
//
//                   // tvName.text = "No Attendance Today"
//                }
//            }
//            .addOnFailureListener {
//              //  tvName.text = "Error Loading"
//                Log.e("Admin", "Failed to load mark-in: $it")
//            }
//    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d(TAG, "onRequestPermissionsResult: requestCode=$requestCode")

        if (requestCode == LOCATION_PERMISSION && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            Log.d(TAG, "onRequestPermissionsResult: Permission GRANTED")
            showMapWithMyLocation()
        } else {
            Log.e(TAG, "onRequestPermissionsResult: Permission DENIED")
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun showMapWithMyLocation() {
        Log.d(TAG, "showMapWithMyLocation: Showing current location")

        //   map.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location == null) {
                Log.e(TAG, "showMapWithMyLocation: Location is NULL")
                return@addOnSuccessListener
            }

            val myLatLng = LatLng(location.latitude, location.longitude)
            Log.d(TAG, "showMapWithMyLocation: Moving camera to → $myLatLng")
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 18f))
            //    addGeofenceCircle()
            //    addGeoMarkOutCircle()
            drawAttendanceAreas()


        }
    }

    private fun addGeofenceCircle() {
        try {
            val center = LatLng(31.3136, 75.5909)
            val radiusInMeters = 80.0

            val circleOptions = CircleOptions()
                .center(center)
                .radius(radiusInMeters)
                .strokeColor(Color.argb(80, 0, 100, 255))
                .strokeWidth(4f)
                .fillColor(Color.argb(30, 0, 100, 255))

            map.addCircle(circleOptions)

//            map.addMarker(
//                MarkerOptions()
//                    .position(center)
//
//                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
//            )

            Log.d("MAP_DEBUG", "✅ Added 200m geofence circle at $center")

        } catch (e: Exception) {
            Log.e("MAP_DEBUG", "❌ Error adding geofence circle: ${e.message}", e)
        }
    }

    private fun addGeoMarkOutCircle() {
        try {
            val center = LatLng(31.3136, 75.5909)
            val radiusInMeters = 300.0

            val circleOptions = CircleOptions()
                .center(center)
                .radius(radiusInMeters)
                .strokeColor(Color.argb(80, 255, 0, 0))
                .strokeWidth(4f)
                .fillColor(Color.argb(10, 255, 0, 0))

            map.addCircle(circleOptions)
//            map.addPolygon()


//            map.addMarker(
//                MarkerOptions()
//                    .position(center)
//
//                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
//            )

            Log.d("MAP_DEBUG", " Added 200m geofence circle at $center")

        } catch (e: Exception) {
            Log.e("MAP_DEBUG", "Error adding geofence circle: ${e.message}", e)
        }
    }

    companion object {
        private const val AUTO_REFRESH_INTERVAL = 30000L // 30 seconds
    }


    private fun loadAttendance(collectionName: String) {
        val today = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
        val db = FirebaseFirestore.getInstance()

        Log.d(TAG, "Admin: Loading $collectionName for date = $today")
        Log.d(TAG, "Auto-refresh active: $isAutoRefreshActive")

        db.collection("users")
            .get()
            .addOnSuccessListener { users ->
                val userList = users.documents.toList()
                Log.d(TAG, "Found ${userList.size} users to process")

                val processedUsers = AtomicInteger(0)
                val totalUsers = userList.size

                for (userDoc in userList) {
                    val uid = userDoc.id
                    val userRole = userDoc.getString("role") ?: "Student"
                    val userName = userDoc.getString("name") ?: "Unknown User"

                    val userIdentifier = if (userRole == "Student") {
                        userDoc.getString("rollno") ?: "No Roll No"
                    } else {
                        userDoc.getString("department") ?: "No Department"
                    }

                    db.collection("$collectionName")
                        .document(uid)
                        .collection("dates")
                        .document(today)
                        .addSnapshotListener { snapshot, error ->
                            if (error != null) {
                                Log.e(
                                    "MAP_DEBUG",
                                    "Error fetching attendance for $uid: ${error.message}"
                                )
                                processedUsers.incrementAndGet()
                                if (processedUsers.get() == totalUsers && !isAutoRefreshActive) {
                                    startAutoRefresh()
                                }
                                return@addSnapshotListener
                            }

                            if (snapshot == null || !snapshot.exists()) {
                                removeUserMarkers(uid)
                                processedUsers.incrementAndGet()
                                if (processedUsers.get() == totalUsers && !isAutoRefreshActive) {
                                    startAutoRefresh()
                                }
                                return@addSnapshotListener
                            }

                            processAttendanceSnapshot(
                                uid,
                                userName,
                                userIdentifier,
                                userRole,
                                snapshot
                            )

                            processedUsers.incrementAndGet()
                            if (processedUsers.get() == totalUsers && !isAutoRefreshActive) {
                                startAutoRefresh()
                            }
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error fetching users: ${e.message}")
            }
    }

    private fun processAttendanceSnapshot(
        uid: String,
        userName: String,
        userIdentifier: String,
        userRole: String,
        doc: DocumentSnapshot
    ) {
        val isMarkedIn = doc.getBoolean("isMarkedIn") ?: false
        val isMarkedOut = doc.getBoolean("isMarkedOut") ?: false
        val lastUpdate = doc.getLong("lastMarkInLocationUpdate") ?: 0L
        val currentTime = System.currentTimeMillis()

        Log.d(
            "MAP_DEBUG",
            "User: $uid → In: $isMarkedIn | Out: $isMarkedOut | LastUpdate: ${if (lastUpdate > 0) "${(currentTime - lastUpdate) / 1000}s ago" else "never"}"
        )

        removeUserMarkers(uid)

        if (isMarkedIn && !isMarkedOut) {
            val location = doc.get("markInLocation") as? Map<String, Any>
            val markInTime = doc.getString("markIn") ?: "Unknown"
            val locationAge = if (lastUpdate > 0) (currentTime - lastUpdate) / 1000 else -1

            val status = if (locationAge in 0..30) {
                "Present • Location updated ${locationAge}s ago"
            } else if (locationAge > 30) {
                "Present • Last location ${locationAge}s ago"
            } else {
                "Present"
            }

            addMarkerForUser(
                uid = uid,
                userName = userName,
                userIdentifier = userIdentifier,
                userRole = userRole,
                doc = doc,
                location = location,
                colorHex = "#e9f619",
                titleType = status,
                timeField = "markIn",
                tag = "$uid-in",
                showLocationAge = true,
                locationAgeSeconds = locationAge
            )

        } else if (isMarkedIn && isMarkedOut) {
            val location = doc.get("markOutLocation") as? Map<String, Any>
            val markOutTime = doc.getString("markOut") ?: "Unknown"

            addMarkerForUser(
                uid = uid,
                userName = userName,
                userIdentifier = userIdentifier,
                userRole = userRole,
                doc = doc,
                location = location,
                colorHex = "#06429D",
                titleType = "Departed",
                timeField = "markOut",
                tag = "$uid-out",
                showLocationAge = false
            )
        }
    }

    private fun addMarkerForUser(
        uid: String,
        userName: String,
        userIdentifier: String,
        userRole: String,
        doc: DocumentSnapshot,
        location: Map<String, Any>?,
        colorHex: String,
        titleType: String,
        timeField: String,
        tag: String,
        showLocationAge: Boolean = false,
        locationAgeSeconds: Long = -1
    ) {
        if (location == null) {
            Log.w("MAP_DEBUG", "No location data for $uid")
            return
        }

        val lat = location["lat"] as? Double ?: 0.0
        val lng = location["lng"] as? Double ?: location["lon"] as? Double ?: 0.0

        if (lat == 0.0 || lng == 0.0) {
            Log.w("MAP_DEBUG", "Invalid location for $uid")
            return
        }

        val time = doc.getString(timeField) ?: "N/A"

        val markerTitle = if (userRole == "Student") {
            "$userName (Roll: $userIdentifier)"
        } else {
            "$userName (Dept: $userIdentifier)"
        }

        val markerSnippet = buildString {
            append("Status: $titleType")
            append("\nTime: $time")
            if (showLocationAge && locationAgeSeconds >= 0) {
                append("\nLocation age: ${locationAgeSeconds}s")
            }
            val distance = if (timeField == "markIn") {
                doc.getDouble("markInDistance")
            } else {
                doc.getDouble("markOutDistance")
            }
            distance?.let {
                append("\nDistance: ${String.format("%.1f", it)}m")
            }
        }

        val existingMarker = userMarkers[tag]
        if (existingMarker != null) {
            existingMarker.position = LatLng(lat, lng)
            existingMarker.title = markerTitle
            existingMarker.snippet = markerSnippet
            Log.d("MAP_DEBUG", "Updated marker for $uid at ($lat, $lng)")
        } else {
            // Create new marker
            val marker = map.addMarker(
                MarkerOptions()
                    .position(LatLng(lat, lng))
                    .title(markerTitle)
                    .snippet(markerSnippet)
                    .icon(
                        bitmapDescriptorFromVector(
                            if (titleType.contains("Present"))
                                R.drawable.person_standing_light_skin_tone_svgrepo_com
                            else
                                R.drawable.person_walking_medium_skin_tone_svgrepo_com
                        )
                    )
            )

            marker?.tag = tag
            marker?.let {
                userMarkers[tag] = it
            }

            Log.d("MAP_DEBUG", "Added new marker for $uid at ($lat, $lng)")
        }
    }

    private fun removeUserMarkers(uid: String) {
        val tagsToRemove = listOf("$uid-in", "$uid-out")

        tagsToRemove.forEach { tag ->
            val marker = userMarkers[tag]
            marker?.remove()
            userMarkers.remove(tag)
            Log.d("MAP_DEBUG", "Removed marker: $tag")
        }
    }

    private fun startAutoRefresh() {
        if (!isAutoRefreshActive) {
            Log.d(TAG, "Starting auto-refresh every 30 seconds")
            isAutoRefreshActive = true

            autoRefreshHandler = Handler(Looper.getMainLooper())
            autoRefreshRunnable = object : Runnable {
                override fun run() {
                    val currentTime =
                        SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                    Log.d(TAG, "Auto-refresh triggered at $currentTime")


                    loadAttendance("staff_attendance")


                    autoRefreshHandler?.postDelayed(this, AUTO_REFRESH_INTERVAL)
                }
            }
            autoRefreshHandler?.postDelayed(autoRefreshRunnable!!, AUTO_REFRESH_INTERVAL)
        }
    }

    private fun stopAutoRefresh() {
        Log.d(TAG, "Stopping auto-refresh")
        autoRefreshHandler?.removeCallbacksAndMessages(null)
        autoRefreshRunnable = null
        isAutoRefreshActive = false
        userMarkers.clear()
    }

//    fun setupRefreshButton() {
//        refreshButton.setOnClickListener {
//            Log.d(TAG, "Manual refresh triggered")
//            loadAttendance("staff_attendance")
//
//        }
//    }

    override fun onDestroy() {
        super.onDestroy()
        stopAutoRefresh()

        userMarkers.values.forEach { it.remove() }
        userMarkers.clear()
    }

    //    private fun loadAttendance(collectionName: String) {
//        val today = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
//        val db = FirebaseFirestore.getInstance()
//
//        Log.d(TAG, "Admin: Loading $collectionName for date = $today")
//        map.clear()
//
//        db.collection("users")
//            .get()
//            .addOnSuccessListener { users ->
//                for (userDoc in users) {
//                    val uid = userDoc.id
//                    val userRole = userDoc.getString("role") ?: "Student"
//                    val userName = userDoc.getString("name") ?: "Unknown User"
//
//                    val userIdentifier = if (userRole == "Student") {
//                        userDoc.getString("rollno") ?: "No Roll No"
//                    } else {
//                        userDoc.getString("department") ?: "No Department"
//                    }
//
//                    Log.d(TAG, "Admin: Found UID = $uid, Name = $userName, Role = $userRole")
//
//                    db.collection("$collectionName")
//                        .document(uid)
//                        .collection("dates")
//                        .document(today)
//                        .get()
//                        .addOnSuccessListener { doc ->
//                            if (!doc.exists()) {
//                                Log.d("MAP_DEBUG", "No attendance for $uid")
//                                return@addOnSuccessListener
//                            }
//
//                            val isMarkedIn = doc.getBoolean("isMarkedIn") ?: false
//                            val isMarkedOut = doc.getBoolean("isMarkedOut") ?: false
//
//                            Log.d(
//                                "MAP_DEBUG",
//                                "User: $uid → In: $isMarkedIn | Out: $isMarkedOut"
//                            )
//
//                            if (isMarkedIn && !isMarkedOut) {
//                                //for this get  conditon if markin get the  markin again and again after 30 sec then //also refresh the page or data
//                                val location = doc.get("markInLocation") as? Map<String, Any>
//                                    addMarkerForUser(
//                                        uid,
//                                        userName,
//                                        userIdentifier,
//                                        userRole,
//                                        doc,
//                                        location,
//                                        "#e9f619",
//                                        "Arrived",
//                                        "markIn",
//                                        "$uid-in"
//                                    )
//
//                            }
//
//                            if (isMarkedIn && isMarkedOut) {
//                                val location = doc.get("markOutLocation") as? Map<String, Any>
//
//                                    addMarkerForUser(
//                                        uid,
//                                        userName,
//                                        userIdentifier,
//                                        userRole,
//                                        doc,
//                                        location,
//                                        "#06429D",
//                                        "Departed",
//                                        "markOut",
//                                        "$uid-out"
//                                    )
//
//
//                            }
//                        }
//                        .addOnFailureListener { e ->
//                            Log.e("MAP_DEBUG", "Error fetching attendance for $uid: ${e.message}")
//                        }
//                }
//            }
//    }
    private fun addMarkerForUser(
        uid: String,
        userName: String,
        userIdentifier: String,
        userRole: String,
        doc: DocumentSnapshot,
        location: Map<String, Any>?,
        colorHex: String,
        titleType: String,
        timeField: String,
        tag: String
    ) {
        if (location == null) return

        val lat = location["lat"] as? Double ?: 0.0
        val lng = location["lng"] as? Double ?: location["lon"] as? Double ?: 0.0

        if (lat == 0.0 || lng == 0.0) {
            Log.w("MAP_DEBUG", "Invalid location for $uid")
            return
        }

        val markerTitle = if (userRole == "Student") {
            "$userName - $userIdentifier - $titleType"
        } else {
            "$userName - $userIdentifier - $titleType"
        }

        val markerSnippet = "Time: ${doc.getString(timeField) ?: "N/A"}"

        map.addMarker(
            MarkerOptions()
                .position(LatLng(lat, lng))
                .title(markerTitle)
                .snippet(markerSnippet)
                .icon(
                    bitmapDescriptorFromVector(
                        if (titleType == "Arrived") R.drawable.person_standing_light_skin_tone_svgrepo_com else R.drawable.person_walking_medium_skin_tone_svgrepo_com
                    )
                )
        )?.tag = tag

        Log.d("MAP_DEBUG", "Added $titleType marker → Name: $userName, ID: $userIdentifier")
    }

    private fun bitmapDescriptorFromVector(vectorResId: Int): BitmapDescriptor {
        val vectorDrawable = ContextCompat.getDrawable(this, vectorResId)!!
        vectorDrawable.setBounds(
            0,
            0,
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight
        )
        val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    private fun drawAttendanceAreas() {

        getLocationFromFirestore {

            try {
                Log.d("MAP_DEBUG", "Center: $centerLat , $centerLon")

                val latOffset80m = metersToLatitudeDegrees(30.0)
                val lonOffset80m = metersToLongitudeDegrees(30.0, centerLat!!)

                val latOffset300m = metersToLatitudeDegrees(150.0)
                val lonOffset300m = metersToLongitudeDegrees(150.0, centerLat!!)

                val rect80mNorthEast = LatLng(
                    centerLat!! + latOffset80m,
                    centerLon!! + lonOffset80m
                )
                val rect80mSouthWest = LatLng(
                    centerLat!! - latOffset80m,
                    centerLon!! - lonOffset80m
                )

                map.addPolygon(
                    PolygonOptions()
                        .add(rect80mSouthWest)
                        .add(LatLng(rect80mNorthEast.latitude, rect80mSouthWest.longitude))
                        .add(rect80mNorthEast)
                        .add(LatLng(rect80mSouthWest.latitude, rect80mNorthEast.longitude))
                        .strokeColor(Color.BLUE)
                        .fillColor(Color.argb(40, 0, 0, 255))
                        .strokeWidth(2f)
                )

                val rect300mNorthEast = LatLng(
                    centerLat!! + latOffset300m,
                    centerLon!! + lonOffset300m
                )
                val rect300mSouthWest = LatLng(
                    centerLat!! - latOffset300m,
                    centerLon!! - lonOffset300m
                )

                map.addPolygon(
                    PolygonOptions()
                        .add(rect300mSouthWest)
                        .add(LatLng(rect300mNorthEast.latitude, rect300mSouthWest.longitude))
                        .add(rect300mNorthEast)
                        .add(LatLng(rect300mSouthWest.latitude, rect300mNorthEast.longitude))
                        .strokeColor(Color.RED)
                        .fillColor(Color.argb(40, 255, 0, 0))
                        .strokeWidth(2f)
                )

                val bounds = LatLngBounds.Builder()
                    .include(rect300mNorthEast)
                    .include(rect300mSouthWest)
                    .build()

                map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 120))

            } catch (e: Exception) {
                Log.e("MAP_DEBUG", "Error drawing areas", e)
            }
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

    private fun metersToLatitudeDegrees(meters: Double): Double {
        // Approximately 111,111 meters per degree of latitude
        return meters / 111111.0
    }

    private fun metersToLongitudeDegrees(meters: Double, latitude: Double?): Double {

        return meters / (111111.0 * Math.cos(Math.toRadians(latitude?.toDouble()?:0.0)))
    }
    }
//private fun drawAttendanceAreas() {
//    try {
//        val centerLat = 31.3136
//        val centerLon = 75.5909
//
//        // Create 80x80m square for mark-in (Blue)
//        val rect80m = createSquareAroundPoint(centerLat, centerLon, 80.0)
//        map.addPolygon(
//            PolygonOptions()
//                .addAll(rect80m)
//                .strokeColor(Color.BLUE)
//                .fillColor(Color.argb(40, 0, 0, 255))
//                .strokeWidth(4f)
//        )
//
//        // Create 300x300m square for mark-out (Red)
//        val rect300m = createSquareAroundPoint(centerLat, centerLon, 300.0)
//        map.addPolygon(
//            PolygonOptions()
//                .addAll(rect300m)
//                .strokeColor(Color.RED)
//                .fillColor(Color.argb(20, 255, 0, 0))
//                .strokeWidth(3f)
//        )
//
//        // Add center point
//        map.addMarker(
//            MarkerOptions()
//                .position(LatLng(centerLat, centerLon))
//                .title("Attendance Point")
//        )
//
//        // Zoom to fit both rectangles
//        val bounds = LatLngBounds.Builder()
//        rect300m.forEach { bounds.include(it) }
//        map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 50))
//
//    } catch (e: Exception) {
//        Log.e("MAP_DEBUG", "Error: ${e.message}", e)
//    }
//}
//
//private fun createSquareAroundPoint(centerLat: Double, centerLon: Double, sizeMeters: Double): List<LatLng> {
//    val halfSize = sizeMeters / 2
//    val latOffset = metersToDegrees(halfSize, true)
//    val lonOffset = metersToDegrees(halfSize, false, centerLat)
//
//    return listOf(
//        LatLng(centerLat - latOffset, centerLon - lonOffset), // SW
//        LatLng(centerLat - latOffset, centerLon + lonOffset), // SE
//        LatLng(centerLat + latOffset, centerLon + lonOffset), // NE
//        LatLng(centerLat + latOffset, centerLon - lonOffset)  // NW
//    )
//}
//
//private fun metersToDegrees(meters: Double, isLatitude: Boolean, latitude: Double = 0.0): Double {
//    return if (isLatitude) {
//        meters / 111320.0 // 1 degree latitude ≈ 111.32 km
//    } else {
//        meters / (111320.0 * Math.cos(Math.toRadians(latitude)))
//    }
//}

