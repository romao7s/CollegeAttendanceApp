package com.example.collegeattendanceapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.location.Geocoder
import android.location.LocationManager
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope

import com.example.collegeattendanceapp.R
import com.example.collegeattendanceapp.databinding.ActivityAdminBinding
import com.example.collegeattendanceapp.databinding.ActivityMapBinding
import com.example.collegeattendanceapp.databinding.ItemRadiusViewBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions
import com.google.firebase.auth.FirebaseAuth

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicInteger
//full no pin is showing in when click the certain user also show lastupdate area loction and pin when enter in map
//all
class MapActivity : AppCompatActivity(), OnMapReadyCallback {
//updating location with pin also location
    lateinit var binding: ActivityMapBinding
    private val TAG = "map"
    var isLocationSelectedManually = false
    var centerLat: Double? = null
    var centerLon: Double? = null
    private val fusedLocationProviderClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }
    var attendanceAreaDrawn=false
    var maps: GoogleMap? = null
    var userLocation = LatLng(0.0, 0.0)
    var markerOptions = MarkerOptions()
    var mCenterMarker: Marker? = null
    private val locationPermission = arrayOf(
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION
    )
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permission ->
            if (permission.values.all { it }) {
                Toast.makeText(this, "all permission granted", Toast.LENGTH_SHORT).show()
                getLastLocation()
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                openappSettings()
            }
        }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        if (!hasPermissions()) {
            requestpermissionWithRetionale()
        }


//        binding.btnUpdate.setOnClickListener {
//           val latitude= userLocation.latitude
//            val longitude=userLocation.longitude
//            saveLocationToFirestore(latitude,longitude)
//        }
        binding.btnUpdate.setOnClickListener {

            val lat = userLocation?.latitude
            val lon = userLocation?.longitude

            if (lat == null || lon == null) {
                Toast.makeText(this, "Location not available, try again", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            centerLat = lat
            centerLon = lon

            saveLocationToFirestore(lat, lon)
        }

        binding.logoutIcon.setOnClickListener {
            finish()
        }
    }



            private fun hasPermissions(): Boolean {
                return locationPermission.all { permission ->
                    ContextCompat.checkSelfPermission(
                        this,
                        permission
                    ) == PackageManager.PERMISSION_GRANTED
                }
            }


//    private fun saveLocationToFirestore(latitude: Double, longitude: Double) {
//
//        val firestore = FirebaseFirestore.getInstance()
//
//        val locationData = hashMapOf(
//            "latitude" to latitude,
//            "longitude" to longitude,
//            "timestamp" to FieldValue.serverTimestamp()
//        )
//
//        firestore.collection("centerloc")
//            .document("location") // or userId if needed
//            .set(locationData)
//            .addOnSuccessListener {
//                Toast.makeText(this, "Location updated", Toast.LENGTH_SHORT).show()
//                  finish() // close screen
//            }
//            .addOnFailureListener {
//                Toast.makeText(this, "Failed to update location", Toast.LENGTH_SHORT).show()
//            }
//    }

            private fun requestpermissionWithRetionale() {
                val shouldShowRationale = locationPermission.any { permission ->
                    ActivityCompat.shouldShowRequestPermissionRationale(this, permission)
                }

                if (shouldShowRationale) {
                    Toast.makeText(
                        this,
                        "Permissions are required for the app to function properly",
                        Toast.LENGTH_LONG
                    ).show()
                    openappSettings()
                } else {
                    requestPermissions()
                }
            }

            private fun requestPermissions() {
                requestPermissionLauncher.launch(
                    locationPermission
                )
            }


    private fun getLastLocation() {

        if (!isLocationEnabled()) {
            Toast.makeText(this, "Please turn on location", Toast.LENGTH_SHORT).show()
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            return
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->

            location ?: run {
                requestNewLoctionData()
                return@addOnSuccessListener
            }

            if (!isLocationSelectedManually) {
                userLocation = LatLng(location.latitude, location.longitude)
                updateMarker(forceUpdate = true)
            }

            if (!attendanceAreaDrawn) {
                attendanceAreaDrawn = true
                drawAttendanceAreas(
                    userLocation.latitude,
                    userLocation.longitude
                )
            }
        }
    }
            private fun openappSettings() {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = android.net.Uri.fromParts(
                    "package",
                    this.applicationContext.packageName,
                    null
                )
                intent.data = uri
                startActivity(intent)
            }


            @SuppressLint("ServiceCast")
            private fun isLocationEnabled(): Boolean {
                val locationManager =
                    this.getSystemService(LOCATION_SERVICE) as LocationManager
                return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                    LocationManager.NETWORK_PROVIDER
                )
            }


            private fun requestNewLoctionData() {
                val mLocationRequest = LocationRequest.Builder(10000)
                    .build()

                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
                fusedLocationProviderClient.requestLocationUpdates(
                    mLocationRequest,
                    mLocationCallback,
                    Looper.myLooper()
                )

            }

            private val mLocationCallback: LocationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    userLocation = LatLng(
                        locationResult.lastLocation?.latitude ?: 0.0,
                        locationResult.lastLocation?.longitude ?: 0.0
                    )
                    updateMarker(false)
                }
            }


            private var isFirstZoomDone = false
            private var initialCameraDone = false


//            private fun updateMarker(forceUpdate: Boolean = false) {
//
//                if (forceUpdate || !isLocationSelectedManually) {
//
//                    if (mCenterMarker == null) {
//                        mCenterMarker = maps?.addMarker(
//                            MarkerOptions().position(userLocation).title("Dropped Pin")
//                        )
//                    } else {
//                        mCenterMarker?.position = userLocation
//                    }
//
//                    if (!initialCameraDone) {
//                        maps?.animateCamera(
//                            CameraUpdateFactory.newLatLngZoom(userLocation, 20f),
//                            object : GoogleMap.CancelableCallback {
//                                override fun onFinish() {
//                                    Log.d("ZoomUsed", "Initial zoom finished")
//                                    initialCameraDone = true
//                                }
//
//                                override fun onCancel() {
//                                    initialCameraDone = true
//                                }
//                            }
//                        )
//                    } else {
//                        val safeZoom = maps?.cameraPosition?.zoom ?: 20f
//                        Log.d("ZoomUsed", "Zoom = $safeZoom")
//
//                        maps?.animateCamera(
//                            CameraUpdateFactory.newLatLngZoom(
//                                userLocation,
//                                safeZoom
//                            )
//                        )
//                    }
//                }
//            }
    private fun updateMarker(forceUpdate: Boolean = false) {

        if (maps == null) return

        if (isLocationSelectedManually) return

        mCenterMarker?.remove()

        mCenterMarker = maps?.addMarker(
            MarkerOptions()
                .position(userLocation)
                .title("Current Location")
        )

        maps?.animateCamera(
            CameraUpdateFactory.newLatLngZoom(userLocation, 17f)
        )

}

    override fun onMapReady(googleMap: GoogleMap) {

        maps = googleMap

        maps?.uiSettings?.isZoomControlsEnabled = true

        getLastLocation()

        maps?.setOnMapClickListener { latLng ->

            isLocationSelectedManually = true
            userLocation = latLng

            mCenterMarker?.remove()

            mCenterMarker = maps?.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title("Selected Location")
            )

            maps?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(latLng, 17f)
            )
            //should be  one the area on currently selected area


        }

    }
    private fun saveLocationToFirestore(latitude: Double, longitude: Double) {

        val firestore = FirebaseFirestore.getInstance()

        val locationData = hashMapOf(
            "latitude" to latitude,
            "longitude" to longitude,
            "checkinRadius" to "",
            "checkoutRadius" to "",
            "timestamp" to FieldValue.serverTimestamp(),

        )

        firestore.collection("centerloc")
            .document("location")
            .set(locationData)
            .addOnSuccessListener {
                Toast.makeText(this, "Location updated", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update location", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setRadius(){
        val dialog = Dialog(this)
        val dialogBinding = ItemRadiusViewBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
//        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT)
        dialogBinding.sliderCheckIn.value = selectedSlider.toFloat()
        dialogBinding.sliderCheckIn.setLabelFormatter{value  :Float->
            return@setLabelFormatter "${value.toInt()} km"
        }
        //set the set
        dialogBinding.sliderCheckOut.addOnChangeListener { slider, value, fromUser ->
            selectedSlider = value.toInt()
            Log.d("Slider Change", "Selected value: $selectedSlider km")
        }
        dialogBinding.sliderCheckOut.value = selectedSlider.toFloat()
        dialogBinding.sliderCheckOut.setLabelFormatter{value  :Float->
            return@setLabelFormatter "${value.toInt()} km"
        }

        dialogBinding.sliderCheckOut.addOnChangeListener { slider, value, fromUser ->
            selectedSlider = value.toInt()
            Log.d("Slider Change", "Selected value: $selectedSlider km")
        }
         //add checkinRadius and checkoutRadius
        dialogBinding.btnApply.setOnClickListener {
            Log.d("Slider Apply", "Applying radius: $selectedSlider km")
           // binding.tvSampleRadiusValue.text = "$selectedSlider km"
            maps?.let { map ->
                val currentLocation =
                    com.google.android.gms.maps.model.LatLng(latitude, longitude)
                val radiusInMetersForZoom = selectedSlider * 1000.0
                val zoomLevel = getZoomLevel(radiusInMetersForZoom)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, zoomLevel))
                //update in
            }
            dialog.dismiss()
        }
        dialog.show()
    }


    private fun drawAttendanceAreas(centerLat: Double,centerLon: Double) {

            try {
                Log.d("MAP_DEBUG", "Center: $centerLat , $centerLon")

                val latOffset80m = metersToLatitudeDegrees(30.0)//instead of 30 set the selected slidercheckin
                val lonOffset80m = metersToLongitudeDegrees(30.0, centerLat!!)

                val latOffset300m = metersToLatitudeDegrees(150.0)//instead of 150 set the selected slidercheckout
                val lonOffset300m = metersToLongitudeDegrees(150.0, centerLat!!)

                val rect80mNorthEast = LatLng(
                    centerLat!! + latOffset80m,
                    centerLon!! + lonOffset80m
                )
                val rect80mSouthWest = LatLng(
                    centerLat!! - latOffset80m,
                    centerLon!! - lonOffset80m
                )

                maps?.addPolygon(
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

                maps?.addPolygon(
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

                maps?.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 120))

            } catch (e: Exception) {
                Log.e("MAP_DEBUG", "Error drawing areas", e)
            }

    }
    private fun metersToLatitudeDegrees(meters: Double): Double {
        return meters / 111111.0
    }

    private fun metersToLongitudeDegrees(meters: Double, latitude: Double?): Double {

        return meters / (111111.0 * Math.cos(Math.toRadians(latitude?.toDouble()?:0.0)))
    }

//    override fun onMapReady(p0: GoogleMap) {
//                this.maps = p0
//                getLastLocation()
//
//                var userZoom: Float = 25f
//                //not selecting the new location or showing pin or pin
//                p0.setOnMapClickListener { latLng ->
//                    userLocation = latLng
//                    isLocationSelectedManually = true
//                    userZoom = p0?.cameraPosition?.zoom ?: 25f
//                    if (mCenterMarker == null) {
//                        mCenterMarker = p0.addMarker(
//                            MarkerOptions().position(userLocation).title("Selected Location")
//                        )
//                    } else {
//                        mCenterMarker?.position = userLocation
//                    }
//
//
//                    p0.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, userZoom))
//
//
//
//                    lifecycleScope.launch(Dispatchers.IO) {
//                        try {
//                            val geocoder = Geocoder(this@MapActivity, Locale.getDefault())
//                            val addresses = geocoder.getFromLocation(
//                                userLocation.latitude,
//                                userLocation.longitude,
//                                1
//                            )
//                            val addressLine =
//                                addresses?.firstOrNull()?.getAddressLine(0) ?: "Unknown Address"
//
//
//                        } catch (e: Exception) {
//                            e.printStackTrace()
//                        }
//                    }
//                }
//            }

}